/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block.material;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import net.minecraft.block.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MapColor.class)
public abstract class MixinMapColor implements com.almuradev.almura.content.material.MapColor, IMixinSetCatalogTypeId {

    private String id;
    private String name;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
