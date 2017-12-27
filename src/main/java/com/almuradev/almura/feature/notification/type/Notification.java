package com.almuradev.almura.feature.notification.type;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public abstract class Notification {

    protected final Text text;

    Notification(Text text) {
        this.text = text;
    }

    public Text getText() {
        return this.text;
    }
}
