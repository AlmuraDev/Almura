/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.transformer;

import com.almuradev.almura.core.mixin.InvalidMixinException;
import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.util.ASMHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime information bundle about a mixin
 */
public class MixinInfo implements Comparable<MixinInfo> {

    /**
     * Global order of mixin infos, used to determine ordering between mixins with equivalent priority
     */
    static int mixinOrder = 0;
    /**
     * Intrinsic order (for sorting mixins with identical priority)
     */
    private final transient int order = MixinInfo.mixinOrder++;
    /**
     * Logger
     */
    private final transient Logger logger = LogManager.getLogger("sponge");
    /**
     * Name of the mixin class itself, dotted notation
     */
    private final String className;
    /**
     * Name of the mixin class, internal notation
     */
    private final transient String classRef;
    /**
     * Mixin priority, read from the {@link com.almuradev.almura.core.mixin.Mixin} annotation on the mixin class
     */
    private final int priority;
    /**
     * Mixin targets, read from the {@link com.almuradev.almura.core.mixin.Mixin} annotation on the mixin class
     */
    private final List<String> targetClasses;
    /**
     * Mixin bytes (read once, generate tree on demand)
     */
    private final transient byte[] mixinBytes;

    /**
     * Internal ctor, called by {@link com.almuradev.almura.core.mixin.transformer.MixinConfig}
     *
     * @param mixinName
     * @param runTransformers
     */
    MixinInfo(String mixinName, boolean runTransformers) {
        this.className = mixinName;
        this.classRef = mixinName.replace('.', '/');

        // Read the class bytes and transform
        this.mixinBytes = this.loadMixinClass(this.className, runTransformers);

        ClassNode classNode = this.getClassNode(0);
        this.targetClasses = this.readTargetClasses(classNode);
        this.priority = this.readPriority(classNode);
    }

    /**
     * Read the target class names from the {@link com.almuradev.almura.core.mixin.Mixin} annotation
     *
     * @param classNode
     * @return
     */
    private List<String> readTargetClasses(ClassNode classNode) {
        AnnotationNode mixin = ASMHelper.getInvisibleAnnotation(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }

        List<Type> targetClasses = ASMHelper.getAnnotationValue(mixin);
        List<String> targetClassNames = new ArrayList<>();
        for (Type targetClass : targetClasses) {
            targetClassNames.add(targetClass.getClassName());
            System.err.println(targetClass.getClassName());
        }

        return targetClassNames;
    }

    /**
     * Read the priority from the {@link Mixin} annotation
     *
     * @param classNode
     * @return
     */
    private int readPriority(ClassNode classNode) {
        AnnotationNode mixin = ASMHelper.getInvisibleAnnotation(classNode, Mixin.class);
        if (mixin == null) {
            throw new InvalidMixinException(String.format("The mixin '%s' is missing an @Mixin annotation", this.className));
        }

        Integer priority = ASMHelper.getAnnotationValue(mixin, "priority");
        return priority == null ? 1000 : priority.intValue();
    }

    /**
     * Get the name of the mixin class
     *
     * @return
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Get the ref (internal name) of the mixin class
     *
     * @return
     */
    public String getClassRef() {
        return this.classRef;
    }

    /**
     * Get the class bytecode
     *
     * @return
     */
    public byte[] getClassBytes() {
        return this.mixinBytes;
    }

    /**
     * Get a new tree for the class bytecode
     *
     * @param flags
     * @return
     */
    public ClassNode getClassNode(int flags) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(this.mixinBytes);
        classReader.accept(classNode, flags);
        return classNode;
    }

    /**
     * Get a new mixin data container for this info
     *
     * @return
     */
    public MixinData getData() {
        return new MixinData(this);
    }

    /**
     * Get the target classes for this mixin
     *
     * @return
     */
    public List<String> getTargetClasses() {
        return Collections.unmodifiableList(this.targetClasses);
    }

    /**
     * Get the mixin priority
     *
     * @return
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * @param mixinClassName
     * @param runTransformers
     * @return
     */
    private byte[] loadMixinClass(String mixinClassName, boolean runTransformers) {
        byte[] mixinBytes = null;

        try {
            if ((mixinBytes = this.getClassBytes(mixinClassName)) == null) {
                throw new InvalidMixinException(String.format("The specified mixin '%s' was not found", mixinClassName));
            }

            if (runTransformers) {
                mixinBytes = this.applyTransformers(mixinClassName, mixinBytes);
            }
        } catch (IOException ex) {
            this.logger.warn("Failed to load mixin %s, the specified mixin will not be applied", mixinClassName);
            throw new InvalidMixinException("An error was encountered whilst loading the mixin class", ex);
        }

        return mixinBytes;
    }

    /**
     * @param mixinClassName
     * @return
     * @throws IOException
     */
    private byte[] getClassBytes(String mixinClassName) throws IOException {
        return Launch.classLoader.getClassBytes(mixinClassName);
    }

    /**
     * Since we obtain the mixin class bytes with getClassBytes(), we need to
     * apply the transformers ourself
     *
     * @param name
     * @param basicClass
     * @return
     */
    private byte[] applyTransformers(String name, byte[] basicClass) {
        final List<IClassTransformer> transformers = Launch.classLoader.getTransformers();

        for (final IClassTransformer transformer : transformers) {
            if (!(transformer instanceof MixinTransformer)) {
                basicClass = transformer.transform(name, name, basicClass);
            }
        }

        return basicClass;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MixinInfo other) {
        if (other == null) {
            return 0;
        }
        if (other.priority == this.priority) {
            return this.order - other.order;
        }
        return (this.priority - other.priority);
    }
}

