/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.handbook.network;

import com.almuradev.almura.feature.complex.item.handbook.SkillRule;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opens the Skills Handbook: every rule the server's Skills plugin has, and the player's level in each skill.
 */
public final class ClientboundSkillsHandbookPacket implements Message {

    /** Skill id to the player's level in it (-1 when not known), in the order the tabs show */
    public final Map<String, Integer> levels = new LinkedHashMap<>();
    public final List<SkillRule> rules = new ArrayList<>();

    @Override
    public void readFrom(final ChannelBuf buf) {
        final int skills = buf.readInteger();
        for (int i = 0; i < skills; i++) {
            this.levels.put(buf.readString(), buf.readInteger());
        }
        final SkillRule.Action[] actions = SkillRule.Action.values();
        final int rules = buf.readInteger();
        for (int i = 0; i < rules; i++) {
            final String skill = buf.readString();
            final int action = buf.readByte();
            final String id = buf.readString();
            final int meta = buf.readInteger();
            final boolean block = buf.readBoolean();
            final int level = buf.readInteger();
            final double xp = buf.readDouble();
            final double economy = buf.readDouble();
            if (action >= 0 && action < actions.length) {
                this.rules.add(new SkillRule(skill, actions[action], id, meta, block, level, xp, economy));
            }
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.levels.size());
        for (final Map.Entry<String, Integer> entry : this.levels.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeInteger(entry.getValue());
        }
        buf.writeInteger(this.rules.size());
        for (final SkillRule rule : this.rules) {
            buf.writeString(rule.skill);
            buf.writeByte((byte) rule.action.ordinal());
            buf.writeString(rule.id);
            buf.writeInteger(rule.meta);
            buf.writeBoolean(rule.block);
            buf.writeInteger(rule.level);
            buf.writeDouble(rule.xp);
            buf.writeDouble(rule.economy);
        }
    }
}
