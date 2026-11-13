/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.client;

import com.almuradev.almura.Almura;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Shows a content item's tooltip ({@code <name>.tooltip} in the content's translations, such as "Ingredient") under its name, grey, as
 * 1.12 did. Client only.
 */
public final class ContentTooltips {

    @SubscribeEvent
    public void onTooltip(final ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Almura.ID)) {
            return;
        }
        final String key = stack.getItem().getDescriptionId() + ".tooltip";
        if (Language.getInstance().has(key)) {
            // Under the name
            event.getToolTip().add(Math.min(1, event.getToolTip().size()), Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }
}
