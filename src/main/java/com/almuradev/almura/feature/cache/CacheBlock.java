/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A cache, as 3.5's {@code CacheBlock}: a block holding a great deal of one item, shown on its front.
 * <ul>
 *     <li>right click with an item: an empty cache takes what is held; one holding something takes no other item;</li>
 *     <li>right click with an empty hand: it takes every stack of what it holds from the player's inventory;</li>
 *     <li>left click: a stack comes out into the inventory (one while sneaking);</li>
 *     <li>in creative, an item held is added without being used up, and a cache holding something is only broken with a diamond in hand.</li>
 * </ul>
 */
public final class CacheBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale.US);

    /**
     * The caches and how much each holds, as 3.5's.
     */
    public enum Tier {
        WOOD("wood", 640),
        IRON("iron", 6_400),
        GOLD("gold", 64_000),
        DIAMOND("diamond", 640_000),
        NETHER("nether", 6_400_000),
        ENDER("ender", 64_000_000);

        private final String id;
        private final int limit;

        Tier(final String id, final int limit) {
            this.id = id;
            this.limit = limit;
        }

        public String id() {
            return this.id;
        }

        public int limit() {
            return this.limit;
        }
    }

    private final Tier tier;

    CacheBlock(final BlockBehaviour.Properties properties, final Tier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public Tier tier() {
        return this.tier;
    }

    public int limit() {
        return this.tier.limit();
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
    public @Nullable BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new CacheBlockEntity(pos, state);
    }

    // ---- Right click

    @Override
    protected InteractionResult useItemOn(final ItemStack held, final BlockState state, final Level level, final BlockPos pos, final Player player,
        final InteractionHand hand, final BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof final CacheBlockEntity cache)) {
            return InteractionResult.PASS;
        }
        final CacheStack stack = cache.stack();
        final ItemResource heldResource = ItemResource.of(held);
        if (stack.isEmpty()) {
            // An empty cache takes what is held; creative keeps it
            final int deposited = cache.handler().insertNow(heldResource, held.getCount());
            if (deposited > 0 && !player.isCreative()) {
                held.shrink(deposited);
            }
            deposited(player, deposited, held.getHoverName());
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!stack.resource().equals(heldResource)) {
            player.sendSystemMessage(Component.literal("This cache does not support this item! Try emptying the cache first."));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player.isCreative()) {
            // Creative adds what is held, again and again
            final int deposited = cache.handler().insertNow(heldResource, held.getCount());
            if (deposited == 0) {
                player.sendSystemMessage(Component.literal("Cannot add more to the cache as it is full!"));
            } else {
                deposited(player, deposited, held.getHoverName());
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        depositFromInventory(player, cache, false);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player,
        final BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof final CacheBlockEntity cache && !cache.stack().isEmpty()) {
            depositFromInventory(player, cache, true);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Puts the player's stacks of what the cache holds into it: every one, or (with that item in hand) the first, as 3.5.
     */
    private static void depositFromInventory(final Player player, final CacheBlockEntity cache, final boolean all) {
        final CacheStack stack = cache.stack();
        final Inventory inventory = player.getInventory();
        int deposited = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final ItemStack inSlot = inventory.getItem(slot);
            if (inSlot.isEmpty() || !stack.resource().matches(inSlot)) {
                continue;
            }
            final int moved = cache.handler().insertNow(stack.resource(), inSlot.getCount());
            inSlot.shrink(moved);
            if (inSlot.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            deposited += moved;
            if (!inSlot.isEmpty() || !all) {
                break;
            }
        }
        deposited(player, deposited, stack.resource().getHoverName());
    }

    private static void deposited(final Player player, final int amount, final Component name) {
        if (amount > 0) {
            player.sendSystemMessage(Component.literal("Deposited ").append(Component.literal(FORMAT.format(amount)).withStyle(ChatFormatting.AQUA))
                .append(" ").append(name).append(" into the cache."));
        }
    }

    // ---- Left click

    @Override
    protected void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof final CacheBlockEntity cache)) {
            return;
        }
        final CacheStack stack = cache.stack();
        if (stack.isEmpty()) {
            return;
        }
        final int wanted = player.isShiftKeyDown() ? 1 : Math.min(stack.resource().getMaxStackSize(), stack.amount());
        final ItemStack out = stack.resource().toStack(wanted);
        final Component name = out.getHoverName();
        player.getInventory().add(out);
        final int taken = wanted - out.getCount();
        if (taken <= 0) {
            player.sendSystemMessage(Component.literal("Cannot withdraw from the cache as your inventory is full!"));
            return;
        }
        cache.handler().extractNow(taken);
        player.sendSystemMessage(Component.literal("Withdrew ").append(Component.literal(FORMAT.format(taken)).withStyle(ChatFormatting.AQUA))
            .append(" ").append(name).append(" from the cache."));
    }

    // ---- Breaking

    /**
     * In creative, a cache holding something is only broken with a diamond in hand, as 3.5 (so its contents aren't lost by a stray click).
     */
    @Override
    public boolean onDestroyedByPlayer(final BlockState state, final Level level, final BlockPos pos, final Player player, final ItemStack tool,
        final boolean willHarvest, final FluidState fluid) {
        if (player.isCreative() && !player.getMainHandItem().is(Items.DIAMOND) && level.getBlockEntity(pos) instanceof final CacheBlockEntity cache
            && !cache.stack().isEmpty()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("Cache not destroyed, you're in creative mode. It can be destroyed using a diamond."));
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }
}
