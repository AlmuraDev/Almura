/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.block.BlockAABB;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.Material;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;

import java.util.Locale;
import java.util.Optional;

public class SetCommonBlockAttributesTask implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    public static final SetCommonBlockAttributesTask instance = new SetCommonBlockAttributesTask();

    @Override
    public void execute(AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) throws TaskExecutionFailedException {
        final BuildableBlockType.Builder builder = context.getBuilder();
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

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
            final Optional<Material> material = Sponge.getRegistry().getType(Material.class, materialNode.getString());
            material.ifPresent(builder::material);
        } else {
            Almura.instance.logger.debug("Block '{}' at '{}' does not have a {}", context.getAsset().getName(), context.getAsset().getPath().toString(), Constants.Config.Block.MATERIAL);
        }

        final ConfigurationNode mapColorNode = generalNode.getNode(Constants.Config.Block.MAP_COLOR);
        if (!mapColorNode.isVirtual()) {
            final Optional<MapColor> MapColor = Sponge.getRegistry().getType(MapColor.class, mapColorNode.getString());
            MapColor.ifPresent(builder::mapColor);
        } else {
            Almura.instance.logger.debug("Block '{}' at '{}' does not have a {}", context.getAsset().getName(), context.getAsset().getPath().toString(), Constants.Config.Block.MAP_COLOR);
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

        final Pack pack = context.getPack();
        builder.build(Constants.Plugin.ID + ":" + pack.getName().toLowerCase(Locale.ENGLISH) + "/" + context.getAsset().getName());
    }
}
