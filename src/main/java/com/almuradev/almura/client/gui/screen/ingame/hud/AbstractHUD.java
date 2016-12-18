/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.client.gui.screen.SimpleScreen;

public abstract class AbstractHUD extends SimpleScreen {

    public abstract int getOriginBossBarOffsetY();

    public abstract int getTabMenuOffsetY();

    public abstract int getPotionOffsetY();
}
