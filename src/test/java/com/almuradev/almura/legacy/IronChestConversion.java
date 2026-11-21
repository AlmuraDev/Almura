/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Iron Chests (7.0 on 3.5) to its 26.3 version, when it is loaded.
 * <p>
 * 1.12 had one block for every chest, {@code ironchest:iron_chest}, its type in the metadata and in the tile entity's id
 * ({@code minecraft:ironchest.diamond}), its facing on the tile entity ({@code facing}, 1.12's direction number) and its items as a vanilla
 * chest keeps them. 26.3 has a block for each type ({@code ironchest:diamond_chest}), facing as a block property, and a block entity of the
 * same name keeping its items as a vanilla chest does. So the block is told by its tile entity's type, and the items go through the game's
 * upgrade as a chest's, then the block entity is named as 26.3 names it. 26.3 has no silver chest: a silver one (72 slots) becomes gold (81),
 * so everything still fits.
 */
final class IronChestConversion {

    private static final String NAMESPACE = "ironchest";
    private static final String TILE_PREFIX = "minecraft:ironchest.";
    // 1.12's types in metadata order, for a chest without its tile entity
    private static final String[] TYPES = {"iron", "gold", "diamond", "copper", "silver", "crystal", "obsidian", "dirtchest9000"};
    // 1.12's types named otherwise (or missing) in 26.3
    private static final Map<String, String> RENAMED = Map.of("silver", "gold", "dirtchest9000", "dirt");

    private final boolean active = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, "iron_chest"));

    boolean handles(final String name) {
        return this.active && name.startsWith(NAMESPACE + ":");
    }

    boolean handlesTileEntity(final String id) {
        return this.active && id.startsWith(TILE_PREFIX);
    }

    /**
     * @param tileEntity the chest's 1.12 tile entity, for its type and facing; null without one
     * @return its 26.3 state, or null for a block 26.3 doesn't have (1.12's iron shulker boxes)
     */
    @Nullable BlockState state(final String name, final int meta, final @Nullable CompoundTag tileEntity) {
        if (!name.equals(NAMESPACE + ":iron_chest")) {
            return null;
        }
        final String type = tileEntity != null && tileEntity.getStringOr("id", "").startsWith(TILE_PREFIX)
            ? tileEntity.getStringOr("id", "").substring(TILE_PREFIX.length())
            : TYPES[Math.min(Math.max(meta, 0), TYPES.length - 1)];
        final Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, RENAMED.getOrDefault(type, type) + "_chest");
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        final BlockState state = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        final Direction facing = tileEntity == null ? Direction.NORTH : Direction.from3DDataValue(tileEntity.getByteOr("facing", (byte) 2));
        return state.hasProperty(HorizontalDirectionalBlock.FACING) && facing.getAxis().isHorizontal()
            ? state.setValue(HorizontalDirectionalBlock.FACING, facing) : state;
    }

    /**
     * The chest's 26.3 block entity, from what the game's upgrade made of its items (as a vanilla chest's), named for its block.
     */
    static void finish(final CompoundTag chest, final CompoundTag legacy, final BlockState block) {
        chest.putString("id", BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString());
        legacy.getString("CustomName").filter(name -> !name.isEmpty()).ifPresent(name -> chest.putString("CustomName", name));
    }
}
