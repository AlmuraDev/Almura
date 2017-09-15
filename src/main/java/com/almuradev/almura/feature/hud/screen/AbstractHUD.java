/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.screen;

import com.almuradev.shared.client.ui.screen.SimpleScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AbstractHUD extends SimpleScreen {

    public abstract int getTabMenuOffsetY();

    public abstract int getPotionOffsetY();
}
