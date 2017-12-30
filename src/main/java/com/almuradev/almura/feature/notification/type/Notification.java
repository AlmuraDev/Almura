/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.type;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public abstract class Notification {

    protected final Text title;
    protected final Text message;

    Notification(Text title, Text message) {
        this.title = title;
        this.message = message;
    }

    public Text getTitle() {
        return this.title;
    }

    public Text getMessage() {
        return this.message;
    }
}
