/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor.foodeffect;

import com.almuradev.content.type.item.type.food.processor.foodeffect.potioneffect.RandomPotionEffectTemplate;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import net.minecraft.potion.Potion;
import org.spongepowered.api.Sponge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public final class FoodEffect {

    public static ConfigurationNodeDeserializer<FoodEffect> PARSER = node -> {
        if (node.isVirtual()) {
            return Optional.empty();
        }

        final Set<RandomPotionEffectTemplate> potionEffects = new HashSet<>();

        if (node.getValue() instanceof List) {
            node.getChildrenList().forEach(potionEffectEntryNode -> RandomPotionEffectTemplate.PARSER.deserialize(potionEffectEntryNode).ifPresent(potionEffects::add));
        } else if (node.getValue() instanceof Map) {
            RandomPotionEffectTemplate.PARSER.deserialize(node).ifPresent(potionEffects::add);
        }

        if (potionEffects.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new FoodEffect(potionEffects));
    };

    private final Set<RandomPotionEffectTemplate> potionEffects;
    private final Map<Potion, RandomPotionEffectTemplate> foundEffectsForPotion = new HashMap<>();
    private boolean cached = false;

    private FoodEffect(Set<RandomPotionEffectTemplate> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public Map<Potion, RandomPotionEffectTemplate> getPotionEffects() {
        if (cached) {
            return this.foundEffectsForPotion;
        }

        // By the time this is called for, we'll be in-game and the potions will be here
        for (Potion potion : Potion.REGISTRY) {
            potionEffects.forEach(potionEffect -> {
                if (potionEffect.getPotionEffect().getPotionPredicate().test(potion)) {
                    foundEffectsForPotion.put(potion, potionEffect);
                }
            });
        }

        cached = true;
        return foundEffectsForPotion;
    }
}
