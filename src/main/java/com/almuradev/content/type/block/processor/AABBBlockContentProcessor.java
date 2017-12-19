/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.component.aabb.AABBConfig;
import com.almuradev.content.type.block.component.aabb.BlockAABBFactory;
import com.almuradev.toolbox.config.tag.ConfigTag;
import com.google.inject.Injector;
import net.minecraft.util.math.AxisAlignedBB;
import ninja.leaping.configurate.ConfigurationNode;

import javax.inject.Inject;

public final class AABBBlockContentProcessor implements BlockContentProcessor.State.Any {

    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.AABB);
    private final Injector injector;

    @Inject
    public AABBBlockContentProcessor(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        final BlockAABBFactory<?, ?, ?> factory = this.injector.getInstance(builder.genre().boxFactory);

        final ConfigurationNode shape = config.getNode(AABBConfig.SHAPE);
        if (!shape.isVirtual()) {
            definition.box(factory.box(shape));
        }

        final ConfigurationNode collision = config.getNode(AABBConfig.COLLISION);
        if (!collision.isVirtual()) {
            // TODO(kashike): configurate is like a wet sock in this case - NOBODY LIKES IT
            if (collision.getValue() instanceof Number && ((Number) collision.getValue()).intValue() == -1) {
                definition.collisionBox(factory.collision((AxisAlignedBB) null));
            } else {
                definition.collisionBox(factory.collision(collision));
            }
        }

        final ConfigurationNode wireframe = config.getNode(AABBConfig.WIREFRAME);
        if (!wireframe.isVirtual()) {
            definition.wireFrame(factory.wireFrame(wireframe));
        }
    }
}
