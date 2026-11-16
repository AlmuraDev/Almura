/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cooking for Blockheads (6.5.0 on 3.5) and Farming for Blockheads (3.1.28) blocks, to their 26.3 versions, when those mods are loaded.
 * <p>
 * 1.12 kept their facing in the metadata and their colour on the tile entity (as a dye number, white by default); 26.3 has a block for
 * each colour (the plain one is white, but for fridges and ovens, which only come coloured). The corner is now the connector. Their items were in
 * an {@code ItemHandler} as 1.12 Forge kept them; 26.3 keeps them under the same name, as the game keeps a container's.
 */
final class BlockheadsConversion {

    private static final String COOKING = "cookingforblockheads", FARMING = "farmingforblockheads";
    // 1.12's dye numbers (EnumDyeColor's "dye damage"), as 26.3's colour names
    private static final String[] DYES = {"black", "red", "green", "brown", "blue", "purple", "cyan", "light_gray", "gray", "pink", "lime",
        "yellow", "light_blue", "magenta", "orange", "white"};
    // Blocks that kept their facing as counters do: east, west, south, north, and 8 for flipped
    private static final Set<String> COUNTER_FACING = Set.of("counter", "cabinet", "fridge", "sink");
    // Blocks that come in colours in 26.3
    private static final Set<String> COLOURED = Set.of("counter", "cabinet", "fridge", "sink", "cooking_table", "oven", "connector");
    // Coloured blocks with no plain (white) one in 26.3
    private static final Set<String> ALWAYS_COLOURED = Set.of("fridge", "oven");
    private static final Map<String, String> RENAMED = Map.of("corner", "connector");
    // 1.12 tile entity data kept as it is, beside the items
    private static final Set<String> KEPT = Set.of("HasIceUpgrade", "HasPreservationUpgrade", "HasPowerUpgrade");

    private final boolean cooking = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(COOKING, "counter"));
    private final boolean farming = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(FARMING, "feeding_trough"));

    boolean handles(final String name) {
        return this.cooking && name.startsWith(COOKING + ":") || this.farming && name.startsWith(FARMING + ":");
    }

    /**
     * @param tileEntity the block's 1.12 tile entity, for its colour; null without one
     * @return its 26.3 state, or null when 26.3 has no such block
     */
    @Nullable BlockState state(final String name, final int meta, final @Nullable CompoundTag tileEntity) {
        final String namespace = name.substring(0, name.indexOf(':'));
        String path = name.substring(name.indexOf(':') + 1);
        final String legacyPath = path;
        path = RENAMED.getOrDefault(path, path);
        if (COLOURED.contains(path)) {
            final int dye = tileEntity == null ? 15 : tileEntity.getByteOr(path.equals("fridge") ? "FridgeColor" : "Color", (byte) 15);
            final String colour = DYES[Math.floorMod(dye, 16)];
            if (!colour.equals("white") || ALWAYS_COLOURED.contains(path)) {
                path = colour + "_" + path;
            }
        }
        final Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        BlockState state = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        if (COUNTER_FACING.contains(legacyPath)) {
            state = set(state, "facing", new String[] {"east", "west", "south", "north"}[Math.min(meta & 7, 3)]);
            state = set(state, "flipped", (meta & 8) != 0 ? "true" : "false");
        } else if (namespace.equals(COOKING)) {
            // 1.12's EnumFacing by number; up and down (never placed) as north
            state = set(state, "facing", switch (meta & 7) {
                case 3 -> "south";
                case 4 -> "west";
                case 5 -> "east";
                default -> "north";
            });
        }
        return state;
    }

    /**
     * Whether the block is a fridge, for {@link #fridgeModel}.
     */
    static boolean isFridge(final BlockState state) {
        final Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.getNamespace().equals(COOKING) && id.getPath().endsWith("fridge");
    }

    /**
     * A fridge with another on top is the lower half of a large one, and the one on top its upper half, as 1.12 drew them.
     */
    static BlockState fridgeModel(final BlockState state, final boolean fridgeAbove, final boolean fridgeBelow) {
        return set(state, "model", fridgeAbove ? "large_lower" : fridgeBelow ? "large_upper" : "small");
    }

    /**
     * The 1.12 tile entity as a 1.12 chest, so the game's upgrade upgrades its items; see {@link #finish}.
     */
    static CompoundTag asChest(final CompoundTag tileEntity) {
        final CompoundTag chest = new CompoundTag();
        chest.putString("id", "minecraft:chest");
        chest.putInt("x", tileEntity.getIntOr("x", 0));
        chest.putInt("y", tileEntity.getIntOr("y", 0));
        chest.putInt("z", tileEntity.getIntOr("z", 0));
        final ListTag items = new ListTag();
        for (final Tag tag : tileEntity.getCompoundOrEmpty("ItemHandler").getListOrEmpty("Items")) {
            if (tag instanceof final CompoundTag slot) {
                final CompoundTag item = slot.copy();
                LegacyWorldConverter.stripForge(item);
                item.putByte("Slot", (byte) slot.getIntOr("Slot", slot.getByteOr("Slot", (byte) 0)));
                items.add(item);
            }
        }
        chest.put("Items", items);
        return chest;
    }

    /**
     * The 26.3 block entity, from the upgraded chest and what else 1.12 kept; null when the block has none.
     */
    static @Nullable CompoundTag finish(final CompoundTag chest, final CompoundTag legacy, final BlockState state) {
        final Optional<BlockEntityType<?>> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.stream().filter(t -> t.isValid(state)).findFirst();
        if (type.isEmpty()) {
            return null;
        }
        final CompoundTag out = new CompoundTag();
        out.putString("id", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type.get()).toString());
        out.putInt("x", chest.getIntOr("x", 0));
        out.putInt("y", chest.getIntOr("y", 0));
        out.putInt("z", chest.getIntOr("z", 0));
        final CompoundTag handler = new CompoundTag();
        handler.put("Items", chest.getListOrEmpty("Items"));
        out.put("ItemHandler", handler);
        for (final String key : KEPT) {
            legacy.getByte(key).ifPresent(value -> out.putBoolean(key, value != 0));
        }
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState set(final BlockState state, final String name, final String value) {
        final Property property = state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) {
            return state;
        }
        final Optional<?> parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : state;
    }
}
