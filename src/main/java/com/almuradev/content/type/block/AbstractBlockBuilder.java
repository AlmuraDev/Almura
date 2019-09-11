/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.DelegateSet;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.block.mixin.iface.IMixinContentBlock;
import com.almuradev.content.type.item.mixin.EffectiveOn;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import com.almuradev.content.type.mapcolor.MapColor;
import com.almuradev.content.type.material.Material;
import net.kyori.mu.Maybe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.api.item.ItemType;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import org.spongepowered.api.util.Tristate;

public abstract class AbstractBlockBuilder<C extends ContentBlock, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends ContentBuilder.Impl<C> implements ContentBlock.Builder<C, D, B> {
    private final Map<String, B> stateBuilders = new HashMap<>();
    private final BlockGenre genre;
    public Delegate<MapColor> mapColor;
    public Delegate<Material> material;
    private Delegate<ItemGroup> itemGroup;
    private BlockRenderLayer renderLayer = BlockRenderLayer.CUTOUT_MIPPED;
    private @Nullable DelegateSet<ItemType, Item> effectiveTools;
    private Maybe<Boolean> topSolid = Maybe.nothing();
    private Maybe<Boolean> fullBlock = Maybe.nothing();
    private Maybe<Boolean> normalCube = Maybe.nothing();

    public AbstractBlockBuilder(final BlockGenre genre) {
        this.genre = genre;
    }

    @Override
    public BlockGenre genre() {
        return this.genre;
    }

    @Override
    public void mapColor(final Delegate<MapColor> mapColor) {
        this.mapColor = mapColor;
    }

    @Override
    public void material(final Delegate<Material> material) {
        this.material = material;
    }

    @Override
    public void itemGroup(final Delegate<ItemGroup> itemGroup) {
        this.itemGroup = itemGroup;
    }

    @Override
    public void renderLayer(BlockRenderLayer renderLayer) {
        this.renderLayer = renderLayer;
    }

    @Override
    public void effectiveTools(final DelegateSet<ItemType, Item> effectiveTools) {
        this.effectiveTools = effectiveTools;
    }

    @Override
    public void topSolid(final boolean topSolid) {
        this.topSolid = Maybe.just(topSolid);
    }

    @Override
    public void fullBlock(final boolean fullBlock) {
        this.fullBlock = Maybe.just(fullBlock);
    }

    @Override
    public void normalCube(final boolean normalCube) {
        this.normalCube = Maybe.just(normalCube);
    }

    @Override
    public Map<String, B> stateBuilders() {
        return this.stateBuilders;
    }

    @Override
    public final B stateBuilder(final String id) {
        return this.stateBuilders.computeIfAbsent(id, s -> this.createDefinitionBuilder(id));
    }

    protected abstract B createDefinitionBuilder(final String id);

    @Override
    public void fill(final IForgeRegistryEntry.Impl entry) {
        super.fill(entry);
        ((Block) entry).setTranslationKey(this.string(StringType.TRANSLATION).replace('/', '.'));
        ((IMixinLazyItemGroup) entry).itemGroup(this.itemGroup);
        ((IMixinContentBlock) entry).setRenderLayer(this.renderLayer);
        ((IMixinContentBlock) entry).cl_topSolid(this.topSolid);
        ((IMixinContentBlock) entry).cl_fullBlock(this.fullBlock);
        ((IMixinContentBlock) entry).cl_normalCube(this.normalCube);
        ((EffectiveOn) entry).effectiveTools(this.effectiveTools);
    }
}
