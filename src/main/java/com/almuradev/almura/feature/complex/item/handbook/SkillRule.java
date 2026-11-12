/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.handbook;

/**
 * One thing a skill rewards or gates, as the Skills Handbook lists it: doing {@link #action} to a block or item needs {@link #level} and
 * gives {@link #xp}. Only names blocks and items by id, so the server can fill it without the client's classes and the other way round.
 */
public final class SkillRule {

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

    /** The skill's id, such as "farming" */
    public final String skill;
    public final Action action;
    /** A block's id when {@link #block}, otherwise an item's */
    public final String id;
    public final int meta;
    public final boolean block;
    /** The level needed, or -1 when it isn't gated */
    public final int level;
    /** The experience given, or -1 when none */
    public final double xp;
    /** The money given, or -1 when none */
    public final double economy;

    public SkillRule(final String skill, final Action action, final String id, final int meta, final boolean block, final int level,
            final double xp, final double economy) {
        this.skill = skill;
        this.action = action;
        this.id = id;
        this.meta = meta;
        this.block = block;
        this.level = level;
        this.xp = xp;
        this.economy = economy;
    }
}
