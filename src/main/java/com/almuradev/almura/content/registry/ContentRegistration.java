/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.convert.ContentPackBuilder;
import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.EffectDef;
import com.almuradev.almura.content.def.ContentDefs.FoodDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers the content's creative tabs, items, blocks and block items, and Almura's {@code heal} consume effect, during NeoForge's
 * {@link RegisterEvent}, before the registries freeze.
 */
public final class ContentRegistration {

    private final List<ItemGroupDef> groups;
    private final List<ItemDef> items;
    private final List<BlockDef> blocks;
    // Each tab's items in the content's order, to fill it with
    private final Map<String, List<Item>> tabItems = new LinkedHashMap<>();
    private final Map<String, Block> registeredBlocks = new HashMap<>();

    public ContentRegistration(final List<ItemGroupDef> groups, final List<ItemDef> items, final List<BlockDef> blocks) {
        this.groups = groups;
        this.items = items;
        this.blocks = blocks;
    }

    @SubscribeEvent
    public void onRegister(final RegisterEvent event) {
        event.register(Registries.CONSUME_EFFECT_TYPE, helper -> helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "heal"),
            HealConsumeEffect.TYPE));
        event.register(Registries.BLOCK, helper -> {
            for (final BlockDef def : this.blocks) {
                final Block block = ContentBlocks.create(def);
                helper.register(Identifier.parse(def.id()), block);
                this.registeredBlocks.put(def.id(), block);
            }
        });
        event.register(Registries.ITEM, helper -> {
            for (final ItemDef def : this.ordered()) {
                final Item item = this.item(def);
                if (item != null) {
                    helper.register(Identifier.parse(def.id()), item);
                    this.addToTab(def.group(), item);
                }
            }
            for (final BlockDef def : this.blocks) {
                final Block block = this.registeredBlocks.get(def.id());
                if (block == null) {
                    continue;
                }
                final Item item = new BlockItem(block, new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.parse(def.id())))
                    .useBlockDescriptionPrefix());
                helper.register(Identifier.parse(def.id()), item);
                this.addToTab(def.group(), item);
            }
        });
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            for (final ItemGroupDef group : this.groups) {
                final List<Item> tabItems = this.tabItems.getOrDefault(group.id(), List.of());
                helper.register(Identifier.parse(group.id()), CreativeModeTab.builder()
                    .title(Component.translatable(ContentPackBuilder.itemGroupKey(group.id())))
                    .icon(() -> icon(group, tabItems))
                    .displayItems((parameters, output) -> tabItems.forEach(output::accept))
                    .build());
            }
        });
    }

    private void addToTab(final String group, final Item item) {
        if (group != null) {
            this.tabItems.computeIfAbsent(group, key -> new ArrayList<>()).add(item);
        }
    }

    private static ItemStack icon(final ItemGroupDef group, final List<Item> items) {
        final Identifier iconId = group.icon() == null ? null : Identifier.tryParse(group.icon());
        if (iconId != null) {
            final Optional<Holder.Reference<Item>> icon = BuiltInRegistries.ITEM.get(iconId);
            if (icon.isPresent()) {
                return new ItemStack(icon.get());
            }
        }
        return items.isEmpty() ? new ItemStack(Items.BARRIER) : new ItemStack(items.getFirst());
    }

    /**
     * Items that others are handed back in (a cup, a jar) first, so they exist when those are made.
     */
    private List<ItemDef> ordered() {
        final Set<String> containers = this.items.stream().map(ItemDef::container).filter(id -> id != null && id.startsWith(Almura.ID + ":"))
            .collect(Collectors.toSet());
        final List<ItemDef> ordered = new ArrayList<>();
        this.items.stream().filter(def -> containers.contains(def.id())).forEach(ordered::add);
        this.items.stream().filter(def -> !containers.contains(def.id())).forEach(ordered::add);
        return ordered;
    }

    private Item item(final ItemDef def) {
        final Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.parse(def.id())));
        if (def.maxStackSize() > 0) {
            properties.stacksTo(Math.min(99, def.maxStackSize()));
        }
        if (def.durability() > 0) {
            properties.durability(def.durability());
        }
        if (def.food() != null) {
            properties.food(food(def.food()), consumable(def));
        }
        final Identifier containerId = def.container() == null ? null : Identifier.tryParse(def.container());
        if (containerId != null && def.containerDamage() == 0) {
            BuiltInRegistries.ITEM.get(containerId)
                .ifPresentOrElse(container -> properties.usingConvertsTo(container.value()),
                    () -> Almura.LOGGER.warn("Content item {} is handed back in {}, which doesn't exist", def.id(), def.container()));
        }
        return new Item(properties);
    }

    /**
     * Hunger and saturation restored as 1.12 added them (its {@code food_level_change} range read at its top).
     */
    private static FoodProperties food(final FoodDef food) {
        return new FoodProperties(Math.max(0, food.nutritionMax()), Math.max(0f, food.saturation()), food.alwaysEdible());
    }

    /**
     * Eating or drinking (a drink is anything under a {@code drink} folder) for 1.12's time, then its healing and its effects at their
     * chance (the middle of 1.12's range).
     */
    private static Consumable consumable(final ItemDef def) {
        final FoodDef food = def.food();
        final Consumable.Builder builder = (def.id().contains("/drink/") ? Consumables.defaultDrink() : Consumables.defaultFood())
            .consumeSeconds(Math.max(1, food.durationTicks()) / 20f);
        if (food.health() > 0f) {
            builder.onConsume(new HealConsumeEffect(food.health()));
        }
        for (final EffectDef effect : food.effects()) {
            final List<MobEffectInstance> instances = new ArrayList<>();
            for (final String id : effect.effects()) {
                // 1.12 took "minecraft:STRENGTH"; 26.3's ids are lower case
                final Identifier effectId = Identifier.tryParse(id.toLowerCase(java.util.Locale.ROOT));
                final Optional<Holder.Reference<MobEffect>> holder = effectId == null ? Optional.empty() : BuiltInRegistries.MOB_EFFECT.get(effectId);
                if (holder.isPresent()) {
                    instances.add(new MobEffectInstance(holder.get(), effect.durationTicks(), effect.amplifier(), effect.ambient(),
                        effect.showParticles()));
                } else {
                    Almura.LOGGER.warn("Content item {} gives the effect {}, which doesn't exist", def.id(), id);
                }
            }
            if (!instances.isEmpty()) {
                builder.onConsume(new ApplyStatusEffectsConsumeEffect(instances, (effect.chanceMin() + effect.chanceMax()) / 200f));
            }
        }
        return builder.build();
    }
}
