/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food.processor.foodeffect.potioneffect;

import com.almuradev.content.component.predicate.FunctionPredicate;
import com.almuradev.content.registry.predicate.ResourceLocationPredicateParser;
import com.almuradev.content.type.item.definition.ItemDefinition;
import com.almuradev.toolbox.config.ConfigurationNodeDeserializer;
import com.google.common.base.MoreObjects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class PotionEffectTemplate {

    private static final ResourceLocationPredicateParser<Potion>
            POTION_FILTER_BY_REGISTRY_NAME = ResourceLocationPredicateParser.of(IForgeRegistryEntry.Impl::getRegistryName);

    public static ConfigurationNodeDeserializer<PotionEffectTemplate> PARSER = new ConfigurationNodeDeserializer<PotionEffectTemplate>() {
        @Override
        public Optional<PotionEffectTemplate> deserialize(ConfigurationNode node) {
            if (node.isVirtual()) {
                return Optional.empty();
            }

            final FunctionPredicate<Potion, ResourceLocation> potionPredicate = POTION_FILTER_BY_REGISTRY_NAME.deserialize(node.getNode
                    (PotionEffectConfig.POTION)).orElse(null);

            if (potionPredicate == null) {
                return Optional.empty();
            }

            final Set<ItemDefinition> curatives = new HashSet<>();
            this.deserializeDefinitions(curatives, node.getNode(PotionEffectConfig.CURATIVE));

            final int duration = node.getNode(PotionEffectConfig.DURATION).getInt(0);
            final int amplifier = node.getNode(PotionEffectConfig.AMPLIFIER).getInt(0);
            final boolean ambient = node.getNode(PotionEffectConfig.AMBIENT).getBoolean(false);
            final boolean showParticles = node.getNode(PotionEffectConfig.SHOW_PARTICLES).getBoolean(true);

            return Optional.of(new PotionEffectTemplate(potionPredicate, curatives, duration, amplifier, ambient, showParticles));
        }

        private void deserializeDefinitions(final Set<ItemDefinition> itemDefinitions, final ConfigurationNode itemNode) {
            if (itemNode.isVirtual()) {
                return;
            }
            if (itemNode.getValue() instanceof List) {
                for (final ConfigurationNode itemNodeEntry : itemNode.getChildrenList()) {
                    ItemDefinition.PARSER.deserialize(itemNodeEntry).ifPresent(itemDefinitions::add);
                }
            } else {
                ItemDefinition.PARSER.deserialize(itemNode).ifPresent(itemDefinitions::add);
            }
        }
    };

    private final FunctionPredicate<Potion, ResourceLocation> potionPredicate;
    private final Set<ItemDefinition> curatives;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean showParticles;

    private PotionEffectTemplate(FunctionPredicate<Potion, ResourceLocation> potionPredicate, Set<ItemDefinition> curatives, int duration, int
            amplifier, boolean ambient, boolean showParticles) {
        this.potionPredicate = potionPredicate;
        this.curatives = curatives;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticles = showParticles;
    }

    public FunctionPredicate<Potion, ResourceLocation> getPotionPredicate() {
        return potionPredicate;
    }

    public Set<ItemDefinition> getCuratives() {
        return this.curatives;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("potionPredicates", this.potionPredicate)
                .add("curatives", this.curatives)
                .add("duration", this.duration)
                .add("amplifier", this.amplifier)
                .add("ambient", this.ambient)
                .add("showParticles", this.showParticles)
                .toString();
    }
}
