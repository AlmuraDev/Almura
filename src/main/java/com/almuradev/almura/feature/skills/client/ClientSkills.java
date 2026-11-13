/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.client;

import com.almuradev.almura.feature.skills.SkillEntry;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The player's skills on the client, and which of them the player shows on the HUD.
 *
 * <p>Nothing fills it yet: on 1.12 the server sent the skills from the Skills plugin, which has no 26.3 version, and the player chose the
 * HUD's skills on the Skills screen, which isn't ported. A dev run started with {@code -PsampleSkills} fills it with sample skills.</p>
 */
public final class ClientSkills {

    // Set with -PsampleSkills (see build.gradle), to see the HUD's skills panel before skills come from a server
    private static final boolean SAMPLE = Boolean.getBoolean("almura.dev.sampleSkills");

    private static volatile List<SkillEntry> skills = SAMPLE ? sample() : List.of();
    // The skills shown on the HUD, in the order chosen; none until chosen, as on 1.12
    private static final Set<String> shownOnHud = new LinkedHashSet<>();

    static {
        if (SAMPLE) {
            skills.forEach(skill -> shownOnHud.add(skill.id()));
        }
    }

    private ClientSkills() {
    }

    public static List<SkillEntry> skills() {
        return ClientSkills.skills;
    }

    public static void setSkills(final List<SkillEntry> skills) {
        ClientSkills.skills = List.copyOf(skills);
    }

    public static boolean isShownOnHud(final String id) {
        return ClientSkills.shownOnHud.contains(id);
    }

    public static void setShownOnHud(final String id, final boolean shown) {
        if (shown) {
            ClientSkills.shownOnHud.add(id);
        } else {
            ClientSkills.shownOnHud.remove(id);
        }
    }

    /**
     * @return The skills the player shows on the HUD, in the skills' order
     */
    public static List<SkillEntry> hudSkills() {
        return ClientSkills.skills.stream().filter(skill -> ClientSkills.shownOnHud.contains(skill.id())).toList();
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
