/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.world;

import com.almuradev.almura.asm.mixin.interfaces.IMixinBossBarColor;
import net.minecraft.world.BossInfo;
import org.spongepowered.api.util.Color;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(BossInfo.Color.class)
@SuppressWarnings("NullableProblems")
public class MixinBossBarColor implements IMixinBossBarColor {

    @Nonnull private Color almuraColor;

    @Override
    public Color getAlmuraColor() {
        return this.almuraColor;
    }

    @Override
    public void setAlmuraColor(final Color color) {
        this.almuraColor = color;
    }
}
