/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.Almura;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A content item with durability that isn't a tool (the knife, grater and grinder), worn as 3.5's were: each craft it is used in takes its
 * {@code container} damage and hands it back, until it is used up. 26.3 won't let an item both wear out and stack, and 3.5's stacked to 64,
 * so the wear is kept in Almura's own {@link #WEAR} instead of vanilla's damage: tools worn alike stack, as on 1.12.
 */
public final class ContentToolItem extends Item {

    // How many uses an item has had
    public static final Identifier WEAR_ID = Identifier.fromNamespaceAndPath(Almura.ID, "wear");
    public static final DataComponentType<Integer> WEAR = DataComponentType.<Integer>builder()
        .persistent(Codec.intRange(0, Integer.MAX_VALUE))
        .networkSynchronized(ByteBufCodecs.VAR_INT)
        .build();

    private final int durability;
    private final int wearPerUse;

    ContentToolItem(final Properties properties, final int durability, final int wearPerUse) {
        super(properties);
        this.durability = durability;
        this.wearPerUse = Math.max(1, wearPerUse);
    }

    static void registerComponent(final RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> helper.register(WEAR_ID, WEAR));
    }

    private int wear(final ItemInstance item) {
        final Integer wear = item.get(WEAR);
        return wear == null ? 0 : wear;
    }

    /**
     * Handed back from a craft one use more worn; nothing once used up.
     */
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(final ItemInstance instance) {
        final int wear = this.wear(instance) + this.wearPerUse;
        if (wear >= this.durability) {
            return null;
        }
        return new ItemStackTemplate(this, 1, DataComponentPatch.builder().set(WEAR, wear).build());
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return this.wear(stack) > 0;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return Mth.clamp(Math.round(13f - this.wear(stack) * 13f / this.durability), 0, 13);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0f, (this.durability - (float) this.wear(stack)) / this.durability) / 3f, 1f, 1f);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder,
        final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        builder.accept(Component.literal("Uses left: " + (this.durability - this.wear(stack)) + " / " + this.durability)
            .withStyle(ChatFormatting.GRAY));
    }
}
