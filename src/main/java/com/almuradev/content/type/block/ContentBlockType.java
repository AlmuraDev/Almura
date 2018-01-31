/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.ItemGrouped;
import com.almuradev.content.type.itemgroup.mixin.iface.IMixinLazyItemGroup;
import com.almuradev.content.type.mapcolor.MapColor;
import com.almuradev.content.type.material.Material;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.api.util.PEBKACException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This cannot extend BlockType.
public interface ContentBlockType extends CatalogedContent, ItemGrouped {

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

    BlockStateDefinition definition(final IBlockState state);

    interface InInventory extends ContentBlockType {

        default Item asBlockItem() {
            return new ItemBlock((Block) this).setRegistryName(((Block) this).getRegistryName());
        }
    }

    interface Builder<C extends ContentBlockType, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends ContentBuilder<C> {

        BlockGenre genre();

        // Properties

        void mapColor(final Delegate<MapColor> mapColor);

        void material(final Delegate<Material> material);

        void itemGroup(final Delegate<ItemGroup> itemGroup);

        // Definitions

        Map<String, B> stateBuilders();

        B stateBuilder(final String id);

        default Optional<B> optionalStateBuilder(final String id) {
            return Optional.ofNullable(this.stateBuilders().get(id));
        }

        interface Single<C extends ContentBlockType, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends Builder<C, D, B> {

            default D singleState() {
                return Preconditions.checkNotNull(this.stateBuilder(BlockConfig.DEFAULT_STATE_NAME), "%s state", BlockConfig.DEFAULT_STATE_NAME).build();
            }
        }

        abstract class Impl<C extends ContentBlockType, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends ContentBuilder.Impl<C> implements Builder<C, D, B> {

            private final Map<String, B> stateBuilders = new HashMap<>();
            private final BlockGenre genre;
            public Delegate<MapColor> mapColor;
            public Delegate<Material> material;
            private Delegate<ItemGroup> itemGroup;

            public Impl(final BlockGenre genre) {
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
                ((Block) entry).setUnlocalizedName(this.string(StringType.TRANSLATION).replace('/', '.'));
                ((IMixinLazyItemGroup) entry).itemGroup(this.itemGroup);
            }

            public static abstract class Parsed<C extends ContentBlockType, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends Builder.Impl<C, D, B> {

                public Parsed(final BlockGenre genre) {
                    super(genre);
                }

                protected abstract Pattern pattern();

                protected abstract String describePattern();

                @Override
                protected B createDefinitionBuilder(final String id) {
                    final Matcher matcher = this.pattern().matcher(id);
                    if (!matcher.matches()) {
                        throw new IllegalStateException(String.format("Definition does not follow state naming conventions (%s): %s", this.describePattern(), id));
                    }
                    return this.createDefinitionBuilder(id, matcher);
                }

                protected abstract B createDefinitionBuilder(final String id, final Matcher matcher);
            }
        }
    }
}
