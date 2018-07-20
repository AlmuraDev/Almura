package com.almuradev.almura.feature.death.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.death.network.ServerboundReviveRequestPacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class PlayerDiedGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel messageLabel, dropsLabel;
    private UIFormContainer form;
    private UIButton buttonRespawn, buttonRevive, buttonRagequit;

    private EntityPlayer player;
    private double dropAmount;
    private boolean dropCoins;

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;
    @Inject
    private static ClientNotificationManager clientNotificationManager;
    @Inject
    private static PluginContainer container;

    public PlayerDiedGUI(EntityPlayer player, boolean dropCoins, double dropAmount) {
        this.player = player;
        this.dropCoins = dropCoins;
        this.dropAmount = dropAmount;
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        form = new UIFormContainer(this, 50, 180, "You have died.");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(false);
        form.setClosable(false);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        // Almura header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.DEAD_STEVE), null);
        almuraHeader.setSize(99, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        messageLabel = new UILabel(this, getMessage(player.getName()));
        messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        messageLabel.setPosition(0, 102, Anchor.CENTER | Anchor.TOP);

        DecimalFormat dFormat = new DecimalFormat("###,###,###,###.00");

        dropsLabel = new UILabel(this, "You lost: " + TextFormatting.RED + "$" + dFormat.format(dropAmount) + TextFormatting.RESET + " to death tax.");
        dropsLabel.setVisible(dropCoins);
        dropsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        dropsLabel.setPosition(0, -23, Anchor.CENTER | Anchor.BOTTOM);

        form.setSize(messageLabel.getWidth() + 30, form.getHeight());

        if (form.getWidth() < dropsLabel.getWidth() + 30) {
            form.setSize(dropsLabel.getWidth() + 20, form.getHeight()); // Account for the width possibly being too small.
        }

        if (!dropCoins) {
            form.setSize(form.getWidth(), 160);
        }

        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth() -5, 1);
        separator.setPosition(0, -5, Anchor.TOP | Anchor.CENTER);
        form.add(separator);

        final UISeparator separator2 = new UISeparator(this);
        separator2.setSize(form.getWidth() -5, 1);
        separator2.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);
        form.add(separator2);

        final UISeparator separator3 = new UISeparator(this);
        separator3.setSize(form.getWidth() -5, 1);
        separator3.setPosition(0, 50, Anchor.CENTER | Anchor.MIDDLE);
        separator3.setVisible(dropCoins);
        form.add(separator3);

        // Revive button
        buttonRevive = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
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
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of(I18n.format("almura.menu_button.quit")))
                .fro(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
                .listener(this)
                .build("button.ragequit");

        form.add(almuraHeader, messageLabel, dropsLabel, buttonRespawn, buttonRevive, buttonRagequit);

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

                this.mc.loadWorld((WorldClient) null);
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

    private String getMessage(String player) {
        Random random = new Random();
        String message = "";
        int selection = random.nextInt(14) + 1;

        if (player.equalsIgnoreCase("chimwolfeye")) {
            return "You have become one with the Sisko, rejoice!";
        }

        switch (selection) {
            case 1:
                message = "I can't believe you died again...";
                break;
            case 2:
                message = "Death is only the beginning...";
                break;
            case 3:
                message = "The PLEBIAN... has died...";
                break;
            case 4:
                message = "Oohhh, I'm sure that's gonna be fine...";
                break;
            case 5:
                message = "Ouch.  I'll bet that leaves a scar.";
                break;
            case 6:
                message = "Seriously...";
                break;
            case 7:
                message = "Why does this keep happening?";
                break;
            case 8:
                message = "What is going on exactly?";
                break;
            case 9:
                message = "Repeating death only bring more death...";
                break;
            case 10:
                message = "Try, try, try try try Again?!?";
                break;
            case 11:
                message = "Sigh... this is getting aggravating.";
                break;
            case 12:
                message = "Failure is the fog from which we all glimpse triumph";
                break;

            case 13:
                message = "Get up and walk it off... geez.";
                break;

            case 14:
                message = "Shhh. Only dreams now....";
                break;

            default:
                message = "You have died...again";
                break;
        }

        return message;
    }
}
