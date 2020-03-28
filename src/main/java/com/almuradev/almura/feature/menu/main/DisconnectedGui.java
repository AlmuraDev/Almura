/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.feature.menu.multiplayer.MultiplayerScreen;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class DisconnectedGui extends BasicScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel messageLabel;
    private BasicForm form;
    private UIButton buttonClose;
    private String reason;

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;
    @Inject
    private static ClientNotificationManager clientNotificationManager;
    @Inject
    private static PluginContainer container;

    public DisconnectedGui(String reason) {
        this.reason = reason;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        this.form = new BasicForm(this, 170, 170, "Server Status");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(false);
        this.form.setClosable(false);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        // Almura header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.DEAD_STEVE), null);
        almuraHeader.setSize(99, 99);
        almuraHeader.setPosition(0, 5, Anchor.TOP | Anchor.CENTER);

        this.messageLabel = new UILabel(this, "Disconnected from Server");
        if(reason.length() > 0) {
            this.messageLabel.setText(reason);
        }
        this.messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.messageLabel.setPosition(0, 112, Anchor.CENTER | Anchor.TOP);

        final UISeparator aboveButtonsSeparator = new UISeparator(this);
        aboveButtonsSeparator.setSize(this.form.getWidth() -5, 1);
        aboveButtonsSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);
        this.form.add(aboveButtonsSeparator);

        // Close button
        this.buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                //.text(I18n.format("almura.menu_button.quit"))
                .position(0,-1)
                .text("Return to Server Menu")
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build())
                .listener(this)
                .build("button.return");

        this.form.add(almuraHeader, this.messageLabel, this.buttonClose);

        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {

        switch (event.getComponent().getName().toLowerCase()) {
            case "button.return":
                new MultiplayerScreen(null).display();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            new MultiplayerScreen(null).display();
        }
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
