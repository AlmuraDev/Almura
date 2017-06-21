/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import com.almuradev.almura.content.material.Material;
import com.almuradev.almura.content.material.Materials;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MaterialRegistryModule implements CatalogRegistryModule<Material> {

    @RegisterCatalog(Materials.class)
    public final Map<String, Material> map = new HashMap<>(36);

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.register("air", net.minecraft.block.material.Material.AIR);
        this.register("grass", net.minecraft.block.material.Material.GRASS);
        this.register("ground", net.minecraft.block.material.Material.GROUND);
        this.register("wood", net.minecraft.block.material.Material.WOOD);
        this.register("rock", net.minecraft.block.material.Material.ROCK);
        this.register("iron", net.minecraft.block.material.Material.IRON);
        this.register("anvil", net.minecraft.block.material.Material.ANVIL);
        this.register("water", net.minecraft.block.material.Material.WATER);
        this.register("lava", net.minecraft.block.material.Material.LAVA);
        this.register("leaves", net.minecraft.block.material.Material.LEAVES);
        this.register("plants", net.minecraft.block.material.Material.PLANTS);
        this.register("vine", net.minecraft.block.material.Material.VINE);
        this.register("sponge", net.minecraft.block.material.Material.SPONGE);
        this.register("cloth", net.minecraft.block.material.Material.CLOTH);
        this.register("fire", net.minecraft.block.material.Material.FIRE);
        this.register("sand", net.minecraft.block.material.Material.SAND);
        this.register("circuits", net.minecraft.block.material.Material.CIRCUITS);
        this.register("carpet", net.minecraft.block.material.Material.CARPET);
        this.register("glass", net.minecraft.block.material.Material.GLASS);
        this.register("redstone_light", net.minecraft.block.material.Material.REDSTONE_LIGHT);
        this.register("tnt", net.minecraft.block.material.Material.TNT);
        this.register("coral", net.minecraft.block.material.Material.CORAL);
        this.register("ice", net.minecraft.block.material.Material.ICE);
        this.register("packed_ice", net.minecraft.block.material.Material.PACKED_ICE);
        this.register("snow", net.minecraft.block.material.Material.SNOW);
        this.register("crafted_snow", net.minecraft.block.material.Material.CRAFTED_SNOW);
        this.register("cactus", net.minecraft.block.material.Material.CACTUS);
        this.register("clay", net.minecraft.block.material.Material.CLAY);
        this.register("gourd", net.minecraft.block.material.Material.GOURD);
        this.register("dragon_egg", net.minecraft.block.material.Material.DRAGON_EGG);
        this.register("portal", net.minecraft.block.material.Material.PORTAL);
        this.register("cake", net.minecraft.block.material.Material.CAKE);
        this.register("web", net.minecraft.block.material.Material.WEB);
        this.register("piston", net.minecraft.block.material.Material.PISTON);
        this.register("barrier", net.minecraft.block.material.Material.BARRIER);
        this.register("structure_void", net.minecraft.block.material.Material.STRUCTURE_VOID);
    }

    private void register(String id, final net.minecraft.block.material.Material material) {
        final String name = id;
        id = SpongeImplHooks.getModIdFromClass(material.getClass()) + ':' + id;
        this.map.put(id, (Material) material);
        ((IMixinSetCatalogTypeId) material).setId(id, name);
    }

    @Override
    public Optional<Material> getById(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<Material> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

}
