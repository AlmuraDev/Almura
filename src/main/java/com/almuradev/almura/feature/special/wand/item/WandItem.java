/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.special.wand.item;

import com.almuradev.almura.Almura;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;

public abstract class WandItem extends Item {

    WandItem(ResourceLocation registryName, String unlocalizedName) {
        this.setRegistryName(registryName);
        this.setUnlocalizedName(unlocalizedName);
        this.setMaxStackSize(1);

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":tool").ifPresent((itemGroup) -> this.setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }
}
