/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.Almura;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The content's containers ({@code content/block/container}), as 3.5's {@code ContainerBlockImpl} and {@code MultiSlotTileEntity} kept
 * them:
 * <ul>
 *     <li>a chest's screen of their slots (9 to 54), each holding up to their {@code limit};</li>
 *     <li>broken, they keep what they hold in the item they drop (placed again, it's all there), unless broken while sneaking, when it
 *     spills out;</li>
 *     <li>the barrels made for one crop's seed ({@code container/*_seed_barrel}) take only that seed, and turn into the storage barrel
 *     ({@code container/seed_barrel}) when opened, keeping their contents;</li>
 *     <li>the storage barrel takes any seed, crop or tree fruit, Almura's or Minecraft's, and its screen is named for what it holds
 *     first ("Basil Seed Storage Barrel").</li>
 * </ul>
 */
public final class ContentContainers {

    public static final Identifier STORAGE_BARREL = Identifier.fromNamespaceAndPath(Almura.ID, "container/seed_barrel");
    private static final String CROP_BARREL_SUFFIX = "_seed_barrel";

    static @Nullable BlockEntityType<ContainerBlockEntity> blockEntityType;
    static @Nullable MenuType<ChestMenu> menuType, storageBarrelMenuType;
    // What the storage barrel takes: seeds, what crops drop, what fruit trees' fruit leaves drop, and Minecraft's seeds and crops
    private static final Set<String> STORAGE_BARREL_ITEMS = new HashSet<>(Set.of(
        "minecraft:wheat_seeds", "minecraft:wheat", "minecraft:carrot", "minecraft:potato", "minecraft:poisonous_potato",
        "minecraft:beetroot_seeds", "minecraft:beetroot", "minecraft:melon_seeds", "minecraft:melon_slice", "minecraft:pumpkin_seeds",
        "minecraft:pumpkin", "minecraft:nether_wart", "minecraft:sugar_cane", "minecraft:apple", "minecraft:cocoa_beans"));

    private ContentContainers() {
    }

    public static MenuType<ChestMenu> menuType() {
        return menuType;
    }

    public static MenuType<ChestMenu> storageBarrelMenuType() {
        return storageBarrelMenuType;
    }

    static void acceptInStorageBarrel(final Iterable<String> items) {
        items.forEach(STORAGE_BARREL_ITEMS::add);
    }

    public static boolean storageBarrelAccepts(final ItemStack stack) {
        return !stack.isEmpty() && storageBarrelAccepts(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    public static boolean storageBarrelAccepts(final String item) {
        return STORAGE_BARREL_ITEMS.contains(item);
    }

    static boolean isStorageBarrel(final Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).equals(STORAGE_BARREL);
    }

    /**
     * The seed a barrel made for one crop takes ({@code container/basil_seed_barrel} takes {@code seed/basil_seed}), or null for any
     * other container.
     */
    static @Nullable String cropBarrelSeed(final Block block) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (!id.getNamespace().equals(Almura.ID) || !id.getPath().startsWith("container/") || !id.getPath().endsWith(CROP_BARREL_SUFFIX)
            || id.equals(STORAGE_BARREL)) {
            return null;
        }
        final String crop = id.getPath().substring("container/".length(), id.getPath().length() - CROP_BARREL_SUFFIX.length());
        return Almura.ID + ":seed/" + crop + "_seed";
    }

    /**
     * A container block: faces whoever placed it, its shape turned with it.
     */
    public static final class ContainerBlock extends HorizontalDirectionalBlock implements EntityBlock {

        private final int slots, limit;
        private final Map<Direction, VoxelShape> shapes;

        ContainerBlock(final BlockBehaviour.Properties properties, final VoxelShape north, final int slots, final int limit) {
            super(properties);
            // 3.5 kept a container between 9 and 54 slots, whole rows of a chest's screen
            this.slots = Math.max(9, Math.min(54, slots)) / 9 * 9;
            this.limit = limit <= 0 ? 64 : limit;
            this.shapes = Shapes.rotateHorizontal(north);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
            return this.shapes.get(state.getValue(FACING));
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
            return new ContainerBlockEntity(pos, state);
        }

        @Override
        protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player,
            final BlockHitResult hit) {
            if (level instanceof final ServerLevel serverLevel) {
                if (cropBarrelSeed(this) != null) {
                    convertToStorageBarrel(serverLevel, pos, state);
                }
                if (level.getBlockEntity(pos) instanceof final ContainerBlockEntity container) {
                    // The client's menu is told its rows
                    player.openMenu(container, buffer -> buffer.writeVarInt(container.getContainerSize() / 9));
                }
            }
            return InteractionResult.SUCCESS;
        }

        /**
         * A barrel made for one crop becomes the storage barrel, facing the same way and holding the same.
         */
        private static void convertToStorageBarrel(final ServerLevel level, final BlockPos pos, final BlockState state) {
            final Block barrel = BuiltInRegistries.BLOCK.getOptional(STORAGE_BARREL).orElse(null);
            if (barrel == null || !(level.getBlockEntity(pos) instanceof final ContainerBlockEntity old)) {
                return;
            }
            final NonNullList<ItemStack> contents = NonNullList.withSize(old.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < contents.size(); i++) {
                contents.set(i, old.getItem(i).copy());
            }
            // Cleared first, so replacing the block can't drop them
            old.clearContent();
            BlockState converted = barrel.defaultBlockState();
            if (converted.hasProperty(FACING)) {
                converted = converted.setValue(FACING, state.getValue(FACING));
            }
            level.setBlock(pos, converted, Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof final ContainerBlockEntity now) {
                for (int i = 0; i < Math.min(contents.size(), now.getContainerSize()); i++) {
                    now.setItem(i, contents.get(i));
                }
                now.setChanged();
            }
        }

        /**
         * Broken while sneaking, what it holds spills out; otherwise it stays in the dropped item (its loot table copies it).
         */
        @Override
        public BlockState playerWillDestroy(final Level level, final BlockPos pos, final BlockState state, final Player player) {
            if (!level.isClientSide() && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof final ContainerBlockEntity container) {
                Containers.dropContents(level, pos, container);
                container.clearContent();
            }
            return super.playerWillDestroy(level, pos, state, player);
        }

        @Override
        protected boolean hasAnalogOutputSignal(final BlockState state) {
            return true;
        }

        @Override
        protected int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos, final Direction direction) {
            return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
        }
    }

    /**
     * What a container holds; hoppers fill and empty it, minding what it takes.
     */
    public static final class ContainerBlockEntity extends BaseContainerBlockEntity {

        private NonNullList<ItemStack> items;

        public ContainerBlockEntity(final BlockPos pos, final BlockState state) {
            super(blockEntityType, pos, state);
            this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        }

        private ContainerBlock block() {
            return (ContainerBlock) this.getBlockState().getBlock();
        }

        @Override
        public int getContainerSize() {
            return this.getBlockState().getBlock() instanceof final ContainerBlock block ? block.slots : 54;
        }

        @Override
        public int getMaxStackSize() {
            return this.getBlockState().getBlock() instanceof final ContainerBlock block ? block.limit : 64;
        }

        @Override
        public boolean canPlaceItem(final int slot, final ItemStack stack) {
            return accepts(this.getBlockState().getBlock(), stack);
        }

        static boolean accepts(final Block block, final ItemStack stack) {
            if (isStorageBarrel(block)) {
                return storageBarrelAccepts(stack);
            }
            final String seed = cropBarrelSeed(block);
            return seed == null || BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(seed);
        }

        @Override
        protected NonNullList<ItemStack> getItems() {
            return this.items;
        }

        @Override
        protected void setItems(final NonNullList<ItemStack> items) {
            this.items = items;
        }

        @Override
        protected void saveAdditional(final ValueOutput output) {
            super.saveAdditional(output);
            ContainerHelper.saveAllItems(output, this.items);
        }

        @Override
        protected void loadAdditional(final ValueInput input) {
            super.loadAdditional(input);
            this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(input, this.items);
        }

        @Override
        protected Component getDefaultName() {
            return this.getBlockState().getBlock().getName();
        }

        /**
         * The storage barrel is named for what it holds first: "Basil Storage Barrel", "Basil Seed Storage Barrel".
         */
        @Override
        public Component getDisplayName() {
            final Component name = super.getDisplayName();
            if (!isStorageBarrel(this.getBlockState().getBlock()) || this.getCustomName() != null || this.items.isEmpty() || this.items.getFirst().isEmpty()) {
                return name;
            }
            return Component.empty().append(this.items.getFirst().getHoverName()).append(" ").append(name);
        }

        @Override
        protected AbstractContainerMenu createMenu(final int containerId, final Inventory inventory) {
            final boolean storageBarrel = isStorageBarrel(this.getBlockState().getBlock());
            return new ContainerMenu(storageBarrel ? storageBarrelMenuType : menuType, containerId, inventory, this, this.getContainerSize() / 9,
                storageBarrel);
        }
    }

    /**
     * A chest's menu whose container slots take only what the container takes (on the client too, so nothing appears to go in and pops back).
     */
    public static final class ContainerMenu extends ChestMenu {

        ContainerMenu(final MenuType<ChestMenu> type, final int containerId, final Inventory inventory, final Container container, final int rows,
            final boolean storageBarrel) {
            super(type, containerId, inventory, container, rows);
            for (int i = 0; i < rows * 9; i++) {
                final Slot old = this.slots.get(i);
                final Slot slot = new Slot(container, old.getContainerSlot(), old.x, old.y) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        if (container instanceof final ContainerBlockEntity entity) {
                            return entity.canPlaceItem(this.getContainerSlot(), stack);
                        }
                        return !storageBarrel || storageBarrelAccepts(stack);
                    }
                };
                slot.index = i;
                this.slots.set(i, slot);
            }
        }

        /**
         * The client's menu: the rows the server said, of a container it hasn't (the server's fills it).
         */
        static ChestMenu client(final MenuType<ChestMenu> type, final int containerId, final Inventory inventory, final int rows,
            final boolean storageBarrel) {
            final int clamped = Math.max(1, Math.min(6, rows));
            return new ContainerMenu(type, containerId, inventory, new SimpleContainer(clamped * 9), clamped, storageBarrel);
        }
    }

    @SuppressWarnings("unchecked")
    static MenuType<ChestMenu> newMenuType(final boolean storageBarrel) {
        final MenuType<ChestMenu>[] holder = new MenuType[1];
        holder[0] = IMenuTypeExtension.create((id, inventory, buffer) -> ContainerMenu.client(holder[0], id, inventory, buffer.readVarInt(),
            storageBarrel));
        return holder[0];
    }
}
