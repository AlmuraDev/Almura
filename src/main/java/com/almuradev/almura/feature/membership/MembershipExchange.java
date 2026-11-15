/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.membership.server.ServerMemberships;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * 3.5's Membership Exchange: using it opens the memberships window. Faces the player who placed it, in the Currency tab, with the Coin
 * Exchange's sides.
 */
public final class MembershipExchange extends HorizontalDirectionalBlock {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "machine/membership_exchange");
    private static final String TAB = Almura.ID + ":currency";

    private static @Nullable Block block;

    private MembershipExchange(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * Registers the block.
     */
    public static void registerBlock(final RegisterEvent.RegisterHelper<Block> helper) {
        // 3.5's: hardness 3, resistance 1000
        block = new MembershipExchange(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ID)).strength(3f, 1000f)
            .sound(SoundType.STONE));
        helper.register(ID, block);
    }

    /**
     * Registers the block's item, added to the Currency tab.
     */
    public static void registerItem(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        if (block == null) {
            return;
        }
        final Item item = new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ID)).useBlockDescriptionPrefix());
        helper.register(ID, item);
        addToTab.accept(TAB, item);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player,
        final BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof final ServerPlayer serverPlayer) {
            ServerMemberships.open(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
