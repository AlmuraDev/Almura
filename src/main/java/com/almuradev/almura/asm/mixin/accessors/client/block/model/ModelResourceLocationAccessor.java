/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.block.model;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ModelResourceLocation.class)
public interface ModelResourceLocationAccessor {
    //public net.minecraft.client.renderer.block.model.ModelResourceLocation <init>(I[Ljava/lang/String;)V # ModelResourceLocation
    // Todo: @Zidane build this please.
    //@Invoker("init") List<IResourcePack> accessor$getDefaultResourcePacks();
}
