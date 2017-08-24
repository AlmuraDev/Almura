/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.content.material.MapColor;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;

public class MapColorRegistryModule extends AbstractCatalogRegistryModule.Mapped<MapColor, net.minecraft.block.material.MapColor> {

    public MapColorRegistryModule() {
        super(52);
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.register("air", net.minecraft.block.material.MapColor.AIR);
        this.register("grass", net.minecraft.block.material.MapColor.GRASS);
        this.register("sand", net.minecraft.block.material.MapColor.SAND);
        this.register("cloth", net.minecraft.block.material.MapColor.CLOTH);
        this.register("tnt", net.minecraft.block.material.MapColor.TNT);
        this.register("ice", net.minecraft.block.material.MapColor.ICE);
        this.register("iron", net.minecraft.block.material.MapColor.IRON);
        this.register("foliage", net.minecraft.block.material.MapColor.FOLIAGE);
        this.register("snow", net.minecraft.block.material.MapColor.SNOW);
        this.register("clay", net.minecraft.block.material.MapColor.CLAY);
        this.register("dirt", net.minecraft.block.material.MapColor.DIRT);
        this.register("stone", net.minecraft.block.material.MapColor.STONE);
        this.register("water", net.minecraft.block.material.MapColor.WATER);
        this.register("wood", net.minecraft.block.material.MapColor.WOOD);
        this.register("quartz", net.minecraft.block.material.MapColor.QUARTZ);
        this.register("adobe", net.minecraft.block.material.MapColor.ADOBE);
        this.register("magenta", net.minecraft.block.material.MapColor.MAGENTA);
        this.register("light_blue", net.minecraft.block.material.MapColor.LIGHT_BLUE);
        this.register("yellow", net.minecraft.block.material.MapColor.YELLOW);
        this.register("lime", net.minecraft.block.material.MapColor.LIME);
        this.register("pink", net.minecraft.block.material.MapColor.PINK);
        this.register("gray", net.minecraft.block.material.MapColor.GRAY);
        this.register("silver", net.minecraft.block.material.MapColor.SILVER);
        this.register("cyan", net.minecraft.block.material.MapColor.CYAN);
        this.register("purple", net.minecraft.block.material.MapColor.PURPLE);
        this.register("blue", net.minecraft.block.material.MapColor.BLUE);
        this.register("brown", net.minecraft.block.material.MapColor.BROWN);
        this.register("green", net.minecraft.block.material.MapColor.GREEN);
        this.register("red", net.minecraft.block.material.MapColor.RED);
        this.register("black", net.minecraft.block.material.MapColor.BLACK);
        this.register("gold", net.minecraft.block.material.MapColor.GOLD);
        this.register("diamond", net.minecraft.block.material.MapColor.DIAMOND);
        this.register("lapis", net.minecraft.block.material.MapColor.LAPIS);
        this.register("emerald", net.minecraft.block.material.MapColor.EMERALD);
        this.register("obsidian", net.minecraft.block.material.MapColor.OBSIDIAN);
        this.register("netherrack", net.minecraft.block.material.MapColor.NETHERRACK);
        this.register("white_stained_hardened_clay", net.minecraft.block.material.MapColor.WHITE_STAINED_HARDENED_CLAY);
        this.register("orange_stained_hardened_clay", net.minecraft.block.material.MapColor.ORANGE_STAINED_HARDENED_CLAY);
        this.register("magenta_stained_hardened_clay", net.minecraft.block.material.MapColor.MAGENTA_STAINED_HARDENED_CLAY);
        this.register("yellow_stained_hardened_clay", net.minecraft.block.material.MapColor.YELLOW_STAINED_HARDENED_CLAY);
        this.register("lime_stained_hardened_clay", net.minecraft.block.material.MapColor.LIME_STAINED_HARDENED_CLAY);
        this.register("pink_stained_hardened_clay", net.minecraft.block.material.MapColor.PINK_STAINED_HARDENED_CLAY);
        this.register("gray_stained_hardened_clay", net.minecraft.block.material.MapColor.GRAY_STAINED_HARDENED_CLAY);
        this.register("silver_stained_hardened_clay", net.minecraft.block.material.MapColor.SILVER_STAINED_HARDENED_CLAY);
        this.register("cyan_stained_hardened_clay", net.minecraft.block.material.MapColor.CYAN_STAINED_HARDENED_CLAY);
        this.register("purple_stained_hardened_clay", net.minecraft.block.material.MapColor.PURPLE_STAINED_HARDENED_CLAY);
        this.register("blue_stained_hardened_clay", net.minecraft.block.material.MapColor.BLUE_STAINED_HARDENED_CLAY);
        this.register("brown_stained_hardened_clay", net.minecraft.block.material.MapColor.BROWN_STAINED_HARDENED_CLAY);
        this.register("green_stained_hardened_clay", net.minecraft.block.material.MapColor.GREEN_STAINED_HARDENED_CLAY);
        this.register("red_stained_hardened_clay", net.minecraft.block.material.MapColor.RED_STAINED_HARDENED_CLAY);
        this.register("black_stained_hardened_clay", net.minecraft.block.material.MapColor.BLACK_STAINED_HARDENED_CLAY);
        this.register("light_blue_stained_hardened_clay", net.minecraft.block.material.MapColor.LIGHT_BLUE_STAINED_HARDENED_CLAY);
    }

}
