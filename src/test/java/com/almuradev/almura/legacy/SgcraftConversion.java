/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

/**
 * SGCraft (2.x on 3.5, package {@code gcewing.sg}) to its 26.3 rewrite, when it is loaded.
 * <p>
 * 1.12 kept nearly everything about a gate on its base's tile entity, and each ring block had one naming its base. 26.3's ring blocks have
 * no block entity (a {@code merged} property instead); its base's block entity mostly mirrors the gate, whose settings, iris, energy, owner
 * and access live in the server's {@code data/sgcraft/gates.dat}. A gate is only formed as its blocks are placed, never as its chunk
 * loads, so the converted gates are written formed (base {@code formed}, rings {@code merged}) and recorded in gates.dat, merged into the
 * 4.0 world's own file. ZPM devices' links to gates go to {@code data/sgcraft/zpm_links.dat}, and every converted chunk is marked in the
 * dimension's {@code naquadah_ore_chunks.dat}, so 26.3 adds no naquadah ore to the old world.
 * <p>
 * Facing: 1.12's gate bases and DHDs kept the way the placing player looked (west, east, south, north by metadata), with the front
 * towards the player; 26.3's face the player. Its ZPM devices, obelisks, ring panels and generators faced the player already (vanilla's
 * horizontal order). Energy: 1.12's gate energy is 26.3's; ZPM energy was in 1.12's ZPM units (80 to a gate unit), IC2 power units' in EU
 * (20), FE power units' in FE (80). Addresses are made anew by 26.3's rules (see {@link SgcraftAddresses}).
 */
final class SgcraftConversion {

    private static final String NS = "sgcraft";
    // The data kept for the post-upgrade pass, inside the 1.12 item's tag, which the game's upgrade moves into minecraft:custom_data
    private static final String CARRIED = "almura_sgcraft";
    // 1.12's horizontal orders: SGCraft's own blocks (BaseOrientation.PropertyTurn), vanilla's (EnumFacing.byHorizontalIndex)
    private static final String[] TURN = {"west", "east", "south", "north"};
    private static final String[] HORIZONTAL = {"south", "west", "north", "east"};
    private static final double ZPM_UNITS_PER_GE = 80, EU_PER_GE = 20, FE_PER_GE = 80;
    // 26.3's defaults: a full ZPM, a power unit's capacity (energy.power_unit_capacity), a ring platform's (rings.capacity)
    private static final double ZPM_CAPACITY = 26_843_546, POWER_UNIT_CAPACITY = 50_000, RING_CAPACITY = 50_000;
    // 1.12's ring platform capacity (rings.energyCapacity in 3.5's sgcraft.cfg): its energy is scaled to 26.3's by the two
    private static final double RING_CAPACITY_112 = 200_000;
    private static final double DHD_RANGE = 6, DHD_MAX_RANGE = 32;
    private static final long IDLE = Long.MIN_VALUE / 2;

    // 1.12's item ids (lowercased, as they were saved) as 26.3's; the ring blocks by their damage, the spawners by name
    private static final Map<String, String> ITEMS = Map.ofEntries(
        Map.entry("naquadahingot", "naquadah_ingot"), Map.entry("sgcorecrystal", "core_crystal"),
        Map.entry("sgcontrollercrystal", "controller_crystal"), Map.entry("sgchevronupgrade", "chevron_upgrade"),
        Map.entry("sgirisupgrade", "iris_upgrade"), Map.entry("sgshieldupgrade", "shield_upgrade"), Map.entry("sgirisblade", "iris_blade"),
        Map.entry("stargatebase", "stargate_base"), Map.entry("stargatebasepegasus", "stargate_base_pegasus"),
        Map.entry("stargatebaseuniverse", "stargate_base_universe"), Map.entry("supergatebase", "supergate_base"),
        Map.entry("stargatecontroller", "dhd"), Map.entry("naquadahore", "naquadah_ore"), Map.entry("naquadahblock", "naquadah_block"),
        Map.entry("sgpowerunit", "power_unit"), Map.entry("ic2powerunit", "power_unit"), Map.entry("rfpowerunit", "power_unit"),
        Map.entry("asgard_obelisk_celtic", "asgard_obelisk_knotwork"));

    private final boolean active = BuiltInRegistries.BLOCK.containsKey(Identifier.fromNamespaceAndPath(NS, "stargate_base"));
    final Report report = new Report();

    // ---- What the conversion needs to know, from setup()

    private String dimension = "minecraft:overworld";
    private int legacyDimension;
    // 3.5's dimension numbers as 4.0's worlds, and the dimension map 1.12's addresses count dimensions by
    private Map<Integer, String> worlds = Map.of();
    private List<Integer> dimensionMap = List.of();
    // 3.5's players by lowercased name (its usercache.json), and whether their offline-mode UUIDs get the same rights
    private Map<String, UUID> players = Map.of();
    private boolean offlineCopies;
    private @Nullable SgcraftAddresses addresses;

    // ---- Gathered while converting

    // 1.12 gate types by base, from the pre-pass over the regions (for the DHD made for each)
    private final Map<BlockPos, Integer> gateTypes = new HashMap<>();
    private final Map<BlockPos, CompoundTag> records = new LinkedHashMap<>();
    private final Map<BlockPos, BlockPos> dhdLinks = new LinkedHashMap<>();
    private final Map<BlockPos, BlockPos> zpmLinks = new LinkedHashMap<>();
    private final Set<Long> chunks = new LinkedHashSet<>();

    boolean active() {
        return this.active;
    }

    /**
     * @return the 3.5 dimension being converted, for the same-world addresses in its items
     */
    int legacyDimension() {
        return this.legacyDimension;
    }

    boolean handles(final String name) {
        return this.active && name.startsWith(NS + ":");
    }

    boolean handlesTileEntity(final String id) {
        return this.active && (id.startsWith("minecraft:gcewing.sg.") || id.startsWith(NS + ":tile_"));
    }

    /**
     * @param dimension the 4.0 world the chunks go to
     * @param legacyDimension its 3.5 dimension number
     * @param worlds 3.5's dimension numbers as 4.0's worlds
     * @param dimensionMap 1.12's address dimension map ({@code data/sgcraft-dimension_map.dat}): dimension numbers by index
     * @param players 3.5's players by lowercased name
     * @param addresses 26.3's address maker for the 4.0 server, or null when its layout can't be known (a secret layout)
     */
    void setup(final String dimension, final int legacyDimension, final Map<Integer, String> worlds, final List<Integer> dimensionMap,
        final Map<String, UUID> players, final boolean offlineCopies, final @Nullable SgcraftAddresses addresses) {
        this.dimension = dimension;
        this.legacyDimension = legacyDimension;
        this.worlds = worlds;
        this.dimensionMap = dimensionMap;
        this.players = players;
        this.offlineCopies = offlineCopies;
        this.addresses = addresses;
    }

    /**
     * Notes the gate bases of a 1.12 chunk before any is converted, so a DHD is made for its gate's type wherever the gate is.
     */
    void prescan(final CompoundTag chunk) {
        for (final Tag tag : chunk.getCompoundOrEmpty("Level").getListOrEmpty("TileEntities")) {
            if (tag instanceof final CompoundTag tileEntity && tileEntity.getStringOr("id", "").endsWith("gcewing.sg.sgbasete")) {
                this.gateTypes.put(at(tileEntity), tileEntity.getIntOr("gateType", 1));
            }
        }
    }

    /**
     * Notes a chunk converted, to mark it as having had its naquadah ore.
     */
    void chunk(final int chunkX, final int chunkZ) {
        if (this.active) {
            this.chunks.add((long) chunkX << 32 | chunkZ & 0xFFFFFFFFL);
        }
    }

    // ---- Blocks

    /**
     * @param tileEntity the block's 1.12 tile entity, if it had one
     * @return the 26.3 state for a 1.12 SGCraft block and its metadata
     */
    BlockState state(final String name, final int meta, final @Nullable CompoundTag tileEntity) {
        final String path = name.substring(NS.length() + 1);
        final CompoundTag te = tileEntity == null ? new CompoundTag() : tileEntity;
        final String flipped = opposite(TURN[meta & 3]);
        BlockState state = switch (path) {
            case "stargatering", "supergatering" -> {
                final String kind = path.startsWith("super") ? "supergate_" : "stargate_";
                yield set(set(block(kind + ((meta & 1) == 1 ? "chevron" : "ring")), "merged", String.valueOf(te.getBooleanOr("isMerged", false))),
                    "lit", "false");
            }
            case "stargatebase", "stargatebasepegasus", "stargatebaseuniverse", "supergatebase" -> {
                final int type = te.getIntOr("gateType", path.equals("stargatebasepegasus") ? 2 : path.equals("stargatebaseuniverse") ? 3 : 1);
                final String id = path.equals("supergatebase") ? "supergate_base" : switch (type) {
                    case 2 -> "stargate_base_pegasus";
                    case 3 -> "stargate_base_universe";
                    default -> "stargate_base";
                };
                yield set(set(block(id), "facing", flipped), "formed", String.valueOf(te.getBooleanOr("isMerged", false)));
            }
            case "stargatecontroller" -> {
                final BlockPos gate = new BlockPos(te.getIntOr("linkedX", 0), te.getIntOr("linkedY", 0), te.getIntOr("linkedZ", 0));
                final boolean pegasus = te.getBooleanOr("isLinkedToStargate", false) && this.gateTypes.getOrDefault(gate, 1) == 2;
                yield set(block(pegasus ? "dhd_pegasus" : "dhd"), "facing", flipped);
            }
            case "asgard_obelisk", "asgard_obelisk_celtic" -> set(set(block(path.equals("asgard_obelisk") ? "asgard_obelisk" : "asgard_obelisk_knotwork"),
                "facing", HORIZONTAL[meta & 3]), "half", (meta & 4) != 0 ? "upper" : "lower");
            case "zpm_console", "zpm_interface_cart" -> set(set(block(path), "facing", HORIZONTAL[meta & 3]), "loaded",
                String.valueOf(zpms(te) > 0));
            case "zpm_hub" -> set(set(block(path), "facing", HORIZONTAL[meta & 3]), "zpms", String.valueOf(Math.min(3, zpms(te))));
            case "ring_panel", "naquadah_generator_mk1", "naquadah_generator_mk2" -> set(block(path), "facing", HORIZONTAL[meta & 3]);
            case "event_horizon_light" -> Blocks.AIR.defaultBlockState();
            default -> {
                if (path.startsWith("gatespawner_")) {
                    // gatespawner_milkyway_stargate -> milky_way_stargate_spawner
                    yield block(path.substring("gatespawner_".length()).replace("milkyway", "milky_way") + "_spawner");
                }
                yield block(ITEMS.getOrDefault(path, path));
            }
        };
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = set(state, "waterlogged", "false");
        }
        this.report.blocks.merge(name + " -> " + (state.isAir() ? "air" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()), 1,
            Integer::sum);
        return state;
    }

    private static int zpms(final CompoundTag tileEntity) {
        int count = 0;
        for (final Tag tag : tileEntity.getListOrEmpty("inventory")) {
            if (tag instanceof final CompoundTag item && item.getStringOr("id", "").equals(NS + ":zpm") && item.getByteOr("Count", (byte) 0) > 0) {
                count++;
            }
        }
        return count;
    }

    private static BlockState block(final String path) {
        final Identifier id = Identifier.fromNamespaceAndPath(NS, path);
        return BuiltInRegistries.BLOCK.containsKey(id) ? BuiltInRegistries.BLOCK.getValue(id).defaultBlockState() : Blocks.AIR.defaultBlockState();
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

    private static String opposite(final String direction) {
        return switch (direction) {
            case "west" -> "east";
            case "east" -> "west";
            case "south" -> "north";
            default -> "south";
        };
    }

    // ---- Tile entities

    /**
     * The 26.3 block entity of a 1.12 SGCraft tile entity, its items through {@code legacyItem}; null for one 26.3 has none for (a ring
     * block's, an obelisk's top half) or that isn't converted. Gates, DHD links and ZPM links are gathered for {@link #finish}.
     *
     * @param block the 26.3 state put where it was
     */
    @Nullable CompoundTag blockEntity(final CompoundTag tileEntity, final BlockState block, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final String id = tileEntity.getStringOr("id", "");
        final String kind = id.substring(id.lastIndexOf('.') + 1).replace("sgcraft:", "");
        final CompoundTag out = switch (kind) {
            case "sgringte" -> null;
            case "sgbasete" -> this.base(tileEntity, block, legacyItem);
            case "dhdte" -> this.dhd(tileEntity, legacyItem);
            case "tile_asgard_obelisk" -> block.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && block.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).toString().equals("upper")
                ? null : this.obelisk(tileEntity);
            case "tile_ring_platform" -> this.ringPlatform(tileEntity, legacyItem);
            case "tile_cityshield" -> this.cityShield(tileEntity, legacyItem);
            case "tile_zpmconsole", "tile_zpminterfacecart", "tile_zpmmodulehub" -> this.zpmDevice(tileEntity, kind, legacyItem);
            case "ic2powerte", "sgpowerte" -> this.powerUnit(tileEntity, kind.equals("ic2powerte") ? EU_PER_GE : FE_PER_GE);
            default -> {
                this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                yield null;
            }
        };
        if (out != null) {
            this.report.blockEntities.merge(out.getStringOr("id", "?"), 1, Integer::sum);
        } else if (kind.equals("sgringte")) {
            this.report.blockEntities.merge("(ring tile entity, none in 26.3)", 1, Integer::sum);
        }
        return out;
    }

    private static CompoundTag newBlockEntity(final String id, final CompoundTag tileEntity) {
        final CompoundTag out = new CompoundTag();
        out.putString("id", NS + ":" + id);
        out.putInt("x", tileEntity.getIntOr("x", 0));
        out.putInt("y", tileEntity.getIntOr("y", 0));
        out.putInt("z", tileEntity.getIntOr("z", 0));
        return out;
    }

    private static BlockPos at(final CompoundTag tileEntity) {
        return new BlockPos(tileEntity.getIntOr("x", 0), tileEntity.getIntOr("y", 0), tileEntity.getIntOr("z", 0));
    }

    // 1.12's BlockPos.toLong: x 26 bits at 38, y 12 at 26, z 26 at 0
    private static BlockPos fromLong112(final long packed) {
        return new BlockPos((int) (packed >> 38), (int) (packed << 26 >> 52), (int) (packed << 38 >> 38));
    }

    private static Optional<UUID> creator(final CompoundTag tileEntity) {
        final CompoundTag creator = tileEntity.getCompoundOrEmpty("ForgeData").getCompoundOrEmpty("SpongeData").getCompoundOrEmpty("Creator");
        final long most = creator.getLongOr("UUIDMost", 0L), least = creator.getLongOr("UUIDLeast", 0L);
        return most == 0L && least == 0L ? Optional.empty() : Optional.of(new UUID(most, least));
    }

    private static CompoundTag globalPos(final String world, final BlockPos pos) {
        final CompoundTag out = new CompoundTag();
        out.putString("dimension", world);
        out.putIntArray("pos", new int[] {pos.getX(), pos.getY(), pos.getZ()});
        return out;
    }

    /**
     * 1.12 items in a list (each with its slot under {@code slotKey}) as a 26.3 container's {@code Items}, slots {@code from} to
     * {@code from + count - 1} becoming 0 to {@code count - 1}; {@code energy} sets a ZPM's energy (in 1.12's ZPM units) by its slot.
     */
    private ListTag items(final ListTag legacy, final String slotKey, final int from, final int count,
        final Function<CompoundTag, @Nullable CompoundTag> legacyItem, final Map<Integer, Double> energy) {
        final ListTag out = new ListTag();
        for (final Tag tag : legacy) {
            if (!(tag instanceof final CompoundTag item)) {
                continue;
            }
            final int slot = item.getIntOr(slotKey, item.getByteOr(slotKey, (byte) -1));
            final String id = item.getStringOr("id", "minecraft:air");
            if (slot < from || slot >= from + count || id.equals("minecraft:air") || item.getByteOr("Count", (byte) 0) <= 0) {
                continue;
            }
            final CompoundTag stack = item.copy();
            stack.remove("slot");
            stack.remove("Slot");
            final Double zpmEnergy = energy.get(slot);
            if (zpmEnergy != null && id.equals(NS + ":zpm")) {
                final CompoundTag itemTag = stack.getCompoundOrEmpty("tag");
                itemTag.putDouble("AvailableEnergy", zpmEnergy);
                itemTag.putBoolean("loadedIntoCart", false);
                stack.put("tag", itemTag);
            }
            final CompoundTag upgraded = legacyItem.apply(stack);
            if (upgraded != null) {
                upgraded.putByte("Slot", (byte) (slot - from));
                out.add(upgraded);
                this.report.containedItems++;
            }
        }
        return out;
    }

    // -- Gates

    private CompoundTag base(final CompoundTag te, final BlockState block, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag out = newBlockEntity("stargate_base", te);
        final boolean formed = te.getBooleanOr("isMerged", false);
        final int type = te.getIntOr("gateType", 1);
        final String typeKey = NS + ":" + switch (type) {
            case 2 -> "pegasus";
            case 3 -> "universe";
            case 4 -> "ori";
            default -> "milky_way";
        };
        final boolean chevronUpgrade = te.getBooleanOr("hasChevronUpgrade", false);
        final int chevrons = chevronUpgrade || type == 4 ? 9 : 7;
        // 1.12 gave back one chevron upgrade for the two chevrons it added; 26.3 one for each chevron past those included
        final int included = chevronUpgrade && type != 4 ? 8 : chevrons;
        final int radius = te.getIntOr("gateRadius", 0) > 0 ? te.getIntOr("gateRadius", 0) : 2;
        final String orientation = switch (te.getIntOr("gateOrientation", 1)) {
            case 2 -> "face_up";
            case 3 -> "face_down";
            default -> "standing";
        };
        out.putBoolean("formed", formed);
        out.putString("gate_type", typeKey);
        out.putInt("chevrons", chevrons);
        out.putInt("included_chevrons", included);
        out.putBoolean("type_fitted", false);
        out.putInt("radius", radius);
        out.putString("orientation", orientation);
        out.putBoolean("powered", te.getBooleanOr("redstoneInput", false));
        final ListTag inventory = te.getListOrEmpty("inventory");
        out.put("Items", this.items(inventory, "slot", 0, 5, legacyItem, Map.of()));
        final CompoundTag zpms = new CompoundTag();
        zpms.put("Items", this.items(inventory, "slot", 5, 3, legacyItem, Map.of()));
        out.put("zpms", zpms);
        if (!formed) {
            this.report.unformedGates++;
            return out;
        }

        // Its record in gates.dat: what 1.12 kept on the base that 26.3 keeps with the server
        final BlockPos pos = at(te);
        final CompoundTag record = new CompoundTag();
        record.put("position", globalPos(this.dimension, pos));
        record.putString("type", typeKey);
        record.putString("facing", block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            ? block.getValue(BlockStateProperties.HORIZONTAL_FACING).getSerializedName() : "north");
        record.putInt("chevrons", chevrons);
        record.putInt("radius", radius);
        record.putString("orientation", orientation);
        creator(te).ifPresent(owner -> record.putIntArray("owner", UUIDUtil.uuidToIntArray(owner)));
        final CompoundTag properties = this.gateProperties(te, type);
        final CompoundTag state = new CompoundTag();
        final double capacity = properties.contains(NS + ":energy_capacity") ? properties.getLongOr(NS + ":energy_capacity", 1000L) : 1000;
        final long energy = (long) Math.min(Math.max(0, te.getDoubleOr("energyInBuffer", 0)), capacity);
        if (energy > 0) {
            state.putLong("energy", energy);
        }
        if (te.getBooleanOr("hasIrisUpgrade", false)) {
            final int iris = te.getIntOr("irisState", 0);
            state.putString("iris_state", iris == 1 || iris == 2 ? "CLOSED" : "OPEN");
            state.putLong("iris_since", 0L);
        }
        if (!properties.isEmpty()) {
            record.put("properties", properties);
        }
        if (!state.isEmpty()) {
            record.put("state", state);
        }
        this.records.put(pos, record);
        for (final Tag tag : te.getListOrEmpty("linkedZpms")) {
            tag.asLong().ifPresent(device -> this.zpmLinks.put(fromLong112(device), pos));
        }
        return out;
    }

    /**
     * The gate's own property values, by 26.3's property ids: a 1.12 setting is carried over only where the gate's differed from 3.5's
     * configuration (its {@code stargate} section), so the rest follow 4.0's server settings.
     */
    private CompoundTag gateProperties(final CompoundTag te, final int type) {
        final CompoundTag properties = new CompoundTag();
        flag(properties, te, "oneWayTravel", true, "one_way_travel", false, true);
        flag(properties, te, "reverseWormholeKills", false, "reverse_travel_kills", false, false);
        flag(properties, te, "closeFromEitherEnd", true, "close_from_either_end", false, true);
        flag(properties, te, "preserveInventory", false, "preserve_inventory", false, false);
        flag(properties, te, "returnToPreviousIrisState", false, "iris_restores_state", false, false);
        flag(properties, te, "allowRedstoneInput", true, "redstone_input", false, true);
        flag(properties, te, "allowRedstoneOutput", true, "redstone_output", false, true);
        flag(properties, te, "transparency", true, "horizon_transparent", false, true);
        flag(properties, te, "transientDamage", true, "opening_damage", false, true);
        flag(properties, te, "useDHDFuelSource", true, "dhd_fuel", false, true);
        flag(properties, te, "displayGateAddress", true, "address_visible", false, true);
        // 1.12's DHD locked a chevron with each key pressed when this was on
        flag(properties, te, "chevronsLockOnDial", false, "chevrons_lock_as_dialed", false, false);
        flag(properties, te, "requiresNoPower", false, "requires_power", true, true);
        if (te.getBooleanOr("invulnerable", false)) {
            properties.putBoolean(NS + ":invulnerable", true);
        }
        if (te.getBooleanOr("isGenerated", false)) {
            properties.putBoolean(NS + ":generated", true);
        }
        final int seconds = te.getIntOr("secondsToStayOpen", 300);
        if (seconds != 300) {
            properties.putInt(NS + ":open_ticks", Math.max(0, seconds) * 20);
        }
        final double speed = te.getDoubleOr("ringRotationSpeed", 2.0);
        if (speed != (type == 2 ? 6.0 : 2.0)) {
            properties.putDouble(NS + ":ring_speed", Math.max(0.1, Math.min(360, speed)));
        }
        final double capacity = te.getDoubleOr("maxEnergyBuffer", 1000);
        if (capacity != 1000) {
            properties.putLong(NS + ":energy_capacity", (long) Math.max(0, capacity));
        }
        number(properties, te, "energyPerFuelItem", 96000, "energy_per_naquadah");
        number(properties, te, "distanceFactorMultiplier", 1.0, "distance_factor_multiplier");
        number(properties, te, "interDimensionMultiplier", 4.0, "inter_world_multiplier");
        if (te.getBooleanOr("hasIrisUpgrade", false)) {
            properties.putString(NS + ":iris", NS + (te.getBooleanOr("hasShield", false) ? ":shield" : ":metal"));
        }
        final boolean incoming = te.getBooleanOr("allowIncomingConnections", true), outgoing = te.getBooleanOr("allowOutgoingConnections", true);
        if (!(incoming && te.getBooleanOr("defaultAllowIncoming", true))) {
            properties.putBoolean(NS + ":allow_incoming", false);
        }
        if (!(outgoing && te.getBooleanOr("defaultAllowOutgoing", true))) {
            properties.putBoolean(NS + ":allow_outgoing", false);
        }

        // Who may do what: everyone's rights, each listed player's (by UUID), and rules for addresses. Everyone's are carried over only
        // where the gate's differed from 3.5's configuration (player-access: all three allowed), as everyone configuring every gate was
        // 1.12's default, not a choice; such gates get 26.3's (use and iris)
        final boolean use = te.getBooleanOr("defaultAllowGateAccess", true), iris = te.getBooleanOr("defaultAllowIrisAccess", true);
        final boolean admin = te.getBooleanOr("defaultAllowAdminAccess", true);
        final List<String> defaults = new ArrayList<>();
        if (use) {
            defaults.add(NS + ":use");
        }
        if (iris) {
            defaults.add(NS + ":iris");
        }
        if (admin) {
            defaults.add(NS + ":configure");
        }
        if (!(use && iris && admin) && !defaults.equals(List.of(NS + ":use", NS + ":iris"))) {
            properties.put(NS + ":default_rights", strings(defaults));
        }
        final CompoundTag playerRights = new CompoundTag();
        for (final Tag tag : te.getListOrEmpty("playeraccesslist")) {
            if (!(tag instanceof final CompoundTag entry)) {
                continue;
            }
            final String name = entry.getStringOr("player", "");
            final List<UUID> ids = new ArrayList<>();
            entry.getString("uuid").flatMap(SgcraftConversion::parseUuid).ifPresent(ids::add);
            if (ids.isEmpty()) {
                final UUID known = this.players.get(name.toLowerCase(Locale.ROOT));
                if (known == null) {
                    this.report.unknownPlayers.add(name);
                    continue;
                }
                ids.add(known);
            }
            if (this.offlineCopies && !name.isEmpty()) {
                ids.add(UUIDUtil.createOfflinePlayerUUID(this.properName(name)));
            }
            final List<String> rights = new ArrayList<>();
            if (entry.getBooleanOr("access", false)) {
                rights.add(NS + ":use");
            }
            if (entry.getBooleanOr("iris", false)) {
                rights.add(NS + ":iris");
            }
            if (entry.getBooleanOr("admin", false)) {
                rights.add(NS + ":configure");
            }
            ids.forEach(id -> playerRights.put(id.toString(), strings(rights)));
            this.report.playerRights++;
        }
        if (!playerRights.isEmpty()) {
            properties.put(NS + ":player_rights", playerRights);
        }
        final CompoundTag rules = new CompoundTag();
        for (final Tag tag : te.getListOrEmpty("gateaccesslist")) {
            if (tag instanceof final CompoundTag entry) {
                final Optional<String> address = this.address(entry.getStringOr("address", ""), this.legacyDimension);
                if (address.isEmpty()) {
                    this.report.unconvertedAddresses.add("rule " + entry.getStringOr("address", "") + " on gate " + at(te).toShortString());
                    continue;
                }
                final CompoundTag rule = new CompoundTag();
                rule.putBoolean("incoming", incoming && entry.getBooleanOr("iaccess", true));
                rule.putBoolean("outgoing", outgoing && entry.getBooleanOr("oaccess", true));
                rules.put(address.get(), rule);
                this.report.addressRules++;
            }
        }
        if (!rules.isEmpty()) {
            properties.put(NS + ":address_rules", rules);
        }
        return properties;
    }

    private static void flag(final CompoundTag properties, final CompoundTag te, final String key, final boolean configured, final String property,
        final boolean invert, final boolean default26) {
        final boolean value = te.getBooleanOr(key, configured);
        final boolean converted = invert != value;
        if (value != configured && converted != default26) {
            properties.putBoolean(NS + ":" + property, converted);
        }
    }

    private static void number(final CompoundTag properties, final CompoundTag te, final String key, final double configured, final String property) {
        final double value = te.getDoubleOr(key, configured);
        if (value != configured && Double.isFinite(value) && value >= 0) {
            properties.putDouble(NS + ":" + property, value);
        }
    }

    private static ListTag strings(final List<String> values) {
        final ListTag out = new ListTag();
        values.forEach(value -> out.add(StringTag.valueOf(value)));
        return out;
    }

    private static Optional<UUID> parseUuid(final String text) {
        try {
            return text.isEmpty() ? Optional.empty() : Optional.of(UUID.fromString(text));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // A name as its player wrote it (1.12 kept access lists lowercased), for their offline-mode UUID
    private String properName(final String name) {
        return this.report.properNames.getOrDefault(name.toLowerCase(Locale.ROOT), name);
    }

    void properNames(final Map<String, String> names) {
        this.report.properNames.putAll(names);
    }

    private CompoundTag dhd(final CompoundTag te, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag out = newBlockEntity("dhd", te);
        if (te.getBooleanOr("isLinkedToStargate", false)) {
            final BlockPos gate = new BlockPos(te.getIntOr("linkedX", 0), te.getIntOr("linkedY", 0), te.getIntOr("linkedZ", 0));
            out.putIntArray("linked", new int[] {gate.getX(), gate.getY(), gate.getZ()});
            this.dhdLinks.put(at(te), gate);
        }
        out.put("Items", this.items(te.getListOrEmpty("inventory"), "slot", 0, 4, legacyItem, Map.of()));
        out.putLong("buffer", (long) Math.max(0, te.getDoubleOr("energyInBuffer", 0)));
        return out;
    }

    // -- Transporters: obelisks and ring platforms, with their owner and access list

    private void transportAccess(final CompoundTag te, final CompoundTag out) {
        final long most = te.getLongOr("ownerMost", 0L), least = te.getLongOr("ownerLeast", 0L);
        if (most != 0L || least != 0L) {
            out.putIntArray("owner", UUIDUtil.uuidToIntArray(new UUID(most, least)));
        }
        out.putString("owner_name", te.getStringOr("ownerName", ""));
        out.putBoolean("everyone", te.getBooleanOr("allowEveryone", true));
        final ListTag access = new ListTag();
        for (final Tag tag : te.getListOrEmpty("access")) {
            if (tag instanceof final CompoundTag entry) {
                final CompoundTag converted = new CompoundTag();
                converted.putIntArray("player", UUIDUtil.uuidToIntArray(new UUID(entry.getLongOr("uuidMost", 0L), entry.getLongOr("uuidLeast", 0L))));
                converted.putString("name", entry.getStringOr("name", ""));
                converted.putBoolean("use", entry.getBooleanOr("use", true));
                converted.putBoolean("admin", entry.getBooleanOr("admin", false));
                access.add(converted);
            }
        }
        out.put("access", access);
    }

    private CompoundTag obelisk(final CompoundTag te) {
        final CompoundTag out = newBlockEntity("asgard_obelisk", te);
        if (te.getBooleanOr("linked", false)) {
            final String world = this.worlds.get(te.getIntOr("linkDimension", this.legacyDimension));
            if (world != null) {
                out.put("link", globalPos(world, new BlockPos(te.getIntOr("linkX", 0), te.getIntOr("linkY", 0), te.getIntOr("linkZ", 0))));
            } else {
                this.report.notes.add("obelisk " + at(te).toShortString() + ": its link's dimension " + te.getIntOr("linkDimension", 0)
                    + " has no 4.0 world; left unlinked");
            }
        }
        this.transportAccess(te, out);
        return out;
    }

    private CompoundTag ringPlatform(final CompoundTag te, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag out = newBlockEntity("ring_platform", te);
        out.put("Items", this.items(te.getListOrEmpty("Items"), "Slot", 0, 4, legacyItem, Map.of()));
        this.transportAccess(te, out);
        if (te.getBooleanOr("linked", false)) {
            final BlockPos link = fromLong112(te.getLongOr("linkPos", 0L));
            out.putIntArray("link", new int[] {link.getX(), link.getY(), link.getZ()});
        }
        out.putDouble("energy", Math.min(RING_CAPACITY, Math.max(0, te.getIntOr("energy", 0)) * RING_CAPACITY / RING_CAPACITY_112));
        out.putInt("run_ticks", -1);
        out.putBoolean("initiator", false);
        out.putLong("start_time", IDLE);
        out.putDouble("floor_y", te.getDoubleOr("floorY", te.getIntOr("y", 0) + 1));
        return out;
    }

    // -- City shield, ZPM devices, power units

    private CompoundTag cityShield(final CompoundTag te, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag out = newBlockEntity("city_shield_generator", te);
        out.putBoolean("active", te.getBooleanOr("active", false));
        out.putInt("width", te.getIntOr("width", 32));
        out.putInt("above", te.getIntOr("above", te.getIntOr("height", 24)));
        out.putInt("below", te.getIntOr("below", te.getIntOr("height", 8)));
        creator(te).ifPresent(owner -> out.putIntArray("owner", UUIDUtil.uuidToIntArray(owner)));
        out.putDouble("owed", 0);
        out.putInt("ticket", 0);
        out.put("Items", this.items(te.getListOrEmpty("Items"), "Slot", 0, 3, legacyItem, Map.of()));
        return out;
    }

    private CompoundTag zpmDevice(final CompoundTag te, final String kind, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag out = newBlockEntity("zpm_device", te);
        // A ZPM loaded into a 1.12 device kept its energy in the device; its item's was stale
        final Map<Integer, Double> energy = new HashMap<>();
        final int slots = kind.equals("tile_zpmmodulehub") ? 3 : 1;
        switch (kind) {
            case "tile_zpmconsole" -> {
                if (te.contains("capacity")) {
                    energy.put(0, (double) te.getIntOr("energy", 0));
                }
            }
            case "tile_zpminterfacecart" -> {
                if (te.contains("zpmEnergy")) {
                    energy.put(0, te.getDoubleOr("zpmEnergy", 0));
                }
            }
            default -> {
                for (int i = 0; i < 3; i++) {
                    if (te.contains("zpmSlot" + i + "Energy")) {
                        energy.put(i, te.getDoubleOr("zpmSlot" + i + "Energy", 0));
                    }
                }
            }
        }
        out.put("Items", this.items(te.getListOrEmpty("inventory"), "slot", 0, slots, legacyItem, energy));
        creator(te).ifPresent(owner -> out.putIntArray("owner", UUIDUtil.uuidToIntArray(owner)));
        if (te.getBooleanOr("zpmLinked", false)) {
            this.zpmLinks.put(at(te), fromLong112(te.getLongOr("zpmLinkGate", 0L)));
        }
        return out;
    }

    private CompoundTag powerUnit(final CompoundTag te, final double perGe) {
        final CompoundTag out = newBlockEntity("power_unit", te);
        final double energy = te.getDoubleOr("energyBuffer", 0) / perGe;
        if (energy > POWER_UNIT_CAPACITY) {
            this.report.notes.add("power unit " + at(te).toShortString() + ": " + Math.round(energy) + " gate energy, clamped to "
                + (long) POWER_UNIT_CAPACITY);
        }
        out.putDouble("energy", Math.max(0, Math.min(POWER_UNIT_CAPACITY, energy)));
        return out;
    }

    // ---- Items

    /**
     * Readies 1.12 SGCraft items anywhere in a 1.12 tile entity, entity or player for the game's upgrade, in place: each named as 26.3
     * names it (the ring blocks by their damage), and what 26.3 keeps as components (a PDD's addresses, a ZPM's and a power unit's energy,
     * a tainted ZPM) carried in its tag for {@link #postItems}.
     *
     * @param legacyDimension the 3.5 dimension a seven-symbol (same-world) address in a PDD is in
     */
    void preItems(final @Nullable Tag tag, final int legacyDimension) {
        if (!this.active || tag == null) {
            return;
        }
        if (tag instanceof final CompoundTag compound) {
            if (compound.getStringOr("id", "").startsWith(NS + ":") && compound.contains("Count")) {
                this.preItem(compound, legacyDimension);
            }
            for (final String key : compound.keySet()) {
                this.preItems(compound.get(key), legacyDimension);
            }
        } else if (tag instanceof final ListTag list) {
            list.forEach(child -> this.preItems(child, legacyDimension));
        }
    }

    void preItem(final CompoundTag item, final int legacyDimension) {
        final String path = item.getStringOr("id", "").substring(NS.length() + 1);
        final int damage = item.getShortOr("Damage", (short) 0);
        final CompoundTag tag = item.getCompoundOrEmpty("tag");
        final CompoundTag carried = tag.getCompoundOrEmpty(CARRIED);
        String renamed = switch (path) {
            case "stargatering" -> damage == 1 ? "stargate_chevron" : "stargate_ring";
            case "supergatering" -> damage == 1 ? "supergate_chevron" : "supergate_ring";
            default -> path.startsWith("gatespawner_")
                ? path.substring("gatespawner_".length()).replace("milkyway", "milky_way") + "_spawner"
                : ITEMS.getOrDefault(path, path);
        };
        if (path.equals("pdd") && tag.contains("addresses")) {
            this.pdd(tag, carried, legacyDimension);
        } else if (path.equals("zpm")) {
            tag.getDouble("AvailableEnergy").ifPresent(energy ->
                carried.putDouble(NS + ":zpm_energy", Math.max(0, Math.min(ZPM_CAPACITY, energy / ZPM_UNITS_PER_GE))));
            // A tainted ZPM was enchanted with Infinity (id 51), which showed in its tooltip
            final ListTag enchantments = tag.getListOrEmpty("ench");
            if (enchantments.removeIf(e -> e instanceof final CompoundTag enchantment && enchantment.getShortOr("id", (short) 0) == 51)) {
                carried.put(NS + ":zpm_tainted", new CompoundTag());
                if (enchantments.isEmpty()) {
                    tag.remove("ench");
                }
            }
        } else if ((path.equals("ic2powerunit") || path.equals("sgpowerunit")) && tag.contains("energyBuffer")) {
            final double energy = tag.getDoubleOr("energyBuffer", 0) / (path.equals("ic2powerunit") ? EU_PER_GE : FE_PER_GE);
            carried.putDouble(NS + ":energy", Math.max(0, Math.min(POWER_UNIT_CAPACITY, energy)));
        }
        // Only a name, lore and enchantments stay of 1.12's tag; the rest was SGCraft's own
        for (final String key : Set.copyOf(tag.keySet())) {
            if (!key.equals("display") && !key.equals("ench") && !key.equals("RepairCost")) {
                tag.remove(key);
            }
        }
        if (!carried.isEmpty()) {
            tag.put(CARRIED, carried);
        }
        if (tag.isEmpty()) {
            item.remove("tag");
        } else {
            item.put("tag", tag);
        }
        item.putString("id", NS + ":" + renamed);
        item.putShort("Damage", (short) 0);
        if (!BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath(NS, renamed))) {
            this.report.unmappedItems.merge(NS + ":" + path, 1, Integer::sum);
        } else if (!renamed.equals(path)) {
            this.report.renamedItems.merge(NS + ":" + path + (path.endsWith("ring") ? "@" + damage : "") + " -> " + renamed, 1, Integer::sum);
        }
    }

    // A PDD's 1.12 addresses (name, address, locked, index, ...) as 26.3's entries, each address made anew
    private void pdd(final CompoundTag tag, final CompoundTag carried, final int legacyDimension) {
        final List<CompoundTag> entries = new ArrayList<>();
        for (final Tag entry : tag.getListOrEmpty("addresses")) {
            if (entry instanceof final CompoundTag compound) {
                entries.add(compound);
            }
        }
        entries.sort(Comparator.comparingInt(entry -> entry.getIntOr("index", 0)));
        final ListTag converted = new ListTag();
        final ListTag locked = new ListTag();
        for (final CompoundTag entry : entries) {
            final String old = entry.getStringOr("address", "");
            final Optional<String> address = this.address(old, legacyDimension);
            final String kept;
            if (address.isPresent()) {
                kept = address.get();
                this.report.pddAddresses++;
            } else {
                // Kept as it was, so its holder sees the name: 1.12's addresses have no point of origin, so it reaches no gate in 26.3
                kept = old.replace(" ", "");
                this.report.unconvertedAddresses.add("PDD " + entry.getStringOr("name", "") + " " + old);
            }
            if (kept.replace("-", "").isEmpty()) {
                continue;
            }
            final CompoundTag out = new CompoundTag();
            out.putString("address", kept);
            out.putString("name", entry.getStringOr("name", kept));
            converted.add(out);
            if (entry.getBooleanOr("locked", false)) {
                locked.add(StringTag.valueOf(kept));
            }
        }
        carried.put(NS + ":addresses", converted);
        if (!locked.isEmpty()) {
            carried.put(NS + ":locked_addresses", locked);
        }
        this.report.pdds++;
    }

    /**
     * @return a 1.12 address as the 26.3 address that reaches the same chunk's cell from every world, or empty when it can't be made (a
     *     world 4.0 doesn't have, or hasn't given a symbol yet)
     */
    Optional<String> address(final String legacy, final int legacyDimension) {
        if (this.addresses == null) {
            return Optional.empty();
        }
        final Optional<SgcraftAddresses.Legacy> decoded = SgcraftAddresses.decode112(legacy);
        if (decoded.isEmpty()) {
            return Optional.empty();
        }
        final int index = decoded.get().dimensionIndex();
        final Integer dimension = index < 0 ? Integer.valueOf(legacyDimension) : index < this.dimensionMap.size() ? this.dimensionMap.get(index) : null;
        final String world = dimension == null ? null : this.worlds.get(dimension);
        return world == null ? Optional.empty() : this.addresses.lasting(world, decoded.get().chunkX(), decoded.get().chunkZ());
    }

    /**
     * Puts what {@link #preItems} carried in upgraded items' {@code minecraft:custom_data} into the components 26.3 keeps it as, anywhere
     * in an upgraded block entity, entity or player.
     */
    void postItems(final @Nullable Tag tag) {
        if (!this.active || tag == null) {
            return;
        }
        if (tag instanceof final CompoundTag compound) {
            if (compound.get("components") instanceof final CompoundTag components
                && components.get("minecraft:custom_data") instanceof final CompoundTag custom && custom.get(CARRIED) instanceof final CompoundTag carried) {
                for (final String key : carried.keySet()) {
                    components.put(key, carried.get(key).copy());
                }
                custom.remove(CARRIED);
                if (custom.isEmpty()) {
                    components.remove("minecraft:custom_data");
                }
            }
            for (final String key : compound.keySet()) {
                this.postItems(compound.get(key));
            }
        } else if (tag instanceof final ListTag list) {
            list.forEach(this::postItems);
        }
    }

    // ---- After every chunk: gates.dat, zpm_links.dat, naquadah_ore_chunks.dat

    /**
     * Writes what 26.3 keeps outside the chunks, merged into the 4.0 world's own files: the gates, the ZPM links and the chunks marked as
     * having had their naquadah ore. Each file is read from {@code source} (the 4.0 world; null for none) and written under {@code out} as
     * the world keeps it; a file replaced in {@code out} is first copied to {@code backup}, if given.
     */
    void finish(final @Nullable Path source, final Path out, final @Nullable Path backup, final Identifier dimension) throws IOException {
        if (!this.active) {
            return;
        }
        // A DHD further from its gate than 26.3's default reach keeps it with a longer reach on the gate
        this.dhdLinks.forEach((dhd, gate) -> {
            final CompoundTag record = this.records.get(gate);
            if (record == null) {
                this.report.notes.add("DHD " + dhd.toShortString() + ": its gate " + gate.toShortString() + " isn't among the converted gates");
                return;
            }
            final double distance = Math.sqrt(dhd.distSqr(gate));
            if (distance >= DHD_RANGE) {
                final CompoundTag properties = record.getCompoundOrEmpty("properties");
                final double range = Math.min(DHD_MAX_RANGE, Math.floor(distance) + 1);
                properties.putDouble(NS + ":dhd_range", range);
                record.put("properties", properties);
                this.report.notes.add("gate " + gate.toShortString() + ": dhd_range " + range + " for its DHD " + dhd.toShortString()
                    + " (" + String.format(Locale.ROOT, "%.2f", distance) + " away)" + (distance >= DHD_MAX_RANGE ? " - STILL OUT OF REACH" : ""));
            }
        });

        final String world = dimension.toString();
        final int version = SharedConstants.getCurrentVersion().dataVersion().version();

        // gates.dat: other worlds' gates and everything else kept, a converted gate replacing one recorded where it stands
        final String gatesFile = "data/sgcraft/gates.dat";
        final CompoundTag gates = read(source, gatesFile, version);
        final CompoundTag gatesData = gates.getCompoundOrEmpty("data");
        final ListTag gateList = gatesData.getListOrEmpty("gates");
        int replaced = 0;
        for (int i = gateList.size() - 1; i >= 0; i--) {
            if (gateList.get(i) instanceof final CompoundTag existing && this.ours(existing.getCompoundOrEmpty("position"), world, this.records.keySet())) {
                gateList.remove(i);
                replaced++;
            }
        }
        this.records.values().forEach(gateList::add);
        gatesData.put("gates", gateList);
        gates.put("data", gatesData);
        this.report.gatesWritten = this.records.size();
        this.report.gatesReplaced = replaced;
        this.validate("com.almuradev.sgcraft.mod.gate.GateStore", gatesData, gatesFile);
        this.validateProperties();
        write(gates, out, backup, gatesFile);

        // zpm_links.dat: a link per device, a converted device's replacing what it had
        final String linksFile = "data/sgcraft/zpm_links.dat";
        final CompoundTag links = read(source, linksFile, version);
        final ListTag linkList = links.getListOrEmpty("data");
        for (int i = linkList.size() - 1; i >= 0; i--) {
            if (linkList.get(i) instanceof final CompoundTag existing && this.ours(existing.getCompoundOrEmpty("device"), world, this.zpmLinks.keySet())) {
                linkList.remove(i);
            }
        }
        this.zpmLinks.forEach((device, gate) -> {
            final CompoundTag link = new CompoundTag();
            link.put("device", globalPos(world, device));
            link.put("gate", globalPos(world, gate));
            linkList.add(link);
        });
        links.put("data", linkList);
        this.report.zpmLinksWritten = this.zpmLinks.size();
        this.validate("com.almuradev.sgcraft.mod.zpm.ZpmLinks$Store", linkList, linksFile);
        write(links, out, backup, linksFile);

        // naquadah_ore_chunks.dat: a bit for each chunk, by region of 32 x 32
        final String oreFile = "dimensions/" + dimension.getNamespace() + "/" + dimension.getPath() + "/data/sgcraft/naquadah_ore_chunks.dat";
        final CompoundTag ore = read(source, oreFile, version);
        final CompoundTag oreData = ore.getCompoundOrEmpty("data");
        final Map<Long, long[]> regions = new TreeMap<>();
        for (final Tag tag : oreData.getListOrEmpty("regions")) {
            if (tag instanceof final CompoundTag region) {
                final long[] bits = region.getLongArray("chunks").orElse(new long[16]);
                if (bits.length == 16) {
                    regions.put((long) region.getIntOr("x", 0) << 32 | region.getIntOr("z", 0) & 0xFFFFFFFFL, bits.clone());
                }
            }
        }
        for (final long chunk : this.chunks) {
            final int x = (int) (chunk >> 32), z = (int) chunk;
            final long[] bits = regions.computeIfAbsent((long) (x >> 5) << 32 | (z >> 5) & 0xFFFFFFFFL, key -> new long[16]);
            final int index = (z & 31) * 32 + (x & 31);
            bits[index / 64] |= 1L << (index % 64);
        }
        final ListTag regionList = new ListTag();
        regions.forEach((key, bits) -> {
            final CompoundTag region = new CompoundTag();
            region.putInt("x", (int) (key >> 32));
            region.putInt("z", (int) (long) key);
            region.putLongArray("chunks", bits);
            regionList.add(region);
        });
        oreData.put("regions", regionList);
        ore.put("data", oreData);
        this.report.oreChunks = this.chunks.size();
        this.validate("com.almuradev.sgcraft.mod.worldgen.OreChunks", oreData, oreFile);
        write(ore, out, backup, oreFile);
    }

    private boolean ours(final CompoundTag position, final String world, final Set<BlockPos> positions) {
        final int[] pos = position.getIntArray("pos").orElse(new int[0]);
        return position.getStringOr("dimension", "").equals(world) && pos.length == 3 && positions.contains(new BlockPos(pos[0], pos[1], pos[2]));
    }

    private static CompoundTag read(final @Nullable Path source, final String file, final int version) throws IOException {
        if (source != null && Files.isRegularFile(source.resolve(file))) {
            return NbtIo.readCompressed(source.resolve(file), NbtAccounter.unlimitedHeap());
        }
        final CompoundTag fresh = new CompoundTag();
        fresh.putInt("DataVersion", version);
        return fresh;
    }

    private static void write(final CompoundTag tag, final Path out, final @Nullable Path backup, final String file) throws IOException {
        final Path target = out.resolve(file);
        Files.createDirectories(target.getParent());
        if (backup != null && Files.isRegularFile(target)) {
            final Path saved = backup.resolve("sgcraft").resolve(file);
            Files.createDirectories(saved.getParent());
            Files.copy(target, saved, StandardCopyOption.REPLACE_EXISTING);
        }
        NbtIo.writeCompressed(tag, target);
    }

    /**
     * Reads what was written back with SGCraft's own codec for the file (its saved-data type), so a mistake shows now rather than as the
     * server throws the file away.
     */
    private void validate(final String className, final Tag data, final String file) {
        try {
            final Class<?> type = Class.forName(className, true, this.sgcraftLoader());
            final java.lang.reflect.Field field = type.getDeclaredField("TYPE");
            field.setAccessible(true);
            final Object savedDataType = field.get(null);
            final Codec<?> codec = (Codec<?>) savedDataType.getClass().getMethod("codec").invoke(savedDataType);
            final DataResult<?> result = codec.parse(NbtOps.INSTANCE, data);
            this.report.checks.add(file + ": " + result.error().map(error -> "REFUSED by SGCraft: " + error.message()).orElse("read back by SGCraft's codec"));
        } catch (final ReflectiveOperationException | RuntimeException e) {
            this.report.checks.add(file + ": couldn't be checked with SGCraft's codec (" + e + ")");
        }
    }

    // Each gate property written read back with its own codec (GateProperties.all())
    private void validateProperties() {
        try {
            final Class<?> type = Class.forName("com.almuradev.sgcraft.api.gate.GateProperties", true, this.sgcraftLoader());
            final Map<?, ?> all = (Map<?, ?>) type.getMethod("all").invoke(null);
            final Map<String, Codec<?>> codecs = new HashMap<>();
            for (final Map.Entry<?, ?> entry : all.entrySet()) {
                codecs.put(entry.getKey().toString(), (Codec<?>) entry.getValue().getClass().getMethod("codec").invoke(entry.getValue()));
            }
            int checked = 0;
            for (final CompoundTag record : this.records.values()) {
                final CompoundTag properties = record.getCompoundOrEmpty("properties");
                for (final String key : properties.keySet()) {
                    final Codec<?> codec = codecs.get(key);
                    if (codec == null) {
                        this.report.checks.add("gate property " + key + ": SGCraft has none");
                        continue;
                    }
                    codec.parse(NbtOps.INSTANCE, properties.get(key)).error()
                        .ifPresent(error -> this.report.checks.add("gate property " + key + " REFUSED: " + error.message()));
                    checked++;
                }
            }
            this.report.checks.add("gate properties: " + checked + " read back by their SGCraft codecs");
        } catch (final ReflectiveOperationException | RuntimeException e) {
            this.report.checks.add("gate properties couldn't be checked (" + e + ")");
        }
    }

    private ClassLoader sgcraftLoader() {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(NS, "stargate_base")).getClass().getClassLoader();
    }

    /**
     * @return each converted gate with its new addresses, for the report
     */
    String gates() {
        final StringBuilder out = new StringBuilder();
        final Map<String, List<BlockPos>> cells = new TreeMap<>();
        for (final Map.Entry<BlockPos, CompoundTag> entry : this.records.entrySet()) {
            final BlockPos pos = entry.getKey();
            final CompoundTag record = entry.getValue();
            final int chunkX = pos.getX() >> 4, chunkZ = pos.getZ() >> 4;
            final String local = this.addresses == null ? "?" : this.addresses.local(this.dimension, chunkX, chunkZ).orElse("(off the grid)");
            final String lasting = this.addresses == null ? "?" : this.addresses.lasting(this.dimension, chunkX, chunkZ).orElse("(no world symbol)");
            cells.computeIfAbsent(local, key -> new ArrayList<>()).add(pos);
            out.append("\n  ").append(pos.toShortString()).append(' ').append(record.getStringOr("type", "")).append(' ')
                .append(record.getStringOr("facing", "")).append(" chevrons ").append(record.getIntOr("chevrons", 0))
                .append(" | 1.12 ").append(SgcraftAddresses.encode112(chunkX, chunkZ, Math.max(0, this.dimensionMap.indexOf(this.legacyDimension))))
                .append(" -> ").append(local).append(" here, ").append(lasting).append(" from anywhere | ").append(record.getCompoundOrEmpty("properties"))
                .append(' ').append(record.getCompoundOrEmpty("state"));
        }
        cells.forEach((address, positions) -> {
            if (positions.size() > 1) {
                out.append("\n  COLLISION: ").append(address).append(" is the address of ").append(positions);
            }
        });
        return out.toString();
    }

    /**
     * What the SGCraft conversion did.
     */
    static final class Report {

        int containedItems, unformedGates, pdds, pddAddresses, addressRules, playerRights, gatesWritten, gatesReplaced, zpmLinksWritten, oreChunks;
        final Map<String, Integer> blocks = new TreeMap<>(), blockEntities = new TreeMap<>(), droppedTileEntities = new TreeMap<>();
        final Map<String, Integer> renamedItems = new TreeMap<>(), unmappedItems = new TreeMap<>();
        final Set<String> unknownPlayers = new java.util.TreeSet<>(), unconvertedAddresses = new LinkedHashSet<>();
        final List<String> notes = new ArrayList<>(), checks = new ArrayList<>();
        final Map<String, String> properNames = new HashMap<>();

        @Override
        public String toString() {
            return "SGCraft blocks: " + this.blocks + "\nSGCraft block entities: " + this.blockEntities + " (items in them: " + this.containedItems
                + ", unformed gates: " + this.unformedGates + ")\nSGCraft tile entities left out: " + this.droppedTileEntities
                + "\nSGCraft items renamed: " + this.renamedItems + "\nSGCraft items with no 26.3 item (lost): " + this.unmappedItems
                + "\nPDDs: " + this.pdds + ", addresses made anew: " + this.pddAddresses + "; gate address rules: " + this.addressRules
                + ", player rights: " + this.playerRights + "\nAddresses not converted (kept as they were): " + this.unconvertedAddresses.size()
                + " " + this.unconvertedAddresses + "\nAccess-list players with no known UUID: " + this.unknownPlayers
                + "\ngates.dat: " + this.gatesWritten + " gates written, " + this.gatesReplaced + " records replaced; zpm_links.dat: "
                + this.zpmLinksWritten + " links; naquadah_ore_chunks.dat: " + this.oreChunks + " chunks marked"
                + "\nSGCraft notes: " + String.join("\n  ", this.notes) + "\nSGCraft checks: " + String.join("\n  ", this.checks);
        }
    }
}
