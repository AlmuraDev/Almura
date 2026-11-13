/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.type;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class Notification {

    protected final String title;
    protected final String message;

    Notification(final String title, final String message) {
        checkNotNull(title);
        checkNotNull(message);

        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }
}
