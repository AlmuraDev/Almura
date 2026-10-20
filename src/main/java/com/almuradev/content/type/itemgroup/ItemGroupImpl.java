/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public final class ItemGroupImpl extends CreativeTabs {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemGroupImpl.class);
    @Nullable private final String translation;
    private boolean warnedIcon;
    private final ItemDefinition icon;
    private final boolean sort;

    ItemGroupImpl(final String id, @Nullable final String translation, final ItemDefinition icon, final boolean sort) {
        super(id);
        this.translation = translation;
        this.icon = icon;
        this.sort = sort;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslationKey() {
        return this.translation != null ? "itemGroup.".concat(this.translation) : super.getTranslationKey();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        final ItemStack configured = this.icon.create();
        if (!configured.isEmpty()) {
            return configured;
        }

        // The configured icon item doesn't resolve (unknown id, or not registered yet): show the first item in the tab instead
        if (!this.warnedIcon) {
            this.warnedIcon = true;
            LOGGER.warn("Creative tab '{}' has an icon ({}) that could not be resolved, using the first item in the tab instead.",
                    this.getTabLabel(), this.icon);
        }
        for (final Item item : ForgeRegistries.ITEMS) {
            if (item.getCreativeTab() == this) {
                return new ItemStack(item);
            }
        }
        return configured;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void displayAllRelevantItems(final NonNullList<ItemStack> items) {
        super.displayAllRelevantItems(items);
        if (this.sort) {
            items.sort((o1, o2) -> {
                final Item i1 = o1.getItem();
                final Item i2 = o2.getItem();
                final ResourceLocation id1 = i1 instanceof ItemBlock ? ((ItemBlock) i1).getBlock().getRegistryName() : i1.getRegistryName();
                final ResourceLocation id2 = i2 instanceof ItemBlock ? ((ItemBlock) i2).getBlock().getRegistryName() : i2.getRegistryName();
                return id1.compareTo(id2);
            });
        }
    }
}
