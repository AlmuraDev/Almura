/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Base class for transformers which work with ASM tree model
 */
public abstract class TreeTransformer implements IClassTransformer {

    private ClassReader classReader;
    private ClassNode classNode;

    /**
     * @param basicClass
     * @return tree
     */
    protected final ClassNode readClass(byte[] basicClass) {
        return this.readClass(basicClass, true);
    }

    /**
     * @param basicClass
     * @return tree
     */
    protected final ClassNode readClass(byte[] basicClass, boolean cacheReader) {
        ClassReader classReader = new ClassReader(basicClass);
        if (cacheReader) {
            this.classReader = classReader;
        }

        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    /**
     * @param classNode
     * @return
     */
    protected final byte[] writeClass(ClassNode classNode) {
        // Use optimised writer for speed
        if (this.classReader != null && this.classNode == classNode) {
            this.classNode = null;
            ClassWriter writer = new ClassWriter(this.classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            this.classReader = null;
            classNode.accept(writer);
            return writer.toByteArray();
        }

        this.classNode = null;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}

