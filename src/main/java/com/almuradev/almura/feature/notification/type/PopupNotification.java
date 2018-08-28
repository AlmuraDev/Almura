/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.type;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PopupNotification extends Notification {

    private final int secondsToLive;

    public PopupNotification(final String title, final String message, final int secondsToLive) {
        super(title, message);

        checkState(secondsToLive >= 0);

        this.secondsToLive = secondsToLive;
    }

    public int getSecondsToLive() {
        return this.secondsToLive;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("title", this.title)
                .add("message", this.message)
                .add("seconds_to_live", this.secondsToLive)
                .toString();
    }
}
