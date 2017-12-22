package com.almuradev.almura.feature.notification;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.notification.type.PopupNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Queue;

public class ClientNotificationManager {

    private final Queue<PopupNotification> popupNotifications = new ArrayDeque<>();
    private PopupNotification current = null;
    private long nextPoll = 0L;
    private long tickCounter = 0L;
    private long fadeout = 0L;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase == TickEvent.Phase.START) {

            if (this.fadeout >= 0) {
                this.fadeout--;
                return;
            }

            if (this.current == null) {
                this.current = this.popupNotifications.poll();

                if (this.current != null) {
                    this.tickCounter = 0L;
                    this.nextPoll = this.current.getSecondsToLive() * 20L; // Best guess, we don't care about extreme accuracy.

                    // TODO Dockter, show the popup here

                }
            } else if (this.nextPoll == ++this.tickCounter) {

                // TODO Dockter, destroy the popup here

                this.current = null;
                this.fadeout = 10L; // 10 ticks fadeout
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void queuePopup(PopupNotification notification) {
        checkNotNull(notification);

        this.popupNotifications.offer(notification);
    }
}
