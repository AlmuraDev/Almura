/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UINotificationPanel extends UIHUDPanel {

    @Inject private static ClientNotificationManager manager;

    public  UILabel notificationLabel;
    public int timer;

    public UINotificationPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.notificationLabel = new UILabel(gui, "");
        this.notificationLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        this.notificationLabel.setFontOptions(FontColors.WHITE_FO);


        this.add(this.notificationLabel);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().player.world == null) {
            return;
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateNotifications();
    }

    private void updateNotifications() {
        this.notificationLabel.setText("Notifications Here");
    }
}
