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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * BuildCraft (7.99 on 3.5) to its 26.3 port (8.0, BuildCraft's 1.12 code brought forward), when it is loaded.
 * <p>
 * The port keeps every 1.12 id (blocks, items, tile entities: {@code buildcrafttransport:pipe_holder}, {@code buildcraftcore:engine.wood}),
 * and its tiles read their 1.12 saved data themselves ({@code TileBC_Neptune.loadAdditional} hands it to the 1.12 {@code readFromNBT};
 * {@code NbtStacks} upgrades 1.12 item stacks and fluid stacks), so a tile entity goes over as it was. Only the blocks' metadata needs
 * turning into 26.3 properties, by what 1.12 kept in it:
 * <ul>
 *     <li>engines ({@code buildcraftcore:engine}): their type (0 wood, 1 stone, 2 iron, 3 creative, 4 rf); their facing is on the tile;</li>
 *     <li>springs ({@code buildcraftcore:spring}): 0 water, 1 oil;</li>
 *     <li>fluids (oil, fuel and their heats, {@code buildcraftenergy:fluid_block_*}): their level, 0 a source;</li>
 *     <li>anything else with a facing (as {@code BlockBCBase_Neptune} saved it): all six directions as {@code meta & 7} (down, up, north,
 *     south, west, east) for a block that can face up or down (the laser), the four horizontal ones as {@code meta & 3} (south, west, north,
 *     east) otherwise;</li>
 *     <li>the rest (pumps, tubes, the auto workbench, the assembly table, pipe holders): nothing.</li>
 * </ul>
 */
final class BuildcraftConversion {

    private static final String PREFIX = "buildcraft";

    private final boolean active = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath("buildcraftcore", "engine"));

    boolean handles(final String name) {
        return this.active && name.startsWith(PREFIX);
    }

    boolean handlesTileEntity(final String id) {
        return this.active && id.startsWith(PREFIX);
    }

    /**
     * @return its 26.3 state, or null when the port has no such block
     */
    @Nullable BlockState state(final String name, final int meta) {
        final Identifier id = Identifier.tryParse(name);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        final Block block = BuiltInRegistries.BLOCK.getValue(id);
        BlockState state = block.defaultBlockState();
        final String path = id.getPath();
        if (path.equals("engine")) {
            return with(state, "type", new String[] {"wood", "stone", "iron", "creative", "rf"}, meta);
        }
        if (path.equals("spring")) {
            return with(state, "type", new String[] {"water", "oil"}, meta);
        }
        final Optional<Property<?>> level = property(state, "level");
        if (path.startsWith("fluid_block_") && level.isPresent()) {
            return value(state, level.get(), Integer.toString(Math.min(meta, 15)));
        }
        final Optional<Property<?>> facing = property(state, "facing");
        if (facing.isPresent()) {
            final boolean vertical = facing.get().getPossibleValues().contains(Direction.UP);
            final Direction direction = vertical ? Direction.from3DDataValue(meta & 7) : Direction.from2DDataValue(meta & 3);
            state = value(state, facing.get(), direction.getSerializedName());
        }
        return state;
    }

    /**
     * Upgrades every 1.12 item stack in a BuildCraft tile entity (a pipe's filters and the items travelling in it, an inventory's slots) to
     * 26.3's form, in place: the port can read 1.12's itself, but does it with the game's data fixer on the server thread as the chunk
     * loads, which on a dedicated server waits forever (the watchdog then stops the server). A stack 4.0 has no item for becomes empty.
     *
     * @param legacyItem a 1.12 item as 26.3 keeps it, or null when 4.0 has no such item
     */
    static void upgradeItems(final Tag tag, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        if (tag instanceof final CompoundTag compound) {
            for (final String key : Set.copyOf(compound.keySet())) {
                final Tag child = compound.get(key);
                if (child instanceof final CompoundTag stack && legacyStack(stack)) {
                    final CompoundTag upgraded = legacyItem.apply(stack);
                    compound.put(key, upgraded == null ? new CompoundTag() : upgraded);
                } else {
                    upgradeItems(child, legacyItem);
                }
            }
        } else if (tag instanceof final ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof final CompoundTag stack && legacyStack(stack)) {
                    final CompoundTag upgraded = legacyItem.apply(stack);
                    if (upgraded == null) {
                        // Keeps its slot, if the list has slot numbers, without an item
                        final CompoundTag empty = new CompoundTag();
                        stack.getByte("Slot").ifPresent(slot -> empty.putByte("Slot", slot));
                        list.set(i, empty);
                    } else {
                        stack.getByte("Slot").ifPresent(slot -> upgraded.putByte("Slot", slot));
                        list.set(i, upgraded);
                    }
                } else {
                    upgradeItems(list.get(i), legacyItem);
                }
            }
        }
    }

    // As the port tells one: a 1.12 stack has "Count" where 26.3's has "count"
    private static boolean legacyStack(final CompoundTag tag) {
        return tag.contains("Count") && !tag.contains("count") && tag.getString("id").isPresent();
    }

    private static BlockState with(final BlockState state, final String name, final String[] values, final int meta) {
        return property(state, name).map(property -> value(state, property, values[Math.min(meta, values.length - 1)])).orElse(state);
    }

    private static Optional<Property<?>> property(final BlockState state, final String name) {
        return Optional.ofNullable(state.getBlock().getStateDefinition().getProperty(name));
    }

    private static <T extends Comparable<T>> BlockState value(final BlockState state, final Property<T> property, final String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }
}
