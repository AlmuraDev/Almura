/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.material.registry;

import com.almuradev.shared.registry.AbstractCatalogRegistryModule;
import com.almuradev.shared.registry.EagerCatalogRegistration;
import net.minecraft.block.material.MapColor;

@EagerCatalogRegistration
public class MapColorRegistryModule extends AbstractCatalogRegistryModule.Mapped<com.almuradev.almura.content.type.material.MapColor, MapColor> {

    public MapColorRegistryModule() {
        super(52);
    }

    @Override
    public void registerDefaults() {
        this.register("air", MapColor.AIR);
        this.register("grass", MapColor.GRASS);
        this.register("sand", MapColor.SAND);
        this.register("cloth", MapColor.CLOTH);
        this.register("tnt", MapColor.TNT);
        this.register("ice", MapColor.ICE);
        this.register("iron", MapColor.IRON);
        this.register("foliage", MapColor.FOLIAGE);
        this.register("snow", MapColor.SNOW);
        this.register("clay", MapColor.CLAY);
        this.register("dirt", MapColor.DIRT);
        this.register("stone", MapColor.STONE);
        this.register("water", MapColor.WATER);
        this.register("wood", MapColor.WOOD);
        this.register("quartz", MapColor.QUARTZ);
        this.register("adobe", MapColor.ADOBE);
        this.register("magenta", MapColor.MAGENTA);
        this.register("light_blue", MapColor.LIGHT_BLUE);
        this.register("yellow", MapColor.YELLOW);
        this.register("lime", MapColor.LIME);
        this.register("pink", MapColor.PINK);
        this.register("gray", MapColor.GRAY);
        this.register("silver", MapColor.SILVER);
        this.register("cyan", MapColor.CYAN);
        this.register("purple", MapColor.PURPLE);
        this.register("blue", MapColor.BLUE);
        this.register("brown", MapColor.BROWN);
        this.register("green", MapColor.GREEN);
        this.register("red", MapColor.RED);
        this.register("black", MapColor.BLACK);
        this.register("gold", MapColor.GOLD);
        this.register("diamond", MapColor.DIAMOND);
        this.register("lapis", MapColor.LAPIS);
        this.register("emerald", MapColor.EMERALD);
        this.register("obsidian", MapColor.OBSIDIAN);
        this.register("netherrack", MapColor.NETHERRACK);
        this.register("white_stained_hardened_clay", MapColor.WHITE_STAINED_HARDENED_CLAY);
        this.register("orange_stained_hardened_clay", MapColor.ORANGE_STAINED_HARDENED_CLAY);
        this.register("magenta_stained_hardened_clay", MapColor.MAGENTA_STAINED_HARDENED_CLAY);
        this.register("yellow_stained_hardened_clay", MapColor.YELLOW_STAINED_HARDENED_CLAY);
        this.register("lime_stained_hardened_clay", MapColor.LIME_STAINED_HARDENED_CLAY);
        this.register("pink_stained_hardened_clay", MapColor.PINK_STAINED_HARDENED_CLAY);
        this.register("gray_stained_hardened_clay", MapColor.GRAY_STAINED_HARDENED_CLAY);
        this.register("silver_stained_hardened_clay", MapColor.SILVER_STAINED_HARDENED_CLAY);
        this.register("cyan_stained_hardened_clay", MapColor.CYAN_STAINED_HARDENED_CLAY);
        this.register("purple_stained_hardened_clay", MapColor.PURPLE_STAINED_HARDENED_CLAY);
        this.register("blue_stained_hardened_clay", MapColor.BLUE_STAINED_HARDENED_CLAY);
        this.register("brown_stained_hardened_clay", MapColor.BROWN_STAINED_HARDENED_CLAY);
        this.register("green_stained_hardened_clay", MapColor.GREEN_STAINED_HARDENED_CLAY);
        this.register("red_stained_hardened_clay", MapColor.RED_STAINED_HARDENED_CLAY);
        this.register("black_stained_hardened_clay", MapColor.BLACK_STAINED_HARDENED_CLAY);
        this.register("light_blue_stained_hardened_clay", MapColor.LIGHT_BLUE_STAINED_HARDENED_CLAY);
    }

}
