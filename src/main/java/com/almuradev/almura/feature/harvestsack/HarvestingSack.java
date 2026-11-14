/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.DropDef;
import com.almuradev.almura.content.registry.ContentCrop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * The harvesting sack, as 3.5's {@code feature.harvestsack}: right click a ripe crop the content gives {@code sack_harvest} to pick it,
 * its drops going into the sack and the crop going back to its reset age instead of being destroyed; right click anything else (but the
 * ground or the plants of a field) to open the sack, 27 slots that take only what those crops drop, kept on the sack itself.
 * <p>
 * A pick is a real player break of the crop ({@code ServerPlayerGameMode.destroyBlock}), so protection mods and plugins can deny it and
 * break listeners see a harvest. Its drops are taken out of the break, and at the end of the tick, once the break is final, they go into
 * the sack and the crop is put back; if the break was undone, they are discarded, as they would never have dropped.
 */
public final class HarvestingSack {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "normal/tool/harvesting_sack");
    // The creative tab it's in, as on 3.5
    public static final String TAB = Almura.ID + ":agriculture";
    static final int SIZE = 27;
    private static final long PICK_OPEN_GUARD_TICKS = 10;

    private static final List<Pending> PENDING = new ArrayList<>();
    // Player -> game time of their last pick
    private static final Map<UUID, Long> LAST_PICK = new HashMap<>();
    private static @Nullable Pending capturing;
    private static @Nullable Set<Item> harvestItems;
    private static @Nullable MenuType<ChestMenu> menuType;

    /**
     * Registers the sack, added to its tab.
     */
    public static void register(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        final Item sack = new SackItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ID)).stacksTo(1));
        helper.register(ID, sack);
        addToTab.accept(TAB, sack);
    }

    /**
     * Registers the sack's menu.
     */
    @SuppressWarnings("unchecked")
    public static void registerMenu(final RegisterEvent.RegisterHelper<MenuType<?>> helper) {
        final MenuType<ChestMenu>[] holder = new MenuType[1];
        holder[0] = IMenuTypeExtension.create((id, inventory, buffer) ->
            new SackMenu(holder[0], id, inventory, new SimpleContainer(SIZE), InteractionHand.values()[buffer.readVarInt()]));
        menuType = holder[0];
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "harvesting_sack"), menuType);
    }

    public static @Nullable MenuType<ChestMenu> menuType() {
        return menuType;
    }

    private static boolean isSack(final ItemStack stack) {
        return stack.getItem() instanceof SackItem;
    }

    /**
     * Whether a crop is picked with the sack ({@code sack_harvest}) and the state is ripe.
     */
    private static boolean isPickable(final BlockState state) {
        return state.getBlock() instanceof final ContentCrop crop && crop.def().sackResetAge() >= 0;
    }

    /**
     * The age a picked crop goes back to: its {@code reset_age}, below ripe.
     */
    private static int resetAge(final ContentCrop crop) {
        return Math.max(0, Math.min(crop.def().sackResetAge(), crop.getMaxAge() - 1));
    }

    /**
     * Whether the stack is something a pickable crop drops (its seed or a stage's drops), the only things a sack holds.
     */
    static boolean isHarvestItem(final ItemStack stack) {
        if (stack.isEmpty() || isSack(stack)) {
            return false;
        }
        if (harvestItems == null) {
            final Set<Item> items = new HashSet<>();
            for (final Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof final ContentCrop crop && crop.def().sackResetAge() >= 0) {
                    BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(crop.def().seed())).ifPresent(items::add);
                    for (final CropStageDef stage : crop.def().stages()) {
                        for (final DropDef drop : stage.state().drops()) {
                            BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(drop.item())).ifPresent(items::add);
                        }
                    }
                }
            }
            items.remove(net.minecraft.world.item.Items.AIR);
            harvestItems = items;
        }
        return harvestItems.contains(stack.getItem());
    }

    /**
     * Picks the ripe crop: breaks it as the player, collecting its drops for the end of the tick.
     */
    private static void pick(final ServerPlayer player, final InteractionHand hand, final BlockPos pos, final BlockState ripe) {
        final ServerLevel level = player.level();
        LAST_PICK.put(player.getUUID(), level.getGameTime());
        final Pending pending = new Pending(player.getUUID(), hand, level, pos.immutable(), ripe);
        capturing = pending;
        final boolean broken;
        try {
            broken = player.gameMode.destroyBlock(pos);
        } finally {
            capturing = null;
        }
        if (broken) {
            PENDING.add(pending);
        }
    }

    private static boolean pickedRecently(final Player player) {
        final Long last = LAST_PICK.get(player.getUUID());
        return last != null && player.level().getGameTime() - last < PICK_OPEN_GUARD_TICKS;
    }

    // Last, so what other mods do to the drops is what goes into the sack
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDrops(final BlockDropsEvent event) {
        final Pending pending = capturing;
        if (pending == null || event.getLevel() != pending.level || !event.getPos().equals(pending.pos)) {
            return;
        }
        for (final ItemEntity drop : event.getDrops()) {
            if (!drop.getItem().isEmpty()) {
                pending.drops.add(drop.getItem().copy());
            }
        }
        event.getDrops().clear();
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) {
            return;
        }
        final List<Pending> completed = new ArrayList<>(PENDING);
        PENDING.clear();
        completed.forEach(HarvestingSack::complete);
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_PICK.remove(event.getEntity().getUUID());
    }

    private static void complete(final Pending pending) {
        final ServerLevel level = pending.level;
        final BlockState now = level.getBlockState(pending.pos);
        if (now.is(pending.ripe.getBlock())) {
            // The break was undone (denied): nothing dropped
            return;
        }
        final ContentCrop crop = (ContentCrop) pending.ripe.getBlock();
        // A crop in water leaves its water when it's broken, not air: put it back over that too
        if (now.isAir() || crop.isInWater(pending.ripe) && now.is(Blocks.WATER) && now.getFluidState().isSource()) {
            level.setBlock(pending.pos, crop.aged(pending.ripe, resetAge(crop)), Block.UPDATE_ALL);
        }
        final ServerPlayer player = level.getServer().getPlayerList().getPlayer(pending.player);
        if (player != null) {
            // The breaker doesn't get the break effect (everyone else does): show it to them too
            player.connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pending.pos,
                Block.getId(pending.ripe), false));
        }
        for (final ItemStack drop : pending.drops) {
            final ItemStack left = player == null ? drop : intoSack(player, pending.hand, drop);
            if (!left.isEmpty()) {
                Block.popResource(level, pending.pos, left);
            }
        }
    }

    /**
     * Into the sack in the hand that picked, else the first sack in the inventory.
     *
     * @return what didn't fit
     */
    private static ItemStack intoSack(final ServerPlayer player, final InteractionHand hand, final ItemStack stack) {
        ItemStack sack = player.getItemInHand(hand);
        if (!isSack(sack)) {
            sack = ItemStack.EMPTY;
            for (final ItemStack slot : player.getInventory()) {
                if (isSack(slot)) {
                    sack = slot;
                    break;
                }
            }
        }
        if (sack.isEmpty()) {
            return stack;
        }
        final SackContainer contents = new SackContainer(sack);
        final ItemStack left = contents.addItem(stack);
        contents.save();
        return left;
    }

    /**
     * The sack's contents, read from and written back to the sack ({@code minecraft:container}, as a shulker box keeps its own).
     */
    static final class SackContainer extends SimpleContainer {

        private final ItemStack sack;

        SackContainer(final ItemStack sack) {
            super(SIZE);
            this.sack = sack;
            final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            sack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
            for (int i = 0; i < SIZE; i++) {
                this.setItem(i, items.get(i));
            }
        }

        @Override
        public boolean canPlaceItem(final int slot, final ItemStack stack) {
            return isHarvestItem(stack);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            this.save();
        }

        void save() {
            final List<ItemStack> items = new ArrayList<>(SIZE);
            for (int i = 0; i < SIZE; i++) {
                items.add(this.getItem(i));
            }
            this.sack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        }
    }

    /**
     * A single chest's menu over the sack. Its slots take only harvest items (on the client too), the slot holding the open sack is
     * locked so it can't be moved (or swapped away with a number key) while it is open, and the menu closes if the sack leaves the hand.
     */
    static final class SackMenu extends ChestMenu {

        private final InteractionHand hand;
        private final ItemStack sack;
        private final int lockedSlot, lockedHotbar;

        SackMenu(final MenuType<ChestMenu> type, final int containerId, final Inventory inventory, final Container container,
            final InteractionHand hand) {
            super(type, containerId, inventory, container, SIZE / 9);
            this.hand = hand;
            this.sack = inventory.player.getItemInHand(hand);
            for (int i = 0; i < SIZE; i++) {
                final Slot old = this.slots.get(i);
                final Slot slot = new Slot(container, old.getContainerSlot(), old.x, old.y) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        return isHarvestItem(stack);
                    }
                };
                slot.index = i;
                this.slots.set(i, slot);
            }
            if (hand == InteractionHand.MAIN_HAND) {
                this.lockedHotbar = inventory.getSelectedSlot();
                // The chest's slots, the inventory's 27, then the hotbar
                this.lockedSlot = SIZE + 27 + this.lockedHotbar;
            } else {
                this.lockedHotbar = -1;
                this.lockedSlot = -1;
            }
        }

        @Override
        public void clicked(final int slot, final int button, final ContainerInput input, final Player player) {
            if (this.lockedSlot >= 0 && (slot == this.lockedSlot || input == ContainerInput.SWAP && button == this.lockedHotbar)) {
                return;
            }
            super.clicked(slot, button, input, player);
        }

        @Override
        public ItemStack quickMoveStack(final Player player, final int slot) {
            return slot == this.lockedSlot ? ItemStack.EMPTY : super.quickMoveStack(player, slot);
        }

        @Override
        public boolean stillValid(final Player player) {
            return player.getItemInHand(this.hand) == this.sack;
        }
    }

    /**
     * The sack item: picks ripe pickable crops, opens otherwise.
     */
    public static final class SackItem extends Item {

        SackItem(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(final UseOnContext context) {
            final Level level = context.getLevel();
            final BlockPos pos = context.getClickedPos();
            final BlockState state = level.getBlockState(pos);
            if (!isPickable(state)) {
                // Not a pickable crop: on to opening the sack
                return InteractionResult.PASS;
            }
            // A pickable crop that isn't ripe (the one just picked, hit again while right click is held) only swings, never opens the sack
            if (((ContentCrop) state.getBlock()).isMaxAge(state) && context.getPlayer() instanceof final ServerPlayer player) {
                pick(player, context.getHand(), pos, state);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            // Not right after a pick (holding right click repeats the use, which would open the sack on whatever is aimed at next), and not
            // while aiming at the ground or the plants of a field
            if (player instanceof final ServerPlayer serverPlayer && !pickedRecently(player) && !aimingAtField(level, player)
                && menuType != null) {
                final ItemStack sack = player.getItemInHand(hand);
                serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, opener) ->
                    new SackMenu(menuType, id, inventory, new SackContainer(sack), hand), sack.getHoverName()),
                    buffer -> buffer.writeVarInt(hand.ordinal()));
            }
            return InteractionResult.SUCCESS;
        }

        /**
         * Whether the player aims at dirt, grass, farmland or a plant (crops, flowers...).
         */
        private static boolean aimingAtField(final Level level, final Player player) {
            final BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return false;
            }
            final BlockState state = level.getBlockState(hit.getBlockPos());
            return state.is(BlockTags.DIRT) || state.getBlock() instanceof FarmlandBlock || state.getBlock() instanceof VegetationBlock;
        }
    }

    private static final class Pending {

        final UUID player;
        final InteractionHand hand;
        final ServerLevel level;
        final BlockPos pos;
        final BlockState ripe;
        final List<ItemStack> drops = new ArrayList<>();

        Pending(final UUID player, final InteractionHand hand, final ServerLevel level, final BlockPos pos, final BlockState ripe) {
            this.player = player;
            this.hand = hand;
            this.level = level;
            this.pos = pos;
            this.ripe = ripe;
        }
    }
}
