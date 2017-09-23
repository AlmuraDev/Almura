/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import org.spongepowered.api.util.Color;

public interface IMixinBossBarColor {

    Color getAlmuraColor();

    void setAlmuraColor(final Color color);
}
