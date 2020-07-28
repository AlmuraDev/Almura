/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.server.management;

import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerChunkMap.class)
public interface PlayerChunkMapAccessor {
    //public net.minecraft.server.management.PlayerChunkMap field_187308_a # NOT_SPECTATOR
    @Accessor("NOT_SPECTATOR") static Predicate<EntityPlayerMP> accessor$getNotSpectator() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
