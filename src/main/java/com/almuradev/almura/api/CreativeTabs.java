/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public final class CreativeTabs {

    // TODO Set the sorting things

    public static final CreativeTab BUILDING_BLOCKS = DummyObjectProvider.createFor(CreativeTab.class, "building_blocks");

    public static final CreativeTab DECORATIONS = DummyObjectProvider.createFor(CreativeTab.class, "decoration");

    public static final CreativeTab REDSTONE = DummyObjectProvider.createFor(CreativeTab.class, "redstone");

    public static final CreativeTab TRANSPORTATION = DummyObjectProvider.createFor(CreativeTab.class, "transportation");

    public static final CreativeTab MISC = DummyObjectProvider.createFor(CreativeTab.class, "misc");

    public static final CreativeTab FOOD = DummyObjectProvider.createFor(CreativeTab.class, "food");

    public static final CreativeTab TOOLS = DummyObjectProvider.createFor(CreativeTab.class, "tools");

    public static final CreativeTab COMBAT = DummyObjectProvider.createFor(CreativeTab.class, "combat");

    public static final CreativeTab BREWING = DummyObjectProvider.createFor(CreativeTab.class, "brewing");

    public static final CreativeTab MATERIALS = DummyObjectProvider.createFor(CreativeTab.class, "materials");

    private CreativeTabs() {
    }
}
