package com.almuradev.almura.feature.title.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.title.ClientTitleManager;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ManageTitlesGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel titleLabel, titleNameLabel, permissionNodeLabel;
    private UIFormContainer form;
    private UITextField nameField, permissionField;
    private UIButton buttonAdd, buttonRemove, saveChangesButton;

    private EntityPlayer player;
    private Set<String> titles;

    private int screenWidth = 450;
    private int screenHeight = 300;

    @Inject private static ClientTitleManager clientTitleManager;
    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;

    // Todo: re-activate this once the packet system is created for this module.
    /*public ManageTitlesGUI(EntityPlayer player, Set<String> set) {
        this.player = player;
        this.titles = set;
    }*/

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Pane
        form = new UIFormContainer(this, this.screenWidth, this.screenHeight, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Title Manager");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

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

        titleNameLabel = new UILabel(this, "Title:");
        titleNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleNameLabel.setPosition(0, 5, Anchor.LEFT | Anchor.TOP);

        this.nameField = new UITextField(this, "Title Here", false);
        this.nameField.setSize(200, 0);
        this.nameField.setPosition(10, 20, Anchor.LEFT | Anchor.TOP);
        this.nameField.setEditable(true);
        this.nameField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        permissionNodeLabel = new UILabel(this, "Permission:");
        permissionNodeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        permissionNodeLabel.setPosition(0, 45, Anchor.LEFT | Anchor.TOP);

        this.permissionField = new UITextField(this, "Permission Here", false);
        this.permissionField.setSize(200, 0);
        this.permissionField.setPosition(10, 60, Anchor.LEFT | Anchor.TOP);
        this.permissionField.setEditable(true);
        this.permissionField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Save Changes button
        this.saveChangesButton = new UIButtonBuilder(this)
                .width(30)
                .text("Save Changes")
                .position(0,-5)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.save");

        editArea.add(titleNameLabel, nameField, permissionNodeLabel, permissionField, saveChangesButton);

        final UILabel titleSelectionLabel = new UILabel(this, "Server Titles:");
        titleSelectionLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleSelectionLabel.setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

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

        form.add(titleLabel, listArea, editArea, titleSelectionLabel, buttonAdd, buttonRemove, buttonClose, buttonApply);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }

    protected Consumer<Task> openWindow(String details) {  // Scheduler
        return task -> {
            // Nothing setup yet.
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {
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
