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
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Iron Backpacks (3.0.9 on 3.5) items, to its 26.3 port, when it is loaded.
 * <p>
 * 1.12 had one backpack item ({@code ironbackpacks:backpack}) and one upgrade item ({@code ironbackpacks:upgrade}), told apart by their
 * NBT, which the game's upgrade keeps as custom data: a backpack's {@code packInfo} (its tier {@code type}, specialty {@code spec}, owner
 * {@code own}, colour {@code color} and the ids of its upgrades {@code upgrade}) and {@code packInv} (its items, in 1.12's form, one per
 * slot), an upgrade's {@code upgrade} id. 26.3 has an item for each tier and specialty ({@code backpack_iron_storage}) and for each upgrade
 * ({@code upgrade_lock}, {@code upgrade_blank}); a backpack keeps its items, upgrades and owner as Iron Backpacks' components and its
 * colour as vanilla's dyed colour. The items in a backpack go through the game's upgrade one by one (the converter's), so what 4.0 doesn't
 * have is lost as anywhere else.
 */
final class IronBackpacksConversion {

    private static final String NAMESPACE = "ironbackpacks";
    private static final String BACKPACK = NAMESPACE + ":backpack";
    private static final String UPGRADE = NAMESPACE + ":upgrade";
    private static final Set<String> TIERS = Set.of("basic", "iron", "gold", "diamond");
    private static final Set<String> UPGRADES = Set.of("damage_bar", "lock", "extra_upgrade", "everlasting");
    private static final Map<String, String> SPECIALTIES = Map.of("STORAGE", "storage", "UPGRADE", "upgrade");

    private final boolean active = BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath(NAMESPACE, "backpack_basic"));
    private final LegacyWorldConverter.Report report;

    IronBackpacksConversion(final LegacyWorldConverter.Report report) {
        this.report = report;
    }

    /**
     * Turns every 1.12 backpack and upgrade anywhere in the (upgraded) tag into its 26.3 item, in place.
     *
     * @param legacyItem a 1.12 item (as a backpack kept it) as 26.3 keeps it, or null when 4.0 has no such item
     */
    void convertAll(final Tag tag, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        if (!this.active) {
            return;
        }
        if (tag instanceof final CompoundTag compound) {
            final String id = compound.getStringOr("id", "");
            if (id.equals(BACKPACK) || id.equals(UPGRADE)) {
                this.convert(compound, legacyItem);
                return;
            }
            for (final String key : Set.copyOf(compound.keySet())) {
                this.convertAll(compound.get(key), legacyItem);
            }
        } else if (tag instanceof final ListTag list) {
            for (final Tag entry : list) {
                this.convertAll(entry, legacyItem);
            }
        }
    }

    /**
     * A 1.12 backpack or upgrade (after the game's upgrade) as its 26.3 item, in place; one 26.3 doesn't have (a torn backpack, an
     * upgrade turned off) keeps its 1.12 id, so it is counted as lost.
     */
    void convert(final CompoundTag item, final Function<CompoundTag, @Nullable CompoundTag> legacyItem) {
        final CompoundTag components = item.getCompoundOrEmpty("components");
        final CompoundTag custom = components.getCompoundOrEmpty("minecraft:custom_data");
        final String target;
        if (item.getStringOr("id", "").equals(UPGRADE)) {
            final String upgrade = custom.getStringOr("upgrade", NAMESPACE + ":null");
            custom.remove("upgrade");
            target = upgrade.equals(NAMESPACE + ":null") ? "upgrade_blank" : "upgrade_" + path(upgrade);
            this.report.backpackUpgrades++;
        } else {
            final CompoundTag info = custom.getCompoundOrEmpty("packInfo");
            final String tier = path(info.getStringOr("type", ""));
            final String specialty = SPECIALTIES.get(info.getStringOr("spec", ""));
            if (!TIERS.contains(tier) || !tier.equals("basic") && specialty == null) {
                return;
            }
            target = tier.equals("basic") ? "backpack_basic" : "backpack_" + tier + "_" + specialty;

            // Its items, slot by slot
            final ListTag contents = new ListTag();
            final ListTag inventory = custom.getListOrEmpty("packInv");
            for (int slot = 0; slot < inventory.size() && slot < 256; slot++) {
                if (!(inventory.get(slot) instanceof final CompoundTag legacy) || legacy.getStringOr("id", "minecraft:air").equals("minecraft:air")
                    || legacy.getByteOr("Count", (byte) 0) <= 0) {
                    continue;
                }
                final CompoundTag converted = legacyItem.apply(legacy);
                if (converted != null) {
                    converted.remove("Slot");
                    final CompoundTag entry = new CompoundTag();
                    entry.putInt("slot", slot);
                    entry.put("item", converted);
                    contents.add(entry);
                }
            }
            if (!contents.isEmpty()) {
                components.put(NAMESPACE + ":contents", contents);
            }

            final ListTag upgrades = new ListTag();
            for (final Tag upgrade : info.getListOrEmpty("upgrade")) {
                final String id = upgrade.asString().orElse("");
                if (id.startsWith(NAMESPACE + ":") && UPGRADES.contains(path(id))) {
                    upgrades.add(StringTag.valueOf(id));
                }
            }
            if (!upgrades.isEmpty()) {
                components.put(NAMESPACE + ":upgrades", upgrades);
            }

            info.getString("own").ifPresent(owner -> {
                try {
                    final UUID uuid = UUID.fromString(owner);
                    final long most = uuid.getMostSignificantBits(), least = uuid.getLeastSignificantBits();
                    components.put(NAMESPACE + ":owner", new IntArrayTag(new int[] {(int) (most >> 32), (int) most, (int) (least >> 32), (int) least}));
                } catch (final IllegalArgumentException ignored) {
                    // Not a UUID: the next to open it owns it
                }
            });
            final int color = info.getIntOr("color", -1);
            if (color != -1) {
                components.putInt("minecraft:dyed_color", color & 0xFFFFFF);
            }
            custom.remove("packInfo");
            custom.remove("packInv");
            this.report.backpacks++;
        }

        if (custom.isEmpty()) {
            components.remove("minecraft:custom_data");
        } else {
            components.put("minecraft:custom_data", custom);
        }
        if (components.isEmpty()) {
            item.remove("components");
        } else {
            item.put("components", components);
        }
        item.putString("id", NAMESPACE + ":" + target);
    }

    private static String path(final String id) {
        return id.substring(id.indexOf(':') + 1);
    }
}
