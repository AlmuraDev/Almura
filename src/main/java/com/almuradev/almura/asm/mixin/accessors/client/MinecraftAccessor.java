/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    // public net.minecraft.client.Minecraft field_110449_ao # defaultResourcePacks
    @Accessor("defaultResourcePacks") List<IResourcePack> accessor$getDefaultResourcePacks();
}
