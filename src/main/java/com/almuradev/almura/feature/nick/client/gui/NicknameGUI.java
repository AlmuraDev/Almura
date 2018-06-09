package com.almuradev.almura.feature.nick.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.nick.asm.mixin.iface.IMixinEntityPlayer;
import com.almuradev.almura.feature.nick.network.ServerboundNucleusNameChangePacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
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
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.regex.Pattern;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class NicknameGUI extends SimpleScreen {

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private String originalNickname;
    private UIFormContainer form;
    private UITextField nicknameTextbox;
    private UISelect colorSelector;

    private EntityPlayer entityPlayer;

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public NicknameGUI(EntityPlayer entityPlayer) {
        this.entityPlayer = entityPlayer;
        this.originalNickname = entityPlayer.getDisplayName().getFormattedText(); // Save this so it can be used to revert to in the event the user doesn't click "Apply".

        if (originalNickname.substring(0,1).equalsIgnoreCase("~")) {
            originalNickname = originalNickname.substring(1, originalNickname.length()); // Removes ~ value that exists in already set nicknames.
        }

        if (originalNickname.substring(originalNickname.length()-2,originalNickname.length()).equalsIgnoreCase("§r")) {
            originalNickname = originalNickname.substring(0,originalNickname.length()-2);  // Removes reset character at the end of the nickname.
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new UIFormContainer(this, 300, 125, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Nicknames");
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

        final UILabel nicknameLabel = new UILabel(this, "Enter desired nickname:");
        nicknameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        nicknameLabel.setPosition(7, 10, Anchor.LEFT | Anchor.TOP);

        // Nickname Entry Textbox
        this.nicknameTextbox = new UITextField(this, "", false);
        this.nicknameTextbox.setSize(150, 0);
        this.nicknameTextbox.setText(this.originalNickname);
        this.nicknameTextbox.setPosition(7, 23, Anchor.LEFT | Anchor.TOP);
        this.nicknameTextbox.setEditable(true);
        this.nicknameTextbox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel colorLabel = new UILabel(this, "Add colors:");
        colorLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        colorLabel.setPosition(7, 40, Anchor.LEFT | Anchor.TOP);

        // Color Selection dropdown
        this.colorSelector = new UISelect<>(this,
                100,
                Arrays.asList(
                        "§1Dark Blue",
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
                        "§0Black,")
       );
        this.colorSelector.setPosition(7, 50, Anchor.LEFT | Anchor.TOP);
        this.colorSelector.setOptionsWidth(UISelect.SELECT_WIDTH);
        this.colorSelector.select("§1Dark Blue");

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

        // Reset button
        final UIButton buttonRemove = new UIButtonBuilder(this)
                .width(70)
                .position(105, 65, Anchor.LEFT | Anchor.TOP)
                .text(Text.of("Remove Nickname"))
                .listener(this)
                .build("button.remove");

        final UILabel nicknameRulesLabel = new UILabel(this, "Please follow nickname rules.");
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


        this.form.add(titleLabel, listArea, playerArea, nicknameLabel, this.nicknameTextbox, nicknameRulesLabel, colorLabel, this.colorSelector, buttonColor, buttonReset, buttonRemove, buttonClose, buttonApply);
        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.color":
                final String colorCode = this.colorSelector.getSelectedOption().getLabel().substring(0,2);
                this.nicknameTextbox.addText(colorCode);
                break;

            case "button.reset":
                this.nicknameTextbox.setText(this.originalNickname);
                break;

            case "button.remove":
                this.update = false; // Stop automatic update of name based on textbox
                clientNotificationManager.queuePopup(new PopupNotification(Text.of("Nickname"), Text.of("Removing Nickname from server...."),2));

                network.sendToServer(new ServerboundNucleusNameChangePacket(entityPlayer.getName().trim()));

                this.close();
                break;

            case "button.apply":
                final Text validateText = Text.of(nicknameTextbox.getText());
                final String regexPattern = "[a-zA-Z0-9_§]+";
                final Pattern nickNameRegex = Pattern.compile(regexPattern);

                if (this.nicknameTextbox.getText().isEmpty()) {
                    clientNotificationManager.queuePopup(new PopupNotification(Text.of("Error"), Text.of("Cannot have blank title!"),5));
                    break;
                }

                if (validateText.toPlain().length() <= 3) {
                    clientNotificationManager.queuePopup(new PopupNotification(Text.of("Error"), Text.of("Cannot have nickname < 3 characters!"),5));
                    break;
                }

                if (validateText.toPlain().length() > 20) {
                    clientNotificationManager.queuePopup(new PopupNotification(Text.of("Error"), Text.of("Cannot have nickname > 20 characters!"),5));
                    break;
                }

                if (!nickNameRegex.matcher(validateText.toPlain()).matches()) {
                    clientNotificationManager.queuePopup(new PopupNotification(Text.of("Error"), Text.of("Invalid Character in Nickname!"), 5));
                    break;
                }
                this.update = false; // Stop automatic update of name based on textbox
                clientNotificationManager.queuePopup(new PopupNotification(Text.of("Nickname"), Text.of("Updating Nickname on server...."),2));

                network.sendToServer(new ServerboundNucleusNameChangePacket(this.nicknameTextbox.getText().trim()));

                this.close();
                break;

            case "button.close":
                this.update = false; // Stop automatic update of name based on textbox
                this.setRendererName(this.entityPlayer, this.originalNickname);  // Revert nickname unless player hits apply

                this.close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (this.update) {
            this.setRendererName(this.entityPlayer, this.nicknameTextbox.getText());
        }

        // Set location of Player Entity within Panel
        int i = this.form.screenX()+210;
        int j = this.form.screenY()+25;

        // Draw Player
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float)(i + 51) - mouseX, (float)(j + 75 - 50) - mouseY, this.entityPlayer);

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {}
    }

    private void setRendererName(EntityPlayer player, String name) {
        ((IMixinEntityPlayer) player).setDisplayName(name);
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
