/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

/**
 * A notification shown at the top of the HUD for a while.
 *
 * @param title the title, with legacy formatting codes
 * @param message the message, with legacy formatting codes
 * @param secondsToLive how long it shows
 */
public record PopupNotification(String title, String message, int secondsToLive) {

    public PopupNotification {
        if (secondsToLive < 0) {
            throw new IllegalArgumentException("A notification can't show for less than no time: " + secondsToLive);
        }
    }
}
