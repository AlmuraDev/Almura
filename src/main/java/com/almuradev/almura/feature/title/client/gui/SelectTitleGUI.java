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
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class SelectTitleGUI extends SimpleScreen {

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static ClientTitleManager titleManager;

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UISelect<Title> titlesSelector;
    private UIFormContainer form;

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Pane
        this.form = new UIFormContainer(this, 300, 130, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Titles")
            .setFontOptions(FontOptions
                .builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Player Render Area
        final UIFormContainer playerArea = new UIFormContainer(this, 75, 85, "");
        playerArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        playerArea.setMovable(false);
        playerArea.setClosable(false);
        playerArea.setBorder(FontColors.WHITE, 1, 185);
        playerArea.setBackgroundAlpha(215);
        playerArea.setBottomPadding(3);
        playerArea.setRightPadding(3);
        playerArea.setTopPadding(3);
        playerArea.setLeftPadding(3);

        // Title List Area
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

        final UILabel titleSelectionLabel = new UILabel(this, "Select Title:")
            .setFontOptions(FontOptions
                .builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

        // Title Selection dropdown
        this.titlesSelector = new UISelect<>(this, 110, titleManager.getAvailableTitles())
            .setPosition(10, 20, Anchor.LEFT | Anchor.TOP)
            .setOptionsWidth(UISelect.SELECT_WIDTH)
            .maxDisplayedOptions(7)
            .setLabelFunction(Title::getContent)
            .setName("combobox.select_title")
            .register(this);

        final Title selectedTitle = titleManager.getSelectedTitleFor(this.mc.player.getUniqueID());
        if (selectedTitle != null) {
            this.titlesSelector.select(selectedTitle);
        }

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

        this.form.add(titleLabel, listArea, playerArea, titleSelectionLabel, this.titlesSelector, buttonClose, buttonApply);

        this.addToScreen(this.form);
    }

    @Subscribe
    public void onUISelect(UISelect.SelectEvent<Title> event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "combobox.select_title":
                titleManager.setTitleContentForDisplay(event.getNewValue().copy());
                break;
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.apply":
                notificationManager.queuePopup(new PopupNotification(Text.of("Title"), Text.of("Updating Title on server..."), 2));
                titleManager.setTitleContentForDisplay(null);

                final Title selectedTitle = this.titlesSelector.getSelectedValue();
                network.sendToServer(new ServerboundSelectedTitlePacket(selectedTitle != null ? selectedTitle.getId() : null));
            case "button.close":
                this.close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        // Set location of Player Entity within Panel
        int i = this.form.screenX() + 210;
        int j = this.form.screenY() + 25;

        // Draw Player
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.mc.player);

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.
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
