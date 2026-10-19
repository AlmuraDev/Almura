/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import org.spongepowered.api.network.ChannelBuf;

/**
 * A snapshot of one of a player's skills, sent from the server to the client.
 */
public final class SkillEntry {

    /** Skill id without the "skills:" prefix, e.g. "farming". */
    public final String id;
    /** Display name with legacy formatting codes, e.g. "§aFarming". */
    public final String name;
    /** RGB color of the skill's name, used for its progress bar. */
    public final int color;
    public final int level;
    public final int maxLevel;
    public final double experience;
    /** Total experience needed to reach the current level. */
    public final double levelExperience;
    /** Total experience needed to reach the next level (equal to levelExperience at max level). */
    public final double nextLevelExperience;
    /** Total experience needed to reach the max level. */
    public final double maxExperience;

    public SkillEntry(final String id, final String name, final int color, final int level, final int maxLevel, final double experience,
            final double levelExperience, final double nextLevelExperience, final double maxExperience) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.level = level;
        this.maxLevel = maxLevel;
        this.experience = experience;
        this.levelExperience = levelExperience;
        this.nextLevelExperience = nextLevelExperience;
        this.maxExperience = maxExperience;
    }

    public boolean isMaxLevel() {
        return this.level >= this.maxLevel;
    }

    /**
     * @return experience still needed for the next level, 0 at max level
     */
    public double getExperienceToNextLevel() {
        return this.isMaxLevel() ? 0 : Math.max(0, this.nextLevelExperience - this.experience);
    }

    /**
     * @return progress through the current level, from 0 to 1 (1 at max level)
     */
    public float getLevelProgress() {
        if (this.isMaxLevel()) {
            return 1f;
        }
        final double span = this.nextLevelExperience - this.levelExperience;
        if (span <= 0) {
            return 0f;
        }
        return (float) Math.min(1, Math.max(0, (this.experience - this.levelExperience) / span));
    }

    public static SkillEntry readFrom(final ChannelBuf buf) {
        return new SkillEntry(buf.readString(), buf.readString(), buf.readInteger(), buf.readInteger(), buf.readInteger(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void writeTo(final ChannelBuf buf) {
        buf.writeString(this.id);
        buf.writeString(this.name);
        buf.writeInteger(this.color);
        buf.writeInteger(this.level);
        buf.writeInteger(this.maxLevel);
        buf.writeDouble(this.experience);
        buf.writeDouble(this.levelExperience);
        buf.writeDouble(this.nextLevelExperience);
        buf.writeDouble(this.maxExperience);
    }
}
