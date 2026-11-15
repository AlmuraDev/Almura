/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.handbook.HandbookRule;
import com.almuradev.almura.feature.handbook.HandbookRule.Action;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.jspecify.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The Skills plugin's rules, as 3.5's {@code SkillsRuleReader} and {@code SkillsCropLevels} read them: Skills keeps them in its own classes
 * (its {@code RuleListener}'s {@code RuleBook}), not its API, so they are reached by reflection, as 3.5 did. If a Skills update moves them,
 * this logs once and the Handbook, the Almanac's levels and the sack's experience go without.
 * <p>
 * Only loaded with Sponge and Skills present; see {@link ServerSkills}.
 */
final class SkillsRules {

    // Sponge's types named in full where Minecraft's share their simple names
    private static final Class<?> SPONGE_PLAYER = org.spongepowered.api.entity.living.player.server.ServerPlayer.class;
    private static final Class<?> SPONGE_STATE = org.spongepowered.api.block.BlockState.class;

    private static boolean warned;

    private SkillsRules() {
    }

    /**
     * Skills' rule listener, which pays rewards, and its rule book.
     */
    private record Rules(Object listener, Object book, Class<?> triggers) {

        Object trigger(final String name) {
            return enumValue(this.triggers, name);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumValue(final Class<?> type, final String name) {
        return Enum.valueOf((Class) type, name);
    }

    // PluginContainer.instance() is deprecated in Sponge's plugin SPI, but it is the only way to Skills' plugin object
    @SuppressWarnings("removal")
    private static @Nullable Rules rules() {
        try {
            final Optional<PluginContainer> container = Sponge.pluginManager().plugin(Skills.ID);
            if (container.isEmpty()) {
                return null;
            }
            final Object plugin = container.get().instance();
            final Field listenerField = plugin.getClass().getDeclaredField("ruleListener");
            listenerField.setAccessible(true);
            final Object listener = listenerField.get(plugin);
            final Object book = listener.getClass().getMethod("rules").invoke(listener);
            final Class<?> triggers = Class.forName("org.inspirenxe.skills.rule.Trigger", true, listener.getClass().getClassLoader());
            return new Rules(listener, book, triggers);
        } catch (final ReflectiveOperationException | RuntimeException e) {
            warn(e);
            return null;
        }
    }

    private static void warn(final Throwable e) {
        if (!warned) {
            warned = true;
            Almura.LOGGER.warn("Couldn't read the Skills plugin's rules; the Skills Handbook, the Almanac's levels and the harvesting sack's "
                + "experience go without them", e);
        }
    }

    // ---- The Handbook

    /**
     * @return every rule that needs a level or gives experience, each block or item it names listed once
     */
    @SuppressWarnings("unchecked")
    static List<HandbookRule> handbook() {
        final Rules rules = rules();
        if (rules == null) {
            return List.of();
        }
        final List<HandbookRule> handbook = new ArrayList<>();
        try {
            final Field byTriggerField = rules.book.getClass().getDeclaredField("rules");
            byTriggerField.setAccessible(true);
            final Map<?, Map<SkillType, ?>> byTrigger = (Map<?, Map<SkillType, ?>>) byTriggerField.get(rules.book);
            // The same block or item listed again by a rule's other states (a crop's ages): listed once
            final Set<String> seen = new HashSet<>();
            for (final Map.Entry<?, Map<SkillType, ?>> trigger : byTrigger.entrySet()) {
                final Action action = action(((Enum<?>) trigger.getKey()).name());
                if (action == null) {
                    continue;
                }
                final boolean blocks = (boolean) trigger.getKey().getClass().getMethod("matchesBlocks").invoke(trigger.getKey());
                for (final Map.Entry<SkillType, ?> skill : trigger.getValue().entrySet()) {
                    final String skillId = skill.getKey().key().value();
                    final Field indexRules = skill.getValue().getClass().getDeclaredField("rules");
                    indexRules.setAccessible(true);
                    for (final Object rule : (List<?>) indexRules.get(skill.getValue())) {
                        final Integer level = (Integer) call(rule, "level");
                        final Double xp = (Double) call(rule, "xp");
                        final Double money = (Double) call(rule, "money");
                        if (level == null && (xp == null || xp <= 0)) {
                            continue;
                        }
                        for (final Object matcher : (List<?>) call(rule, "matchers")) {
                            for (final String id : expand(matcher.toString(), blocks)) {
                                final HandbookRule entry = new HandbookRule(skillId, action, id, blocks, level == null ? -1 : level,
                                    xp == null || xp <= 0 ? -1 : xp, money == null || money <= 0 ? -1 : money);
                                if (seen.add(skillId + '|' + action + '|' + id + '|' + entry.level() + '|' + entry.xp())) {
                                    handbook.add(entry);
                                }
                            }
                        }
                    }
                }
            }
        } catch (final ReflectiveOperationException | RuntimeException e) {
            warn(e);
            return List.of();
        }
        return handbook;
    }

    private static @Nullable Action action(final String trigger) {
        return switch (trigger) {
            case "BREAK" -> Action.BREAK;
            case "PLACE" -> Action.PLACE;
            case "INTERACT_BLOCK" -> Action.USE_ON_BLOCK;
            case "USE_TOOL" -> Action.USE;
            case "CRAFT" -> Action.CRAFT;
            case "DROP" -> Action.GATHER;
            case "SMELT" -> Action.SMELT;
            default -> null;
        };
    }

    private static Object call(final Object target, final String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }

    /**
     * The ids a rule's entry names: one block or item (its states left off), a tag's ({@code #minecraft:planks}) or a namespace's
     * ({@code almura:*}).
     */
    private static List<String> expand(final String entry, final boolean blocks) {
        final int open = entry.indexOf('[');
        final String id = (open < 0 ? entry : entry.substring(0, open)).trim();
        final Registry<?> registry = blocks ? BuiltInRegistries.BLOCK : BuiltInRegistries.ITEM;
        final List<String> ids = new ArrayList<>();
        if (id.startsWith("#")) {
            final Identifier tag = Identifier.tryParse(id.substring(1));
            if (tag != null) {
                final Iterable<? extends Holder<?>> members = blocks
                    ? BuiltInRegistries.BLOCK.getTagOrEmpty(TagKey.create(Registries.BLOCK, tag))
                    : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, tag));
                for (final Holder<?> member : members) {
                    member.unwrapKey().ifPresent(key -> ids.add(key.identifier().toString()));
                }
            }
        } else if (id.endsWith(":*")) {
            final String namespace = id.substring(0, id.length() - 2);
            for (final Identifier key : registry.keySet()) {
                if (key.getNamespace().equals(namespace)) {
                    ids.add(key.toString());
                }
            }
        } else {
            ids.add(id);
        }
        return ids;
    }

    // ---- The Almanac

    /**
     * @return the level the skill's first rule of the trigger matching the state needs, or -1 if none does
     */
    static int level(final String trigger, final String skillId, final BlockState state) {
        final Rules rules = rules();
        if (rules == null || !Skills.isAvailable()) {
            return -1;
        }
        final SkillType skill = Skills.service().skillType(skillId).orElse(null);
        if (skill == null) {
            return -1;
        }
        try {
            final Method find = rules.book.getClass().getMethod("find", rules.triggers, SkillType.class, SPONGE_STATE);
            final Optional<?> rule = (Optional<?>) find.invoke(rules.book, rules.trigger(trigger), skill, state);
            if (rule.isEmpty()) {
                return -1;
            }
            final Integer level = (Integer) call(rule.get(), "level");
            return level == null ? -1 : level;
        } catch (final ReflectiveOperationException | RuntimeException e) {
            warn(e);
            return -1;
        }
    }

    // ---- The harvesting sack

    /**
     * A sack's harvest, as Skills treats a crop's drops (its gather rules): a drop the player's level is too low for is lost, and the rest
     * pay their experience and money as Skills pays them.
     *
     * @return the drops the player keeps
     */
    static List<ItemStack> harvested(final ServerPlayer player, final List<ItemStack> drops) {
        final Rules rules = rules();
        final SkillHolder holder = SpongeSkills.holder(player).orElse(null);
        if (rules == null || holder == null) {
            return drops;
        }
        try {
            final Object drop = rules.trigger("DROP");
            final Method find = rules.book.getClass().getMethod("find", rules.triggers, SkillType.class, ItemType.class);
            final Method reward = rules.listener.getClass().getMethod("rewardItem", Event.class, SPONGE_PLAYER, SkillHolder.class, rules.triggers,
                ItemType.class, int.class);
            final List<ItemStack> kept = new ArrayList<>();
            final Map<ItemType, Integer> counts = new LinkedHashMap<>();
            for (final ItemStack stack : drops) {
                final ItemType type = (ItemType) (Object) stack.getItem();
                boolean gated = false;
                for (final Skill skill : holder.skills().values()) {
                    final Optional<?> rule = (Optional<?>) find.invoke(rules.book, drop, skill.type(), type);
                    if (rule.isPresent()) {
                        final Integer level = (Integer) call(rule.get(), "level");
                        if (level != null && level > skill.level()) {
                            gated = true;
                        }
                    }
                }
                if (!gated) {
                    kept.add(stack);
                    counts.merge(type, stack.getCount(), Integer::sum);
                }
            }
            for (final Map.Entry<ItemType, Integer> count : counts.entrySet()) {
                reward.invoke(rules.listener, null, player, holder, drop, count.getKey(), count.getValue());
            }
            return kept;
        } catch (final ReflectiveOperationException | RuntimeException e) {
            warn(e);
            return drops;
        }
    }
}
