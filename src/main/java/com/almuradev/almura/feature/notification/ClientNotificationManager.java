package com.almuradev.almura.feature.notification;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNotificationManager implements Witness {

    private final Queue<PopupNotification> popupNotifications = new ArrayDeque<>();
    private PopupNotification current = null;
    private long nextPoll = 0L;
    private long tickCounter = 0L;
    private long fadeout = 0L;

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.popupNotifications.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        // TODO Null screen means in-game but unsure if we want the notifications to pop up in the background
        if (event.phase == TickEvent.Phase.START && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().ingameGUI != null) {

            if (this.fadeout >= 0) {
                this.fadeout--;
                return;
            }

            if (this.current == null) {
                this.current = this.popupNotifications.poll();

                if (this.current != null) {
                    this.tickCounter = 0L;
                    this.nextPoll = this.current.getSecondsToLive() * 20L; // Best guess, we don't care about extreme accuracy.

                    // TODO Test Code!
                    System.err.println("Displaying: " + this.current);

                    // TODO Dockter, show the popup here

                }
            } else if (this.nextPoll == ++this.tickCounter) {

                // TODO Dockter, destroy the popup here

                System.err.println("Destroying: " + this.current);

                this.current = null;
                this.fadeout = 10L; // 10 ticks fadeout
            }
        }
    }

    public void queuePopup(PopupNotification notification) {
        checkNotNull(notification);

        this.popupNotifications.offer(notification);
    }
}
