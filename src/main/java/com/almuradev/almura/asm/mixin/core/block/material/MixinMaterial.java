/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block.material;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Material.class)
public abstract class MixinMaterial implements com.almuradev.almura.content.material.Material, IMixinSetCatalogTypeId {

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
