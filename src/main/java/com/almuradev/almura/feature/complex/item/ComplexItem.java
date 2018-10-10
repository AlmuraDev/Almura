/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

public abstract class ComplexItem extends Item {

    public ComplexItem(ResourceLocation registryName, String unlocalizedName) {
        this.setRegistryName(registryName);
        this.setTranslationKey(unlocalizedName);

        if (Sponge.getPlatform().getType().isClient()) {
            this.registerInventoryModel();
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }
}
