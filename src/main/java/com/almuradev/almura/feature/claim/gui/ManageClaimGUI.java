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

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
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
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ManageClaimGUI extends SimpleScreen {

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;

    private UIFormContainer form, functionsArea, econArea;
    private UITextField claimNameField, claimOwnerField, claimGreetingField, claimFarewellField, claimSizeField, claimValueField, claimTaxField;
    private UILabel claimNameLabel, claimForSaleLabel, claimOwnerLabel, claimGreetingLabel, claimFarewellLabel, claimSizeLabel, claimTaxLabel;
    private UICheckBox showWarningsCheckbox;
    private boolean isOwner, isTrusted, isAdmin;



    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientClaimManager claimManager;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static HeadUpDisplay hudData;

    public ManageClaimGUI(boolean isOwner, boolean isTrusted, boolean isAdmin) {
        this.isOwner = isOwner;
        this.isTrusted = isTrusted;
        this.isAdmin = isAdmin;
    }

    // Add/Remove Trusted users [example]
    // claim.addUserTrust(player.getUniqueId(), TrustType.BUILDER);

    // Get list of Trusted Users
    // List<UUID> getUserTrusts(TrustType type);
    // List<UUID> getUserTrusts();

    // Set ClaimType, ADMIN, BASIC, TOWN, SUBDIVISION
    // Boolean Enabled/Disable DENY message to players.


    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        DecimalFormat dFormat = new DecimalFormat("#.00");

        this.form = new UIFormContainer(this, 400, 250, "");
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

        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth() - 15, 1);
        separator.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        form.add(separator);

        final UILabel claimNameLabel = new UILabel(this, "Name:");
        claimNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimNameLabel.setPosition(7, 8, Anchor.LEFT | Anchor.TOP);

        this.claimNameField = new UITextField(this, "", false);
        this.claimNameField.setSize(150, 0);
        this.claimNameField.setText(claimManager.claimName);
        this.claimNameField.setEditable(isOwner || isAdmin);
        this.claimNameField.setPosition(15, claimNameLabel.getY() + 14, Anchor.LEFT | Anchor.TOP);
        this.claimNameField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        claimSizeLabel = new UILabel(this, "Size:");
        claimSizeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimSizeLabel.setPosition(170, 8, Anchor.LEFT | Anchor.TOP);

        this.claimSizeField = new UITextField(this, "", false);
        this.claimSizeField.setSize(85, 0);
        this.claimSizeField.setText("" + NumberFormat.getNumberInstance(Locale.US).format(claimManager.claimSize));
        this.claimSizeField.setTooltip("Total blocks included in claim");
        this.claimSizeField.setPosition(175, claimNameLabel.getY() + 14, Anchor.LEFT | Anchor.TOP);
        this.claimSizeField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimOwnerLabel = new UILabel(this, "Greeting:");
        claimOwnerLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimOwnerLabel.setPosition(7, claimNameLabel.getY() + 30, Anchor.LEFT | Anchor.TOP);

        this.claimOwnerField = new UITextField(this, "", false);
        this.claimOwnerField.setSize(265, 0);
        this.claimOwnerField.setText(claimManager.claimOwner);
        this.claimOwnerField.setEditable(false);
        this.claimOwnerField.setPosition(15, claimNameLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        this.claimOwnerField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimGreetingLabel = new UILabel(this, "Greeting:");
        claimGreetingLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimGreetingLabel.setPosition(7, claimNameLabel.getY() + 33, Anchor.LEFT | Anchor.TOP);

        this.claimGreetingField = new UITextField(this, "", false);
        this.claimGreetingField.setSize(200, 0);
        this.claimGreetingField.setText(claimManager.claimGreeting);
        this.claimGreetingField.setEditable(isOwner || isAdmin);
        this.claimGreetingField.setPosition(60, claimNameLabel.getY() + 32, Anchor.LEFT | Anchor.TOP);
        this.claimGreetingField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel claimFarewellLabel = new UILabel(this, "Farewell:");
        claimFarewellLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimFarewellLabel.setPosition(7, claimOwnerLabel.getY() + 22, Anchor.LEFT | Anchor.TOP);

        this.claimFarewellField = new UITextField(this, "", false);
        this.claimFarewellField.setSize(200, 0);
        this.claimFarewellField.setText(claimManager.claimFarewell);
        this.claimFarewellField.setEditable(isOwner || isAdmin);
        this.claimFarewellField.setPosition(60, claimOwnerLabel.getY() + 20, Anchor.LEFT | Anchor.TOP);
        this.claimFarewellField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Economy Container
        this.econArea = new UIFormContainer(this, 252, 125, "");
        this.econArea.setPosition(7, claimFarewellLabel.getY() + 20, Anchor.LEFT | Anchor.TOP);
        this.econArea.setMovable(false);
        this.econArea.setClosable(false);
        this.econArea.setBorder(FontColors.WHITE, 1, 185);
        this.econArea.setBackgroundAlpha(215);
        this.econArea.setBottomPadding(3);
        this.econArea.setRightPadding(3);
        this.econArea.setTopPadding(3);
        this.econArea.setLeftPadding(3);
        this.econArea.setVisible(!claimManager.isWilderness);

        final UILabel econTitleLabel = new UILabel(this, "Economy Functions");
        econTitleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        econTitleLabel.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);

        final UISeparator econSeparator = new UISeparator(this);
        econSeparator.setSize(econArea.getWidth() - 15, 1);
        econSeparator.setPosition(0, 15, Anchor.TOP | Anchor.CENTER);

        final UILabel claimValueLabel = new UILabel(this, "Estimated Value:");
        claimValueLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimValueLabel.setPosition(15, 25, Anchor.LEFT | Anchor.TOP);

        claimValueField = new UITextField(this, "", false);
        claimValueField.setSize(100, 0);
        claimValueField.setText("$ 1,456,434.00");
        claimValueField.setEditable(false);
        claimValueField.setPosition(120, claimValueLabel.getY() - 1, Anchor.LEFT | Anchor.TOP);
        claimValueField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        claimTaxLabel = new UILabel(this, "Estimated Taxes:");
        claimTaxLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimTaxLabel.setPosition(15, 45, Anchor.LEFT | Anchor.TOP);

        claimTaxField = new UITextField(this, "", false);
        claimTaxField.setSize(100, 0);
        claimTaxField.setText("$ 56,434.00");
        claimTaxField.setEditable(false);
        claimTaxField.setPosition(120, claimTaxLabel.getY() - 1, Anchor.LEFT | Anchor.TOP);
        claimTaxField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        claimForSaleLabel = new UILabel(this, "<- Claim is For Sale ->");
        claimForSaleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimForSaleLabel.setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM);
        claimForSaleLabel.setVisible(true);
        claimForSaleLabel.setFontOptions(FontOptions.builder()
                .from(FontColors.GREEN_FO)
                .shadow(false)
                .scale(1.2F)
                .build());

        this.econArea.add(econSeparator, econSeparator, econTitleLabel, claimValueLabel, claimValueField, claimTaxLabel, claimTaxField, claimForSaleLabel);

        // Functions Container
        this.functionsArea = new UIFormContainer(this, 110, 200, "");
        this.functionsArea.setPosition(-10, 5, Anchor.RIGHT | Anchor.TOP);
        this.functionsArea.setMovable(false);
        this.functionsArea.setClosable(false);
        this.functionsArea.setBorder(FontColors.WHITE, 1, 185);
        this.functionsArea.setBackgroundAlpha(215);
        this.functionsArea.setBottomPadding(3);
        this.functionsArea.setRightPadding(3);
        this.functionsArea.setTopPadding(3);
        this.functionsArea.setLeftPadding(3);
        this.functionsArea.setVisible(!claimManager.isWilderness);

        final UILabel claimFunctionsLabel = new UILabel(this, "Claim Functions");
        claimFunctionsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        claimFunctionsLabel.setPosition(0, 4, Anchor.CENTER | Anchor.TOP);

        final UISeparator functionsSeparator = new UISeparator(this);
        functionsSeparator.setSize(functionsArea.getWidth() - 15, 1);
        functionsSeparator.setPosition(0, 15, Anchor.TOP | Anchor.CENTER);

        final UIButton buttonAbandon = new UIButtonBuilder(this)
                .width(40)
                .y(20)
                .anchor(Anchor.CENTER | Anchor.TOP)
                .text("Abandon Claim")
                .listener(this)
                .enabled(isOwner || isAdmin)
                .build("button.abandon");

        final UIButton buttonSetForSale = new UIButtonBuilder(this)
                .width(40)
                .y(buttonAbandon.getY() + 20)
                .anchor(Anchor.CENTER | Anchor.TOP)
                .text("List For Sale")
                .listener(this)
                .enabled(isOwner || isAdmin)
                .visible(false)
                .build("button.setForSale");

        final UIButton buttonSetSpawnLocation = new UIButtonBuilder(this)
                .width(40)
                .y(buttonSetForSale.getY() + 20)
                .anchor(Anchor.CENTER | Anchor.TOP)
                .text("Set Spawn")
                .listener(this)
                .enabled(isOwner || isAdmin)
                .visible(false)
                .build("button.setSpawnLocation");

        this.functionsArea.add(claimFunctionsLabel, functionsSeparator, buttonAbandon, buttonSetForSale, buttonSetSpawnLocation);

        this.showWarningsCheckbox = new UICheckBox(this);
        this.showWarningsCheckbox.setText(TextFormatting.WHITE + "Show Permission Denied Messages");
        this.showWarningsCheckbox.setPosition(7, -4, Anchor.LEFT | Anchor.BOTTOM);
        this.showWarningsCheckbox.setChecked(claimManager.showWarnings);
        this.showWarningsCheckbox.setEnabled(isOwner || isAdmin);
        this.showWarningsCheckbox.setName("checkbox.showwarnings");
        this.showWarningsCheckbox.register(this);

        final UIButton buttonSave = new UIButtonBuilder(this)
                .width(40)
                .x(-45)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .text("Save")
                .listener(this)
                .enabled(isOwner || isAdmin)
                .build("button.save");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .text("almura.button.close")
                .listener(this)
                .build("button.close");

        this.form.add(titleLabel, claimNameLabel, claimNameField, claimSizeLabel, claimSizeField, claimGreetingLabel, claimGreetingField, claimFarewellLabel, claimFarewellField, econArea, functionsArea, showWarningsCheckbox,
                buttonSave,
                buttonClose);

        updateValues();

        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.abandon":
                //this.network.sendToServer(new ServerboundClaimGuiAbandonRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName));

                this.close();
                break;

            case "button.save":
                this.network.sendToServer(new ServerboundClaimGuiSaveRequestPacket(this.claimNameField.getText().trim(), this.claimGreetingField.getText().trim(), this.claimFarewellField.getText().trim(), this.mc.player.posX, this.mc.player.posY,
                        this.mc.player.posZ, hudData.worldName));
                this.close();
                break;

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

        if (++this.lastUpdate > 100) {

        }
    }

    public void updateValues() {
        DecimalFormat dFormat = new DecimalFormat("#.00");
        this.claimForSaleLabel.setVisible(claimManager.isForSale);
        this.showWarningsCheckbox.setChecked(claimManager.showWarnings);
        this.showWarningsCheckbox.setVisible(!claimManager.isWilderness);
        this.claimValueField.setText("$ " + TextFormatting.GREEN + dFormat.format(claimManager.claimSize * .10));
        this.claimTaxField.setText("$ " + TextFormatting.YELLOW + dFormat.format(claimManager.claimSize * .02));
        this.functionsArea.setEnabled(!claimManager.isWilderness);
        this.econArea.setVisible(!claimManager.isWilderness);
        if (claimManager.isWilderness) {
            form.setSize(300, 125);
            claimSizeField.setText(" -- Unlimited -- ");
        } else {
            form.setSize(400, 250);
            claimSizeField.setText("" + NumberFormat.getNumberInstance(Locale.US).format(claimManager.claimSize));
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_TAB) {
            if (this.claimNameField.isFocused()) {
                this.claimNameField.setFocused(false);
                this.claimGreetingField.setFocused(true);
                this.claimGreetingField.setCursorPosition(this.claimGreetingField.getContentWidth() + 1, 0);
                return;
            }

            if (this.claimGreetingField.isFocused()) {
                this.claimGreetingField.setFocused(false);
                this.claimFarewellField.setFocused(true);
                this.claimFarewellField.setCursorPosition(this.claimFarewellField.getContentWidth() + 1, 0);
                return;
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
}
