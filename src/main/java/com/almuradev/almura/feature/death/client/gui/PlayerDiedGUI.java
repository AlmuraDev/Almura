/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
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
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class PlayerDiedGUI extends BasicScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel messageLabel, droppedLabel, deathTaxLabel;
    private BasicForm form;
    private UIButton buttonRespawn, buttonRevive, buttonRagequit;

    private EntityPlayer player;
    private double droppedAmount, deathTaxAmount;
    private boolean dropCoins, canRevive;

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;
    @Inject
    private static ClientNotificationManager clientNotificationManager;
    @Inject
    private static PluginContainer container;

    public PlayerDiedGUI(EntityPlayer player, double dropAmount, double deathTaxAmount, boolean dropCoins, boolean canRevive) {
        this.player = player;
        this.dropCoins = dropCoins;
        this.droppedAmount = dropAmount;
        this.deathTaxAmount = deathTaxAmount;
        this.canRevive = canRevive;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        this.form = new BasicForm(this, 50, 200, "You have died.");
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
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        this.messageLabel = new UILabel(this, getMessage(this.player.getName()));
        this.messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.messageLabel.setPosition(0, 102, Anchor.CENTER | Anchor.TOP);

        final DecimalFormat dFormat = new DecimalFormat("###,###,###,###.00");

        this.droppedLabel = new UILabel(this, "You dropped: " + TextFormatting.GOLD + "$" + dFormat.format(this.droppedAmount) + TextFormatting.RESET + ".");
        this.droppedLabel.setVisible(this.dropCoins);
        this.droppedLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.droppedLabel.setPosition(0, -40, Anchor.CENTER | Anchor.BOTTOM);

        this.deathTaxLabel = new UILabel(this, "You lost: " + TextFormatting.RED + "$" + dFormat.format(this.deathTaxAmount) + TextFormatting.RESET + " to death taxes.");
        this.deathTaxLabel.setVisible(this.dropCoins);
        this.deathTaxLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.deathTaxLabel.setPosition(0, -26, Anchor.CENTER | Anchor.BOTTOM);

        this.form.setSize(this.messageLabel.getWidth() + 30, this.form.getHeight());

        if (this.form.getWidth() < this.droppedLabel.getWidth() + 30) {
            this.form.setSize(this.droppedLabel.getWidth() + 20, this.form.getHeight()); // Account for the width possibly being too small.
        }

        if (this.form.getWidth() < this.deathTaxLabel.getWidth() + 30) {
            this.form.setSize(this.deathTaxLabel.getWidth() + 20, this.form.getHeight()); // Account for the width possibly being too small.
        }

        if (!this.dropCoins) {
            this.form.setSize(this.form.getWidth(), 160);
        }

        final UISeparator topWindowTitleSeparator = new UISeparator(this);
        topWindowTitleSeparator.setSize(this.form.getWidth() -5, 1);
        topWindowTitleSeparator.setPosition(0, -5, Anchor.TOP | Anchor.CENTER);
        this.form.add(topWindowTitleSeparator);

        final UISeparator aboveButtonsSeparator = new UISeparator(this);
        aboveButtonsSeparator.setSize(this.form.getWidth() -5, 1);
        aboveButtonsSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);
        this.form.add(aboveButtonsSeparator);

        final UISeparator belowMessageSeparator = new UISeparator(this);
        belowMessageSeparator.setSize(form.getWidth() -5, 1);
        belowMessageSeparator.setPosition(0, 40, Anchor.CENTER | Anchor.MIDDLE);
        belowMessageSeparator.setVisible(this.dropCoins);
        this.form.add(belowMessageSeparator);

        // Revive button
        this.buttonRevive = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .text("Revive")
                .listener(this)
                .enabled(this.canRevive)
                .build("button.revive");

        // Respawn button
        this.buttonRespawn = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(0, 0)
                .text("Respawn")
                .listener(this)
                .build("button.respawn");

        // Rage Quit button
        this.buttonRagequit = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(I18n.format("almura.menu_button.quit"))
                .fontOptions(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
                .listener(this)
                .build("button.ragequit");

        this.form.add(almuraHeader, this.messageLabel, this.droppedLabel, this.deathTaxLabel, this.buttonRespawn, this.buttonRevive, this.buttonRagequit);

        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {

        switch (event.getComponent().getName().toLowerCase()) {
            // Note: you have the schedule the close() otherwise for some reason its ignored during respawn.
            case "button.respawn":
                Sponge.getScheduler().createTaskBuilder().delayTicks(30).execute(delayedTask("respawnPlayer", this.mc.player.getEntityWorld().provider.getDimension(), this.player.posX, this.player.posY, this.player.posZ)).submit(container); //
                // delay the close call.
                this.mc.player.respawnPlayer();
                break;

            case "button.revive":
                Sponge.getScheduler().createTaskBuilder().delayTicks(30).execute(delayedTask("revivePlayer", this.mc.player.getEntityWorld().provider.getDimension(), this.player.posX, this.player.posY, this.player.posZ)).submit(container); // delay
                // the close call.
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

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.player.isDead) {
                return;
            } else {
                this.close();
            }

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
        final Random random = new Random();
        String message = "";
        final int selection = random.nextInt(14) + 1;

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
