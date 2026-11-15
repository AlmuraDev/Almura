/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.skills.SkillEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * The player's skills on the client, as 3.5's {@code ClientSkillsManager}: sent by the server from its Skills plugin, and which of them the
 * player shows on the HUD (chosen on the Features menu's Skills, kept in {@code hud-skills} of the client config).
 * <p>
 * A dev run started with {@code -PsampleSkills} shows sample skills until the server sends real ones.
 */
public final class ClientSkills {

    // Set with -PsampleSkills (see build.gradle), to see the HUD's skills panel without a server with Skills
    private static final boolean SAMPLE = Boolean.getBoolean("almura.dev.sampleSkills");

    private static volatile List<SkillEntry> skills = SAMPLE ? sample() : List.of();
    // Changes whenever new skills arrive, so an open Skills screen knows to refresh
    private static volatile int revision;

    private ClientSkills() {
    }

    public static List<SkillEntry> skills() {
        return ClientSkills.skills;
    }

    public static void setSkills(final List<SkillEntry> skills) {
        ClientSkills.skills = skills.isEmpty() && SAMPLE ? sample() : List.copyOf(skills);
        ClientSkills.revision++;
    }

    static int revision() {
        return ClientSkills.revision;
    }

    static int totalLevel() {
        return ClientSkills.skills.stream().mapToInt(SkillEntry::level).sum();
    }

    static int totalMaxLevel() {
        return ClientSkills.skills.stream().mapToInt(SkillEntry::maxLevel).sum();
    }

    public static boolean isShownOnHud(final String id) {
        return AlmuraClientConfig.hudSkills().contains(id) || SAMPLE && AlmuraClientConfig.hudSkills().isEmpty();
    }

    /**
     * Shows or hides a skill on the HUD, saved in the client config.
     */
    public static void setShownOnHud(final String id, final boolean shown) {
        final List<String> chosen = new ArrayList<>(AlmuraClientConfig.hudSkills());
        if (shown == chosen.contains(id)) {
            return;
        }
        if (shown) {
            chosen.add(id);
        } else {
            chosen.remove(id);
        }
        AlmuraClientConfig.set(AlmuraClientConfig.HUD_SKILLS, chosen);
    }

    /**
     * @return The skills the player shows on the HUD, in the skills' order
     */
    public static List<SkillEntry> hudSkills() {
        return ClientSkills.skills.stream().filter(skill -> isShownOnHud(skill.id())).toList();
    }

    private static List<SkillEntry> sample() {
        return List.of(
            sample("crafting", "§dCrafting", 0xFF55FF, 76, 0.55),
            sample("digger", "§6Digger", 0xFFAA00, 63, 0.35),
            sample("farming", "§aFarming", 0x55FF55, 103, 0.05),
            sample("mining", "§7Mining", 0xAAAAAA, 83, 0.6),
            sample("woodcutting", "§aWoodcutting", 0x55FF55, 44, 0.5));
    }

    private static SkillEntry sample(final String id, final String name, final int color, final int level, final double progress) {
        final double levelExperience = level * 1000.0;
        return new SkillEntry(id, name, color, level, 120, levelExperience + progress * 1000.0, levelExperience, levelExperience + 1000.0,
            120_000.0);
    }
}
