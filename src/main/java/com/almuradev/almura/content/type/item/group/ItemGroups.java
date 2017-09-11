/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.group;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public final class ItemGroups {

    // SORTFIELDS:ON

    public static final ItemGroup BREWING = DummyObjectProvider.createFor(ItemGroup.class, "brewing");

    public static final ItemGroup BUILDING_BLOCKS = DummyObjectProvider.createFor(ItemGroup.class, "building_blocks");

    public static final ItemGroup COMBAT = DummyObjectProvider.createFor(ItemGroup.class, "combat");

    public static final ItemGroup DECORATIONS = DummyObjectProvider.createFor(ItemGroup.class, "decoration");

    public static final ItemGroup FOOD = DummyObjectProvider.createFor(ItemGroup.class, "food");

    public static final ItemGroup INVENTORY = DummyObjectProvider.createFor(ItemGroup.class, "inventory");

    public static final ItemGroup MATERIALS = DummyObjectProvider.createFor(ItemGroup.class, "materials");

    public static final ItemGroup MISC = DummyObjectProvider.createFor(ItemGroup.class, "misc");

    public static final ItemGroup REDSTONE = DummyObjectProvider.createFor(ItemGroup.class, "redstone");

    public static final ItemGroup SEARCH = DummyObjectProvider.createFor(ItemGroup.class, "search");

    public static final ItemGroup TOOLS = DummyObjectProvider.createFor(ItemGroup.class, "tools");

    public static final ItemGroup TRANSPORTATION = DummyObjectProvider.createFor(ItemGroup.class, "transportation");

    // SORTFIELDS:OFF

    private ItemGroups() {
    }
}
