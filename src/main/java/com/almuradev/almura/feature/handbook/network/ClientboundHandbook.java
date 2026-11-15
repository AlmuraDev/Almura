/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook.network;

import com.almuradev.almura.feature.handbook.HandbookRule;
import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Opens the Skills Handbook, as 3.5's {@code ClientboundSkillsHandbookPacket}: the player's level in each skill (-1 when not known), in the
 * order of the handbook's tabs, and every rule that needs a level or gives experience.
 */
public record ClientboundHandbook(List<SkillLevel> levels, List<HandbookRule> rules) implements CustomPacketPayload {

    /**
     * The player's level in a skill.
     */
    public record SkillLevel(String skill, int level) {

        public static final StreamCodec<ByteBuf, SkillLevel> CODEC = StreamCodec.of((buffer, entry) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, entry.skill);
            ByteBufCodecs.VAR_INT.encode(buffer, entry.level + 1);
        }, buffer -> new SkillLevel(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer) - 1));
    }

    public static final Type<ClientboundHandbook> TYPE = AlmuraNetwork.type("skills_handbook");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHandbook> CODEC = StreamCodec.composite(
        SkillLevel.CODEC.apply(ByteBufCodecs.list()), ClientboundHandbook::levels,
        HandbookRule.CODEC.apply(ByteBufCodecs.list()), ClientboundHandbook::rules,
        ClientboundHandbook::new);

    @Override
    public Type<ClientboundHandbook> type() {
        return TYPE;
    }
}
