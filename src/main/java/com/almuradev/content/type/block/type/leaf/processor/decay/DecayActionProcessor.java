/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.leaf.processor.decay;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.loader.MultiTypeExternalContentProcessor;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.action.ActionConfig;
import com.almuradev.content.type.action.ActionContentType;
import com.almuradev.content.type.action.ActionGenre;
import com.almuradev.content.type.action.type.blockdecay.BlockDecayAction;
import com.almuradev.content.type.block.BlockConfig;
import com.almuradev.content.type.block.type.leaf.LeafBlock;
import com.almuradev.content.type.block.type.leaf.processor.LeafBlockContentProcessor;
import com.almuradev.content.type.block.type.leaf.state.LeafBlockStateDefinitionBuilder;
import com.almuradev.toolbox.config.tag.ConfigTag;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.UUID;

import javax.inject.Inject;

public final class DecayActionProcessor implements LeafBlockContentProcessor.AnyState {
    private static final ConfigTag TAG = ConfigTag.create(BlockConfig.State.ACTION, ActionConfig.DECAY);
    private final MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>> da;

    @Inject
    public DecayActionProcessor(final MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>> da) {
        this.da = da;
    }

    @Override
    public ConfigTag tag() {
        return TAG;
    }

    @Override
    public void processState(ConfigurationNode config, LeafBlock.Builder builder, LeafBlockStateDefinitionBuilder definition) {
        final BlockDecayAction.Builder bda = this.da.processExternal(builder.string(ContentBuilder.StringType.NAMESPACE), ActionGenre.BLOCK_DECAY, config,
                UUID.randomUUID().toString());
        definition.decayAction(Delegate.supplying(bda::build));
    }
}
