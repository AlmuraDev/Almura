/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

/**
 * Heals whoever eats or drinks the item by an amount of health (1.12 food's {@code health_change}), registered as {@code almura:heal}.
 */
public record HealConsumeEffect(float amount) implements ConsumeEffect {

    public static final MapCodec<HealConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.FLOAT.fieldOf("amount").forGetter(HealConsumeEffect::amount)).apply(instance, HealConsumeEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HealConsumeEffect> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, HealConsumeEffect::amount, HealConsumeEffect::new);
    public static final ConsumeEffect.Type<HealConsumeEffect> TYPE = new ConsumeEffect.Type<>(CODEC, STREAM_CODEC);

    @Override
    public ConsumeEffect.Type<HealConsumeEffect> getType() {
        return TYPE;
    }

    @Override
    public boolean apply(final Level level, final ItemStack stack, final LivingEntity user) {
        if (this.amount <= 0f || user.getHealth() >= user.getMaxHealth()) {
            return false;
        }
        user.heal(this.amount);
        return true;
    }
}
