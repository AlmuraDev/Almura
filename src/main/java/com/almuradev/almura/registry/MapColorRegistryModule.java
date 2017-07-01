/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.content.material.MapColor;
import com.almuradev.almura.content.material.MapColors;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MapColorRegistryModule implements CatalogRegistryModule<MapColor>, RegistryHelper {

    @RegisterCatalog(MapColors.class)
    public final Map<String, MapColor> map = new HashMap<>(52);

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
        this.register("white_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193561_M);
        this.register("orange_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193562_N);
        this.register("magenta_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193563_O);
        this.register("yellow_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193564_P);
        this.register("lime_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193565_Q);
        this.register("pink_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193566_R);
        this.register("gray_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193567_S);
        this.register("silver_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193568_T);
        this.register("cyan_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193569_U);
        this.register("purple_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193570_V);
        this.register("blue_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193571_W);
        this.register("brown_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193572_X);
        this.register("green_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193573_Y);
        this.register("red_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193574_Z);
        this.register("black_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193559_aa);
        this.register("light_blue_stained_hardened_clay", net.minecraft.block.material.MapColor.field_193560_ab);
    }

    private void register(String id, final net.minecraft.block.material.MapColor color) {
        this.registerSetId(this.map, id, color);
    }

    @Override
    public Optional<MapColor> getById(String id) {
        return Optional.ofNullable(this.map.get(this.withDomain(id)));
    }

    @Override
    public Collection<MapColor> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

}
