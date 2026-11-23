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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * Carpenter's Blocks (1.12.2) to its 26.3 port: the same block names and block data ({@code cbMetadata}), with the attributes' items in the
 * 26.3 item format. The bed's facing and half, kept in its 1.12 block data, become its 26.3 blockstate. Slope items keep their slope (1.12's
 * item damage, 26.3's {@code carpentersblocks:slope_type}); Carpenter's Tiles, entities, keep their 1.12 data, which the port reads.
 */
final class CarpentersBlocksConversion {

    private static final String NAMESPACE = "carpentersblocks";
    // 1.12 saved tile entities registered without a mod id under minecraft:, lower cased
    private static final String BLOCK_TILE = "tileentitycarpentersblock";
    private static final String SAFE_TILE = "tileentitycarpenterssafe";
    private static final String TILE_ENTITY = NAMESPACE + ":carpenters_tile";
    private static final String SLOPE = NAMESPACE + ":carpenters_slope";
    private static final String SLOPE_TYPE = NAMESPACE + ":slope_type";
    private static final int SLOPE_ITEMS = 5;
    // The 1.12 item stack's own keys in an item attribute (the rest are the attribute's)
    private static final Set<String> ITEM_KEYS = Set.of("id", "Count", "Damage", "tag", "ForgeCaps");
    // 1.12 bed block data: direction from head to foot (2D value), and the head bit
    private static final int BED_DIR_MASK = 0x3;
    private static final int BED_HEAD_BIT = 0x8000;

    private final boolean active = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, "carpenters_block"));
    private final boolean tiles = BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, "carpenters_tile"));
    private final LegacyWorldConverter.Report report;

    CarpentersBlocksConversion(final LegacyWorldConverter.Report report) {
        this.report = report;
    }

    boolean handles(final String name) {
        return this.active && name.startsWith(NAMESPACE + ":");
    }

    boolean handlesTileEntity(final String id) {
        final String name = id.toLowerCase(Locale.ROOT);
        final String path = name.startsWith("minecraft:") ? name.substring(10) : name;
        return this.active && (path.equals(BLOCK_TILE) || path.equals(SAFE_TILE));
    }

    /**
     * Whether a 1.12 entity is a Carpenter's Tile the port has.
     */
    boolean handlesEntity(final String id) {
        return this.tiles && id.equals(TILE_ENTITY);
    }

    /**
     * @param tileEntity the block's 1.12 tile entity, for the bed's facing and half; null without one
     * @return its 26.3 state, or null for a block the port doesn't have (turned off in its config)
     */
    @Nullable BlockState state(final String name, final @Nullable CompoundTag tileEntity) {
        final Identifier id = Identifier.tryParse(name);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        final Block block = BuiltInRegistries.BLOCK.getValue(id);
        BlockState state = block.defaultBlockState();
        if (tileEntity != null && state.hasProperty(AbstractBedBlock.FACING) && state.hasProperty(AbstractBedBlock.PART)) {
            final int data = tileEntity.getIntOr("cbMetadata", 0);
            // 26.3's facing points from the foot to the head
            state = state.setValue(AbstractBedBlock.FACING, Direction.from2DDataValue(data & BED_DIR_MASK).getOpposite())
                .setValue(AbstractBedBlock.PART, (data & BED_HEAD_BIT) != 0 ? BedPart.HEAD : BedPart.FOOT);
        }
        return state;
    }

    /**
     * The 26.3 block entity of a 1.12 Carpenter's tile entity: its attributes with their items as 26.3 keeps them (an attribute whose item
     * 4.0 doesn't have is left out), its block data and owner, and the safe's items.
     *
     * @param legacyItem a 1.12 item as 26.3 keeps it, or null when 4.0 has no such item
     */
    CompoundTag blockEntity(final CompoundTag tileEntity, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final String id = tileEntity.getStringOr("id", "").toLowerCase(Locale.ROOT);
        final boolean safe = id.endsWith(SAFE_TILE);
        final CompoundTag blockEntity = new CompoundTag();
        blockEntity.putString("id", NAMESPACE + (safe ? ":safe" : ":coverable"));
        blockEntity.putInt("x", tileEntity.getIntOr("x", 0));
        blockEntity.putInt("y", tileEntity.getIntOr("y", 0));
        blockEntity.putInt("z", tileEntity.getIntOr("z", 0));

        final ListTag attributes = new ListTag();
        for (final Tag tag : tileEntity.getListOrEmpty("cbAttrList")) {
            if (!(tag instanceof final CompoundTag legacy)) {
                continue;
            }
            final CompoundTag attribute = new CompoundTag();
            attribute.putInt("cbAttrLocation", legacy.getIntOr("cbAttrLocation", -1) & 255);
            attribute.putInt("cbAttrType", legacy.getIntOr("cbAttrType", -1) & 255);
            final String model = legacy.getStringOr("cbAttrModel", "");
            attribute.putString("cbAttrModel", model);
            if (model.equalsIgnoreCase("String")) {
                attribute.putString("cbAttrString", legacy.getStringOr("cbAttrString", ""));
            } else if (model.equalsIgnoreCase("ItemStack")) {
                final CompoundTag stack = new CompoundTag();
                for (final String key : ITEM_KEYS) {
                    final Tag value = legacy.get(key);
                    if (value != null) {
                        stack.put(key, value.copy());
                    }
                }
                final CompoundTag item = stack.getStringOr("id", "minecraft:air").equals("minecraft:air") ? null : legacyItem.apply(stack);
                if (item == null) {
                    this.report.lostItems.merge(legacy.getStringOr("id", "?") + " (Carpenter's attribute)", 1, Integer::sum);
                    continue;
                }
                item.putInt("count", 1);
                attribute.put("cbItem", item);
            } else {
                continue;
            }
            attributes.add(attribute);
        }
        blockEntity.put("cbAttrList", attributes);
        blockEntity.putInt("cbMetadata", tileEntity.getIntOr("cbMetadata", 0));
        blockEntity.putString("cbOwner", tileEntity.getStringOr("cbOwner", ""));

        if (safe) {
            final ListTag items = new ListTag();
            for (final Tag tag : tileEntity.getListOrEmpty("Items")) {
                if (tag instanceof final CompoundTag slot) {
                    final CompoundTag item = legacyItem.apply(slot);
                    if (item != null) {
                        item.putByte("Slot", slot.getByteOr("Slot", (byte) 0));
                        this.convertAll(item);
                        items.add(item);
                    }
                }
            }
            blockEntity.put("Items", items);
        }
        return blockEntity;
    }

    /**
     * Gives every slope item anywhere in the (upgraded) tag its slope back: 1.12 kept it as the item's damage, which the game's upgrade
     * made a damage component.
     */
    void convertAll(final Tag tag) {
        if (!this.active) {
            return;
        }
        if (tag instanceof final CompoundTag compound) {
            if (compound.getStringOr("id", "").equals(SLOPE)) {
                final CompoundTag components = compound.getCompoundOrEmpty("components");
                int damage = compound.getIntOr("Damage", 0);
                final Tag componentDamage = components.get("minecraft:damage");
                if (componentDamage != null) {
                    damage = components.getIntOr("minecraft:damage", 0);
                    components.remove("minecraft:damage");
                }
                compound.remove("Damage");
                if (damage > 0 && damage < SLOPE_ITEMS) {
                    components.putInt(SLOPE_TYPE, damage);
                    this.report.carpentersSlopes++;
                }
                if (!components.isEmpty()) {
                    compound.put("components", components);
                } else {
                    compound.remove("components");
                }
            }
            for (final String key : Set.copyOf(compound.keySet())) {
                this.convertAll(compound.get(key));
            }
        } else if (tag instanceof final ListTag list) {
            list.forEach(this::convertAll);
        }
    }
}
