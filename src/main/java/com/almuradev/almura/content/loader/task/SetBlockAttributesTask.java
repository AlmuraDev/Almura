/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.task;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.content.block.impl.BlockAABB;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.block.data.blockbreak.BlockBreakSerializer;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;

import java.util.Optional;

public class SetBlockAttributesTask implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    @Override
    public void execute(AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) {
        final BuildableBlockType.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();
        final GameRegistry registry = Sponge.getRegistry();

        // AABB
        final ConfigurationNode aabbNode = node.getNode(Constants.Config.Block.AABB.KEY);
        if (!aabbNode.isVirtual()) {
            BlockAABB.COLLISION.deserialize(aabbNode.getNode(Constants.Config.Block.AABB.COLLISION)).ifPresent(builder::collisionAABB);
            BlockAABB.WIRE_FRAME.deserialize(aabbNode.getNode(Constants.Config.Block.AABB.WIREFRAME)).ifPresent(builder::wireFrameAABB);
        }

        // General
        final ConfigurationNode generalNode = node.getNode(Constants.Config.GENERAL);

        final ConfigurationNode materialNode = generalNode.getNode(Constants.Config.Block.MATERIAL);
        if (!materialNode.isVirtual()) {
            final Optional<Material> material = registry.getType(Material.class, materialNode.getString());
            material.ifPresent(builder::material);
        } else {
            this.doesNotHave(context.getAsset(), Constants.Config.Block.MATERIAL);
        }

        final ConfigurationNode mapColorNode = generalNode.getNode(Constants.Config.Block.MAP_COLOR);
        if (!mapColorNode.isVirtual()) {
            final Optional<MapColor> mapColor = registry.getType(MapColor.class, mapColorNode.getString());
            mapColor.ifPresent(builder::mapColor);
        } else {
            this.doesNotHave(context.getAsset(), Constants.Config.Block.MAP_COLOR);
        }

        final ConfigurationNode soundGroupNode = generalNode.getNode(Constants.Config.Block.SOUND_GROUP);
        if (!soundGroupNode.isVirtual()) {
            final Optional<BlockSoundGroup> soundGroup = registry.getType(BlockSoundGroup.class, soundGroupNode.getString());
            soundGroup.ifPresent(builder::soundGroup);
        } else {
            this.doesNotHave(context.getAsset(), Constants.Config.Block.SOUND_GROUP);
        }

        final ConfigurationNode slipperinessNode = generalNode.getNode(Constants.Config.Block.SLIPPERINESS);
        if (!slipperinessNode.isVirtual()) {
            builder.slipperiness(slipperinessNode.getFloat());
        }

        final ConfigurationNode hardnessNode = generalNode.getNode(Constants.Config.Block.HARDNESS);
        if (!hardnessNode.isVirtual()) {
            builder.hardness(hardnessNode.getFloat());
        }

        final ConfigurationNode lightNode = generalNode.getNode(Constants.Config.Block.LIGHT);
        if (!lightNode.isVirtual()) {
            final ConfigurationNode emissionNode = lightNode.getNode(Constants.Config.Block.LIGHT_EMISSION);
            if (!emissionNode.isVirtual()) {
                final float emission = emissionNode.getFloat();
                builder.lightEmission(emission > 1f ? emission / 15f : emission); // * 15f in Block#setLightLevel
            }
            final ConfigurationNode opacity = lightNode.getNode(Constants.Config.Block.LIGHT_OPACITY);
            if (!opacity.isVirtual()) {
                builder.lightOpacity(opacity.getInt());
            }
        }

        final ConfigurationNode resistanceNode = generalNode.getNode(Constants.Config.Block.RESISTANCE);
        if (!resistanceNode.isVirtual()) {
            builder.resistance(resistanceNode.getFloat());
        }

        final ConfigurationNode breakNode = node.getNode(Constants.Config.Block.BREAK);
        if (!breakNode.isVirtual()) {
            BlockBreakSerializer.INSTANCE.deserialize(breakNode).ifPresent(builder::breaks);
        }
    }

    private void doesNotHave(final Asset asset, final String what) {
        Almura.instance.logger.debug("Block '{}' at '{}' does not have a {}", asset.getName(), asset.getPath().toString(), what);
    }
}
