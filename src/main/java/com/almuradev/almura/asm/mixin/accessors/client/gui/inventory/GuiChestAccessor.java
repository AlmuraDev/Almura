/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiChest.class)
public interface GuiChestAccessor {
    //public net.minecraft.client.gui.inventory.GuiChest field_147017_u # CHEST_GUI_TEXTURE
    @Mutable @Accessor("CHEST_GUI_TEXTURE") static ResourceLocation accessor$getChestGuiTexture() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
