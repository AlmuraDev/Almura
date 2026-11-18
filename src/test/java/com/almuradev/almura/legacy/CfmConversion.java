/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * MrCrayfish's Furniture Mod (6.3.0 on 3.5) blocks, to its 26.3 port (the 1.20 furniture, with 1.12's brought forward), when it is loaded.
 * <p>
 * 1.12 named its wooden furniture {@code <kind>_<wood>} ({@code table_dark_oak}), 26.3 {@code <wood>_<kind>}; its coloured furniture kept the
 * colour on the tile entity ({@code colour}: the wool colour's number) or, for bar stools, in the metadata, where 26.3 has a block for each
 * colour. Its facing was the metadata's low two bits, as the horizontal index (south, west, north, east), as 26.3 faces them. What 1.12
 * worked out from the neighbours each time it drew (fence and table connections, counter corners, bench, desk and couch ends, gates in
 * pairs) is left to the game, which works it out again as the converted chunk first loads (its upgrade data).
 * <p>
 * 1.12's tile entity ids were {@code minecraft:cfm<kind>}; their items (a vanilla container's {@code Items}) are kept where 26.3's block is
 * a container. Furniture 26.3 doesn't have yet is left out (air), and said so.
 */
final class CfmConversion {

    private static final String NAMESPACE = "cfm";
    // 1.12's colour numbers (wool's), as 26.3's colour names
    private static final String[] COLOURS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
        "purple", "blue", "brown", "green", "red", "black"};
    private static final String[] WOODS = {"dark_oak", "oak", "spruce", "birch", "jungle", "acacia"};
    // 1.12's stone furniture, which 26.3 has no stone for: the wood nearest its colour
    private static final Map<String, String> STONE_AS_WOOD = Map.of("granite", "acacia", "diorite", "birch", "andesite", "stripped_oak",
        "stone", "stripped_oak");
    // Coloured furniture: 1.12's kind, as 26.3's (after the colour)
    private static final Map<String, String> COLOURED = Map.of("counter", "kitchen_counter", "counter_sink", "kitchen_sink",
        "cabinet_kitchen", "kitchen_cabinet", "couch", "sofa", "modern_couch", "modern_couch");
    // 1.12's blocks named otherwise in 26.3 (or one block where 1.12 had one for each state)
    private static final Map<String, String> RENAMED = Map.ofEntries(Map.entry("white_fence", "white_picket_fence"),
        Map.entry("white_gate", "white_picket_gate"), Map.entry("couch_jeb", "rainbow_sofa"), Map.entry("grill", "black_grill"),
        Map.entry("fridge", "fridge_light"), Map.entry("freezer", "freezer_light"), Map.entry("fire_pit_on", "fire_pit"),
        Map.entry("fire_pit_off", "fire_pit"), Map.entry("ceiling_light_on", "ceiling_light"), Map.entry("ceiling_light_off", "ceiling_light"),
        Map.entry("range_hood_powered", "range_hood"), Map.entry("modern_bed_top", "white_modern_bed"),
        Map.entry("modern_bed_bottom", "white_modern_bed"), Map.entry("mail_box", "oak_mail_box"), Map.entry("door_bell", "oak_door_bell"),
        Map.entry("fire_alarm_off", "fire_alarm"), Map.entry("fire_alarm_on", "fire_alarm"), Map.entry("table_outdoor", "outdoor_table"),
        Map.entry("modern_table_outdoor", "modern_outdoor_table"), Map.entry("tree_bottom", "christmas_tree"), Map.entry("tree_top", "christmas_tree"),
        Map.entry("grand_chair_bottom", "grand_chair"), Map.entry("grand_chair_top", "grand_chair"), Map.entry("item_cup", "cup"));
    // Beds stand in two halves, which the game would take apart if it looked at them before both are there
    private static final Set<String> NO_REFRESH = Set.of("modern_bed_top", "modern_bed_bottom", "tree_bottom", "tree_top", "grand_chair_bottom",
        "grand_chair_top");
    // Coloured furniture whose colour 1.12 kept in the metadata (as bar stools), and its 26.3 kind
    private static final Map<String, String> COLOURED_BY_META = Map.of("inflatable_castle", "inflatable_castle", "present", "present");
    // Coloured furniture whose colour 1.12 kept on the tile entity under another key: the key, and the 26.3 kind
    private static final Map<String, String[]> COLOURED_BY_KEY = Map.of("digital_clock", new String[] {"color", "digital_clock"},
        "photo_frame", new String[] {"Color", "photo_frame"}, "curtains_open", new String[] {"colour", "curtains"},
        "curtains_closed", new String[] {"colour", "curtains"});

    private final boolean active = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, "dark_oak_upgraded_fence"));
    private final Map<String, String> resolved = new HashMap<>();
    // 1.12 blocks with a stand-in, for the report
    final Map<String, String> substituted = new HashMap<>();

    boolean handles(final String name) {
        return this.active && name.startsWith(NAMESPACE + ":");
    }

    boolean handlesTileEntity(final String id) {
        return this.active && id.startsWith("minecraft:cfm");
    }

    /**
     * Whether the game should work out the block's shape from its neighbours as the chunk first loads (connections, corners, ends).
     */
    static boolean refreshes(final String legacyName) {
        return !NO_REFRESH.contains(legacyName.substring(legacyName.indexOf(':') + 1));
    }

    /**
     * @param tileEntity the block's 1.12 tile entity, for its colour; null without one
     * @return its 26.3 state, or null when the furniture port doesn't have it yet
     */
    @Nullable BlockState state(final String name, final int meta, final @Nullable CompoundTag tileEntity) {
        final String path = name.substring(name.indexOf(':') + 1);
        final String target = this.target(path, meta, tileEntity);
        if (target == null) {
            return null;
        }
        final Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, target);
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        BlockState state = BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        state = set(state, "facing", new String[] {"south", "west", "north", "east"}[meta & 3]);
        if (path.startsWith("upgraded_gate") || path.startsWith("white_gate")) {
            state = set(state, "open", (meta & 4) != 0 ? "true" : "false");
            state = set(state, "hinge", (meta & 8) == 0 ? "left" : "right");
        } else if (path.startsWith("blinds")) {
            state = set(state, "open", path.endsWith("_open") ? "true" : "false");
        } else if (path.equals("modern_bed_top") || path.equals("modern_bed_bottom")) {
            state = set(state, "part", path.endsWith("top") ? "head" : "foot");
            state = set(state, "occupied", "false");
        } else if (path.startsWith("fire_pit")) {
            state = set(state, "lit", path.endsWith("_on") ? "true" : "false");
            state = set(state, "stage", "3");
        } else if (path.startsWith("ceiling_light")) {
            state = set(state, "lit", path.endsWith("_on") ? "true" : "false");
        } else if (path.startsWith("range_hood")) {
            state = set(state, "lit", path.endsWith("_powered") ? "true" : "false");
        } else if (path.equals("tree_bottom") || path.equals("grand_chair_bottom")) {
            state = set(state, "half", "lower");
        } else if (path.equals("tree_top") || path.equals("grand_chair_top")) {
            state = set(state, "half", "upper");
        } else if (path.equals("modern_tv")) {
            // 1.12 added 4 to the metadata for a TV hung on the wall
            state = set(state, "mounted", (meta & 4) != 0 ? "true" : "false");
        } else if (path.startsWith("curtains")) {
            state = set(state, "open", path.endsWith("_open") ? "true" : "false");
        } else if (path.equals("ceiling_fan")) {
            state = set(state, "powered", tileEntity != null && tileEntity.getBooleanOr("powered", false) ? "true" : "false");
        }
        return state;
    }

    private @Nullable String target(final String path, final int meta, final @Nullable CompoundTag tileEntity) {
        if (path.equals("bar_stool")) {
            return COLOURS[meta & 15] + "_bar_stool";
        }
        final String byMeta = COLOURED_BY_META.get(path);
        if (byMeta != null) {
            return COLOURS[meta & 15] + "_" + byMeta;
        }
        final String[] byKey = COLOURED_BY_KEY.get(path);
        if (byKey != null) {
            final int colour = tileEntity == null ? 0 : tileEntity.getIntOr(byKey[0], tileEntity.getByteOr(byKey[0], (byte) 0));
            return COLOURS[Math.floorMod(colour, 16)] + "_" + byKey[1];
        }
        final String coloured = COLOURED.get(path);
        if (coloured != null) {
            final int colour = tileEntity == null ? 0 : tileEntity.getIntOr("colour", 0);
            return COLOURS[Math.floorMod(colour, 16)] + "_" + coloured;
        }
        return this.resolved.computeIfAbsent(path, this::named);
    }

    /**
     * The 26.3 name of a 1.12 block without colour: renamed, or {@code <kind>_<wood>} as {@code <wood>_<kind>} (blinds also having
     * {@code _open} or {@code _closed}, oak's no wood), or the same name; null when 26.3 has none.
     */
    private @Nullable String named(final String path) {
        final String renamed = RENAMED.get(path);
        if (renamed != null) {
            return renamed;
        }
        String kind = path, wood = null;
        if (path.startsWith("blinds")) {
            kind = "blinds";
            final String middle = path.replaceFirst("^blinds_?", "").replaceFirst("_?(open|closed)$", "");
            wood = middle.isEmpty() ? "oak" : middle;
        } else {
            for (final String candidate : WOODS) {
                if (path.endsWith("_" + candidate)) {
                    kind = path.substring(0, path.length() - candidate.length() - 1);
                    wood = candidate;
                    break;
                }
            }
            for (final Map.Entry<String, String> stone : STONE_AS_WOOD.entrySet()) {
                if (wood == null && path.endsWith("_" + stone.getKey())) {
                    kind = path.substring(0, path.length() - stone.getKey().length() - 1);
                    wood = stone.getValue();
                    this.substituted.put(path, wood + "_" + kind);
                }
            }
        }
        final String target = wood == null ? path : wood + "_" + kind;
        return BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, target)) ? target : null;
    }

    /**
     * The 26.3 block entity, from the upgraded chest (the 1.12 tile entity's items through the game's upgrade) and what else 1.12 kept;
     * the items only where 26.3's block is a container that keeps them. Null when the block has no block entity.
     */
    static @Nullable CompoundTag finish(final @Nullable CompoundTag chest, final @Nullable CompoundTag legacy, final BlockState state, final BlockPos pos) {
        final Optional<BlockEntityType<?>> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.stream().filter(t -> t.isValid(state)).findFirst();
        if (type.isEmpty()) {
            return null;
        }
        final Identifier typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type.get());
        final CompoundTag out = new CompoundTag();
        out.putString("id", typeId.toString());
        out.putInt("x", pos.getX());
        out.putInt("y", pos.getY());
        out.putInt("z", pos.getZ());
        if (chest != null && keepsItems(type.get(), state, pos, typeId)) {
            out.put("Items", chest.getListOrEmpty("Items"));
        }
        if (legacy != null) {
            // A TV's channels and a photo frame's picture (web addresses), and whether the TV is on
            final ListTag channels = legacy.getListOrEmpty("Channels");
            final ListTag urls = new ListTag();
            for (int i = 0; i < channels.size(); i++) {
                channels.getString(i).ifPresent(url -> urls.add(StringTag.valueOf(url)));
            }
            legacy.getString("Photo").ifPresent(url -> urls.add(StringTag.valueOf(url)));
            if (!urls.isEmpty()) {
                out.put("Urls", urls);
                out.putBoolean("Stretch", legacy.getBooleanOr("Stretch", false));
                out.putInt("Channel", legacy.getIntOr("CurrentChannel", 0));
                out.putBoolean("Powered", legacy.getBooleanOr("Powered", false));
            }
            // A digital clock's number colour (1.12's dye number)
            if (legacy.contains("textColor")) {
                out.putString("TextColour", COLOURS[Math.floorMod(legacy.getByteOr("textColor", (byte) 0), 16)]);
            }
            // A mail box's owner
            legacy.getString("OwnerName").ifPresent(name -> out.putString("OwnerName", name));
            legacy.getString("OwnerUUID").ifPresent(owner -> {
                try {
                    final UUID uuid = UUID.fromString(owner);
                    out.put("OwnerUUID", new IntArrayTag(new int[] {(int) (uuid.getMostSignificantBits() >> 32), (int) uuid.getMostSignificantBits(),
                        (int) (uuid.getLeastSignificantBits() >> 32), (int) uuid.getLeastSignificantBits()}));
                } catch (final IllegalArgumentException ignored) {
                    // No owner then
                }
            });
        }
        return out;
    }

    /**
     * Whether the block entity keeps a container's items as the game does ({@code Items}): CFM's storage furniture and appliances do; its
     * grill keeps its grill and fuel apart, so 1.12's grill items (its slots differ too) are left out.
     */
    private static boolean keepsItems(final BlockEntityType<?> type, final BlockState state, final BlockPos pos, final Identifier typeId) {
        if (typeId.getPath().contains("grill")) {
            return false;
        }
        final BlockEntity blockEntity = type.create(pos, state);
        return blockEntity instanceof Container;
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
