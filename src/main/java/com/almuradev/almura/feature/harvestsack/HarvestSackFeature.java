/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Harvesting sacks: picking ripe crops configured with {@code sack_harvest} without destroying them.
 *
 * <p>A pick is a real player break of the crop ({@code tryHarvestBlock}), so claim/protection plugins can deny it and break listeners
 * (Skills XP, ...) see it like a normal harvest. The crop's drops are taken out of the break and held back. At the end of the tick,
 * once the break is final (Sponge can still undo a cancelled break after it ran), they go into the sack and the crop is put back at its
 * reset age; if the break was undone, they are discarded, as they would never have dropped.</p>
 */
public final class HarvestSackFeature extends Witness.Impl implements Witness.Lifecycle {

    private static final long PICK_OPEN_GUARD_TICKS = 10;
    private static final List<Pending> PENDING = new ArrayList<>();
    // Player -> world time of their last pick
    private static final Map<UUID, Long> LAST_PICK = new HashMap<>();
    @Nullable private static Pending capturing;
    @Nullable private static List<CropBlockImpl> harvestableCrops;

    private final Almura plugin;

    @Inject
    public HarvestSackFeature(final Almura plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        AlmuraGuiHandler.register(this.plugin, AlmuraGuiHandler.HARVEST_SACK, new HarvestSackGuiHandler());
    }

    @SubscribeEvent
    public void onRegisterItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new HarvestingSackItem());
    }

    /**
     * Picks the ripe crop at {@code pos}: breaks it as the player, collecting its drops for the end of the tick.
     */
    static void harvest(final EntityPlayerMP player, final EnumHand hand, final BlockPos pos, final CropBlockImpl crop) {
        LAST_PICK.put(player.getUniqueID(), player.world.getTotalWorldTime());
        final Pending pending = new Pending(player.getUniqueID(), hand, player.world, pos.toImmutable(), crop, player.world.getBlockState(pos),
                ((org.spongepowered.api.world.World) player.world).createSnapshot(pos.getX(), pos.getY(), pos.getZ()));
        capturing = pending;
        final boolean broken;
        try {
            broken = player.interactionManager.tryHarvestBlock(pos);
        } finally {
            capturing = null;
        }
        if (broken) {
            PENDING.add(pending);
        }
    }

    /**
     * @return True within {@link #PICK_OPEN_GUARD_TICKS} of the player's last pick (opening the sack is skipped then)
     */
    static boolean pickedRecently(final EntityPlayer player) {
        final Long last = LAST_PICK.get(player.getUniqueID());
        return last != null && player.world.getTotalWorldTime() - last < PICK_OPEN_GUARD_TICKS;
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_PICK.remove(event.player.getUniqueID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHarvestDrops(final BlockEvent.HarvestDropsEvent event) {
        final Pending pending = capturing;
        if (pending == null || event.getWorld() != pending.world || !event.getPos().equals(pending.pos)) {
            return;
        }
        for (final ItemStack drop : event.getDrops()) {
            if (!drop.isEmpty()) {
                pending.drops.add(drop.copy());
            }
        }
        event.getDrops().clear();
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        final List<Pending> completed = new ArrayList<>(PENDING);
        PENDING.clear();
        for (final Pending pending : completed) {
            this.complete(pending);
        }
    }

    private void complete(final Pending pending) {
        final World world = pending.world;
        final IBlockState now = world.getBlockState(pending.pos);
        if (now.getBlock() == pending.crop) {
            return; // The break was undone (denied by a plugin): nothing dropped
        }

        if (now.getBlock().isAir(now, world, pending.pos)) {
            world.setBlockState(pending.pos, pending.crop.sackHarvestedState(), 3);
        }

        final EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(pending.player);
        if (player instanceof EntityPlayerMP) {
            // The breaker doesn't get the vanilla break effect (it is sent to everyone else), show it to them too
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketEffect(2001, pending.pos, Block.getStateId(pending.ripe), false));
        }
        world.playSound(null, pending.pos, pending.ripe.getBlock().getSoundType(pending.ripe, world, pending.pos, player).getBreakSound(),
                SoundCategory.BLOCKS, 0.6F, 1.2F);

        for (final ItemStack drop : this.postDropEvent(player, pending)) {
            final ItemStack left = player == null ? drop : insertIntoSack(player, pending.hand, drop);
            if (!left.isEmpty()) {
                Block.spawnAsEntity(world, pending.pos, left);
            }
        }
    }

    /**
     * Fires the drop event a normal harvest fires for the crop's drops (root cause: the ripe crop, with the player), so plugins see the
     * pick as a harvest: Skills pays its crop XP from this event, and plugins may filter or cancel the drops.
     *
     * @return The drops left after the event, none if it was cancelled
     */
    private List<ItemStack> postDropEvent(@Nullable final EntityPlayer player, final Pending pending) {
        final List<ItemStack> drops = new ArrayList<>();
        if (pending.drops.isEmpty()) {
            return drops;
        }

        final List<Entity> items = new ArrayList<>();
        for (final ItemStack drop : pending.drops) {
            items.add((Entity) new EntityItem(pending.world, pending.pos.getX() + 0.5, pending.pos.getY() + 0.5, pending.pos.getZ() + 0.5, drop));
        }

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (player != null) {
                frame.pushCause(player);
            }
            frame.pushCause(pending.snapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            final DropItemEvent.Destruct event = SpongeEventFactory.createDropItemEventDestruct(frame.getCurrentCause(), items);
            if (Sponge.getEventManager().post(event)) {
                return drops;
            }
            for (final Entity entity : event.getEntities()) {
                if (entity instanceof EntityItem && !((EntityItem) entity).getItem().isEmpty()) {
                    drops.add(((EntityItem) entity).getItem());
                }
            }
        }
        return drops;
    }

    /**
     * @return What didn't fit: into the sack in the hand that picked, else the first sack in the inventory
     */
    private static ItemStack insertIntoSack(final EntityPlayer player, final EnumHand hand, final ItemStack stack) {
        ItemStack sack = player.getHeldItem(hand);
        if (!(sack.getItem() instanceof HarvestingSackItem)) {
            sack = ItemStack.EMPTY;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                if (player.inventory.getStackInSlot(i).getItem() instanceof HarvestingSackItem) {
                    sack = player.inventory.getStackInSlot(i);
                    break;
                }
            }
        }
        return sack.isEmpty() ? stack : ItemHandlerHelper.insertItemStacked(new SackInventory(sack), stack, false);
    }

    /**
     * @return True if a sack harvestable crop can drop the stack, the only things a sack holds
     */
    public static boolean isHarvestItem(final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof HarvestingSackItem) {
            return false;
        }
        if (harvestableCrops == null) {
            final List<CropBlockImpl> crops = new ArrayList<>();
            for (final Block block : ForgeRegistries.BLOCKS) {
                if (block instanceof CropBlockImpl && ((CropBlockImpl) block).canSackHarvest(((CropBlockImpl) block).withAge(((CropBlockImpl) block).getMaxAge()))) {
                    crops.add((CropBlockImpl) block);
                }
            }
            harvestableCrops = crops;
        }
        for (final CropBlockImpl crop : harvestableCrops) {
            if (crop.isSackHarvestItem(stack)) {
                return true;
            }
        }
        return false;
    }

    private static final class Pending {

        final UUID player;
        final EnumHand hand;
        final World world;
        final BlockPos pos;
        final CropBlockImpl crop;
        final IBlockState ripe;
        final BlockSnapshot snapshot;
        final List<ItemStack> drops = new ArrayList<>();

        Pending(final UUID player, final EnumHand hand, final World world, final BlockPos pos, final CropBlockImpl crop, final IBlockState ripe,
                final BlockSnapshot snapshot) {
            this.player = player;
            this.hand = hand;
            this.world = world;
            this.pos = pos;
            this.crop = crop;
            this.ripe = ripe;
            this.snapshot = snapshot;
        }
    }
}
