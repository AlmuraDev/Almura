/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.shop.gui;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

public class ModifyItemsGUI extends SimpleScreen {
    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel titleLabel, itemNameLabel;
    private UIButton buttonCancel, buttonOk;
    private UIFormContainer form;
    private MalisisInventoryContainer container;

    private int screenWidth = 300;
    private int screenHeight = 100;

    private ItemStack itemStack;
    private int quantity;
    private int type; // Buy = 1, Sell = 2, Modify = 3
    private double price;
    private boolean isNew;

    @Inject
    @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public ModifyItemsGUI (EntityPlayer player, ItemStack itemStack, int type, int quantity, double price, boolean isNew) {
        this.itemStack = itemStack;
        this.quantity = quantity;
        this.isNew = isNew;
        this.type = type;
        this.price = price;
        this.setInventoryContainer(new MalisisInventoryContainer(player, 0));
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

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
        form.setClipContent(false);

        titleLabel = new UILabel(this, "Modify Item");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Tab Area
        final UIFormContainer itemArea = new UIFormContainer(this, 285, 60, "");
        itemArea.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);
        itemArea.setMovable(false);
        itemArea.setClosable(false);
        itemArea.setBorder(FontColors.WHITE, 1, 185);
        itemArea.setBackgroundAlpha(215);
        itemArea.setPadding(3, 3);

        final UISlot itemSlot = new UISlot(this, new MalisisSlot(itemStack));
        itemSlot.setPosition(10, 0, Anchor.LEFT | Anchor.MIDDLE);

        itemNameLabel = new UILabel(this, "Item:");
        itemNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        itemNameLabel.setPosition(40,0, Anchor.LEFT | Anchor.MIDDLE);

        // Cancel button
        final UIButton buttonOk = new UIButtonBuilder(this)
                .position(-20, 0)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text("Cancel")
                .listener(this)
                .build("button.cancel");

        // OK button
        final UIButton buttonCancel = new UIButtonBuilder(this)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text("OK")
                .listener(this)
                .build("button.ok");

        UIPlayerInventory playerInv = new UIPlayerInventory(this, inventoryContainer.getPlayerInventory());
        playerInv.setPosition(0, 100, Anchor.CENTER | Anchor.BOTTOM);

        form.add(titleLabel, itemArea, itemSlot, itemNameLabel, buttonOk, buttonCancel, playerInv);

        // Detect if screen area is large enough to display.
        if (screenWidth > resolution.getScaledWidth() || screenHeight > resolution.getScaledHeight()) {
            clientNotificationManager.queuePopup(new PopupNotification(Text.of("NPC Shop Error"), Text.of("Screen area of: " + screenHeight + " x " + screenWidth + " required."), 5));
            this.close();
        } else {
            addToScreen(form);
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.cancel":
                close();
                break;

            case "button.ok":
                close();
                // Do something magical here!
                break;
        }
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
