/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.factory;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.loader.task.StageTask;
import com.almuradev.almura.content.type.block.BlockConfig;
import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.blockbreak.BlockBreakSerializer;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
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
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : node.getNode(BlockConfig.STATES).getChildrenMap().entrySet()) {
            final BlockStateDefinition definition = this.state(asset, String.valueOf(entry), entry.getValue());
            context.getBuilder().state(definition);
        }
    }

    private BlockStateDefinition state(final Asset asset, final String id, final ConfigurationNode node) {
        final BlockStateDefinitionBuilder<?> builder = BlockStateDefinitionBuilder.create();

        builder.id(asset.getName(), id);

        // --------------
        // ---- AABB ----
        // --------------
        final ConfigurationNode aabb = node.getNode(BlockConfig.State.AABB_KEY);
        if (!aabb.isVirtual()) {
            CollisionBox.deserialize(aabb.getNode(BlockConfig.State.AABB.COLLISION)).ifPresent(builder::collisionAABB);
            WireFrame.deserialize(aabb.getNode(BlockConfig.State.AABB.WIREFRAME)).ifPresent(builder::wireFrameAABB);
        }

        // -----------------
        // ---- Generic ----
        // -----------------
        final ConfigurationNode generic = node.getNode(BlockConfig.State.GENERIC_KEY);

        final ConfigurationNode material = generic.getNode(BlockConfig.State.Generic.MATERIAL);
        if (!material.isVirtual()) {
            Registries.findCatalog(Material.class, material.getString()).ifPresent(builder::material);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MATERIAL);
        }

        final ConfigurationNode mapColor = generic.getNode(BlockConfig.State.Generic.MAP_COLOR);
        if (!mapColor.isVirtual()) {
            Registries.findCatalog(MapColor.class, mapColor.getString()).ifPresent(builder::mapColor);
        } else {
            this.missingData(asset, BlockConfig.State.Generic.MAP_COLOR);
        }

        final ConfigurationNode slipperiness = generic.getNode(BlockConfig.State.Generic.SLIPPERINESS);
        if (!slipperiness.isVirtual()) {
            builder.slipperiness(slipperiness.getFloat());
        }

        final ConfigurationNode hardness = generic.getNode(BlockConfig.State.Generic.HARDNESS);
        if (!hardness.isVirtual()) {
            builder.hardness(hardness.getFloat());
        }

        final ConfigurationNode lightNode = generic.getNode(BlockConfig.State.Generic.LIGHT);
        if (!lightNode.isVirtual()) {
            final ConfigurationNode emissionNode = lightNode.getNode(BlockConfig.State.Generic.LIGHT_EMISSION);
            if (!emissionNode.isVirtual()) {
                final float emission = emissionNode.getFloat();
                builder.lightEmission(emission > 1f ? emission / 15f : emission); // * 15f in Block#setLightLevel
            }
            final ConfigurationNode opacity = lightNode.getNode(BlockConfig.State.Generic.LIGHT_OPACITY);
            if (!opacity.isVirtual()) {
                builder.lightOpacity(opacity.getInt());
            }
        }

        final ConfigurationNode resistance = generic.getNode(BlockConfig.State.Generic.RESISTANCE);
        if (!resistance.isVirtual()) {
            builder.resistance(resistance.getFloat());
        }

        // ---------------
        // ---- Sound ----
        // ---------------
        final ConfigurationNode soundGroup = generic.getNode(BlockConfig.State.SOUND_KEY);
        if (!soundGroup.isVirtual()) {
            if (soundGroup.getValue() instanceof String) {
                builder.soundGroup(new CatalogDelegate<>(BlockSoundGroup.class, soundGroup.getString()));
            } else {
                builder.soundGroup(new CatalogDelegate<>(BlockSoundGroup.class, UUID.randomUUID().toString().replace("-", "")));
            }
        } else {
            this.missingData(asset, BlockConfig.State.Generic.SOUND_GROUP);
        }

        // ----------------
        // ---- Action ----
        // ----------------
        final ConfigurationNode breaks = node.getNode(BlockConfig.State.ACTION_KEY, BlockConfig.State.Action.BREAK);
        if (!breaks.isVirtual()) {
            BlockBreakSerializer.INSTANCE.deserialize(breaks).ifPresent(builder::breaks);
        }

        return builder.build();
    }

    private void missingData(final Asset asset, final String what) {
        Almura.instance.logger.debug("Block '{}' at '{}' does not have a {}", asset.getName(), asset.getPath().toString(), what);
    }
}
