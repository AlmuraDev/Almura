/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Points NEI's JEIProxy.getExtraAreas at {@code JeiAdvancedGuiAreas}, in place of the JeiRuntime method JEI 4.15 removed (see there).
 *
 * <p>A transformer rather than a mixin: Almura's mixin configs are read before mod jars are on the classpath, so a mixin targeting NEI
 * looks its class up too early and the class loader then refuses to load it at all. This only acts when NEI's class actually loads.</p>
 */
public class NeiJeiProxyTransformer implements IClassTransformer {

    private static final String TARGET = "codechicken.nei.jei.proxy.JEIProxy";
    private static final String GUI_CONTAINER = "Lnet/minecraft/client/gui/inventory/GuiContainer;";

    @Override
    public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
        if (basicClass == null || !TARGET.equals(transformedName)) {
            return basicClass;
        }

        final ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean changed = false;
        for (final MethodNode method : node.methods) {
            if (!"getExtraAreas".equals(method.name)) {
                continue;
            }
            for (final AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
                    continue;
                }
                final MethodInsnNode call = (MethodInsnNode) insn;
                if ("mezz/jei/runtime/JeiRuntime".equals(call.owner) && "getActiveAdvancedGuiHandlers".equals(call.name)) {
                    // same stack (runtime, gui) and result (List): the runtime is passed as an Object
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/almuradev/almura/shared/client/integration/JeiAdvancedGuiAreas", "getActiveAdvancedGuiHandlers",
                        "(Ljava/lang/Object;" + GUI_CONTAINER + ")Ljava/util/List;", false));
                    changed = true;
                }
            }
        }
        if (!changed) {
            return basicClass; // a NEI that no longer makes the call: leave it alone
        }

        // Only an invoke was swapped, so the existing frames and max sizes still hold
        final ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
