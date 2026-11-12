/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.type.skill.builtin.BuiltinEventListener;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.FarmingRegistar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import com.almuradev.almura.feature.complex.item.almanac.VanillaCrops;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The farming levels the Skills plugin requires to plant and harvest a block. Skills keeps these in its own rules (FarmingRegistar), so
 * they're read from there and always match what the plugin enforces.
 *
 * <p>Only use this class when the Skills plugin is loaded; it refers to the plugin's classes.</p>
 */
public final class SkillsCropLevels {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillsCropLevels.class);
    private static boolean warned;

    private final Map<SkillType, List<BlockChain>> plantRules;
    private final Map<SkillType, List<BlockChain>> harvestRules;
    @Nullable private final SkillType farming;

    private SkillsCropLevels(final Map<SkillType, List<BlockChain>> plantRules, final Map<SkillType, List<BlockChain>> harvestRules,
            @Nullable final SkillType farming) {
        this.plantRules = plantRules;
        this.harvestRules = harvestRules;
        this.farming = farming;
    }

    /**
     * @return the current rules, or null if they can't be read from this version of the Skills plugin
     */
    @Nullable
    public static SkillsCropLevels load() {
        try {
            final Field listenerField = FarmingRegistar.class.getDeclaredField("listener");
            listenerField.setAccessible(true);
            final BuiltinEventListener listener = (BuiltinEventListener) listenerField.get(null);
            if (listener == null) {
                return null;
            }

            final Field chainsField = BuiltinEventListener.class.getDeclaredField("blockChains");
            chainsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>> chains =
                    (Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>>) chainsField.get(listener);

            final SkillType farming = Sponge.getRegistry().getType(SkillType.class, "skills:farming").orElse(null);
            return new SkillsCropLevels(chains.getOrDefault(ChangeBlockEvent.Place.class, Collections.emptyMap()),
                    chains.getOrDefault(ChangeBlockEvent.Break.class, Collections.emptyMap()), farming);
        } catch (final ReflectiveOperationException | RuntimeException e) {
            if (!warned) {
                warned = true;
                LOGGER.warn("Couldn't read the farming levels from the Skills plugin; the Farmer's Almanac won't show them.", e);
            }
            return null;
        }
    }

    /**
     * Fills in the plant and harvest levels of every Almura crop, by block id, and of Minecraft's crops, by their {@link VanillaCrops} id.
     *
     * @return false if the levels couldn't be read from the Skills plugin
     */
    public static boolean fill(final Map<String, int[]> levels) {
        final SkillsCropLevels rules = load();
        if (rules == null) {
            return false;
        }
        for (final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (block instanceof CropBlockImpl && block.getRegistryName() != null) {
                final BlockType type = (BlockType) block;
                levels.put(block.getRegistryName().toString(), new int[] {rules.plantLevel(type), rules.harvestLevel(type)});
            }
        }
        // Minecraft's own crops, by the Almanac's id for them: planted as one block, harvested as another for melons and pumpkins
        for (final VanillaCrops.VanillaCrop crop : VanillaCrops.ALL) {
            final Block plant = Block.getBlockFromName(crop.plantBlock);
            final Block harvest = Block.getBlockFromName(crop.harvestBlock);
            levels.put(crop.id, new int[] {plant == null ? -1 : rules.plantLevel((BlockType) plant),
                    harvest == null ? -1 : rules.harvestLevel((BlockType) harvest)});
        }
        return true;
    }

    /**
     * @return the farming level needed to plant the block, or -1 if planting it isn't gated
     */
    public int plantLevel(final BlockType type) {
        return level(this.plantRules, type);
    }

    /**
     * @return the farming level needed to harvest the block, or -1 if harvesting it isn't gated
     */
    public int harvestLevel(final BlockType type) {
        return level(this.harvestRules, type);
    }

    private int level(final Map<SkillType, List<BlockChain>> rules, final BlockType type) {
        if (this.farming == null) {
            return -1;
        }
        final List<BlockChain> chains = rules.get(this.farming);
        if (chains == null) {
            return -1;
        }
        // Skills uses the first registered rule that matches, so the first one naming this block is the one that applies
        for (final BlockChain chain : chains) {
            if (chain.inverseQuery || chain.level == null) {
                continue;
            }
            for (final BlockState query : chain.toQuery) {
                if (query.getType() == type) {
                    return chain.level;
                }
            }
        }
        return -1;
    }
}
