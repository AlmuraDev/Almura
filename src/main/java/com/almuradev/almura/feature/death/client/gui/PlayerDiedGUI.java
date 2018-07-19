package com.almuradev.almura.feature.death.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.feature.menu.ingame.network.ServerboundFeaturesOpenRequestPacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

import java.io.IOException;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class PlayerDiedGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel titleLabel, messageLabel;
    private UIFormContainer form;
    private UIButton buttonRespawn, buttonRevive, buttonRagequit;

    private EntityPlayer player;
    private double dropAmount;

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;
    @Inject private static PluginContainer container;

    public PlayerDiedGUI(EntityPlayer player, double dropAmount) {
        this.player = player;
        this.dropAmount = dropAmount;
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        form = new UIFormContainer(this, 200, 100, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(false);
        form.setClosable(false);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "You have died...");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        messageLabel = new UILabel(this, "");
        messageLabel.setText("You lost: " + dropAmount + " to death tax.");
        messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        messageLabel.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        // Revive button
        buttonRevive = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(-50, 0)
                .text("Revive")
                .listener(this)
                .enabled(true)
                .build("button.revive");

        // Respawn button
        buttonRespawn = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(0, 0)
                .text("Respawn")
                .listener(this)
                .build("button.respawn");

        // Rage Quit button
        buttonRagequit = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(55, 0)
                .text("Give up...")
                .listener(this)
                .build("button.ragequit");

        form.add(titleLabel, messageLabel, buttonRespawn, buttonRevive, buttonRagequit);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {

        switch (event.getComponent().getName().toLowerCase()) {
            // Note: you have the schedule the close() otherwise for some reason its ignored during respawn.

            case "button.respawn":
               Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("respawnPlayer", this.mc.player.getEntityWorld().provider.getDimension(), this.player.posX, this.player.posY, this.player.posZ)).submit(container); // delay the close call.
               this.mc.player.respawnPlayer();
               break;

            case "button.revive":
                Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("revivePlayer", this.mc.player.getEntityWorld().provider.getDimension(), this.player.posX, this.player.posY, this.player.posZ)).submit(container); // delay the close call.
                this.mc.player.respawnPlayer();
                break;

            case "button.ragequit":
                if (this.mc.world != null) {
                    this.mc.world.sendQuittingDisconnectingPacket();
                }

                this.mc.loadWorld((WorldClient)null);
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
        }
    }

    protected Consumer<Task> delayedTask(final String details, final int dimID, final double x, final double y, final double z) {  // Scheduler
        return task -> {
            if (details.equalsIgnoreCase("revivePlayer")) {
                this.network.sendToServer(new ServerboundReviveRequestPacket(dimID, x, y, z));
                close();
            }
            if (details.equalsIgnoreCase("respawnPlayer")) {
                close();
            }
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            return; // ignore escape key to prevent false-close to parent.
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
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
