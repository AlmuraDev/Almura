package com.almuradev.almura.feature.notification.type;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public final class PopupNotification extends Notification {

    private final int secondsToLive;

    public PopupNotification(Text text, int secondsToLive) {
        super(text);

        this.secondsToLive = secondsToLive;
    }

    public int getSecondsToLive() {
        return this.secondsToLive;
    }
}
