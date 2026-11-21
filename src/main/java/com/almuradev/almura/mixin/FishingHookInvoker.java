/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Hooks an entity on a bobber, for Mo' Creatures' fish biting it ({@code MocFishing}): 26.3 keeps it private, as 1.12 kept the caught entity
 * public.
 */
@Mixin(FishingHook.class)
public interface FishingHookInvoker {

    @Invoker("setHookedEntity")
    void almura$setHookedEntity(@Nullable Entity entity);
}
