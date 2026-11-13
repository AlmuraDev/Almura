/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.entity;

import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityList.class)
public interface EntityListAccessor {
    //public net.minecraft.entity.EntityList field_191310_e # PLAYER
    @Accessor("PLAYER") static ResourceLocation accessor$getPlayerResourceLocation() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
