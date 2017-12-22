package com.almuradev.almura.feature.notification.type;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.time.Instant;

@SideOnly(Side.CLIENT)
public abstract class Notification {

    private final Instant timestamp;
    private final Text text;

    protected Notification(Text text) {
        this.timestamp = Instant.now();
        this.text = text;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public Text getText() {
        return this.text;
    }
}
