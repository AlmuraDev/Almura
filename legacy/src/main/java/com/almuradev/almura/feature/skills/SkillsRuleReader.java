/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import com.almuradev.almura.feature.complex.item.handbook.SkillRule;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.type.skill.builtin.BuiltinEventListener;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.Chain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.FarmingRegistar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.block.tileentity.SmeltEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Reads every rule the Skills plugin enforces (what each skill gates by level and rewards with experience) for the Skills Handbook. The
 * rules live in the plugin's listener, which every skill's registrar fills, so they're read from there and always match the plugin.
 *
 * <p>Only use this class when the Skills plugin is loaded; it refers to the plugin's classes.</p>
 */
public final class SkillsRuleReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkillsRuleReader.class);
    private static final String SKILL_ID_PREFIX = "skills:";
    private static boolean warned;

    private SkillsRuleReader() {
    }

    /**
     * Adds every rule that needs a level or gives experience.
     *
     * @return false if the rules couldn't be read from the Skills plugin
     */
    @SuppressWarnings("unchecked")
    public static boolean read(final List<SkillRule> rules) {
        final Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>> blockChains;
        final Map<Class<? extends Event>, Map<SkillType, List<ItemChain>>> itemChains;
        try {
            // Every registrar shares the one listener
            final Field listenerField = FarmingRegistar.class.getDeclaredField("listener");
            listenerField.setAccessible(true);
            final BuiltinEventListener listener = (BuiltinEventListener) listenerField.get(null);
            if (listener == null) {
                return false;
            }
            final Field blockField = BuiltinEventListener.class.getDeclaredField("blockChains");
            blockField.setAccessible(true);
            blockChains = (Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>>) blockField.get(listener);
            final Field itemField = BuiltinEventListener.class.getDeclaredField("itemChains");
            itemField.setAccessible(true);
            itemChains = (Map<Class<? extends Event>, Map<SkillType, List<ItemChain>>>) itemField.get(listener);
        } catch (final ReflectiveOperationException | RuntimeException e) {
            if (!warned) {
                warned = true;
                LOGGER.warn("Couldn't read the rules from the Skills plugin; the Skills Handbook will be empty.", e);
            }
            return false;
        }

        // The same block or item can be listed by several of a rule's states (a crop's ages, a cocoa pod's facings): list it once
        final Set<String> seen = new HashSet<>();
        for (final Map.Entry<Class<? extends Event>, Map<SkillType, List<BlockChain>>> byEvent : blockChains.entrySet()) {
            final SkillRule.Action action = action(byEvent.getKey());
            if (action == null) {
                continue;
            }
            for (final Map.Entry<SkillType, List<BlockChain>> bySkill : byEvent.getValue().entrySet()) {
                final String skill = skillId(bySkill.getKey());
                for (final BlockChain chain : bySkill.getValue()) {
                    if (chain.inverseQuery || !counts(chain)) {
                        continue;
                    }
                    for (final BlockState query : chain.toQuery) {
                        final IBlockState state = (IBlockState) query;
                        final Block block = state.getBlock();
                        if (block.getRegistryName() == null) {
                            continue;
                        }
                        add(rules, seen, new SkillRule(skill, action, block.getRegistryName().toString(), block.getMetaFromState(state), true,
                                level(chain), xp(chain), economy(chain)));
                    }
                }
            }
        }
        for (final Map.Entry<Class<? extends Event>, Map<SkillType, List<ItemChain>>> byEvent : itemChains.entrySet()) {
            final SkillRule.Action action = action(byEvent.getKey());
            if (action == null) {
                continue;
            }
            for (final Map.Entry<SkillType, List<ItemChain>> bySkill : byEvent.getValue().entrySet()) {
                final String skill = skillId(bySkill.getKey());
                for (final ItemChain chain : bySkill.getValue()) {
                    if (chain.inverseQuery || !counts(chain)) {
                        continue;
                    }
                    for (final org.spongepowered.api.item.inventory.ItemStack query : chain.toQuery) {
                        final ItemStack stack = (ItemStack) (Object) query;
                        if (stack.getItem().getRegistryName() == null) {
                            continue;
                        }
                        add(rules, seen, new SkillRule(skill, action, stack.getItem().getRegistryName().toString(), stack.getMetadata(), false,
                                level(chain), xp(chain), economy(chain)));
                    }
                }
            }
        }
        return true;
    }

    private static void add(final List<SkillRule> rules, final Set<String> seen, final SkillRule rule) {
        if (seen.add(rule.skill + '|' + rule.action + '|' + rule.id + '|' + rule.meta + '|' + rule.block + '|' + rule.level + '|' + rule.xp)) {
            rules.add(rule);
        }
    }

    @Nullable
    private static SkillRule.Action action(final Class<? extends Event> event) {
        final Map<Class<?>, SkillRule.Action> actions = new HashMap<>();
        actions.put(ChangeBlockEvent.Break.class, SkillRule.Action.BREAK);
        actions.put(ChangeBlockEvent.Place.class, SkillRule.Action.PLACE);
        actions.put(InteractBlockEvent.Secondary.class, SkillRule.Action.USE_ON_BLOCK);
        actions.put(CraftItemEvent.Craft.class, SkillRule.Action.CRAFT);
        actions.put(DropItemEvent.Destruct.class, SkillRule.Action.GATHER);
        actions.put(InteractItemEvent.class, SkillRule.Action.USE);
        actions.put(SmeltEvent.Finish.class, SkillRule.Action.SMELT);
        return actions.get(event);
    }

    private static String skillId(final SkillType type) {
        final String id = type.getId();
        return id.startsWith(SKILL_ID_PREFIX) ? id.substring(SKILL_ID_PREFIX.length()) : id;
    }

    /**
     * @return true when the rule needs a level or gives experience
     */
    private static boolean counts(final Chain<?> chain) {
        return chain.level != null || (chain.xp != null && chain.xp > 0);
    }

    private static int level(final Chain<?> chain) {
        return chain.level == null ? -1 : chain.level;
    }

    private static double xp(final Chain<?> chain) {
        return chain.xp == null || chain.xp <= 0 ? -1 : chain.xp;
    }

    private static double economy(final Chain<?> chain) {
        return chain.economy == null || chain.economy <= 0 ? -1 : chain.economy;
    }
}
