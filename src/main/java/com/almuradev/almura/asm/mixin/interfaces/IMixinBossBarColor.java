/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import org.spongepowered.api.util.Color;

public interface IMixinBossBarColor {

    Color getAlmuraColor();

    void setAlmuraColor(final Color color);
}
