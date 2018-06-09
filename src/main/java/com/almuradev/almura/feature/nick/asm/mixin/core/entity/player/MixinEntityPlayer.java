/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.asm.mixin.core.entity.player;

import com.almuradev.almura.feature.nick.asm.mixin.iface.IMixinEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer implements IMixinEntityPlayer {
    @Shadow private String displayname;

    @Override
    public void setDisplayName(String displayName) {
        this.displayname = displayName;
    }
}
