/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.creativetab;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public final class CreativeTabs {

    // SORTFIELDS:ON

    public static final CreativeTab BREWING = DummyObjectProvider.createFor(CreativeTab.class, "brewing");

    public static final CreativeTab BUILDING_BLOCKS = DummyObjectProvider.createFor(CreativeTab.class, "building_blocks");

    public static final CreativeTab COMBAT = DummyObjectProvider.createFor(CreativeTab.class, "combat");

    public static final CreativeTab DECORATIONS = DummyObjectProvider.createFor(CreativeTab.class, "decoration");

    public static final CreativeTab FOOD = DummyObjectProvider.createFor(CreativeTab.class, "food");

    public static final CreativeTab INVENTORY = DummyObjectProvider.createFor(CreativeTab.class, "inventory");

    public static final CreativeTab MATERIALS = DummyObjectProvider.createFor(CreativeTab.class, "materials");

    public static final CreativeTab MISC = DummyObjectProvider.createFor(CreativeTab.class, "misc");

    public static final CreativeTab REDSTONE = DummyObjectProvider.createFor(CreativeTab.class, "redstone");

    public static final CreativeTab SEARCH = DummyObjectProvider.createFor(CreativeTab.class, "search");

    public static final CreativeTab TOOLS = DummyObjectProvider.createFor(CreativeTab.class, "tools");

    public static final CreativeTab TRANSPORTATION = DummyObjectProvider.createFor(CreativeTab.class, "transportation");

    // SORTFIELDS:OFF

    private CreativeTabs() {
    }
}
