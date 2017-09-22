/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block;

import com.almuradev.almura.content.loader.Asset;
import com.almuradev.almura.content.loader.LoaderPhase;
import com.almuradev.almura.content.type.block.component.aabb.factory.BlockStateAABBFactory;
import com.almuradev.almura.content.type.block.component.action.breaks.BlockStateBreakActionFactory;
import com.almuradev.almura.content.type.block.component.action.fertilize.BlockStateFertilizeActionFactory;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroupBuilder;
import com.almuradev.almura.content.type.block.component.sound.factory.BlockSoundGroupFactory;
import com.almuradev.almura.content.type.block.component.sound.factory.BlockStateSoundGroupFactory;
import com.almuradev.almura.content.type.block.factory.BlockItemGroupProvider;
import com.almuradev.almura.content.type.block.factory.BlockStateGenericFactory;
import com.almuradev.almura.content.type.block.registry.BlockSoundGroupRegistryModule;
import com.almuradev.almura.content.type.block.type.crop.CropBlockType;
import com.almuradev.almura.content.type.block.type.crop.CropBlockTypeBuilder;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalBlockType;
import com.almuradev.almura.content.type.block.type.horizontal.HorizontalBlockTypeBuilder;
import com.almuradev.almura.content.type.block.type.normal.NormalBlockType;
import com.almuradev.almura.content.type.block.type.normal.NormalBlockTypeBuilder;
import com.almuradev.shared.asset.AssetFactoryBinder;
import com.almuradev.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

import java.util.function.Consumer;

public class BlockModule extends AbstractModule implements CommonBinder {

    private final Consumer<AssetFactoryBinder.Entry> generic = binder -> {
        binder.phase(LoaderPhase.CONSTRUCTION);
        binder.type(Asset.Type.NORMAL_BLOCK, Asset.Type.CROP_BLOCK, Asset.Type.HORIZONTAL_BLOCK);
    };
    private final Consumer<AssetFactoryBinder.Entry> crop = binder -> {
        binder.phase(LoaderPhase.CONSTRUCTION);
        binder.type(Asset.Type.CROP_BLOCK);
    };

    @Override
    protected void configure() {
        this.facet()
                .add(BlockListener.class);
        this.registry()
                .module(BlockSoundGroup.class, BlockSoundGroupRegistryModule.class)
                .builder(BlockSoundGroup.Builder.class, BlockSoundGroupBuilder::new)
                .builder(CropBlockType.Builder.class, CropBlockTypeBuilder::new)
                .builder(NormalBlockType.Builder.class, NormalBlockTypeBuilder::new)
                .builder(HorizontalBlockType.Builder.class, HorizontalBlockTypeBuilder::new);
        this.asset()
                .provider(BlockSoundGroupFactory.class, binder -> {
                    binder.phase(LoaderPhase.CONSTRUCTION);
                    binder.type(Asset.Type.BLOCK_SOUNDGROUP);
                })
                .provider(BlockItemGroupProvider.class, this.generic::accept)
                .provider(BlockStateAABBFactory.class, this.generic::accept)
                .provider(BlockStateBreakActionFactory.class, this.generic::accept)
                .provider(BlockStateFertilizeActionFactory.class, this.crop::accept)
                .provider(BlockStateGenericFactory.class, this.generic::accept)
                .provider(BlockStateSoundGroupFactory.class, this.generic::accept);
    }
}
