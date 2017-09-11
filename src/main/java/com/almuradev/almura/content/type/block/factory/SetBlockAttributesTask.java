/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.Almura;
import com.almuradev.almura.configuration.ConfigurationNodes;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.StageTask;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreakSerializer;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.type.block.state.BlockStateDefinition;
import com.almuradev.almura.content.type.block.state.BlockStateDefinitionBuilder;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.almura.registry.CatalogDelegate;
import com.almuradev.almura.registry.Registries;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Map;
import java.util.UUID;

public class SetBlockAttributesTask implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    @Override
    public void execute(final AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) {
        final Asset asset = context.getAsset();
        final ConfigurationNode node = asset.getConfigurationNode();

        final BuildableBlockType.Builder builder = context.getBuilder();
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getNode(BlockConfig.STATES).getChildrenMap().entrySet()) {
            final BlockStateDefinition definition = this.buildStateDefinition(asset, builder, String.valueOf(entry), entry.getValue());
            builder.putState(definition);
        }
    }

    private BlockStateDefinition buildStateDefinition(final Asset asset, final BuildableBlockType.Builder<?, ?> block, final String id, final ConfigurationNode node) {
        final BlockStateDefinitionBuilder<?> builder = BlockStateDefinitionBuilder.create();

        builder.id(asset.getName(), id);

        ConfigurationNodes.whenRealString(node.getNode(BlockConfig.State.PARENT_KEY), parentId -> block.findState(parentId).ifPresent(builder::from));

        this.configureAABB(asset, builder, node.getNode(BlockConfig.State.AABB_KEY));
        this.configureGeneric(asset, builder, node.getNode(BlockConfig.State.GENERIC_KEY));
        this.configureSound(asset, builder, node.getNode(BlockConfig.State.SOUND_KEY));
        this.configureAction(asset, builder, node.getNode(BlockConfig.State.ACTION_KEY));

        return builder.build();
    }

    private void configureAABB(final Asset asset, final BlockStateDefinitionBuilder<?> builder, final ConfigurationNode node) {
        if (node.isVirtual()) {
            return;
        }
        CollisionBox.deserialize(node.getNode(BlockConfig.State.AABB.COLLISION)).ifPresent(builder::collisionAABB);
        WireFrame.deserialize(node.getNode(BlockConfig.State.AABB.WIREFRAME)).ifPresent(builder::wireFrameAABB);
    }

    private void configureGeneric(final Asset asset, final BlockStateDefinitionBuilder<?> builder, final ConfigurationNode node) {
        final ConfigurationNode material = node.getNode(BlockConfig.State.Generic.MATERIAL);
        if (!material.isVirtual()) {
            Registries.findCatalog(Material.class, material.getString()).ifPresent(builder::material);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MATERIAL);
        }

        final ConfigurationNode mapColor = node.getNode(BlockConfig.State.Generic.MAP_COLOR);
        if (!mapColor.isVirtual()) {
            Registries.findCatalog(MapColor.class, mapColor.getString()).ifPresent(builder::mapColor);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MAP_COLOR);
        }

        final ConfigurationNode slipperiness = node.getNode(BlockConfig.State.Generic.SLIPPERINESS);
        if (!slipperiness.isVirtual()) {
            builder.slipperiness(slipperiness.getFloat());
        }

        final ConfigurationNode hardness = node.getNode(BlockConfig.State.Generic.HARDNESS);
        if (!hardness.isVirtual()) {
            builder.hardness(hardness.getFloat());
        }

        final ConfigurationNode light = node.getNode(BlockConfig.State.Generic.LIGHT);
        if (!light.isVirtual()) {
            final ConfigurationNode emissionNode = light.getNode(BlockConfig.State.Generic.LIGHT_EMISSION);
            if (!emissionNode.isVirtual()) {
                final float emission = emissionNode.getFloat();
                builder.lightEmission(emission > 1f ? emission / 15f : emission); // * 15f in Block#setLightLevel
            }
            final ConfigurationNode opacity = light.getNode(BlockConfig.State.Generic.LIGHT_OPACITY);
            if (!opacity.isVirtual()) {
                builder.lightOpacity(opacity.getInt());
            }
        }

        final ConfigurationNode resistance = node.getNode(BlockConfig.State.Generic.RESISTANCE);
        if (!resistance.isVirtual()) {
            builder.resistance(resistance.getFloat());
        }
    }

    private void configureSound(final Asset asset, final BlockStateDefinitionBuilder<?> builder, final ConfigurationNode node) {
        if (!node.isVirtual()) {
            if (node.getValue() instanceof String) {
                builder.soundGroup(new CatalogDelegate<>(BlockSoundGroup.class, node.getString()));
            } else {
                final String id = UUID.randomUUID().toString().replace("-", "");
                builder.soundGroup(CatalogDelegate.of(BlockSoundGroupBuilder.createVirtual(id, node)));
            }
        } else {
            this.missingData(asset, BlockConfig.State.Generic.SOUND_GROUP);
        }
    }

    private void configureAction(final Asset asset, final BlockStateDefinitionBuilder<?> builder, final ConfigurationNode node) {
        final ConfigurationNode breaks = node.getNode(BlockConfig.State.Action.BREAK);
        if (!breaks.isVirtual()) {
            BlockBreakSerializer.INSTANCE.deserialize(breaks).ifPresent(builder::breaks);
        }
    }

    private void missingData(final Asset asset, final String what) {
        Almura.instance.logger.debug("Block '{}' at '{}' does not have a {}", asset.getName(), asset.getPath().toString(), what);
    }
}
