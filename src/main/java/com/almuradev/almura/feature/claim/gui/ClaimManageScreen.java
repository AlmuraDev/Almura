/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.gui;

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiAbandonRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiForSaleRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSetSpawnRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiToggleDenyMessagesRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiToggleVisualsRequestPacket;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
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
public final class ClaimManageScreen extends BasicScreen {

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientClaimManager clientClaimManager;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static HeadUpDisplay hudData;

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;

    private BasicForm form, functionsArea, econArea;
    private UITextField claimNameField, claimOwnerField, claimGreetingField, claimFarewellField, claimSizeField, claimValueField, claimTaxField, claimTaxBalanceField;
    private UITextField forSalePriceField;
    private UILabel claimNameLabel, claimForSaleLabel, claimOwnerLabel, claimGreetingLabel, claimFarewellLabel, claimSizeLabel, claimTaxLabel, claimTaxBalanceLabel;
    private UILabel econTitleLabel, claimValueLabel, claimFunctionsLabel, forSaleLabel;
    private UICheckBox showWarningsCheckbox;
    private UIButton buttonVisuals, buttonHideVisuals, buttonAbandon, buttonSetForSale, buttonSetSpawnLocation;

    private boolean isOwner, isTrusted, isAdmin, toggleVisuals, closeOnAbandon;

    public ClaimManageScreen(boolean isOwner, boolean isTrusted, boolean isAdmin) {
        this.isOwner = isOwner;
        this.isTrusted = isTrusted;
        this.isAdmin = isAdmin;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final DecimalFormat dFormat = new DecimalFormat("###,###,##0.00");

        this.form = new BasicForm(this, 400, 250, "");
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

        this.claimNameLabel = new UILabel(this, "Name:");
        this.claimNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimNameLabel.setPosition(7, 8, Anchor.LEFT | Anchor.TOP);

        this.claimNameField = new UITextField(this, "", false);
        this.claimNameField.setSize(150, 0);
        this.claimNameField.setText(this.clientClaimManager.claimName);
        this.claimNameField.setEditable(this.isOwner || this.isAdmin);
        this.claimNameField.setPosition(60, claimNameLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimNameField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimOwnerLabel = new UILabel(this, "Owner:");
        this.claimOwnerLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimOwnerLabel.setPosition(7, claimNameLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimOwnerField = new UITextField(this, "", false);
        this.claimOwnerField.setSize(150, 0);
        this.claimOwnerField.setText(this.clientClaimManager.claimOwner);
        this.claimOwnerField.setEditable(false);
        this.claimOwnerField.setPosition(60, claimOwnerLabel.getY() - 2 , Anchor.LEFT | Anchor.TOP);
        this.claimOwnerField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimGreetingLabel = new UILabel(this, "Greeting:");
        this.claimGreetingLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimGreetingLabel.setPosition(7, claimOwnerLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimGreetingField = new UITextField(this, "", false);
        this.claimGreetingField.setSize(200, 0);
        this.claimGreetingField.setText(this.clientClaimManager.claimGreeting);
        this.claimGreetingField.setEditable(this.isOwner || this.isAdmin);
        this.claimGreetingField.setPosition(60, claimGreetingLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimGreetingField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimFarewellLabel = new UILabel(this, "Farewell:");
        this.claimFarewellLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimFarewellLabel.setPosition(7, claimGreetingLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimFarewellField = new UITextField(this, "", false);
        this.claimFarewellField.setSize(200, 0);
        this.claimFarewellField.setText(this.clientClaimManager.claimFarewell);
        this.claimFarewellField.setEditable(this.isOwner || this.isAdmin);
        this.claimFarewellField.setPosition(60, claimFarewellLabel.getY() -2, Anchor.LEFT | Anchor.TOP);
        this.claimFarewellField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Economy Container
        this.econArea = new BasicForm(this, 252, 125, "");
        this.econArea.setPosition(7, claimFarewellLabel.getY() + 20, Anchor.LEFT | Anchor.TOP);
        this.econArea.setMovable(false);
        this.econArea.setClosable(false);
        this.econArea.setBorder(FontColors.WHITE, 1, 185);
        this.econArea.setBackgroundAlpha(215);
        this.econArea.setBottomPadding(3);
        this.econArea.setRightPadding(3);
        this.econArea.setTopPadding(3);
        this.econArea.setLeftPadding(3);
        this.econArea.setVisible(!this.clientClaimManager.isWilderness);

        this.econTitleLabel = new UILabel(this, "Economy Functions");
        this.econTitleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.econTitleLabel.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);

        final UISeparator econSeparator = new UISeparator(this);
        econSeparator.setSize(econArea.getWidth() - 15, 1);
        econSeparator.setPosition(0, econTitleLabel.getY() + 13, Anchor.TOP | Anchor.CENTER);

        this.claimValueLabel = new UILabel(this, "Estimated Value:");
        this.claimValueLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimValueLabel.setPosition(15, econSeparator.getY() + 10, Anchor.LEFT | Anchor.TOP);

        this.claimValueField = new UITextField(this, "", false);
        this.claimValueField.setSize(100, 0);
        this.claimValueField.setText("$ 1,456,434.00");
        this.claimValueField.setEditable(false);
        this.claimValueField.setPosition(120, claimValueLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimValueField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimTaxLabel = new UILabel(this, "Estimated Taxes:");
        this.claimTaxLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimTaxLabel.setPosition(15, claimValueLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimTaxField = new UITextField(this, "", false);
        this.claimTaxField.setSize(100, 0);
        this.claimTaxField.setText("$ 56,434.00");
        this.claimTaxField.setEditable(false);
        this.claimTaxField.setPosition(120, this.claimTaxLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimTaxField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimTaxBalanceLabel = new UILabel(this, "Claim Tax Balance:");
        this.claimTaxBalanceLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimTaxBalanceLabel.setPosition(15, claimTaxLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimTaxBalanceField = new UITextField(this, "", false);
        this.claimTaxBalanceField.setSize(100, 0);
        this.claimTaxBalanceField.setText("$ 56,434.00");
        this.claimTaxBalanceField.setEditable(false);
        this.claimTaxBalanceField.setPosition(120, this.claimTaxBalanceLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimTaxBalanceField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.claimForSaleLabel = new UILabel(this, "<- Claim is For Sale ->");
        this.claimForSaleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimForSaleLabel.setPosition(0, 0, Anchor.CENTER | Anchor.BOTTOM);
        this.claimForSaleLabel.setVisible(true);
        this.claimForSaleLabel.setFontOptions(FontOptions.builder()
            .from(FontColors.GREEN_FO)
            .shadow(false)
            .scale(1.2F)
            .build());

        this.claimSizeLabel = new UILabel(this, "Claim Size: (Blocks)");
        this.claimSizeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimSizeLabel.setPosition(15, claimTaxBalanceLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.claimSizeField = new UITextField(this, "", false);
        this.claimSizeField.setSize(95, 0);
        this.claimSizeField.setText("" + NumberFormat.getNumberInstance(Locale.US).format(this.clientClaimManager.claimSize));
        this.claimSizeField.setTooltip("Total blocks included in claim");
        this.claimSizeField.setPosition(125, this.claimSizeLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.claimSizeField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.forSaleLabel = new UILabel(this, "For Sale Price:");
        this.forSaleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.forSaleLabel.setPosition(15, claimSizeLabel.getY() + 25, Anchor.LEFT | Anchor.TOP);

        this.forSalePriceField = new UITextField(this, "", false);
        this.forSalePriceField.setSize(100, 0);
        this.forSalePriceField.setText("" + NumberFormat.getNumberInstance(Locale.US).format(this.clientClaimManager.claimSalePrice));
        this.forSalePriceField.setTooltip("Sale Price of this Claim");
        this.forSalePriceField.setPosition(120, this.forSaleLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        this.forSalePriceField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.econArea.add(econSeparator, econSeparator, econTitleLabel, claimValueLabel, this.claimValueField, this.claimTaxLabel,
            this.claimTaxField, this.claimForSaleLabel, this.claimTaxBalanceLabel, this.claimTaxBalanceField, this.claimSizeLabel,
            this.claimSizeField, this.forSaleLabel, this.forSalePriceField);

        // Functions Container
        this.functionsArea = new BasicForm(this, 110, 200, "");
        this.functionsArea.setPosition(-10, 5, Anchor.RIGHT | Anchor.TOP);
        this.functionsArea.setMovable(false);
        this.functionsArea.setClosable(false);
        this.functionsArea.setBorder(FontColors.WHITE, 1, 185);
        this.functionsArea.setBackgroundAlpha(215);
        this.functionsArea.setBottomPadding(3);
        this.functionsArea.setRightPadding(3);
        this.functionsArea.setTopPadding(3);
        this.functionsArea.setLeftPadding(3);
        this.functionsArea.setVisible(!this.clientClaimManager.isWilderness);

        this.claimFunctionsLabel = new UILabel(this, "Claim Functions");
        this.claimFunctionsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.claimFunctionsLabel.setPosition(0, 4, Anchor.CENTER | Anchor.TOP);

        final UISeparator functionsSeparator = new UISeparator(this);
        functionsSeparator.setSize(this.functionsArea.getWidth() - 15, 1);
        functionsSeparator.setPosition(0, 15, Anchor.TOP | Anchor.CENTER);

        this.buttonVisuals = new UIButton(this);
        this.buttonVisuals.setText(TextFormatting.WHITE + "Toggle Visuals");
        this.buttonVisuals.setPosition(0, 20, Anchor.CENTER | Anchor.TOP);
        this.buttonVisuals.setEnabled(this.isOwner || this.isAdmin);
        this.buttonVisuals.setName("button.visuals");
        // this.buttonVisuals.setVisible(this.clientClaimManager.hasWECUI);
        this.buttonVisuals.register(this);

        buttonAbandon = new UIButtonBuilder(this)
            .width(40)
            .y(buttonVisuals.getY() + 20)
            .anchor(Anchor.CENTER | Anchor.TOP)
            .text("Abandon Claim")
            .listener(this)
            .enabled(this.isOwner || this.isAdmin)
            .build("button.abandon");

        buttonSetForSale = new UIButtonBuilder(this)
            .width(40)
            .y(buttonAbandon.getY() + 20)
            .anchor(Anchor.CENTER | Anchor.TOP)
            .text("For Sale")
            .listener(this)
            .enabled(this.isOwner || this.isAdmin)
            .visible(true)
            .build("button.toggleforsale");

        buttonSetSpawnLocation = new UIButtonBuilder(this)
            .width(40)
            .y(buttonSetForSale.getY() + 20)
            .anchor(Anchor.CENTER | Anchor.TOP)
            .text("Set Spawn")
            .listener(this)
            .enabled(this.isOwner || this.isAdmin)
            .visible(true)
            .build("button.setspawnlocation");

        this.functionsArea.add(claimFunctionsLabel, functionsSeparator, buttonAbandon, buttonSetForSale, buttonSetSpawnLocation, buttonVisuals);

        this.showWarningsCheckbox = new UICheckBox(this);
        this.showWarningsCheckbox.setText(TextFormatting.WHITE + "Show Permission Denied Messages");
        this.showWarningsCheckbox.setPosition(7, 0, Anchor.LEFT | Anchor.BOTTOM);
        this.showWarningsCheckbox.setChecked(this.clientClaimManager.showWarnings);
        this.showWarningsCheckbox.setEnabled(this.isOwner || this.isAdmin);
        this.showWarningsCheckbox.setName("checkbox.showwarnings");
        this.showWarningsCheckbox.setVisible(!this.clientClaimManager.isWilderness);
        this.showWarningsCheckbox.register(this);

        this.buttonHideVisuals = new UIButton(this);
        this.buttonHideVisuals.setText(TextFormatting.WHITE + "Hide Visuals");
        this.buttonHideVisuals.setPosition(0, 0, Anchor.LEFT | Anchor.BOTTOM);
        this.buttonHideVisuals.setEnabled(this.isOwner || this.isAdmin);
        this.buttonHideVisuals.setName("button.visuals");
        this.buttonHideVisuals.register(this);

        final UIButton buttonSave = new UIButtonBuilder(this)
            .width(40)
            .x(-45)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .text("Save")
            .listener(this)
            .enabled(this.isOwner || this.isAdmin)
            .build("button.save");

        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .text("almura.button.close")
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, separator, claimNameLabel, this.claimNameField, this.claimOwnerLabel, this.claimOwnerField, claimGreetingLabel, this.claimGreetingField, claimFarewellLabel, this.claimFarewellField, this.econArea, this
            .functionsArea, this.showWarningsCheckbox, buttonHideVisuals, buttonSave, buttonClose);

        updateValues();

        addToScreen(this.form);
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "checkbox.showwarnings":
                this.network.sendToServer(new ServerboundClaimGuiToggleDenyMessagesRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc
                    .player.posZ, hudData.worldName, !this.showWarningsCheckbox.isChecked()));
                break;
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.visuals":
                this.toggleVisuals = !this.toggleVisuals;
                if (this.clientClaimManager.isWilderness) {
                    this.toggleVisuals = false;  // Forces disable.
                }
                this.network.sendToServer(new ServerboundClaimGuiToggleVisualsRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName, this.toggleVisuals));
                break;

            case "button.toggleforsale":
                Double value = Double.valueOf(this.forSalePriceField.getText().replace("$","").replace("§b","").trim());

                if (!this.clientClaimManager.isWilderness) {
                    if (!this.clientClaimManager.isForSale) {
                        BasicMessageBox.showDialog(this, "List Claim For Sale",
                            "Do you wish to list this claim for sale?",
                            MessageBoxButtons.YES_NO, (result) -> {
                                if (result != MessageBoxResult.YES) return;
                                this.network.sendToServer(new ServerboundClaimGuiForSaleRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName, true, value));
                            });
                    } else {
                        BasicMessageBox.showDialog(this, "Delist Claim For Sale",
                            "Do you wish to delist this claim?",
                            MessageBoxButtons.YES_NO, (result) -> {
                                if (result != MessageBoxResult.YES) return;
                                this.network.sendToServer(new ServerboundClaimGuiForSaleRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName, false, value));
                            });
                    }
                }

                break;

            case "button.setspawnlocation":
                this.network.sendToServer(new ServerboundClaimGuiSetSpawnRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName));
                break;

            case "button.save":
                this.network.sendToServer(new ServerboundClaimGuiSaveRequestPacket(this.claimNameField.getText().trim(), this.claimGreetingField.getText().trim(), this.claimFarewellField.getText().trim(), this.mc.player.posX, this.mc.player.posY,
                    this.mc.player.posZ, hudData.worldName));
                this.close();
                break;

            case "button.abandon":
                BasicMessageBox.showDialog(this, "Abandon Claim",
                    "Do you wish to abandon this claim?",
                    MessageBoxButtons.YES_NO, (result) -> {
                        if (result != MessageBoxResult.YES) return;
                        this.network.sendToServer(new ServerboundClaimGuiAbandonRequestPacket(this.mc.player.posX, this.mc.player.posY, this.mc.player.posZ, hudData.worldName));
                        this.closeOnAbandon = true;
                    });
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
        if (closeOnAbandon) {
            this.close();
        }
        final DecimalFormat dFormat = new DecimalFormat("###,###,##0.00");
        this.claimForSaleLabel.setVisible(this.clientClaimManager.isForSale);
        this.showWarningsCheckbox.setChecked(this.clientClaimManager.showWarnings);
        this.showWarningsCheckbox.setVisible(!this.clientClaimManager.isWilderness);
        this.claimValueField.setText("$ " + TextFormatting.GREEN + dFormat.format(this.clientClaimManager.claimBlockCost * this.clientClaimManager.claimSize));
        this.claimTaxField.setText("$ " + TextFormatting.YELLOW + dFormat.format(this.clientClaimManager.claimTaxes));
        this.claimTaxBalanceField.setText("$ " + TextFormatting.RED + dFormat.format(this.clientClaimManager.claimTaxBalance));
        this.forSalePriceField.setText("$ " + TextFormatting.AQUA + dFormat.format(this.clientClaimManager.claimSalePrice));
        this.econArea.setVisible(!this.clientClaimManager.isWilderness);
        this.functionsArea.setVisible(!this.clientClaimManager.isWilderness);

        if (this.functionsArea.isVisible()) {
            this.buttonHideVisuals.setVisible(false);
            this.buttonVisuals.setEnabled(true);
        } else {
            this.buttonHideVisuals.setVisible(true);
        }

        if (this.clientClaimManager.isWilderness) {
            this.form.setSize(300, 125);
            this.claimSizeField.setText(" -- Unlimited -- ");
        } else {
            this.form.setSize(400, 250);
            this.claimSizeField.setText("" + NumberFormat.getNumberInstance(Locale.US).format(this.clientClaimManager.claimSize));
        }

        if (this.clientClaimManager.isForSale) {
            this.buttonSetForSale.setText(("Delist For Sale"));
        } else {
            this.buttonSetForSale.setText(("List For Sale"));
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
