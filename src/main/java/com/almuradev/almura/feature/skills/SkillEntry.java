/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

/**
 * One of the player's skills, as the server sends it.
 *
 * @param id the skill without the "skills:" prefix, e.g. "farming"
 * @param name the name to show, with legacy formatting codes, e.g. "§aFarming"
 * @param color the RGB colour of the name, for its progress bar
 * @param levelExperience the total experience needed to reach the current level
 * @param nextLevelExperience the total experience needed to reach the next level (the same as levelExperience at the most)
 * @param maxExperience the total experience needed to reach the most
 */
public record SkillEntry(String id, String name, int color, int level, int maxLevel, double experience, double levelExperience,
                         double nextLevelExperience, double maxExperience) {

    public boolean isMaxLevel() {
        return this.level >= this.maxLevel;
    }

    /**
     * @return How far through the current level, from 0 to 1 (1 at the most)
     */
    public float levelProgress() {
        if (this.isMaxLevel()) {
            return 1f;
        }
        final double span = this.nextLevelExperience - this.levelExperience;
        if (span <= 0) {
            return 0f;
        }
        return (float) Math.min(1, Math.max(0, (this.experience - this.levelExperience) / span));
    }
}
