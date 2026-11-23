/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Upgrades a 3.5 (1.12.2) chunk to 26.3, keeping Almura's blocks and TerrainControl's biomes, which 26.3's own upgrade cannot read.
 * <p>
 * 1.12 saved blocks as numbers, and the game's upgrade only knows vanilla's; Almura's become air. So first Almura's blocks (and other mods',
 * which 4.0 doesn't have) are taken out and remembered as 26.3 block states, by 3.5's registry in its level.dat; the game's own upgrade then
 * upgrades the vanilla rest, and Almura's are put back. TerrainControl's custom biomes (saved by their own number) are put back the same
 * way. Caches and storage crates are rewritten as 4.0 keeps them.
 */
final class LegacyWorldConverter {

    // 1.12's tile entities that are vanilla's; other mods' sometimes used minecraft: too, and are left out
    private static final Set<String> VANILLA_TILE_ENTITIES = Set.of(
        "furnace", "chest", "ender_chest", "jukebox", "dispenser", "dropper", "sign", "mob_spawner", "noteblock", "piston", "brewing_stand",
        "enchanting_table", "end_portal", "beacon", "skull", "daylight_detector", "hopper", "comparator", "flower_pot", "banner",
        "structure_block", "end_gateway", "command_block", "shulker_box", "bed",
        "Furnace", "Chest", "EnderChest", "RecordPlayer", "Trap", "Dropper", "Sign", "MobSpawner", "Music", "Piston", "Cauldron",
        "EnchantTable", "Airportal", "Beacon", "Skull", "DLDetector", "Hopper", "Comparator", "FlowerPot", "Banner", "Structure",
        "EndGateway", "Control");
    // 3.5's Almura blocks that 4.0 keeps under another folder
    private static final Map<String, String> RENAMED = Map.of(
        "almura:normal/building/juko_plank", "almura:horizontal/building/juko_plank",
        "almura:normal/crop/wheatbale", "almura:horizontal/crop/wheatbale",
        "almura:normal/crop/alfalfabale", "almura:horizontal/crop/alfalfabale",
        "almura:normal/crop/grassbale", "almura:horizontal/crop/grassbale",
        "almura:horizontal/flower/daffodil", "almura:flower/daffodil",
        // The lemon tree's log became the citrus trees' log
        "almura:log/lemon_tree_log", "almura:log/citrus_tree_log",
        // A half block the content no longer has: the nearest, a bottom slab of the same marble
        "almura:horizontal/building/brownmarblehalf", "almura:slab/brownmarbletile_slab");

    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATES = PalettedContainer.codecRW(BlockState.CODEC,
        Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY), Blocks.AIR.defaultBlockState());

    private final Map<Integer, String> blockNames;
    private final Map<Integer, String> customBiomes;
    private final ResourceKey<Level> dimension;
    private final DataFixer fixer = DataFixers.getDataFixer();
    private final int target = SharedConstants.getCurrentVersion().dataVersion().version();
    private final Map<String, BlockState> resolved = new HashMap<>();
    // Entities already converted, by UUID: 3.5's saves sometimes held one twice
    private final Set<UUID> entityIds = new HashSet<>();
    private final BlockheadsConversion blockheads = new BlockheadsConversion();
    final CfmConversion cfm = new CfmConversion();
    final Report report = new Report();
    private final IronBackpacksConversion ironBackpacks = new IronBackpacksConversion(this.report);
    private final BuildcraftConversion buildcraft = new BuildcraftConversion();
    private final IronChestConversion ironChests = new IronChestConversion();
    final SgcraftConversion sgcraft = new SgcraftConversion();

    /**
     * @param blockNames 3.5's block numbers, from its level.dat
     * @param customBiomes TerrainControl's custom biomes by the number 3.5 saved them with, as their 26.3 biome's id
     * @param dimension the 26.3 dimension the chunks go to
     */
    LegacyWorldConverter(final Map<Integer, String> blockNames, final Map<Integer, String> customBiomes, final Identifier dimension) {
        this.blockNames = blockNames;
        this.customBiomes = customBiomes;
        this.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
    }

    private record Placed(int x, int y, int z, BlockState state) {
    }

    private record Cache(int x, int y, int z, CompoundTag tag) {
    }

    CompoundTag convert(final CompoundTag chunk) {
        // A chunk last saved before 1.9 has no DataVersion: as vanilla, every fix since then applies (old entity names, item ids, signs)
        final int version = NbtUtils.getDataVersion(chunk, 99);
        final CompoundTag level = chunk.getCompoundOrEmpty("Level");
        final List<Placed> placed = new ArrayList<>();
        // Blocks whose shape 1.12 worked out from their neighbours each time it drew them (furniture connections), for the game to work
        // out again as the chunk first loads: x, y, z in the chunk
        final List<int[]> reshape = new ArrayList<>();
        final int chunkX = level.getIntOr("xPos", 0), chunkZ = level.getIntOr("zPos", 0);
        // Tile entities by where they are, for blocks whose 1.12 tile entity held some of their state (Blockheads' colours)
        final Map<Long, CompoundTag> tileEntityAt = new HashMap<>();
        for (final Tag tag : level.getListOrEmpty("TileEntities")) {
            if (tag instanceof final CompoundTag tileEntity) {
                tileEntityAt.put(pack(tileEntity.getIntOr("x", 0), tileEntity.getIntOr("y", 0), tileEntity.getIntOr("z", 0)), tileEntity);
                // SGCraft's items in any container named as 26.3 names them, for the game's upgrade (its own tile entities' come after)
                if (!this.sgcraft.handlesTileEntity(tileEntity.getStringOr("id", ""))) {
                    this.sgcraft.preItems(tileEntity, this.sgcraft.legacyDimension());
                }
            }
        }
        this.sgcraft.preItems(level.get("Entities"), this.sgcraft.legacyDimension());
        this.sgcraft.chunk(chunkX, chunkZ);

        // Almura's blocks (and other mods') out, remembered as 26.3 states
        for (final Tag sectionTag : level.getListOrEmpty("Sections")) {
            if (!(sectionTag instanceof final CompoundTag section)) {
                continue;
            }
            final int sectionY = section.getByteOr("Y", (byte) 0);
            final byte[] blocks = section.getByteArray("Blocks").orElse(null);
            final byte[] data = section.getByteArray("Data").orElse(null);
            if (blocks == null || data == null) {
                continue;
            }
            final byte @Nullable [] add = section.getByteArray("Add").orElse(null);
            for (int i = 0; i < 4096; i++) {
                final int id = (blocks[i] & 0xFF) | (add == null ? 0 : nibble(add, i) << 8);
                final String name = this.blockNames.get(id);
                if (name == null || name.startsWith("minecraft:")) {
                    continue;
                }
                final BlockState state = this.state(name, nibble(data, i),
                    tileEntityAt.get(pack(chunkX * 16 + (i & 15), sectionY * 16 + (i >> 8), chunkZ * 16 + ((i >> 4) & 15))));
                blocks[i] = 0;
                setNibble(data, i, 0);
                if (add != null) {
                    setNibble(add, i, 0);
                }
                if (!state.isAir()) {
                    placed.add(new Placed(i & 15, sectionY * 16 + (i >> 8), (i >> 4) & 15, state));
                    if (this.cfm.handles(name) && CfmConversion.refreshes(name)) {
                        reshape.add(new int[] {i & 15, sectionY * 16 + (i >> 8), (i >> 4) & 15});
                    }
                }
            }
            section.putByteArray("Blocks", blocks);
            section.putByteArray("Data", data);
            if (add != null) {
                section.putByteArray("Add", add);
            }
        }

        // Tile entities: vanilla's stay for the game's upgrade; caches are rewritten after; storage crates go through as chests
        final Map<Long, BlockState> placedAt = new HashMap<>();
        for (final Placed block : placed) {
            placedAt.put(pack(chunkX * 16 + block.x, block.y, chunkZ * 16 + block.z), block.state);
        }
        // Fridges stacked two high are one large fridge, as 1.12 drew them
        for (int i = 0; i < placed.size(); i++) {
            final Placed block = placed.get(i);
            if (BlockheadsConversion.isFridge(block.state)) {
                final int x = chunkX * 16 + block.x, z = chunkZ * 16 + block.z;
                final BlockState above = placedAt.get(pack(x, block.y + 1, z)), below = placedAt.get(pack(x, block.y - 1, z));
                final BlockState state = BlockheadsConversion.fridgeModel(block.state, above != null && BlockheadsConversion.isFridge(above),
                    below != null && BlockheadsConversion.isFridge(below));
                placed.set(i, new Placed(block.x, block.y, block.z, state));
                placedAt.put(pack(x, block.y, z), state);
            }
        }
        final Map<Long, CompoundTag> blockheadsAt = new HashMap<>();
        final Map<Long, CompoundTag> machinesAt = new HashMap<>();
        final Map<Long, CompoundTag> furnitureAt = new HashMap<>();
        // BuildCraft's tile entities, as 1.12 saved them (its port reads them so), for after the game's upgrade
        final List<CompoundTag> buildcraftTiles = new ArrayList<>();
        // Iron Chests' 1.12 tile entities by where they are, for their 26.3 block entity after the game's upgrade
        final Map<Long, CompoundTag> ironChestsAt = new HashMap<>();
        // SGCraft's 26.3 block entities, made from its 1.12 tile entities (their items through legacyItem), for after the game's upgrade
        final List<CompoundTag> sgcraftBlockEntities = new ArrayList<>();
        final List<Cache> caches = new ArrayList<>();
        final Map<Long, String> storage = new HashMap<>();
        final ListTag tileEntities = new ListTag();
        for (final Tag tag : level.getListOrEmpty("TileEntities")) {
            if (!(tag instanceof final CompoundTag tileEntity)) {
                continue;
            }
            final String id = tileEntity.getStringOr("id", "");
            final int x = tileEntity.getIntOr("x", 0), y = tileEntity.getIntOr("y", 0), z = tileEntity.getIntOr("z", 0);
            if (id.equals("almura:cache")) {
                caches.add(new Cache(x, y, z, this.cache(tileEntity)));
                this.report.tileEntities.merge("almura:cache", 1, Integer::sum);
            } else if (id.equals("almura:storage")) {
                // 3.5's block entity for every Almura container: 4.0's storage crates and dock chest have their own, the content's
                // containers (barrels) theirs
                final BlockState block = placedAt.get(pack(x, y, z));
                final String path = block == null ? "" : BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath();
                final String type = path.startsWith("storage/") ? "almura:storage_crate" : path.startsWith("container/") ? "almura:container" : null;
                if (type == null) {
                    this.report.droppedTileEntities.merge("almura:storage on " + (block == null ? "nothing" : path), 1, Integer::sum);
                    continue;
                }
                tileEntities.add(this.crateAsChest(tileEntity));
                storage.put(pack(x, y, z), type);
                this.report.tileEntities.merge("almura:storage as " + type, 1, Integer::sum);
            } else if (MACHINES.contains(id)) {
                // 3.5's machines each had their own block entity; 4.0's share one. Their items go through the game's upgrade as a chest's
                if (placedAt.containsKey(pack(x, y, z))) {
                    tileEntities.add(itemsAsChest(tileEntity, tileEntity.getCompoundOrEmpty("Inventory")));
                    machinesAt.put(pack(x, y, z), tileEntity);
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (this.blockheads.handles(id)) {
                if (placedAt.containsKey(pack(x, y, z))) {
                    tileEntities.add(BlockheadsConversion.asChest(tileEntity));
                    blockheadsAt.put(pack(x, y, z), tileEntity);
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (this.cfm.handlesTileEntity(id)) {
                // MrCrayfish's furniture: its items go through the game's upgrade as a chest's, then into 26.3's block entity
                final BlockState block = placedAt.get(pack(x, y, z));
                if (block != null && BuiltInRegistries.BLOCK.getKey(block.getBlock()).getNamespace().equals("cfm")) {
                    tileEntities.add(itemsAsChest(tileEntity, tileEntity));
                    furnitureAt.put(pack(x, y, z), tileEntity);
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (this.ironChests.handlesTileEntity(id)) {
                // Its items go through the game's upgrade as a chest's, then into 26.3's block entity
                final BlockState block = placedAt.get(pack(x, y, z));
                if (block != null && BuiltInRegistries.BLOCK.getKey(block.getBlock()).getNamespace().equals("ironchest")) {
                    tileEntities.add(itemsAsChest(tileEntity, tileEntity));
                    ironChestsAt.put(pack(x, y, z), tileEntity);
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (this.buildcraft.handlesTileEntity(id)) {
                final BlockState block = placedAt.get(pack(x, y, z));
                if (block != null && BuiltInRegistries.BLOCK.getKey(block.getBlock()).getNamespace().startsWith("buildcraft")) {
                    final CompoundTag kept = tileEntity.copy();
                    stripForge(kept);
                    // Its items as 26.3's now, so the port doesn't upgrade them as the chunk loads
                    BuildcraftConversion.upgradeItems(kept, this::legacyItem);
                    buildcraftTiles.add(kept);
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (this.sgcraft.handlesTileEntity(id)) {
                final BlockState block = placedAt.get(pack(x, y, z));
                if (block != null && BuiltInRegistries.BLOCK.getKey(block.getBlock()).getNamespace().equals("sgcraft")) {
                    final CompoundTag blockEntity = this.sgcraft.blockEntity(tileEntity, block, this::legacyItem);
                    if (blockEntity != null) {
                        sgcraftBlockEntities.add(blockEntity);
                    }
                    this.report.tileEntities.merge(id, 1, Integer::sum);
                } else {
                    this.report.droppedTileEntities.merge(id, 1, Integer::sum);
                }
            } else if (VANILLA_TILE_ENTITIES.contains(id.startsWith("minecraft:") ? id.substring(10) : id)) {
                stripForge(tileEntity);
                tileEntities.add(tileEntity);
            } else {
                this.report.droppedTileEntities.merge(id, 1, Integer::sum);
            }
        }
        level.put("TileEntities", tileEntities);

        // Entities: vanilla's, but not the falling blocks stuck in the air
        final ListTag entities = new ListTag();
        for (final Tag tag : level.getListOrEmpty("Entities")) {
            if (tag instanceof final CompoundTag entity) {
                final String id = entity.getStringOr("id", "");
                final UUID uuid = new UUID(entity.getLongOr("UUIDMost", 0L), entity.getLongOr("UUIDLeast", 0L));
                if (uuid.getMostSignificantBits() != 0L && !this.entityIds.add(uuid)) {
                    this.report.droppedEntities.merge(id + " (twice)", 1, Integer::sum);
                } else if ((id.startsWith("minecraft:") || isOldVanillaEntity(id)) && !id.equals("minecraft:falling_block")
                    && !id.equals("FallingSand")) {
                    stripForge(entity);
                    entities.add(entity);
                } else {
                    this.report.droppedEntities.merge(id, 1, Integer::sum);
                }
            }
        }
        level.put("Entities", entities);

        // TerrainControl's custom biomes, by column; the game's upgrade sees plains there
        final String[] columnBiomes = new String[256];
        final byte @Nullable [] biomes = level.getByteArray("Biomes").orElse(null);
        if (biomes != null && biomes.length == 256) {
            for (int i = 0; i < 256; i++) {
                final String custom = this.customBiomes.get(biomes[i] & 0xFF);
                if (custom != null) {
                    columnBiomes[i] = custom;
                    biomes[i] = 1;
                }
            }
            level.putByteArray("Biomes", biomes);
        }

        // The game's own upgrade
        SimpleRegionStorage.injectDatafixingContext(chunk, ChunkMap.getChunkDataFixContextTag(this.dimension,
            Optional.of(Identifier.fromNamespaceAndPath("terraincontrol", "terraincontrol"))));
        final CompoundTag upgraded = DataFixTypes.CHUNK.update(this.fixer, chunk, version, this.target);
        upgraded.remove("__context");
        NbtUtils.addDataVersion(upgraded, this.target);

        // Almura's blocks back
        final Map<Integer, CompoundTag> sections = new TreeMap<>();
        for (final Tag tag : upgraded.getListOrEmpty("sections")) {
            if (tag instanceof final CompoundTag section) {
                sections.put((int) section.getByteOr("Y", (byte) 0), section);
            }
        }
        final Map<Integer, PalettedContainer<BlockState>> containers = new HashMap<>();
        for (final Placed block : placed) {
            final CompoundTag section = sections.get(block.y >> 4);
            if (section == null) {
                this.report.unplaced++;
                continue;
            }
            final PalettedContainer<BlockState> container = containers.computeIfAbsent(block.y >> 4,
                y -> BLOCK_STATES.parse(NbtOps.INSTANCE, section.get("block_states")).getOrThrow());
            container.getAndSetUnchecked(block.x, block.y & 15, block.z, block.state);
            this.report.placed.merge(BuiltInRegistries.BLOCK.getKey(block.state.getBlock()).getNamespace(), 1, Integer::sum);
        }
        containers.forEach((y, container) -> sections.get(y).put("block_states", BLOCK_STATES.encodeStart(NbtOps.INSTANCE, container).getOrThrow()));

        // Crates and containers named as 4.0 names them; items of mods 4.0 doesn't have left out of every container
        final ListTag blockEntities = new ListTag();
        final Set<Long> furnished = new HashSet<>();
        for (final Tag upgradedTag : upgraded.getListOrEmpty("block_entities")) {
            Tag tag = upgradedTag;
            if (tag instanceof final CompoundTag chest) {
                final long at = pack(chest.getIntOr("x", 0), chest.getIntOr("y", 0), chest.getIntOr("z", 0));
                final CompoundTag furniture = furnitureAt.get(at);
                if (furniture != null) {
                    furnished.add(at);
                    tag = CfmConversion.finish(chest, furniture, placedAt.get(at),
                        new BlockPos(chest.getIntOr("x", 0), chest.getIntOr("y", 0), chest.getIntOr("z", 0)));
                    if (tag == null) {
                        continue;
                    }
                }
                final CompoundTag ironChest = ironChestsAt.get(at);
                if (ironChest != null) {
                    IronChestConversion.finish(chest, ironChest, placedAt.get(at));
                }
                final CompoundTag machine = machinesAt.get(at);
                if (machine != null) {
                    chest.putString("id", "almura:processing_machine");
                    chest.putInt("Progress", machine.getIntOr("Progress", 0));
                    chest.putInt("Selected", machine.getIntOr("Selected", 0));
                    chest.putBoolean("Started", machine.getBooleanOr("Started", false));
                }
                final CompoundTag legacy = blockheadsAt.get(at);
                if (legacy != null) {
                    tag = BlockheadsConversion.finish(chest, legacy, placedAt.get(at));
                    if (tag == null) {
                        continue;
                    }
                }
            }
            blockEntities.add(tag);
            if (tag instanceof final CompoundTag blockEntity) {
                final String type = storage.get(pack(blockEntity.getIntOr("x", 0), blockEntity.getIntOr("y", 0), blockEntity.getIntOr("z", 0)));
                if (type != null) {
                    blockEntity.putString("id", type);
                }
                this.ironBackpacks.convertAll(blockEntity, this::legacyItem);
                this.sgcraft.postItems(blockEntity);
                this.knownItemsOnly(blockEntity);
            }
        }
        for (final Cache cache : caches) {
            blockEntities.add(cache.tag);
        }
        blockEntities.addAll(sgcraftBlockEntities);
        // Furniture whose 1.12 block kept nothing gets its 26.3 block entity, so what ticks or draws from one has it from the start
        for (final Placed block : placed) {
            final int x = chunkX * 16 + block.x, z = chunkZ * 16 + block.z;
            if (BuiltInRegistries.BLOCK.getKey(block.state.getBlock()).getNamespace().equals("cfm") && !furnished.contains(pack(x, block.y, z))) {
                final CompoundTag blockEntity = CfmConversion.finish(null, null, block.state, new BlockPos(x, block.y, z));
                if (blockEntity != null) {
                    blockEntities.add(blockEntity);
                }
            }
        }
        blockEntities.forEach(LegacyWorldConverter::knownEnchantmentsOnly);
        // BuildCraft's as they were: the port upgrades their items and fluids itself as they load
        blockEntities.addAll(buildcraftTiles);

        // Entities: items on the ground of mods 4.0 doesn't have go, item frames holding one are emptied, and trades for them dropped
        final ListTag upgradedEntities = upgraded.getListOrEmpty("entities");
        upgradedEntities.forEach(tag -> this.ironBackpacks.convertAll(tag, this::legacyItem));
        upgradedEntities.forEach(this.sgcraft::postItems);
        upgradedEntities.removeIf(tag -> tag instanceof final CompoundTag entity && entity.getStringOr("id", "").equals("minecraft:item")
            && !this.known(entity.getCompoundOrEmpty("Item")));
        for (final Tag tag : upgradedEntities) {
            if (tag instanceof final CompoundTag entity) {
                if (entity.getCompound("Item").isPresent() && !this.known(entity.getCompoundOrEmpty("Item"))) {
                    entity.remove("Item");
                }
                entity.getCompound("Offers").ifPresent(offers -> offers.getList("Recipes").ifPresent(recipes -> recipes.removeIf(recipe ->
                    recipe instanceof final CompoundTag trade && (!this.known(trade.getCompoundOrEmpty("buy"))
                        || trade.getCompound("buyB").isPresent() && !this.known(trade.getCompoundOrEmpty("buyB"))
                        || !this.known(trade.getCompoundOrEmpty("sell"))))));
                knownEnchantmentsOnly(entity);
            }
        }
        upgraded.put("entities", upgradedEntities);
        upgraded.put("block_entities", blockEntities);

        // The furniture's shapes worked out again from their neighbours as the chunk first loads (the game's upgrade data, by section)
        if (!reshape.isEmpty()) {
            final CompoundTag upgradeData = upgraded.getCompoundOrEmpty("UpgradeData");
            final CompoundTag indices = upgradeData.getCompoundOrEmpty("Indices");
            final Map<Integer, List<Integer>> bySection = new TreeMap<>();
            for (final int[] at : reshape) {
                bySection.computeIfAbsent(at[1] >> 4, y -> new ArrayList<>()).add((at[1] & 15) << 8 | at[2] << 4 | at[0]);
            }
            bySection.forEach((section, added) -> {
                final String key = String.valueOf(section);
                final int[] existing = indices.getIntArray(key).orElse(new int[0]);
                final int[] merged = new int[existing.length + added.size()];
                System.arraycopy(existing, 0, merged, 0, existing.length);
                for (int i = 0; i < added.size(); i++) {
                    merged[existing.length + i] = added.get(i);
                }
                indices.putIntArray(key, merged);
            });
            upgradeData.put("Indices", indices);
            upgraded.put("UpgradeData", upgradeData);
            this.report.reshaped += reshape.size();
        }

        // TerrainControl's biomes back, where most of a 4x4 column is one
        this.placeBiomes(sections, columnBiomes);

        this.report.chunks++;
        this.report.statuses.merge(upgraded.getStringOr("Status", "?"), 1, Integer::sum);
        return upgraded;
    }

    // ---- Players

    // 1.12's respawn points (beds, per-dimension spawns) and what the player rode, which belong to 3.5's worlds
    private static final List<String> PLAYER_PLACES = List.of("SpawnX", "SpawnY", "SpawnZ", "SpawnForced", "SpawnDimension", "Spawns",
        "HasSpawnDimensionSet", "RootVehicle");

    /**
     * A 3.5 player (its playerdata file) as 26.3 keeps it, through the game's own upgrade: inventory, ender chest, experience, health and
     * hunger. Iron Backpacks' equipped backpack (a 1.12 capability) goes into the inventory, or else the ender chest. Items 4.0 doesn't
     * have are left out; 1.12's respawn points are dropped (they were in 3.5's worlds).
     *
     * @param dimension the 4.0 dimension the player stays where they were in, or null to put them at the spawn
     * @param spawnDimension the 4.0 world's spawn dimension
     * @param spawn the 4.0 world's spawn block
     */
    CompoundTag convertPlayer(final CompoundTag player, final @Nullable String dimension, final String spawnDimension, final int[] spawn) {
        final CompoundTag equipped = player.getCompoundOrEmpty("ForgeCaps").getCompoundOrEmpty("ironbackpacks:equipped_backpack").copy();
        if (!equipped.getStringOr("id", "minecraft:air").equals("minecraft:air") && equipped.getByteOr("Count", (byte) 0) > 0) {
            this.placeEquipped(player, equipped);
        }
        stripForge(player);
        PLAYER_PLACES.forEach(player::remove);
        this.sgcraft.preItems(player, player.getIntOr("Dimension", 0));

        final CompoundTag upgraded = DataFixTypes.PLAYER.update(this.fixer, player, NbtUtils.getDataVersion(player, 1343), this.target);
        this.sgcraft.postItems(upgraded);
        this.ironBackpacks.convertAll(upgraded, this::legacyItem);
        this.knownItemsOnly(upgraded.getList("Inventory"));
        this.knownItemsOnly(upgraded.getList("EnderItems"));
        upgraded.getCompound("equipment").ifPresent(equipment -> {
            for (final String slot : Set.copyOf(equipment.keySet())) {
                if (equipment.get(slot) instanceof final CompoundTag item && !this.known(item)) {
                    equipment.remove(slot);
                }
            }
        });
        knownEnchantmentsOnly(upgraded);

        if (dimension != null) {
            upgraded.putString("Dimension", dimension);
            this.report.playersInPlace++;
        } else {
            upgraded.putString("Dimension", spawnDimension);
            final ListTag pos = new ListTag();
            pos.add(DoubleTag.valueOf(spawn[0] + 0.5));
            pos.add(DoubleTag.valueOf(spawn[1]));
            pos.add(DoubleTag.valueOf(spawn[2] + 0.5));
            upgraded.put("Pos", pos);
            final ListTag motion = new ListTag();
            for (int i = 0; i < 3; i++) {
                motion.add(DoubleTag.valueOf(0.0));
            }
            upgraded.put("Motion", motion);
            upgraded.putDouble("fall_distance", 0.0);
            this.report.playersAtSpawn++;
        }
        NbtUtils.addDataVersion(upgraded, this.target);
        return upgraded;
    }

    /**
     * Puts 1.12's equipped backpack into the first empty slot of the player's inventory (0-35), else of their ender chest; with no room,
     * it is counted as lost.
     */
    private void placeEquipped(final CompoundTag player, final CompoundTag backpack) {
        for (final String list : List.of("Inventory", "EnderItems")) {
            final ListTag items = player.getListOrEmpty(list);
            final Set<Integer> used = new HashSet<>();
            for (final Tag tag : items) {
                if (tag instanceof final CompoundTag item) {
                    used.add((int) item.getByteOr("Slot", (byte) -1));
                }
            }
            final int size = list.equals("Inventory") ? 36 : 27;
            for (int slot = 0; slot < size; slot++) {
                if (!used.contains(slot)) {
                    backpack.putByte("Slot", (byte) slot);
                    items.add(backpack);
                    player.put(list, items);
                    this.report.equippedBackpacks++;
                    return;
                }
            }
        }
        this.report.lostItems.merge(backpack.getStringOr("id", "?") + " (equipped, no room)", 1, Integer::sum);
    }

    // 3.5's machines' block entities, by their 3.5 ids
    private static final Set<String> MACHINES = Set.of("almura:machine/seed_processor", "almura:machine/kitchen_slicer",
        "almura:machine/kitchen_grinder");

    /**
     * A 1.12 tile entity whose items Forge kept in an item handler ({@code Size}, {@code Items}), as a 1.12 chest, so the game's upgrade
     * upgrades the items.
     */
    static CompoundTag itemsAsChest(final CompoundTag tileEntity, final CompoundTag handler) {
        final CompoundTag chest = new CompoundTag();
        chest.putString("id", "minecraft:chest");
        chest.putInt("x", tileEntity.getIntOr("x", 0));
        chest.putInt("y", tileEntity.getIntOr("y", 0));
        chest.putInt("z", tileEntity.getIntOr("z", 0));
        final ListTag items = new ListTag();
        for (final Tag tag : handler.getListOrEmpty("Items")) {
            if (tag instanceof final CompoundTag slot) {
                final CompoundTag item = slot.copy();
                stripForge(item);
                item.putByte("Slot", (byte) slot.getIntOr("Slot", slot.getByteOr("Slot", (byte) 0)));
                items.add(item);
            }
        }
        chest.put("Items", items);
        return chest;
    }

    private static long pack(final int x, final int y, final int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }

    /**
     * Takes out what 1.12 Forge kept on tile entities, entities and items (capabilities, its own data), and its attributes, which 26.3
     * can't read.
     */
    static void stripForge(final CompoundTag tag) {
        tag.remove("ForgeCaps");
        tag.remove("ForgeData");
        final Optional<ListTag> attributes = tag.getList("Attributes");
        attributes.ifPresent(list -> list.removeIf(attribute -> attribute instanceof final CompoundTag compound
            && !compound.getStringOr("Name", "").startsWith("generic.") && !compound.getStringOr("Name", "").startsWith("horse.")
            && !compound.getStringOr("Name", "").startsWith("zombie.")));
        for (final String key : Set.copyOf(tag.keySet())) {
            final Tag child = tag.get(key);
            if (child instanceof final CompoundTag compound) {
                stripForge(compound);
            } else if (child instanceof final ListTag list) {
                for (final Tag entry : list) {
                    if (entry instanceof final CompoundTag compound) {
                        stripForge(compound);
                    }
                }
            }
        }
    }

    /**
     * @return whether 4.0 has the item; if not, it is counted as lost
     */
    private boolean known(final CompoundTag item) {
        final String id = item.getStringOr("id", "minecraft:air");
        final Identifier parsed = Identifier.tryParse(id);
        if (parsed != null && BuiltInRegistries.ITEM.containsKey(parsed)) {
            return true;
        }
        this.report.lostItems.merge(id, 1, Integer::sum);
        return false;
    }

    /**
     * Takes other mods' enchantments off items, anywhere in a block entity or entity: 1.12 saved enchantments by number, and the game's
     * upgrade only knows vanilla's, leaving others as "null", which keeps the whole item from loading.
     */
    private static void knownEnchantmentsOnly(final Tag tag) {
        if (tag instanceof final CompoundTag compound) {
            for (final String key : Set.copyOf(compound.keySet())) {
                final Tag child = compound.get(key);
                if ((key.equals("minecraft:enchantments") || key.equals("minecraft:stored_enchantments")) && child instanceof final CompoundTag levels) {
                    for (final String enchantment : Set.copyOf(levels.keySet())) {
                        if (Identifier.tryParse(enchantment) == null || enchantment.equals("null")) {
                            levels.remove(enchantment);
                        }
                    }
                } else {
                    knownEnchantmentsOnly(child);
                }
            }
        } else if (tag instanceof final ListTag list) {
            list.forEach(LegacyWorldConverter::knownEnchantmentsOnly);
        }
    }

    private void knownItemsOnly(final CompoundTag blockEntity) {
        // As the game keeps a container's, or under ItemHandler, as Blockheads' blocks keep theirs
        this.knownItemsOnly(blockEntity.getList("Items"));
        this.knownItemsOnly(blockEntity.getCompoundOrEmpty("ItemHandler").getList("Items"));
    }

    private void knownItemsOnly(final Optional<ListTag> list) {
        list.ifPresent(items -> items.removeIf(tag -> {
            if (tag instanceof final CompoundTag item) {
                final String id = item.getStringOr("id", "minecraft:air");
                final Identifier parsed = Identifier.tryParse(id);
                if (parsed == null || !BuiltInRegistries.ITEM.containsKey(parsed)) {
                    this.report.lostItems.merge(id, 1, Integer::sum);
                    return true;
                }
            }
            return false;
        }));
    }

    // ---- Blocks

    /**
     * @return the 26.3 state for a 3.5 block and its metadata: Almura's, by its name in 4.0; other mods' ores as stone, the rest as air
     */
    private BlockState state(final String name, final int meta, final @Nullable CompoundTag tileEntity) {
        if (this.sgcraft.handles(name)) {
            this.report.converted.merge(name, 1, Integer::sum);
            return this.sgcraft.state(name, meta, tileEntity);
        }
        if (this.cfm.handles(name)) {
            final BlockState state = this.cfm.state(name, meta, tileEntity);
            if (state == null) {
                this.report.unmapped.merge(name + " (not in the furniture port yet)", 1, Integer::sum);
                return Blocks.AIR.defaultBlockState();
            }
            this.report.converted.merge(name, 1, Integer::sum);
            return state;
        }
        if (this.blockheads.handles(name)) {
            final BlockState state = this.blockheads.state(name, meta, tileEntity);
            if (state == null) {
                this.report.unmapped.merge(name, 1, Integer::sum);
                return Blocks.AIR.defaultBlockState();
            }
            this.report.converted.merge(name, 1, Integer::sum);
            return state;
        }
        if (this.ironChests.handles(name)) {
            final BlockState state = this.ironChests.state(name, meta, tileEntity);
            if (state == null) {
                this.report.unmapped.merge(name + " (not in Iron Chests 26.3)", 1, Integer::sum);
                return Blocks.AIR.defaultBlockState();
            }
            this.report.converted.merge(name, 1, Integer::sum);
            return state;
        }
        if (this.buildcraft.handles(name)) {
            final BlockState state = this.buildcraft.state(name, meta);
            if (state == null) {
                this.report.unmapped.merge(name + " (not in the BuildCraft port)", 1, Integer::sum);
                return Blocks.AIR.defaultBlockState();
            }
            this.report.converted.merge(name, 1, Integer::sum);
            return state;
        }
        if (!name.startsWith("almura:")) {
            // By the block's own name, not its mod's (buildcraftcore isn't an ore)
            final boolean ore = name.substring(name.indexOf(':') + 1).contains("ore") || name.equals("ic2:resource");
            this.report.replaced.merge(name + (ore ? " -> stone" : " -> air"), 1, Integer::sum);
            return ore ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        final BlockState base = this.resolved.computeIfAbsent(name, this::almura);
        if (base.isAir()) {
            this.report.unmapped.merge(name, 1, Integer::sum);
            return base;
        }
        this.report.converted.merge(name, 1, Integer::sum);
        return withMeta(name, base, meta);
    }

    private BlockState almura(final String name) {
        final String renamed = RENAMED.getOrDefault(name, name);
        final Identifier id = Identifier.parse(renamed);
        if (BuiltInRegistries.BLOCK.containsKey(id)) {
            return BuiltInRegistries.BLOCK.getValue(id).defaultBlockState();
        }
        // 4.0 has no double slab blocks: a 1.12 double slab is its single slab's double state (see withMeta)
        if (renamed.endsWith("_double_slab")) {
            final Identifier single = Identifier.parse(renamed.substring(0, renamed.length() - "_double_slab".length()) + "_slab");
            if (BuiltInRegistries.BLOCK.containsKey(single)) {
                return BuiltInRegistries.BLOCK.getValue(single).defaultBlockState();
            }
        }
        // Moved to another folder: the one block of that name
        final String last = renamed.substring(renamed.lastIndexOf('/') + 1);
        Block found = null;
        for (final Identifier key : BuiltInRegistries.BLOCK.keySet()) {
            if (key.getNamespace().equals("almura") && key.getPath().endsWith("/" + last)) {
                if (found != null) {
                    return Blocks.AIR.defaultBlockState();
                }
                found = BuiltInRegistries.BLOCK.getValue(key);
            }
        }
        if (found != null) {
            this.report.guessed.put(name, BuiltInRegistries.BLOCK.getKey(found).toString());
            return found.defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    /**
     * 3.5's metadata as 4.0's properties, by what each kind of block kept in it.
     */
    private static BlockState withMeta(final String name, BlockState state, final int meta) {
        final String path = name.substring(name.indexOf(':') + 1);
        final Property<?> facing = state.getBlock().getStateDefinition().getProperty("facing");
        if (path.startsWith("stair/")) {
            // As vanilla's stairs: east, west, south, north, and the top half
            state = set(state, "facing", new String[] {"east", "west", "south", "north"}[meta & 3]);
            return set(state, "half", (meta & 4) != 0 ? "top" : "bottom");
        }
        if (path.startsWith("slab/")) {
            if (path.endsWith("_double_slab")) {
                return set(state, "type", "double");
            }
            return set(state, "type", meta == 0 ? "bottom" : "top");
        }
        if (path.startsWith("log/")) {
            return set(state, "axis", switch (meta & 12) {
                case 4 -> "x";
                case 8 -> "z";
                default -> "y";
            });
        }
        if (path.startsWith("leaf/")) {
            // Kept as they are: they don't decay
            return set(state, "persistent", "true");
        }
        if (path.startsWith("sapling/")) {
            return set(state, "stage", String.valueOf((meta >> 3) & 1));
        }
        if (path.startsWith("crop/")) {
            state = setAge(state, meta & 7);
            return set(state, "wet", (meta & 8) != 0 ? "true" : "false");
        }
        if (facing != null) {
            // Horizontal blocks, caches and crates: south, west, north, east
            return set(state, "facing", new String[] {"south", "west", "north", "east"}[meta & 3]);
        }
        return state;
    }

    private static BlockState setAge(final BlockState state, final int age) {
        final Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        if (property == null) {
            return state;
        }
        int clamped = age;
        int max = 0;
        for (final Object value : property.getPossibleValues()) {
            max = Math.max(max, (Integer) value);
        }
        clamped = Math.min(clamped, max);
        return set(state, "age", String.valueOf(clamped));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    /**
     * Vanilla's entity names before 1.11 ("Chicken", "MinecartChest"): no namespace, and not another mod's ("MoCreatures.Bear"). The
     * game's upgrade renames them, for chunks old enough to have them.
     */
    private static boolean isOldVanillaEntity(final String id) {
        return !id.isEmpty() && id.indexOf(':') < 0 && id.indexOf('.') < 0;
    }

    private static BlockState set(final BlockState state, final String name, final String value) {
        final Property property = state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) {
            return state;
        }
        final Optional<?> parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : state;
    }

    // ---- Caches and crates

    private CompoundTag cache(final CompoundTag tileEntity) {
        final CompoundTag out = new CompoundTag();
        out.putString("id", "almura:cache");
        out.putInt("x", tileEntity.getIntOr("x", 0));
        out.putInt("y", tileEntity.getIntOr("y", 0));
        out.putInt("z", tileEntity.getIntOr("z", 0));
        final ListTag stacks = new ListTag();
        final CompoundTag slot = tileEntity.getCompoundOrEmpty("ForgeCaps").getCompoundOrEmpty("almura:single_slot").getCompoundOrEmpty("Slot");
        final int count = slot.getIntOr("Count", 0);
        if (count > 0) {
            final CompoundTag item = this.item(slot);
            if (item != null) {
                final CompoundTag stack = new CompoundTag();
                final CompoundTag resource = new CompoundTag();
                resource.putString("id", item.getStringOr("id", "minecraft:air"));
                item.getCompound("components").ifPresent(components -> resource.put("components", components));
                stack.put("item", resource);
                stack.putInt("count", count);
                stacks.add(stack);
                this.report.cacheItems++;
            }
        }
        if (stacks.isEmpty()) {
            // An empty cache still has its one (empty) slot
            stacks.add(new CompoundTag());
        }
        out.put("stacks", stacks);
        return out;
    }

    /**
     * A 3.5 item as 26.3 keeps it, through the game's own upgrade; null when 4.0 has no such item.
     */
    private @Nullable CompoundTag item(final CompoundTag legacy) {
        final CompoundTag stack = new CompoundTag();
        stack.putString("id", legacy.getStringOr("id", "minecraft:air"));
        stack.putByte("Count", (byte) 1);
        stack.putShort("Damage", legacy.getShortOr("Damage", (short) 0));
        legacy.getCompound("tag").ifPresent(tag -> stack.put("tag", tag.copy()));
        this.sgcraft.preItems(stack, this.sgcraft.legacyDimension());
        final Tag upgraded = this.fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, (Tag) stack), 1343, this.target).getValue();
        if (!(upgraded instanceof final CompoundTag item) || !BuiltInRegistries.ITEM.containsKey(Identifier.parse(item.getStringOr("id", "minecraft:air")))) {
            this.report.lostItems.merge(legacy.getStringOr("id", "?"), 1, Integer::sum);
            return null;
        }
        this.sgcraft.postItems(item);
        return item;
    }

    /**
     * A 3.5 item kept inside another (in an Iron Backpack), with its count, as 26.3 keeps it, through the game's own upgrade; null (and
     * counted as lost) when 4.0 has no such item. A backpack in it is converted too.
     */
    @Nullable CompoundTag legacyItem(final CompoundTag legacy) {
        final CompoundTag stack = legacy.copy();
        stripForge(stack);
        this.sgcraft.preItems(stack, this.sgcraft.legacyDimension());
        final Tag upgraded = this.fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, (Tag) stack), 1343, this.target).getValue();
        if (!(upgraded instanceof final CompoundTag item)) {
            this.report.lostItems.merge(legacy.getStringOr("id", "?"), 1, Integer::sum);
            return null;
        }
        this.sgcraft.postItems(item);
        this.ironBackpacks.convertAll(item, this::legacyItem);
        if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(item.getStringOr("id", "minecraft:air")))) {
            this.report.lostItems.merge(legacy.getStringOr("id", "?"), 1, Integer::sum);
            return null;
        }
        return item;
    }

    /**
     * A storage crate as a 1.12 chest, so the game's upgrade upgrades its items; it is named a crate again after.
     */
    private CompoundTag crateAsChest(final CompoundTag tileEntity) {
        final CompoundTag chest = new CompoundTag();
        chest.putString("id", "minecraft:chest");
        chest.putInt("x", tileEntity.getIntOr("x", 0));
        chest.putInt("y", tileEntity.getIntOr("y", 0));
        chest.putInt("z", tileEntity.getIntOr("z", 0));
        final ListTag items = new ListTag();
        for (final Tag tag : tileEntity.getCompoundOrEmpty("ForgeCaps").getCompoundOrEmpty("almura:multi_slot").getListOrEmpty("Slots")) {
            if (tag instanceof final CompoundTag slot) {
                if (this.report.crateSample == null) {
                    this.report.crateSample = slot.toString();
                }
                final CompoundTag item = slot.copy();
                stripForge(item);
                item.putByte("Slot", (byte) slot.getIntOr("Slot", slot.getByteOr("Slot", (byte) 0)));
                items.add(item);
                this.report.crateItems++;
            }
        }
        chest.put("Items", items);
        return chest;
    }

    // ---- Biomes

    private void placeBiomes(final Map<Integer, CompoundTag> sections, final String[] columnBiomes) {
        final String[] cells = new String[16];
        boolean any = false;
        for (int cellX = 0; cellX < 4; cellX++) {
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                final Map<String, Integer> counts = new LinkedHashMap<>();
                for (int dx = 0; dx < 4; dx++) {
                    for (int dz = 0; dz < 4; dz++) {
                        final String biome = columnBiomes[(cellZ * 4 + dz) * 16 + cellX * 4 + dx];
                        if (biome != null) {
                            counts.merge(biome, 1, Integer::sum);
                        }
                    }
                }
                final int custom = counts.values().stream().mapToInt(Integer::intValue).sum();
                if (custom >= 8) {
                    cells[cellZ * 4 + cellX] = counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
                    any = true;
                }
            }
        }
        if (!any) {
            return;
        }
        for (final CompoundTag section : sections.values()) {
            final String[] values = readBiomes(section.getCompoundOrEmpty("biomes"));
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    for (int x = 0; x < 4; x++) {
                        if (cells[z * 4 + x] != null) {
                            values[(y << 2 | z) << 2 | x] = cells[z * 4 + x];
                            this.report.biomeCells++;
                        }
                    }
                }
            }
            section.put("biomes", writeBiomes(values));
        }
    }

    private static String[] readBiomes(final CompoundTag biomes) {
        final List<String> palette = new ArrayList<>();
        for (final Tag tag : biomes.getListOrEmpty("palette")) {
            palette.add(tag.asString().orElse("minecraft:plains"));
        }
        if (palette.isEmpty()) {
            palette.add("minecraft:plains");
        }
        final String[] values = new String[64];
        final int bits = bits(palette.size());
        final long[] data = biomes.getLongArray("data").orElse(new long[0]);
        for (int i = 0; i < 64; i++) {
            if (bits == 0) {
                values[i] = palette.get(0);
            } else {
                final int perLong = 64 / bits;
                final long word = data[i / perLong];
                values[i] = palette.get((int) ((word >>> ((i % perLong) * bits)) & ((1L << bits) - 1)));
            }
        }
        return values;
    }

    private static CompoundTag writeBiomes(final String[] values) {
        final List<String> palette = new ArrayList<>();
        for (final String value : values) {
            if (!palette.contains(value)) {
                palette.add(value);
            }
        }
        final CompoundTag out = new CompoundTag();
        final ListTag paletteTag = new ListTag();
        palette.forEach(name -> paletteTag.add(StringTag.valueOf(name)));
        out.put("palette", paletteTag);
        final int bits = bits(palette.size());
        if (bits > 0) {
            final int perLong = 64 / bits;
            final long[] data = new long[(64 + perLong - 1) / perLong];
            for (int i = 0; i < 64; i++) {
                data[i / perLong] |= (long) palette.indexOf(values[i]) << ((i % perLong) * bits);
            }
            out.put("data", new LongArrayTag(data));
        }
        return out;
    }

    private static int bits(final int paletteSize) {
        return paletteSize <= 1 ? 0 : 32 - Integer.numberOfLeadingZeros(paletteSize - 1);
    }

    // ---- 1.12's half-byte arrays

    private static int nibble(final byte[] array, final int index) {
        final int value = array[index >> 1];
        return (index & 1) == 0 ? value & 15 : (value >> 4) & 15;
    }

    private static void setNibble(final byte[] array, final int index, final int value) {
        final int i = index >> 1;
        array[i] = (byte) ((index & 1) == 0 ? (array[i] & 0xF0) | (value & 15) : (array[i] & 0x0F) | ((value & 15) << 4));
    }

    /**
     * What a conversion did.
     */
    static final class Report {

        int chunks, unplaced, cacheItems, crateItems, biomeCells, reshaped;
        // Iron Backpacks' items converted (IronBackpacksConversion)
        int backpacks, backpackUpgrades;
        // Players: kept where they were, put at the spawn, and 1.12 equipped backpacks moved into their inventory
        int playersInPlace, playersAtSpawn, equippedBackpacks;
        @Nullable String crateSample;
        final Map<String, Integer> converted = new TreeMap<>(), unmapped = new TreeMap<>(), replaced = new TreeMap<>();
        final Map<String, Integer> placed = new TreeMap<>(), tileEntities = new TreeMap<>(), droppedTileEntities = new TreeMap<>();
        final Map<String, Integer> droppedEntities = new TreeMap<>(), lostItems = new TreeMap<>(), statuses = new TreeMap<>();
        final Map<String, String> guessed = new TreeMap<>();

        /**
         * The report of a players conversion (the chunk counts mean nothing there).
         */
        String players() {
            return "Players kept where they were: " + this.playersInPlace + ", put at the spawn: " + this.playersAtSpawn
                + "\nIron Backpacks converted: " + this.backpacks + " backpacks, " + this.backpackUpgrades + " upgrades"
                + ", equipped moved into players' inventories: " + this.equippedBackpacks
                + "\nItems lost (4.0 has none): " + this.lostItems;
        }

        @Override
        public String toString() {
            final StringBuilder out = new StringBuilder();
            out.append("Chunks: ").append(this.chunks).append("\nStatuses: ").append(this.statuses)
                .append("\nAlmura block kinds converted: ").append(this.converted.size())
                .append(" (").append(this.converted.values().stream().mapToInt(Integer::intValue).sum()).append(" blocks)")
                .append("\nPlaced back by namespace: ").append(this.placed).append(" (not placed: ").append(this.unplaced).append(')')
                .append("\nGuessed renames: ").append(this.guessed)
                .append("\nUnmapped Almura blocks (now air): ").append(this.unmapped)
                .append("\nOther mods' blocks replaced: ").append(this.replaced)
                .append("\nAlmura tile entities: ").append(this.tileEntities).append(", cache items kept: ").append(this.cacheItems)
                .append(", crate stacks: ").append(this.crateItems).append(", crate sample: ").append(this.crateSample)
                .append("\nIron Backpacks converted: ").append(this.backpacks).append(" backpacks, ").append(this.backpackUpgrades).append(" upgrades")
                .append(", equipped moved into players' inventories: ").append(this.equippedBackpacks)
                .append("\nItems lost (4.0 has none): ").append(this.lostItems)
                .append("\nTile entities left out: ").append(this.droppedTileEntities)
                .append("\nEntities left out: ").append(this.droppedEntities)
                .append("\nCustom biome cells: ").append(this.biomeCells)
                .append("\nFurniture reshaped from its neighbours on first load: ").append(this.reshaped);
            return out.toString();
        }
    }
}
