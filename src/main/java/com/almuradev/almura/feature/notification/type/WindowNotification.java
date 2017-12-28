/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.type;

import com.google.common.base.MoreObjects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public final class WindowNotification extends Notification {

    public WindowNotification(Text text) {
        super(text);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", this.text)
                .toString();
    }
}
