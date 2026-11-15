/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.network;

import com.almuradev.almura.feature.skills.SkillEntry;
import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * The player's skills from the server's Skills plugin, as 3.5's {@code ClientboundSkillsPacket}: for the HUD's skills panel and the
 * Features menu's Skills.
 */
public record ClientboundSkills(List<SkillEntry> skills) implements CustomPacketPayload {

    private static final StreamCodec<ByteBuf, SkillEntry> ENTRY = StreamCodec.of((buffer, skill) -> {
        ByteBufCodecs.STRING_UTF8.encode(buffer, skill.id());
        ByteBufCodecs.STRING_UTF8.encode(buffer, skill.name());
        buffer.writeInt(skill.color());
        ByteBufCodecs.VAR_INT.encode(buffer, skill.level());
        ByteBufCodecs.VAR_INT.encode(buffer, skill.maxLevel());
        buffer.writeDouble(skill.experience());
        buffer.writeDouble(skill.levelExperience());
        buffer.writeDouble(skill.nextLevelExperience());
        buffer.writeDouble(skill.maxExperience());
    }, buffer -> new SkillEntry(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readInt(),
        ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
        buffer.readDouble()));

    public static final Type<ClientboundSkills> TYPE = AlmuraNetwork.type("skills");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSkills> CODEC = StreamCodec.composite(
        ENTRY.apply(ByteBufCodecs.list()), ClientboundSkills::skills, ClientboundSkills::new);

    @Override
    public Type<ClientboundSkills> type() {
        return TYPE;
    }
}
