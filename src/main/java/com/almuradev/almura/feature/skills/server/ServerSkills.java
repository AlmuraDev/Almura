/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.server;

import com.almuradev.almura.feature.handbook.HandbookRule;
import com.almuradev.almura.feature.handbook.SkillsHandbook;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/**
 * Hooks Almura up to the server's Skills plugin (2.0, on SpongeAPI 21) when it has one, as 3.5's {@code SkillsModule}, and what the rest of
 * Almura asks of it (the Skills Handbook, the Farmer's Almanac, the harvesting sack). Touches nothing of Sponge's or Skills' itself, so a
 * server without them loads none of their classes; without them each answer is "no skills".
 */
public final class ServerSkills {

    /** The skills shown to players, in display order, as 3.5's. */
    public static final List<String> SKILLS = List.of("crafting", "digger", "farming", "mining", "woodcutting");

    private static final boolean PRESENT = present("org.spongepowered.api.Sponge") && present("org.inspirenxe.skills.api.Skills");

    private ServerSkills() {
    }

    public static void install() {
        // The handbook's permission, with or without Skills
        NeoForge.EVENT_BUS.register(new SkillsHandbook.Permission());
        if (PRESENT) {
            SpongeSkills.install();
        }
    }

    /**
     * @return whether the server runs the Skills plugin, enabled
     */
    public static boolean available() {
        return PRESENT && SpongeSkills.available();
    }

    /**
     * @return the skills shown to players, in display order
     */
    public static List<String> skills() {
        return SKILLS;
    }

    /**
     * @return the player's level in the skill, or -1 if not known
     */
    public static int level(final ServerPlayer player, final String skill) {
        return available() ? SpongeSkills.level(player, skill) : -1;
    }

    /**
     * @return every rule that needs a level or gives experience, for the Skills Handbook
     */
    public static List<HandbookRule> handbookRules() {
        return available() ? SkillsRules.handbook() : List.of();
    }

    /**
     * @return the farming level needed to plant a crop (its planted state) and to harvest it (its ripe state), -1 for either that isn't
     *     gated, as 3.5's {@code SkillsCropLevels}
     */
    public static int[] cropLevels(final BlockState planted, final BlockState ripe) {
        if (!available()) {
            return new int[] {-1, -1};
        }
        return new int[] {SkillsRules.level("PLACE", "farming", planted), SkillsRules.level("BREAK", "farming", ripe)};
    }

    /**
     * A harvesting sack's harvest, as Skills treats a crop's drops: those the player's level is too low for are lost, the rest pay their
     * experience and money.
     *
     * @return the drops the player keeps
     */
    public static List<ItemStack> harvested(final ServerPlayer player, final List<ItemStack> drops) {
        return available() ? SkillsRules.harvested(player, drops) : drops;
    }

    private static boolean present(final String className) {
        try {
            Class.forName(className, false, ServerSkills.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
