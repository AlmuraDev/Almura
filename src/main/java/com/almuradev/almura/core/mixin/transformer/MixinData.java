/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.transformer;

import com.almuradev.almura.core.mixin.Shadow;
import com.almuradev.almura.core.mixin.util.ASMHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Data for applying a mixin. Keeps a copy of the mixin tree and also handles any pre-transformations required by the mixin class itself such as
 * method renaming and any other pre-processing of the mixin bytecode.
 */
public class MixinData {

    /**
     * Tree
     */
    private final ClassNode classNode;

    /**
     * Methods we need to rename
     */
    private final Map<String, String> renamedMethods = new HashMap<String, String>();

    /**
     * ctor
     *
     * @param info
     */
    MixinData(MixinInfo info) {
        this.classNode = info.getClassNode(ClassReader.EXPAND_FRAMES);
        this.prepare();
    }

    /**
     * Gets an annotation value or returns the default if the annotation value is not present
     *
     * @param annotation
     * @param key
     * @param annotationClass
     * @return
     */
    private static String getAnnotationValue(AnnotationNode annotation, String key, Class<?> annotationClass) {
        String value = ASMHelper.getAnnotationValue(annotation, key);
        if (value == null) {
            try {
                value = (String) Shadow.class.getDeclaredMethod(key).getDefaultValue();
            } catch (NoSuchMethodException ex) {
                // Don't care
            }
        }
        return value;
    }

    /**
     * Get the mixin tree
     *
     * @return
     */
    public ClassNode getClassNode() {
        return this.classNode;
    }

    /**
     * Prepare the mixin, applies any pre-processing transformations
     */
    private void prepare() {
        this.findRenamedMethods();
        this.transformMethods();
    }

    /**
     * Let's do this
     */
    private void findRenamedMethods() {
        for (MethodNode mixinMethod : this.classNode.methods) {
            AnnotationNode shadowAnnotation = ASMHelper.getVisibleAnnotation(mixinMethod, Shadow.class);
            if (shadowAnnotation == null) {
                continue;
            }

            String prefix = MixinData.getAnnotationValue(shadowAnnotation, "prefix", Shadow.class);
            if (mixinMethod.name.startsWith(prefix)) {
                String newName = mixinMethod.name.substring(prefix.length());
                this.renamedMethods.put(mixinMethod.name + mixinMethod.desc, newName);
                mixinMethod.name = newName;
            }
        }
    }

    /**
     * Apply discovered method renames to method invokations in the mixin
     */
    private void transformMethods() {
        for (MethodNode mixinMethod : this.classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = mixinMethod.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodNode = (MethodInsnNode) insn;
                    String newName = this.renamedMethods.get(methodNode.name + methodNode.desc);
                    if (newName != null) {
                        methodNode.name = newName;
                    }
                }
            }
        }
    }
}

