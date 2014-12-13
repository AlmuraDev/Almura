/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.transformer;

import com.almuradev.almura.core.mixin.InvalidMixinException;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import com.almuradev.almura.core.mixin.util.ASMHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Transformer which applies Mixin classes to their declared target classes
 */
public class MixinTransformer extends TreeTransformer {

    /**
     * Mixin configuration bundle
     */
    private final MixinConfig config;

    private final Logger logger = LogManager.getLogger("almura");

    /**
     * ctor 
     */
    public MixinTransformer() {
        this.config = MixinConfig.create("mixins.almura.json");
    }

    /**
     * Check whether the specified flag is set on the specified method
     *
     * @param method
     * @param flag
     * @return
     */
    private static boolean hasFlag(MethodNode method, int flag) {
        return (method.access & flag) == flag;
    }

    /**
     * Check whether the specified flag is set on the specified field
     *
     * @param field
     * @param flag
     * @return
     */
    private static boolean hasFlag(FieldNode field, int flag) {
        return (field.access & flag) == flag;
    }

    /* (non-Javadoc)
     * @see net.minecraft.launchwrapper.IClassTransformer#transform(java.lang.String, java.lang.String, byte[])
     */
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName != null && transformedName.startsWith(this.config.getMixinPackage())) {
            throw new RuntimeException(String.format("%s is a mixin class and cannot be referenced directly", transformedName));
        }

        if (this.config.hasMixinsFor(transformedName)) {
            try {
                return this.applyMixins(transformedName, basicClass);
            } catch (InvalidMixinException th) {
                logger.warn(String.format("Class mixin failed: %s %s", th.getClass().getName(), th.getMessage()), th);
                th.printStackTrace();
            }
        }

        return basicClass;
    }

    /**
     * Apply mixins for specified target class to the class described by the supplied byte array
     *
     * @param transformedName
     * @param basicClass
     * @return
     */
    private byte[] applyMixins(String transformedName, byte[] basicClass) {
        // Tree for target class
        ClassNode targetClass = this.readClass(basicClass, true);

        // Get and sort mixins for the class
        List<MixinInfo> mixins = this.config.getMixinsFor(transformedName);
        Collections.sort(mixins);

        for (MixinInfo mixin : mixins) {
            logger.info("Applying mixin {} to {}", mixin.getClassName(), transformedName);
            this.applyMixin(targetClass, mixin.getData());
        }

        // Extension point
        this.postTransform(transformedName, targetClass, mixins);

        // Collapse tree to bytes
        return this.writeClass(targetClass);
    }

    /**
     * @param transformedName
     * @param targetClass
     * @param mixins
     */
    protected void postTransform(String transformedName, ClassNode targetClass, List<MixinInfo> mixins) {
        // Stub for subclasses
    }

    /**
     * Apply the mixin described by mixin to the supplied classNode
     *
     * @param targetClass
     * @param mixin
     */
    protected void applyMixin(ClassNode targetClass, MixinData mixin) {

        try {
            this.verifyClasses(targetClass, mixin);
            this.applyMixinInterfaces(targetClass, mixin);
            this.applyMixinAttributes(targetClass, mixin);
            this.applyMixinFields(targetClass, mixin);
            this.applyMixinMethods(targetClass, mixin);
        } catch (Exception ex) {
            throw new InvalidMixinException("Unexpecteded error whilst applying the mixin class", ex);
        }
    }

    /**
     * Perform pre-flight checks on the mixin and target classes
     *
     * @param targetClass
     * @param mixin
     */
    protected void verifyClasses(ClassNode targetClass, MixinData mixin) {
        String superName = mixin.getClassNode().superName;
        if (targetClass.superName == null || superName == null || !targetClass.superName.equals(superName)) {
            throw new InvalidMixinException("Mixin classes must have the same superclass as their target class");
        }
    }

    /**
     * Mixin interfaces implemented by the mixin class onto the target class
     *
     * @param targetClass
     * @param mixin
     */
    private void applyMixinInterfaces(ClassNode targetClass, MixinData mixin) {
        for (String interfaceName : mixin.getClassNode().interfaces) {
            if (!targetClass.interfaces.contains(interfaceName)) {
                targetClass.interfaces.add(interfaceName);
            }
        }
    }

    /**
     * Mixin misc attributes from mixin class onto the target class
     *
     * @param targetClass
     * @param mixin
     */
    private void applyMixinAttributes(ClassNode targetClass, MixinData mixin) {
        if (this.config.shouldSetSourceFile()) {
            targetClass.sourceFile = mixin.getClassNode().sourceFile;
        }
    }

    /**
     * Mixin fields from mixin class into the target class. It is vital that this is done before mixinMethods because we need to compute renamed
     * fields so that transformMethod can rename field references in the method body
     *
     * @param targetClass
     * @param mixin
     */
    private void applyMixinFields(ClassNode targetClass, MixinData mixin) {
        for (FieldNode field : mixin.getClassNode().fields) {
            // Public static fields will fall foul of early static binding in java, including them in a mixin is an error condition
            if (MixinTransformer.hasFlag(field, Opcodes.ACC_STATIC) && !MixinTransformer.hasFlag(field, Opcodes.ACC_PRIVATE)) {
                throw new InvalidMixinException(String.format("Mixin classes cannot contain visible static methods or fields, found %s", field.name));
            }

            FieldNode target = this.findTargetField(targetClass, field);
            if (target == null) {
                // If this field is a shadow field but is NOT found in the target class, that's bad, mmkay
                boolean isShadow = ASMHelper.getVisibleAnnotation(field, Shadow.class) != null;
                if (isShadow) {
                    throw new InvalidMixinException(String.format("Shadow field %s was not located in the target class", field.name));
                }

                // This is just a local field, so add it
                targetClass.fields.add(field);
            } else {
                // Check that the shadow field has a matching descriptor
                if (!target.desc.equals(field.desc)) {
                    throw new InvalidMixinException(String.format("The field %s in the target class has a conflicting signature", field.name));
                }
            }
        }
    }

    /**
     * Mixin methods from the mixin class into the target class
     *
     * @param targetClass
     * @param mixin
     */
    private void applyMixinMethods(ClassNode targetClass, MixinData mixin) {
        for (MethodNode mixinMethod : mixin.getClassNode().methods) {
            // Reparent all mixin methods into the target class
            this.transformMethod(mixinMethod, mixin.getClassNode().name, targetClass.name);

            boolean isShadow = ASMHelper.getVisibleAnnotation(mixinMethod, Shadow.class) != null;
            boolean isOverwrite = ASMHelper.getVisibleAnnotation(mixinMethod, Overwrite.class) != null;
            boolean isAbstract = MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_ABSTRACT);

            if (isShadow || isAbstract) {
                // For shadow (and abstract, which can be used as a shorthand for Shadow) methods, we just check they're present
                MethodNode target = this.findTargetMethod(targetClass, mixinMethod);
                if (target == null) {
                    throw new InvalidMixinException(String.format("Shadow method %s was not located in the target class", mixinMethod.name));
                }
            } else if (!mixinMethod.name.startsWith("<")) {
                if (MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_STATIC)
                    && !MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_PRIVATE)
                    && !isOverwrite) {
                    throw new InvalidMixinException(
                            String.format("Mixin classes cannot contain visible static methods or fields, found %s", mixinMethod.name));
                }

                MethodNode target = this.findTargetMethod(targetClass, mixinMethod);
                if (target != null) {
                    targetClass.methods.remove(target);
                } else if (isOverwrite) {
                    throw new InvalidMixinException(String.format("Overwrite target %s was not located in the target class", mixinMethod.name));
                }
                targetClass.methods.add(mixinMethod);
            } else if ("<clinit>".equals(mixinMethod.name)) {
                // Class initialiser insns get appended
                this.appendInsns(targetClass, mixinMethod.name, mixinMethod);
            }
        }
    }

    /**
     * Handles "re-parenting" the method supplied, changes all references to the mixin class to refer to the target class (for field accesses and
     * method invokations) and also renames fields accesses to their obfuscated versions
     *
     * @param method
     * @param fromClass
     * @param toClass
     * @return
     */
    private void transformMethod(MethodNode method, String fromClass, String toClass) {
        Iterator<AbstractInsnNode> iter = method.instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode insn = iter.next();

            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.owner.equals(fromClass)) {
                    methodInsn.owner = toClass;
                }
            }
            if (insn instanceof FieldInsnNode) {
                FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                if (fieldInsn.owner.equals(fromClass)) {
                    fieldInsn.owner = toClass;
                }
            }
        }
    }

    /**
     * Handles appending instructions from the property method to the target method
     *
     * @param targetClass
     * @param targetMethodName
     * @param sourceMethod
     */
    private void appendInsns(ClassNode targetClass, String targetMethodName, MethodNode sourceMethod) {
        if (Type.getReturnType(sourceMethod.desc) != Type.VOID_TYPE) {
            throw new IllegalArgumentException("Attempted to merge insns into a method which does not return void");
        }

        if (targetMethodName == null || targetMethodName.length() == 0) {
            targetMethodName = sourceMethod.name;
        }

        for (MethodNode method : targetClass.methods) {
            if ((targetMethodName.equals(method.name)) && sourceMethod.desc.equals(method.desc)) {
                AbstractInsnNode returnNode = null;
                Iterator<AbstractInsnNode> findReturnIter = method.instructions.iterator();
                while (findReturnIter.hasNext()) {
                    AbstractInsnNode insn = findReturnIter.next();
                    if (insn.getOpcode() == Opcodes.RETURN) {
                        returnNode = insn;
                        break;
                    }
                }

                Iterator<AbstractInsnNode> injectIter = sourceMethod.instructions.iterator();
                while (injectIter.hasNext()) {
                    AbstractInsnNode insn = injectIter.next();
                    if (!(insn instanceof LineNumberNode) && insn.getOpcode() != Opcodes.RETURN) {
                        method.instructions.insertBefore(returnNode, insn);
                    }
                }
            }
        }
    }

    /**
     * Finds a method in the target class
     *
     * @param targetClass
     * @param searchFor
     * @return
     */
    private MethodNode findTargetMethod(ClassNode targetClass, MethodNode searchFor) {
        for (MethodNode target : targetClass.methods) {
            if (target.name.equals(searchFor.name) && target.desc.equals(searchFor.desc)) {
                return target;
            }
        }

        return null;
    }

    /**
     * Finds a field in the target class
     *
     * @param targetClass
     * @param searchFor
     * @return
     */
    private FieldNode findTargetField(ClassNode targetClass, FieldNode searchFor) {
        for (FieldNode target : targetClass.fields) {
            if (target.name.equals(searchFor.name)) {
                return target;
            }
        }

        return null;
    }
}

