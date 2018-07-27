/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.nick.NickUtil;
import com.almuradev.almura.feature.nick.asm.mixin.iface.IMixinEntityPlayer;
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
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public final class ClaimGUI extends SimpleScreen {

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;

    private UIFormContainer form;
    private UITextField claimNameField, claimOwnerField, claimGreetingField, claimFarewellField;
    private UILabel claimNameLabel, claimOwnerLabel, claimGreetingLabel, claimFarewellLabel;
    private String claimName, claimOwner, claimGreeting, claimFarewell;
    private boolean isOwner, isTrusted;
    private double econBalance;

    // Features to add
    private EntityPlayer entityPlayer;

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNickManager nickManager;
    @Inject private static ClientNotificationManager notificationManager;

    public ClaimGUI(EntityPlayer entityPlayer, String claimName, String claimOwner, String claimGreeting, String claimFarewell, boolean isOwner, boolean isTrusted, double econBalance) {
        this.entityPlayer = entityPlayer;
        this.claimName = claimName;
        this.claimOwner = claimOwner;
        this.claimGreeting = claimGreeting;
        this.claimFarewell = claimFarewell;

        // Features to Add
        this.isOwner = isOwner;
        this.isTrusted = isTrusted;
        this.econBalance = econBalance;

        // Add/Remove Trusted users [example]
        // claim.addUserTrust(player.getUniqueId(), TrustType.BUILDER);

        // Get list of Trusted Users
        // List<UUID> getUserTrusts(TrustType type);
        // List<UUID> getUserTrusts();

        // Claim Area claim.getArea()

        // Set ClaimType, ADMIN, BASIC, TOWN, SUBDIVISION
        // Boolean Enabled/Disable DENY message to players.


    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Master Panel
        this.form = new UIFormContainer(this, 300, 250, "");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Claim Manager");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Line separator
        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth() - 15, 1);
        separator.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        form.add(separator);

        final UILabel claimNameLabel = new UILabel(this, "Name:");
        claimNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimNameLabel.setPosition(7, 10, Anchor.LEFT | Anchor.TOP);

        this.claimNameField = new UITextField(this, "", false);
        this.claimNameField.setSize(265, 0);
        this.claimNameField.setText(this.claimName);
        this.claimNameField.setPosition(15, claimNameLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);
        this.claimNameField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimOwnerLabel = new UILabel(this, "Greeting:");
        claimOwnerLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimOwnerLabel.setPosition(7, claimNameLabel.getY() + 30, Anchor.LEFT | Anchor.TOP);

        this.claimOwnerField = new UITextField(this, "", false);
        this.claimOwnerField.setSize(265, 0);
        this.claimOwnerField.setText(this.claimOwner);
        this.claimOwnerField.setPosition(15, claimGreetingLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);
        this.claimOwnerField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimGreetingLabel = new UILabel(this, "Greeting:");
        claimGreetingLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimGreetingLabel.setPosition(7, claimNameLabel.getY() + 30, Anchor.LEFT | Anchor.TOP);

        this.claimGreetingField = new UITextField(this, "", false);
        this.claimGreetingField.setSize(265, 0);
        this.claimGreetingField.setText(this.claimGreeting);
        this.claimGreetingField.setPosition(15, claimOwnerLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);
        this.claimGreetingField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimFarewellLabel = new UILabel(this, "Farewell:");
        claimFarewellLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimFarewellLabel.setPosition(7, claimOwnerLabel.getY() + 30, Anchor.LEFT | Anchor.TOP);

        this.claimFarewellField = new UITextField(this, "", false);
        this.claimFarewellField.setSize(265, 0);
        this.claimFarewellField.setText(this.claimFarewell);
        this.claimFarewellField.setPosition(15, claimFarewellLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);
        this.claimFarewellField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UIButton buttonAbandon = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .text("Abandon Claim")
                .listener(this)
                .build("button.abandon");

        final UIButton buttonSave = new UIButtonBuilder(this)
                .width(40)
                .x(-45)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .text("Save")
                .listener(this)
                .build("button.reset");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .text("almura.button.close")
                .listener(this)
                .build("button.close");

        this.form.add(titleLabel, claimNameLabel, claimNameField, claimGreetingLabel, claimGreetingField, claimFarewellLabel, claimFarewellField, buttonAbandon, buttonSave, buttonClose);
        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.abandon":

                break;

            case "button.save":

                break;

            case "button.close":
                this.update = false; // Stop automatic update of name based on textbox
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

        if (++this.lastUpdate > 100) {}
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
