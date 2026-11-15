/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.bank.server.ServerBank;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import java.math.BigDecimal;
import java.util.function.BiConsumer;

/**
 * 3.5's Coin Exchange: coins in hand used on it are cashed in on the spot, into the player's wallet; anything else opens the bank. Faces the
 * player who placed it, in the Currency tab.
 */
public final class CoinExchange extends HorizontalDirectionalBlock {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "machine/coin_exchange");
    private static final String TAB = Almura.ID + ":currency";
    private static final String TITLE = "Coin Exchange";

    private static @Nullable Block block;

    private CoinExchange(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * Registers the block.
     */
    public static void registerBlock(final RegisterEvent.RegisterHelper<Block> helper) {
        // 3.5's: hardness 3, resistance 1000
        block = new CoinExchange(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ID)).strength(3f, 1000f)
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
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos,
        final Player player, final InteractionHand hand, final BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || ServerBank.coinValue(stack) == 0) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide() || !(player instanceof final ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        cashIn(serverPlayer, stack);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player,
        final BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof final ServerPlayer serverPlayer) {
            ServerBank.open(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The coins in hand into the player's wallet; they are only taken once the money is in.
     */
    private static void cashIn(final ServerPlayer player, final ItemStack held) {
        final EconomyAccounts economy = Economy.accounts();
        if (economy == null) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "There is no economy on this server.", 5);
            return;
        }
        final BigDecimal amount = BigDecimal.valueOf(ServerBank.coinValue(held) * held.getCount());
        if (economy.deposit(Account.player(player.getUUID()), amount)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "Your coins could not be deposited, you still have them.", 5);
        }
    }
}
