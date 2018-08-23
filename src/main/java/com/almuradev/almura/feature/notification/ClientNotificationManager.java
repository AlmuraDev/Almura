/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.hud.ClientHeadUpDisplayManager;
import com.almuradev.almura.feature.hud.screen.AbstractHUD;
import com.almuradev.almura.feature.hud.screen.origin.OriginHUD;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.feature.notification.type.WindowNotification;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientNotificationManager implements Witness {

    private final Queue<PopupNotification> popupNotifications = new ArrayDeque<>();
    private PopupNotification current = null;
    private long nextPoll = 0L;
    private long tickCounter = 0L;
    private long fadeout = 0L;

    private final ClientHeadUpDisplayManager manager;

    @Inject
    public ClientNotificationManager(final ClientHeadUpDisplayManager manager) {
        this.manager = manager;
    }

    @SubscribeEvent
    public void onClientConnectedToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.popupNotifications.clear();
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {

        final AbstractHUD hud = this.manager.getHUDDirect();

        // TODO Null screen means in-game but unsure if we want the notifications to pop up in the background
        if (event.phase == TickEvent.Phase.START && Minecraft.getMinecraft().ingameGUI != null) {

            if (this.fadeout >= 0) {
                this.fadeout--;
                return;
            }

            if (this.current == null) {
                this.current = this.popupNotifications.poll();

                if (this.current != null) {
                    this.tickCounter = 0L;
                    this.nextPoll = this.current.getSecondsToLive() * 20L; // Best guess, we don't care about extreme accuracy.

                    if (hud == null) {
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(SpongeTexts.toComponent(SpongeTexts.fromLegacy(this.current
                            .getMessage())));
                    } else {
                        final OriginHUD originHUD = (OriginHUD) hud;
                        originHUD.notificationPanel.displayPopup();
                    }
                }
            } else if (this.nextPoll == ++this.tickCounter) {

                if (hud != null) {
                    final OriginHUD originHUD = (OriginHUD) hud;
                    originHUD.notificationPanel.destroyPopup();
                }

                this.current = null;
                this.fadeout = 10L; // 10 ticks fadeout
            }
        }
    }

    public void handlePopup(final PopupNotification notification) {
        checkNotNull(notification);

        this.popupNotifications.offer(notification);
    }

    public void handleWindow(final WindowNotification notification) {
        checkNotNull(notification);

        UIMessageBox.showDialog(Minecraft.getMinecraft().currentScreen, notification.getTitle(), notification.getMessage(), MessageBoxButtons.OK,
            null);
    }

    @Nullable
    public PopupNotification getCurrent() {
        return this.current;
    }
}
