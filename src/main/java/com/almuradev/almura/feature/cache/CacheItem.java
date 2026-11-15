/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * A cache's item: its tooltip says what it holds, how many and its limit, as 3.5's.
 */
public final class CacheItem extends BlockItem {

    CacheItem(final CacheBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> tooltip,
        final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        final HolderLookup.Provider registries = context.registries();
        final CacheStack contents = registries == null ? CacheStack.EMPTY : CacheBlockEntity.stackOf(stack, registries);
        if (contents.isEmpty()) {
            tooltip.accept(Component.literal("Type: Empty"));
        } else {
            tooltip.accept(Component.literal("Type: ").append(contents.resource().getHoverName()));
            tooltip.accept(Component.literal("Size: " + CacheBlock.FORMAT.format(contents.amount())));
        }
        tooltip.accept(Component.literal("Limit: " + CacheBlock.FORMAT.format(((CacheBlock) this.getBlock()).limit())));
    }
}
