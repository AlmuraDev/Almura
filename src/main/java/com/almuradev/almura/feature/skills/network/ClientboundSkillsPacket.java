/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.network;

import com.almuradev.almura.feature.skills.SkillEntry;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends the player all of their skills. The client replaces what it had with this list.
 */
public final class ClientboundSkillsPacket implements Message {

    public List<SkillEntry> skills = new ArrayList<>();

    public ClientboundSkillsPacket() {
    }

    public ClientboundSkillsPacket(final List<SkillEntry> skills) {
        this.skills = skills;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();
        this.skills = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.skills.add(SkillEntry.readFrom(buf));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.skills.size());
        for (final SkillEntry skill : this.skills) {
            skill.writeTo(buf);
        }
    }
}
