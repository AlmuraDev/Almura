/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cruet;

import com.almuradev.almura.Almura;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Almura's glass cruets, as 3.5's {@code feature.complex.item.cruet} made them:
 * <ul>
 *     <li>the glass cruet takes up a water source as a bucket does, giving a water cruet, and is filled with milk by milking a grown cow
 *     (3.5's cow);</li>
 *     <li>the water cruet pours its water out as a bucket does, giving the glass cruet back;</li>
 *     <li>the milk cruet is an ingredient; a recipe using a water or milk cruet hands the glass cruet back.</li>
 * </ul>
 * They're items of their own (not the content's); their models, the glass cruet's recipe and the recipes using them are the content's.
 */
public final class Cruets {

    public static final Identifier EMPTY = Identifier.fromNamespaceAndPath(Almura.ID, "normal/ingredient/glass_cruet");
    public static final Identifier WATER = Identifier.fromNamespaceAndPath(Almura.ID, "normal/ingredient/glass_water_cruet");
    public static final Identifier MILK = Identifier.fromNamespaceAndPath(Almura.ID, "normal/ingredient/glass_milk_cruet");
    // The creative tab they're in, as on 3.5
    public static final String TAB = Almura.ID + ":ingredient";

    private static @Nullable Item empty, water, milk;

    public Cruets() {
    }

    /**
     * Registers the three cruets, the glass cruet first (the others are handed back as it), each added to its tab.
     */
    public static void register(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        empty = new GlassCruet(properties(EMPTY));
        helper.register(EMPTY, empty);
        water = new WaterCruet(properties(WATER).craftRemainder(empty));
        helper.register(WATER, water);
        milk = new Item(properties(MILK).craftRemainder(empty));
        helper.register(MILK, milk);
        addToTab.accept(TAB, empty);
        addToTab.accept(TAB, water);
        addToTab.accept(TAB, milk);
    }

    private static Item.Properties properties(final Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(64);
    }

    /**
     * A glass cruet used on a grown cow fills with milk, as 3.5's cow did (not in creative, where a bucket isn't filled either).
     */
    @SubscribeEvent
    public void onMilk(final PlayerInteractEvent.EntityInteract event) {
        final Player player = event.getEntity();
        final ItemStack held = event.getItemStack();
        if (empty == null || milk == null || !held.is(empty) || !(event.getTarget() instanceof final AbstractCow cow) || cow.isBaby()
            || player.hasInfiniteMaterials()) {
            return;
        }
        player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
        player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(held, player, new ItemStack(milk)));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /**
     * The glass cruet: takes up a water source (or the water of a waterlogged block) as a bucket does.
     */
    static final class GlassCruet extends Item {

        GlassCruet(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            final ItemStack held = player.getItemInHand(hand);
            final BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() != HitResult.Type.BLOCK || water == null) {
                return InteractionResult.PASS;
            }
            final BlockPos pos = hit.getBlockPos();
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(hit.getDirection()), hit.getDirection(), held)) {
                return InteractionResult.FAIL;
            }
            final BlockState state = level.getBlockState(pos);
            // Water only: a cruet holds nothing else
            if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource()
                || !(state.getBlock() instanceof final BucketPickup pickup)) {
                return InteractionResult.PASS;
            }
            if (pickup.pickupBlock(player, level, pos, state).isEmpty()) {
                return InteractionResult.PASS;
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            pickup.getPickupSound(state).ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(held, player, new ItemStack(water)));
        }
    }

    /**
     * The water cruet: pours its water as a bucket of water does (into a waterloggable block, or evaporating where water does), and gives
     * the glass cruet back.
     */
    static final class WaterCruet extends Item {

        WaterCruet(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            final ItemStack held = player.getItemInHand(hand);
            final BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() != HitResult.Type.BLOCK || empty == null) {
                return InteractionResult.PASS;
            }
            final BlockPos pos = hit.getBlockPos();
            final Direction direction = hit.getDirection();
            final BlockPos beside = pos.relative(direction);
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(beside, direction, held)) {
                return InteractionResult.FAIL;
            }
            final BlockState clicked = level.getBlockState(pos);
            final BlockPos placeAt = clicked.getBlock() instanceof final LiquidBlockContainer container
                && container.canPlaceLiquid(player, level, pos, clicked, Fluids.WATER) ? pos : beside;
            if (!pour(player, level, placeAt, hit)) {
                return InteractionResult.FAIL;
            }
            if (player instanceof final ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, placeAt, held);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            final ItemStack back = player.hasInfiniteMaterials() ? held : new ItemStack(empty);
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(held, player, back));
        }

        /**
         * Vanilla's {@code BucketItem.emptyContents} for water.
         */
        private static boolean pour(final Player player, final Level level, final BlockPos pos, final @Nullable BlockHitResult hit) {
            final BlockState state = level.getBlockState(pos);
            final Block block = state.getBlock();
            final boolean mayReplace = state.canBeReplaced(Fluids.WATER);
            final boolean intoBlock = block instanceof final LiquidBlockContainer container && container.canPlaceLiquid(player, level, pos, state,
                Fluids.WATER);
            if (!(state.isAir() || (mayReplace || intoBlock) && (!player.isShiftKeyDown() || hit == null))) {
                return hit != null && pour(player, level, hit.getBlockPos().relative(hit.getDirection()), null);
            }
            if (level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES, pos)) {
                final RandomSource random = level.getRandom();
                level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
                if (level instanceof final ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 8, 1.0, 1.0, 1.0, 0.0);
                }
                return true;
            }
            if (block instanceof final LiquidBlockContainer container && intoBlock) {
                container.placeLiquid(level, pos, state, Fluids.WATER.getSource(false));
            } else {
                if (!level.isClientSide() && mayReplace && !state.liquid()) {
                    level.destroyBlock(pos, true);
                }
                if (!level.setBlock(pos, Fluids.WATER.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE)
                    && !state.getFluidState().isSource()) {
                    return false;
                }
            }
            level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            return true;
        }
    }

    static boolean isCruet(final Item item) {
        final Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id.equals(EMPTY) || id.equals(WATER) || id.equals(MILK);
    }
}
