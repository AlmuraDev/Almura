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
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.ItemGrouped;
import com.almuradev.content.type.mapcolor.MapColor;
import com.almuradev.content.type.material.Material;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.PEBKACException;

import java.util.Map;
import java.util.Optional;

// This cannot extend BlockType.
public interface ContentBlock extends CatalogedContent, ItemGrouped {
    @Override
    default String getId() {
        throw new PEBKACException("api");
    }

    @Override
    default String getName() {
        throw new PEBKACException("api");
    }

    @Override
    default Optional<ItemGroup> itemGroup() {
        throw new PEBKACException("mixin");
    }

    @SideOnly(Side.CLIENT)
    default BlockRenderLayer renderLayer() {
        throw new PEBKACException("render_layer");
    }

    BlockStateDefinition definition(final IBlockState state);

    interface InInventory extends ContentBlock {
        default Item asBlockItem() {
            return new ItemBlock((Block) this).setRegistryName(((Block) this).getRegistryName());
        }
    }

    interface Builder<C extends ContentBlock, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends ContentBuilder<C> {
        BlockGenre genre();

        // Properties

        void mapColor(final Delegate<MapColor> mapColor);

        void material(final Delegate<Material> material);

        void itemGroup(final Delegate<ItemGroup> itemGroup);

        void renderLayer(final BlockRenderLayer renderLayer);

        void effectiveTools(final DelegateSet<ItemType, Item> effectiveTools);

        void topSolid(final boolean topSolid);

        void fullBlock(final boolean fullBlock);

        void normalCube(final boolean normalCube);

        // Definitions

        Map<String, B> stateBuilders();

        B stateBuilder(final String id);

        default Optional<B> optionalStateBuilder(final String id) {
            return Optional.ofNullable(this.stateBuilders().get(id));
        }

        interface Single<C extends ContentBlock, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends Builder<C, D, B> {
            default D singleState() {
                return Preconditions.checkNotNull(this.stateBuilder(BlockConfig.DEFAULT_STATE_NAME), "%s state", BlockConfig.DEFAULT_STATE_NAME).build();
            }
        }
    }
}
