/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client.gui;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.ServerboundCreateTitlePacket;
import com.almuradev.almura.feature.title.network.ServerboundSelectedTitlePacket;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ManageTitlesGUI extends SimpleScreen {

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel titleLabel, titleNameLabel, permissionNodeLabel;
    private UIFormContainer form;
    private UITextField nameField, permissionField;
    private UIButton buttonAdd, buttonRemove, saveChangesButton;

    private int screenWidth = 450;
    private int screenHeight = 300;

    @Inject private static ClientTitleManager titleManager;
    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager notificationManager;

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Pane
        this.form = new UIFormContainer(this, this.screenWidth, this.screenHeight, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        this.titleLabel = new UILabel(this, "Title Manager")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Title List Area
        final UIFormContainer listArea = new UIFormContainer(this, 220, 260, "");
        listArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        listArea.setMovable(false);
        listArea.setClosable(false);
        listArea.setBorder(FontColors.WHITE, 1, 185);
        listArea.setBackgroundAlpha(215);
        listArea.setBottomPadding(3);
        listArea.setRightPadding(3);
        listArea.setTopPadding(3);
        listArea.setLeftPadding(3);

        // Edit Container
        final UIFormContainer editArea = new UIFormContainer(this, 220, 260, "");
        editArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        editArea.setMovable(false);
        editArea.setClosable(false);
        editArea.setBorder(FontColors.WHITE, 1, 185);
        editArea.setBackgroundAlpha(215);
        editArea.setBottomPadding(3);
        editArea.setRightPadding(3);
        editArea.setTopPadding(3);
        editArea.setLeftPadding(3);

        this.titleNameLabel = new UILabel(this, "Title:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(0, 5, Anchor.LEFT | Anchor.TOP);

        this.nameField = new UITextField(this, "Title Here", false)
            .setSize(200, 0)
            .setPosition(10, 20, Anchor.LEFT | Anchor.TOP)
            .setEditable(true)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        this.permissionNodeLabel = new UILabel(this, "Permission:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            ).setPosition(0, 45, Anchor.LEFT | Anchor.TOP);

        this.permissionField = new UITextField(this, "Permission Here", false)
            .setSize(200, 0)
            .setPosition(10, 60, Anchor.LEFT | Anchor.TOP)
            .setEditable(true)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        // Save Changes button
        this.saveChangesButton = new UIButtonBuilder(this)
                .width(30)
                .text(Text.of("almura.button.save"))
                .position(0,-5)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.save");

        editArea.add(this.titleNameLabel, this.nameField, this.permissionNodeLabel, this.permissionField, this.saveChangesButton);

        final UILabel titleSelectionLabel = new UILabel(this, "Server Titles:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.GREEN, "+"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.add");

        // Remove button
        this.buttonRemove = new UIButtonBuilder(this)
                .width(10)
                .x(15)
                .text(Text.of(TextColors.RED, "-"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.remove");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.button.close"))
                .listener(this)
                .build("button.close");

        // Apply button
        final UIButton buttonApply = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(-40, 0)
                .text(Text.of("almura.button.apply"))
                .listener(this)
                .build("button.apply");

        this.form.add(this.titleLabel, listArea, editArea, titleSelectionLabel, this.buttonAdd, this.buttonRemove, buttonClose, buttonApply);

        this.addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.apply":
                notificationManager.queuePopup(new PopupNotification(Text.of("Title"), Text.of("Updating Title on server..."), 2));
                titleManager.setTitleContentForDisplay(null);

                // TODO Dockter, finish this as this is test code
                network.sendToServer(new ServerboundCreateTitlePacket("test1", "Test1", "almura.title.test1", "Test 1!"));
            case "button.close":
                this.close();
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
