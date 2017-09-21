/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.type.block.component.aabb.factory.BlockStateAABBFactory;
import com.almuradev.almura.content.type.block.component.action.factory.BlockStateActionFactory;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.type.block.component.sound.factory.BlockSoundGroupFactory;
import com.almuradev.almura.content.type.block.component.sound.factory.BlockStateSoundGroupFactory;
import com.almuradev.almura.content.type.block.factory.BlockItemGroupProvider;
import com.almuradev.almura.content.type.block.factory.BlockStateGenericFactory;
import com.almuradev.almura.content.type.block.registry.BlockSoundGroupRegistryModule;
import com.almuradev.almura.content.type.block.type.AbstractBlockTypeBuilder;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalTypeBuilderImpl;
import com.almuradev.shared.asset.AssetFactoryBinder;
import com.almuradev.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

import java.util.function.Consumer;

public class BlockModule extends AbstractModule implements CommonBinder {

    private final Consumer<AssetFactoryBinder.Entry> block = binder -> {
        binder.phase(LoaderPhase.CONSTRUCTION);
        binder.type(Asset.Type.BLOCK, Asset.Type.HORIZONTAL_BLOCK);
    };

    @Override
    protected void configure() {
        this.facet()
                .add(BlockListener.class);
        this.registry()
                .module(BlockSoundGroup.class, BlockSoundGroupRegistryModule.class)
                .builder(BlockSoundGroup.Builder.class, BlockSoundGroupBuilder::new)
                .builder(BuildableBlockType.Builder.class, AbstractBlockTypeBuilder.BuilderImpl::new)
                .builder(HorizontalType.Builder.class, HorizontalTypeBuilderImpl::new);
        this.asset()
                .provider(BlockSoundGroupFactory.class, binder -> {
                    binder.phase(LoaderPhase.CONSTRUCTION);
                    binder.type(Asset.Type.BLOCK_SOUNDGROUP);
                })
                .provider(BlockItemGroupProvider.class, this.block::accept)
                .provider(BlockStateAABBFactory.class, this.block::accept)
                .provider(BlockStateActionFactory.class, this.block::accept)
                .provider(BlockStateGenericFactory.class, this.block::accept)
                .provider(BlockStateSoundGroupFactory.class, this.block::accept);
    }
}
