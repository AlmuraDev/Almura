/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilderFactory;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.shared.registry.Registries;
import ninja.leaping.configurate.ConfigurationNode;

public class BlockStateGenericFactory extends BlockStateDefinitionBuilderFactory {

    @Override
    protected String key() {
        return BlockConfig.State.GENERIC_KEY;
    }

    @Override
    protected void configure(final Pack pack, final Asset asset, final ConfigurationNode config, final BuildableBlockType.Builder<?, ?> builder, final BlockStateDefinitionBuilder<?> definition) {
        final ConfigurationNode material = config.getNode(BlockConfig.State.Generic.MATERIAL);
        if (!material.isVirtual()) {
            Registries.findCatalog(Material.class, material.getString()).ifPresent(definition::material);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MATERIAL);
        }

        final ConfigurationNode mapColor = config.getNode(BlockConfig.State.Generic.MAP_COLOR);
        if (!mapColor.isVirtual()) {
            Registries.findCatalog(MapColor.class, mapColor.getString()).ifPresent(definition::mapColor);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MAP_COLOR);
        }

        final ConfigurationNode slipperiness = config.getNode(BlockConfig.State.Generic.SLIPPERINESS);
        if (!slipperiness.isVirtual()) {
            definition.slipperiness(slipperiness.getFloat());
        }

        final ConfigurationNode hardness = config.getNode(BlockConfig.State.Generic.HARDNESS);
        if (!hardness.isVirtual()) {
            definition.hardness(hardness.getFloat());
        }

        final ConfigurationNode light = config.getNode(BlockConfig.State.Generic.LIGHT);
        if (!light.isVirtual()) {
            final ConfigurationNode emissionNode = light.getNode(BlockConfig.State.Generic.LIGHT_EMISSION);
            if (!emissionNode.isVirtual()) {
                final float emission = emissionNode.getFloat();
                definition.lightEmission(emission > 1f ? emission / 15f : emission); // * 15f in Block#setLightLevel
            }
            final ConfigurationNode opacity = light.getNode(BlockConfig.State.Generic.LIGHT_OPACITY);
            if (!opacity.isVirtual()) {
                definition.lightOpacity(opacity.getInt());
            }
        }

        final ConfigurationNode resistance = config.getNode(BlockConfig.State.Generic.RESISTANCE);
        if (!resistance.isVirtual()) {
            definition.resistance(resistance.getFloat());
        }
    }
}
