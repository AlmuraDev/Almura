/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.mixin.impl;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.type.cactus.CactusFeature;
import com.almuradev.content.type.deadbush.DeadBushFeature;
import com.almuradev.content.type.flower.FlowerFeature;
import com.almuradev.content.type.generation.type.underground.ore.UndergroundOreGeneratorImpl;
import com.almuradev.content.type.grass.GrassFeature;
import com.almuradev.content.type.tree.BigTreeFeature;
import com.almuradev.content.type.tree.TreeFeature;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.spongepowered.api.CatalogType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    BigTreeFeature.class,
    CactusFeature.class,
    DeadBushFeature.class,
    FlowerFeature.class,
    GrassFeature.class,
    // CreativeTabs.class intentionally excluded
    Item.ToolMaterial.class,
    MapColor.class,
    Material.class,
    SoundType.class,
    TreeFeature.class,
    UndergroundOreGeneratorImpl.class
})
public class MixinSetCatalogTypeId implements CatalogType, IMixinSetCatalogTypeId {
    private String id;
    private String name;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
