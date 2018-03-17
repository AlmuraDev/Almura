/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import com.almuradev.content.registry.AbstractCatalogRegistryModule;
import com.almuradev.content.registry.EagerCatalogRegistration;
import net.minecraft.block.material.Material;

@EagerCatalogRegistration
public final class MaterialRegistryModule extends AbstractCatalogRegistryModule.Mapped<com.almuradev.content.type.material.Material, Material> {
    public MaterialRegistryModule() {
        super(36);
    }

    @Override
    public void registerDefaults() {
        this.register("air", Material.AIR);
        this.register("grass", Material.GRASS);
        this.register("ground", Material.GROUND);
        this.register("wood", Material.WOOD);
        this.register("rock", Material.ROCK);
        this.register("iron", Material.IRON);
        this.register("anvil", Material.ANVIL);
        this.register("water", Material.WATER);
        this.register("lava", Material.LAVA);
        this.register("leaves", Material.LEAVES);
        this.register("plants", Material.PLANTS);
        this.register("vine", Material.VINE);
        this.register("sponge", Material.SPONGE);
        this.register("cloth", Material.CLOTH);
        this.register("fire", Material.FIRE);
        this.register("sand", Material.SAND);
        this.register("circuits", Material.CIRCUITS);
        this.register("carpet", Material.CARPET);
        this.register("glass", Material.GLASS);
        this.register("redstone_light", Material.REDSTONE_LIGHT);
        this.register("tnt", Material.TNT);
        this.register("coral", Material.CORAL);
        this.register("ice", Material.ICE);
        this.register("packed_ice", Material.PACKED_ICE);
        this.register("snow", Material.SNOW);
        this.register("crafted_snow", Material.CRAFTED_SNOW);
        this.register("cactus", Material.CACTUS);
        this.register("clay", Material.CLAY);
        this.register("gourd", Material.GOURD);
        this.register("dragon_egg", Material.DRAGON_EGG);
        this.register("portal", Material.PORTAL);
        this.register("cake", Material.CAKE);
        this.register("web", Material.WEB);
        this.register("piston", Material.PISTON);
        this.register("barrier", Material.BARRIER);
        this.register("structure_void", Material.STRUCTURE_VOID);
    }
}
