package com.almuradev.almura.feature.nick.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class NicknameGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private String originalNickname;
    private UILabel titleLabel;
    private UIFormContainer form;
    private UITextField nicknameTextbox;
    private UISelect colorSelector;

    private EntityPlayer entityPlayer;

    @Inject private static PluginContainer container;
    @Inject private static ClientNickManager nickManager;
    @Inject private static ServerNotificationManager serverNotificationManager;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public NicknameGUI(EntityPlayer entityPlayer) {
        this.entityPlayer = entityPlayer;
        if (nickManager != null) {
            this.originalNickname = entityPlayer.getDisplayName().getFormattedText(); // Save this so it can be used to revert to in the event the user doesn't click "Apply".
        }
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        form = new UIFormContainer(this, 300, 125, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Nicknames");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Player Render Area
        final UIFormContainer playerArea = new UIFormContainer(this, 75, 85, "");
        playerArea.setPosition(0, 0, Anchor.RIGHT | Anchor.MIDDLE);
        playerArea.setMovable(false);
        playerArea.setClosable(false);
        playerArea.setBorder(FontColors.WHITE, 1, 185);
        playerArea.setBackgroundAlpha(215);
        playerArea.setBottomPadding(3);
        playerArea.setRightPadding(3);
        playerArea.setTopPadding(3);
        playerArea.setLeftPadding(3);

        // Nickname List Area
        final UIFormContainer listArea = new UIFormContainer(this, 217, 85, "");
        listArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        listArea.setMovable(false);
        listArea.setClosable(false);
        listArea.setBorder(FontColors.WHITE, 1, 185);
        listArea.setBackgroundAlpha(215);
        listArea.setBottomPadding(3);
        listArea.setRightPadding(3);
        listArea.setTopPadding(3);
        listArea.setLeftPadding(3);

        UILabel nicknameLabel = new UILabel(this, "Enter desired nickname:");
        nicknameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        nicknameLabel.setPosition(7, 10, Anchor.LEFT | Anchor.TOP);

        // Nickname Entry Textbox
        this.nicknameTextbox = new UITextField(this, "", false);
        this.nicknameTextbox.setSize(150, 0);
        this.nicknameTextbox.setText(entityPlayer.getDisplayName().getFormattedText());
        this.nicknameTextbox.setPosition(7, 23, Anchor.LEFT | Anchor.TOP);
        this.nicknameTextbox.setEditable(true);
        this.nicknameTextbox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        UILabel colorLabel = new UILabel(this, "Add colors:");
        colorLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        colorLabel.setPosition(7, 40, Anchor.LEFT | Anchor.TOP);

        // Color Selection dropdown
       colorSelector = new UISelect<>(this,
                100,
                Arrays.asList(	"§1Dark Blue",
                        "§9Blue",
                        "§3Dark Aqua",
                        "§bAqua",
                        "§4Dark Red",
                        "§cRed",
                        "§eYellow",
                        "§6Gold",
                        "§2Dark Green",
                        "§aGreen",
                        "§5Dark Purple",
                        "§dLight Purple",
                        "§fWhite",
                        "§7Gray",
                        "§8Dark Gray",
                        "§0Black,"));
        colorSelector.setPosition(7, 50, Anchor.LEFT | Anchor.TOP);
        colorSelector.setOptionsWidth(UISelect.SELECT_WIDTH);
        colorSelector.select("§1Dark Blue");

        // Add Color character button
        final UIButton buttonColor = new UIButtonBuilder(this)
                .width(40)
                .position(110, 49, Anchor.LEFT | Anchor.TOP)
                .text(Text.of("Add"))
                .listener(this)
                .build("button.color");

        // Reset button
        final UIButton buttonReset = new UIButtonBuilder(this)
                .width(40)
                .position(150, 49, Anchor.LEFT | Anchor.TOP)
                .text(Text.of("Reset"))
                .listener(this)
                .build("button.reset");

        UILabel nicknameRulesLabel = new UILabel(this, "Please follow nickname rules.");
        nicknameRulesLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        nicknameRulesLabel.setPosition(3, -2, Anchor.LEFT | Anchor.BOTTOM);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        // Apply button
        final UIButton buttonApply = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(-40, 0)
                .text("Apply")
                .listener(this)
                .build("button.apply");


        form.add(titleLabel, listArea, playerArea, nicknameLabel, nicknameTextbox, nicknameRulesLabel, colorLabel, colorSelector, buttonColor, buttonReset, buttonClose, buttonApply);
        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.color":
                String colorCode = colorSelector.getSelectedOption().getLabel().substring(0,2);
                nicknameTextbox.addText(colorCode);
                break;

            case "button.reset":
                nicknameTextbox.setText(originalNickname);
                break;

            case "button.apply":
                if (nicknameTextbox.getText().equalsIgnoreCase(" ") || nicknameTextbox.getText().isEmpty()) {
                    clientNotificationManager.queuePopup(new PopupNotification(Text.of("Error"), Text.of("Cannot have blank title"),5));
                    break;
                }

                for (final Player onlinePlayer : serverNotificationManager.game.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.getUniqueId().equals(Minecraft.getMinecraft().player.getUniqueID())) {
                        clientNotificationManager.queuePopup(new PopupNotification(Text.of("New Nickname!"), Text.of("You are now known as " + nicknameTextbox.getText().trim()),5));
                    } else {
                        serverNotificationManager.sendPopupNotification(onlinePlayer, Text.of("New Nickname!"), Text.of(entityPlayer.getName() + " is now known as " + nicknameTextbox.getText().trim()), 5);
                    }
                }

                // Todo:  Sent packet to the server to set new nickname within NucleusNicknameService on Main Thread

                close();
                break;

            case "button.close":
                update = false; // Stop automatic update of name based on textbox
                setName(entityPlayer, originalNickname);  // Revert nickname unless player hits apply
                close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (update) {
            setName(entityPlayer, nicknameTextbox.getText());
        }

        // Set location of Player Entity within Panel
        int i = form.screenX()+210;
        int j = form.screenY()+25;

        // Draw Player
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - mouseX, (float)(j + 75 - 50) - mouseY, this.mc.player);

        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {}
    }

    private void setName(EntityPlayer player, String name) {
        try {
            this.nickManager.adjustPlayerNickname(player, name);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
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
