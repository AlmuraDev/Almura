/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.processor;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.loader.MultiTypeExternalContentProcessor;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.action.ActionConfig;
import com.almuradev.content.type.action.ActionContentType;
import com.almuradev.content.type.action.ActionGenre;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;
import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.BlockContentProcessor;
import com.almuradev.content.type.block.BlockStateDefinition;
import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.UUID;

import javax.inject.Inject;

public final class DestroyActionBlockContentProcessor implements BlockContentProcessor.State.Any {

    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.ACTION, ActionConfig.DESTROY);
    private final MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>> da;

    @Inject
    public DestroyActionBlockContentProcessor(final MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>> da) {
        this.da = da;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(final ConfigurationNode config, final ContentBlockType.Builder<ContentBlockType, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>> builder, final BlockStateDefinition.Builder<BlockStateDefinition> definition) {
        final BlockDestroyAction.Builder bda = this.da.processExternal(builder.string(ContentBuilder.StringType.NAMESPACE), ActionGenre.BLOCK_DESTROY, config, UUID.randomUUID().toString());
        definition.destroyAction(Delegate.supplying(bda::build));
    }
}
