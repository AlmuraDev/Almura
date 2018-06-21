package com.almuradev.almura.feature.title.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.feature.title.network.ServerboundPlayerSetTitlePacket;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class TitleGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel titleLabel;
    private UISelect titlesSelector;
    private UIFormContainer form;

    private EntityPlayer player;
    private Set<String> titles;

    @Inject private static ClientTitleManager clientTitleManager;
    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public TitleGUI(EntityPlayer player, Set<String> set) {
        this.player = player;
        this.titles = set;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Pane
        form = new UIFormContainer(this, 300, 130, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Titles");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

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

        final UILabel titleSelectionLabel = new UILabel(this, "Select Title:");
        titleSelectionLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleSelectionLabel.setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

        // Title Selection dropdown
        titlesSelector = new UISelect<>(this,
                110,
                titles
        );
        titlesSelector.setPosition(10, 20, Anchor.LEFT | Anchor.TOP);
        titlesSelector.setOptionsWidth(UISelect.SELECT_WIDTH);
        if (clientTitleManager.getTitle(Minecraft.getMinecraft().player.getUniqueID()) != null) {
            titlesSelector.select(clientTitleManager.getTitle(Minecraft.getMinecraft().player.getUniqueID()));
        }
        titlesSelector.maxDisplayedOptions(7);
        titlesSelector.setName("selector");
        titlesSelector.register(this);

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

        form.add(titleLabel, listArea, playerArea, titleSelectionLabel, titlesSelector, buttonClose, buttonApply);

        addToScreen(form);
    }

    @Subscribe
    public void onUISelect(UISelect.SelectEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "selector":
                if (event.getNewValue() != null) {
                    clientTitleManager.temporaryTitle = event.getNewValue().toString();
                }
                break;
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.apply":
                clientNotificationManager.queuePopup(new PopupNotification(Text.of("Nickname"), Text.of("Updating Title on server...."),2));
                    if (titlesSelector.getSelectedValue() != null) {
                        network.sendToServer(new ServerboundPlayerSetTitlePacket(titlesSelector.getSelectedValue().toString().trim()));
                    }
                    close();
                    break;

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

        // Set location of Player Entity within Panel
        int i = form.screenX() + 210;
        int j = form.screenY() + 25;

        // Draw Player
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.mc.player);

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
