package com.almuradev.almura.feature.notification.type;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public final class WindowNotification extends Notification {

    public WindowNotification(Text text) {
        super(text);
    }
}
