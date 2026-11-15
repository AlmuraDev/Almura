/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One of the Skills plugin's rules as the Skills Handbook shows it, as 3.5's {@code SkillRule}: a skill, what is done, to which block or item,
 * the level it needs and the experience and money it gives.
 *
 * @param skill the skill's id, such as {@code farming}
 * @param id a block's id when {@code block}, otherwise an item's
 * @param level the level needed, or -1 when it isn't gated
 * @param xp the experience given, or -1 when none
 * @param money the money given (Skills' money modifier for the rule), or -1 when none
 */
public record HandbookRule(String skill, Action action, String id, boolean block, int level, double xp, double money) {

    /**
     * What is done, as 3.5's actions: Skills 2.0's triggers by the names 3.5 gave them.
     */
    public enum Action {
        BREAK("Break"),
        PLACE("Place"),
        USE_ON_BLOCK("Use on"),
        CRAFT("Craft"),
        GATHER("Gather"),
        USE("Use"),
        SMELT("Smelt");

        public final String label;

        Action(final String label) {
            this.label = label;
        }
    }

    private static final Action[] ACTIONS = Action.values();

    public static final StreamCodec<ByteBuf, HandbookRule> CODEC = StreamCodec.of((buffer, rule) -> {
        ByteBufCodecs.STRING_UTF8.encode(buffer, rule.skill);
        buffer.writeByte(rule.action.ordinal());
        ByteBufCodecs.STRING_UTF8.encode(buffer, rule.id);
        buffer.writeBoolean(rule.block);
        ByteBufCodecs.VAR_INT.encode(buffer, rule.level + 1);
        buffer.writeDouble(rule.xp);
        buffer.writeDouble(rule.money);
    }, buffer -> new HandbookRule(ByteBufCodecs.STRING_UTF8.decode(buffer), ACTIONS[buffer.readByte()], ByteBufCodecs.STRING_UTF8.decode(buffer),
        buffer.readBoolean(), ByteBufCodecs.VAR_INT.decode(buffer) - 1, buffer.readDouble(), buffer.readDouble()));
}
