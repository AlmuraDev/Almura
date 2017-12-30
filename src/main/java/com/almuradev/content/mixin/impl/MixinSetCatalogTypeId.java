/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.mixin.impl;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import org.spongepowered.api.CatalogType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    // CreativeTabs.class intentionally excluded
    MapColor.class,
    Material.class,
    SoundType.class
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
