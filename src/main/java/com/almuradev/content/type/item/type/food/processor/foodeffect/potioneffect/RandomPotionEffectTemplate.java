/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor.foodeffect.potioneffect;

import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.google.common.base.MoreObjects;

import java.util.Optional;

public final class RandomPotionEffectTemplate {

    public static ConfigurationNodeDeserializer<RandomPotionEffectTemplate> PARSER = node -> {
        if (node.isVirtual()) {
            return Optional.empty();
        }

        final DoubleRange chanceRange = DoubleRange.PARSER.deserialize(node.getNode(RandomPotionEffectConfig.CHANCE)).orElse(null);
        if (chanceRange == null) {
            return Optional.empty();
        }

        final PotionEffectTemplate potionEffectTemplate = PotionEffectTemplate.PARSER.deserialize(node).orElse(null);
        if (potionEffectTemplate == null) {
            return Optional.empty();
        }

        return Optional.of(new RandomPotionEffectTemplate(potionEffectTemplate, chanceRange));
    };

    private final PotionEffectTemplate potionEffectTemplate;
    private final DoubleRange chanceRange;

    public RandomPotionEffectTemplate(PotionEffectTemplate potionEffectTemplate, DoubleRange chanceRange) {

        this.potionEffectTemplate = potionEffectTemplate;
        this.chanceRange = chanceRange;
    }

    public PotionEffectTemplate getPotionEffect() {
        return potionEffectTemplate;
    }

    public DoubleRange getChanceRange() {
        return chanceRange;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("potionEffectTemplate", this.potionEffectTemplate)
                .add("chanceRange", this.chanceRange)
                .toString();
    }
}
