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
import com.almuradev.almura.feature.title.TitleModifyType;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.TextUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ManageTitlesGUI extends SimpleScreen {

    @Inject private static ClientTitleManager titleManager;
    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static PluginContainer container;

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel modeNameLabel, titleSelectionLabel;
    private UITextField idField, permissionField, contentField;
    private UIButton buttonAdd, buttonDelete, buttonColor, buttonApply, buttonRemove;
    private UIDynamicList<Title> titleList;
    private UICheckBox hiddenCheckbox, formattedCheckbox, editModeCheckbox;
    private UISelect<String> colorSelector;
    private UIFormContainer form, listArea, editArea, playerArea;

    private int screenWidth = 450;
    private int screenHeight = 300;

    private TitleModifyType mode;
    private boolean isAdmin, inEditMode = false;

    public ManageTitlesGUI(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

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

        final UILabel titleLabel = new UILabel(this, "Title Manager")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Title List Area
        this.listArea = new UIFormContainer(this, 220, 260, "");
        this.listArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        this.listArea.setMovable(false);
        this.listArea.setClosable(false);
        this.listArea.setBorder(FontColors.WHITE, 1, 185);
        this.listArea.setBackgroundAlpha(215);
        this.listArea.setBottomPadding(3);
        this.listArea.setRightPadding(3);
        this.listArea.setTopPadding(3);
        this.listArea.setLeftPadding(3);

        // Create left container
        final UIContainer<?> titleContainer = new UIContainer(this, 200, UIComponent.INHERITED);
        titleContainer.setBackgroundAlpha(0);
        titleContainer.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);
        titleContainer.setPadding(4, 4);
        titleContainer.setTopPadding(20);

        this.titleList = new UIDynamicList<>(this, UIComponent.INHERITED, UIComponent.INHERITED);
        this.titleList.setItemComponentFactory(TitleItemComponent::new);
        this.titleList.setItemComponentSpacing(1);
        this.titleList.setCanDeselect(false);
        this.titleList.setName("list.left");
        this.titleList.setItems(titleManager.getAvailableTitles());
        if (!titleManager.getAvailableTitles().isEmpty()) {
            if (titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()) != null) {
                titleManager.setTitleContentForDisplay(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()));
                this.titleList.setSelectedItem(titleManager.getAvailableTitle(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()).getId()));
            } else {
                this.titleList.setSelectedItem(titleManager.getAvailableTitles().get(0));
                titleManager.setTitleContentForDisplay(titleManager.getAvailableTitles().get(0));
            }
        }
        this.titleList.register(this);

        titleContainer.add(this.titleList);
        this.listArea.add(titleContainer);

        // Edit Container
        this.editArea = new UIFormContainer(this, 220, 260, "");
        this.editArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        this.editArea.setMovable(false);
        this.editArea.setClosable(false);
        this.editArea.setBorder(FontColors.WHITE, 1, 185);
        this.editArea.setBackgroundAlpha(215);
        this.editArea.setBottomPadding(3);
        this.editArea.setRightPadding(3);
        this.editArea.setTopPadding(3);
        this.editArea.setLeftPadding(3);
        this.editArea.setVisible(false);

        this.modeNameLabel = new UILabel(this, "Modify Title")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .underline(false)
                .scale(1.2F)
                .build()
            )
            .setVisible(false)
            .setPosition(0, 5, Anchor.CENTER | Anchor.TOP);

        final UILabel idLabel = new UILabel(this, "ID:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.0F)
                .build()
            )
            .setPosition(5, 17, Anchor.LEFT | Anchor.TOP);

        this.idField = new UITextField(this, "", false)
            .setSize(200, 0)
            .setPosition(10, 30, Anchor.LEFT | Anchor.TOP)
            .setEditable(false)
            .setEnabled(false)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        final UILabel permissionNodeLabel = new UILabel(this, "Permission:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.0F)
                .build()
            ).setPosition(5, 57, Anchor.LEFT | Anchor.TOP);

        this.permissionField = new UITextField(this, "", false)
            .setSize(200, 0)
            .setPosition(10, 70, Anchor.LEFT | Anchor.TOP)
            .setEditable(false)
            .setEnabled(false)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        final UILabel titleContextLabel = new UILabel(this, "Content:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.0F)
                .build()
            ).setPosition(5, 97, Anchor.LEFT | Anchor.TOP);

        this.contentField = new UITextField(this, "", false)
            .setSize(200, 0)
            .setPosition(10, 110, Anchor.LEFT | Anchor.TOP)
            .setEditable(true)
            .setEnabled(false)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)

                .shadow(false)
                .build()
            );

        // Color Selection dropdown
        this.colorSelector = new UISelect<>(this,
            110,
            Arrays.asList(
                "§1Dark Blue§f - &1",
                "§9Blue§f - &9",
                "§3Dark Aqua§f - &3",
                "§bAqua§f - &b",
                "§4Dark Red§f - &4",
                "§cRed§f - &c",
                "§eYellow§f - &e",
                "§6Gold§f - &6",
                "§2Dark Green§f - &2",
                "§aGreen§f - &a",
                "§5Dark Purple§f - &5",
                "§dLight Purple§f - &d",
                "§fWhite§f - &f",
                "§7Gray§f - &7",
                "§8Dark Gray§f - &8",
                "§0Black§f - &0,",
                "§lBold§f - &l",
                "§mStrikethrough§f - &m",
                "§nUnderline§f - &n",
                "§oItalic§f - &o")
        );

        this.colorSelector.setPosition(10, 126, Anchor.LEFT | Anchor.TOP);
        this.colorSelector.setOptionsWidth(UISelect.SELECT_WIDTH);
        this.colorSelector.setFontOptions(FontOptions.builder()
            .shadow(false)
            .build()
        );
        this.colorSelector.select("§1Dark Blue§f - &1");
        this.colorSelector.setEnabled(false);
        this.colorSelector.maxDisplayedOptions(7);

        // Add Color character button
        this.buttonColor = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.TOP | Anchor.LEFT)
            .position(130, 125)
            .text(Text.of("Add"))
            .listener(this)
            .build("button.color");

        this.formattedCheckbox = new UICheckBox(this);
        this.formattedCheckbox.setText(TextFormatting.WHITE + "Formatted");
        this.formattedCheckbox.setPosition(9, 145, Anchor.LEFT | Anchor.TOP);
        this.formattedCheckbox.setChecked(false);
        this.formattedCheckbox.setName("checkbox.formatted");
        this.formattedCheckbox.register(this);

        this.hiddenCheckbox = new UICheckBox(this);
        this.hiddenCheckbox.setText(TextFormatting.WHITE + "Title Hidden from Players");
        this.hiddenCheckbox.setPosition(10, -7, Anchor.LEFT | Anchor.BOTTOM);
        this.hiddenCheckbox.setChecked(false);
        this.hiddenCheckbox.setName("checkbox.hidden");
        this.hiddenCheckbox.register(this);

        // Save Changes button
        final UIButton saveChangesButton = new UIButtonBuilder(this)
            .width(30)
            .text(Text.of("almura.button.save"))
            .position(0, -5)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .listener(this)
            .build("button.save");

        editArea.add(this.modeNameLabel, idLabel, permissionNodeLabel,
            this.permissionField, this.idField, titleContextLabel, this.contentField,
            this.colorSelector, this.buttonColor, this.formattedCheckbox, this.hiddenCheckbox, saveChangesButton);

        // Edit Container
        this.playerArea = new UIFormContainer(this, 220, 260, "");
        this.playerArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        this.playerArea.setMovable(false);
        this.playerArea.setClosable(false);
        this.playerArea.setBorder(FontColors.WHITE, 1, 185);
        this.playerArea.setBackgroundAlpha(215);
        this.playerArea.setBottomPadding(3);
        this.playerArea.setRightPadding(3);
        this.playerArea.setTopPadding(3);
        this.playerArea.setLeftPadding(3);
        this.playerArea.setVisible(true);

        final UILabel playerLabel = new UILabel(this, "Player Title")
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)
                        .shadow(true)
                        .underline(false)
                        .scale(1.2F)
                        .build()
                )
                .setVisible(false)
                .setPosition(0, 05, Anchor.CENTER | Anchor.TOP);

        // Apply button
        this.buttonApply = new UIButtonBuilder(this)
                .text(Text.of(TextColors.WHITE, "Apply"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .tooltip("Apply Title and Exit")
                .listener(this)
                .visible(true)
                .build("button.apply");

        // Apply button
        this.buttonRemove = new UIButtonBuilder(this)
                .text(Text.of(TextColors.WHITE, "Remove"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .tooltip("Remove Title and Exit")
                .listener(this)
                .visible(true)
                .build("button.remove");

        this.playerArea.add(playerLabel, this.buttonApply, this.buttonRemove);

        this.titleSelectionLabel = new UILabel(this, "Available Titles:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
            .width(15)
            .x(5)
            .text(Text.of(TextColors.GREEN, "+"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .tooltip("Add New Title")
            .listener(this)
            .visible(false)
            .build("button.add");

        // Remove button
        this.buttonDelete = new UIButtonBuilder(this)
            .width(15)
            .x(24)
            .text(Text.of(TextColors.RED, "-"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .tooltip("Remove Title")
            .listener(this)
            .visible(false)
            .build("button.delete");

        this.editModeCheckbox = new UICheckBox(this);
        this.editModeCheckbox.setText(TextFormatting.WHITE + "Edit Mode");
        this.editModeCheckbox.setPosition(0, -3, Anchor.CENTER | Anchor.BOTTOM);
        this.editModeCheckbox.setChecked(false);
        this.editModeCheckbox.setName("checkbox.editmode");
        this.editModeCheckbox.setVisible(isAdmin);
        this.editModeCheckbox.register(this);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .text(Text.of("almura.button.close"))
            .listener(this)
            .build("button.close");

        this.form.add(titleLabel, this.listArea, this.editArea, this.playerArea, this.titleSelectionLabel, this.buttonAdd, this.buttonDelete, this
            .editModeCheckbox, buttonClose);

        this.addToScreen(this.form);
    }

    @Subscribe
    public void onUIListClickEvent(UIDynamicList.SelectEvent<Title> event) {
        if (event.getNewValue() != null) {
            if (this.editModeCheckbox.isChecked()) {
                this.permissionField.setText(event.getNewValue().getPermission());
                this.permissionField.setEditable(true);
                this.permissionField.setEnabled(true);
                this.idField.setText(event.getNewValue().getId());
                this.idField.setEditable(false);
                this.contentField.setText(event.getNewValue().getContent());
                this.contentField.setEditable(true);
                this.contentField.setEnabled(true);
                this.modeNameLabel.setText("Modify Title");
                this.hiddenCheckbox.setEnabled(true);
                this.hiddenCheckbox.setChecked(event.getNewValue().isHidden());
                this.formattedCheckbox.setChecked(true);
                this.formattedCheckbox.setEnabled(true);
                this.colorSelector.setEnabled(true);
                this.buttonColor.setEnabled(true);
                this.mode = TitleModifyType.MODIFY;
                this.modeNameLabel.setVisible(true);
                this.buttonDelete.setEnabled(true);
                this.formatContent(this.formattedCheckbox.isChecked());
            } else {
                titleManager.setTitleContentForDisplay(event.getNewValue().copy());
            }
        }
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "checkbox.formatted":
                this.formatContent(!this.formattedCheckbox.isChecked());
                break;
            case "checkbox.editmode":
                this.buttonAdd.setVisible(!this.editModeCheckbox.isChecked());
                this.buttonDelete.setVisible(!this.editModeCheckbox.isChecked());
                this.editArea.setVisible(!this.editModeCheckbox.isChecked());
                this.playerArea.setVisible(this.editModeCheckbox.isChecked());

                if (!this.editModeCheckbox.isChecked()) {
                    this.titleSelectionLabel.setText("Server Titles:");
                    this.titleList.clearItems().setItems(titleManager.getTitles());
                    if (!titleManager.getTitles().isEmpty()) {
                        this.titleList.setSelectedItem(titleManager.getTitles().get(0));
                        this.idField.setText((titleManager.getTitles().get(0).getId()));
                        this.permissionField.setText((titleManager.getTitles().get(0).getPermission()));
                        this.contentField.setText((titleManager.getTitles().get(0).getContent()));
                    }
                    this.mode = TitleModifyType.MODIFY;
                    this.inEditMode = true;
                } else {
                    this.titleSelectionLabel.setText("Available Titles:");
                    this.titleList.clearItems().setItems(titleManager.getAvailableTitles());
                    if (titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()) != null) {
                        titleManager.setTitleContentForDisplay(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()));
                        this.titleList.setSelectedItem(titleManager.getAvailableTitle(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()).getId()));
                    } else {
                        this.titleList.setSelectedItem(titleManager.getAvailableTitles().get(0));
                        titleManager.setTitleContentForDisplay(titleManager.getAvailableTitles().get(0));
                    }
                    this.inEditMode = false;
                }

                break;
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        final String colorCode = this.colorSelector.getSelectedOption().getLabel().substring(0, 2);
        final UITextField.CursorPosition cursorPos = this.contentField.getCursorPosition();

        switch (event.getComponent().getName().toLowerCase()) {
            case "button.color":
                this.contentField.addText(colorCode);
                this.formatContent(this.formattedCheckbox.isChecked());
                this.contentField.setCursorPosition(cursorPos.getXOffset(), cursorPos.getYOffset());
                this.contentField.setFocused(true);

                break;

            case "button.apply":
                notificationManager.queuePopup(new PopupNotification(Text.of("Title"), Text.of("Setting Title on server..."), 2));
                titleManager.setTitleContentForDisplay(null);
                titleManager.requestSelectedTitle(this.titleList.getSelectedItem());
                this.close();
                break;

            case "button.remove":
                notificationManager.queuePopup(new PopupNotification(Text.of("Title"), Text.of("Removing Title on server..."), 2));
                titleManager.setTitleContentForDisplay(null);
                titleManager.requestSelectedTitle(null);
                this.close();

                break;

            case "button.add":
                this.modeNameLabel.setText("Add New Title");
                // Clear Fields & Unlock
                this.idField.setText("");
                this.idField.setEditable(true);
                this.idField.setEnabled(true);
                this.permissionField.setText("almura.title.");
                this.permissionField.setEditable(true);
                this.permissionField.setEnabled(true);
                this.contentField.setText("");
                this.contentField.setEditable(true);
                this.contentField.setEnabled(true);
                this.buttonColor.setEnabled(true);
                this.colorSelector.setEnabled(true);
                this.formattedCheckbox.setEnabled(true);
                this.hiddenCheckbox.setEnabled(true);
                this.hiddenCheckbox.setChecked(false);
                this.mode = TitleModifyType.ADD;
                this.modeNameLabel.setVisible(true);
                this.buttonDelete.setEnabled(false);
                break;

            case "button.delete":
                this.mode = TitleModifyType.DELETE;
                notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Removing selected title"), 2));
                if (this.idField.getText().toLowerCase().trim().equalsIgnoreCase(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()).getId())) {
                    titleManager.requestSelectedTitle(null); // remove the title as the selected title from the user if its the one begin deleted.
                }
                titleManager.setTitleContentForDisplay(null);
                titleManager.deleteTitle(this.idField.getText().toLowerCase().trim());

                this.lockForm();
                break;

            case "button.save":
                switch (this.mode) {
                    case ADD:
                        notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Adding new Title"), 2));
                        titleManager.addTitle(this.idField.getText().toLowerCase().trim(), this.permissionField.getText().toLowerCase().trim(), this.contentField.getText().trim(), this.hiddenCheckbox.isChecked());
                        break;
                    case MODIFY:
                        notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Saving Title Changes"), 2));
                        titleManager.modifyTitle(this.idField.getText().toLowerCase().trim(), this.permissionField.getText().toLowerCase().trim(), this.contentField.getText().trim(), this.hiddenCheckbox.isChecked());
                        break;
                }

                this.lockForm();
                break;
            case "button.close":
                titleManager.setTitleContentForDisplay(null);
                this.close();
                break;
        }
    }

    private void formatContent(boolean value) {
        final String currentTitleContent = this.contentField.getText();
        final UITextField.CursorPosition cursorPos = this.contentField.getCursorPosition();

        if (!value) {
            // Need to convert the content from sectional -> ampersand
            this.contentField.setText(TextUtil.asUglyText(currentTitleContent));
        } else {
            // Need to convert the content from ampersand -> sectional
            this.contentField.setText(TextUtil.asFriendlyText(currentTitleContent));
        }

        this.contentField.setCursorPosition(cursorPos.getXOffset(), cursorPos.getYOffset());
        this.contentField.setFocused(true);
    }

    private void lockForm() {
        this.permissionField.setEditable(false);
        this.permissionField.setEnabled(false);
        this.permissionField.setText("");
        this.idField.setEditable(false);
        this.idField.setEnabled(false);
        this.idField.setText("");
        this.contentField.setEditable(false);
        this.contentField.setEnabled(false);
        this.contentField.setText("");
        this.modeNameLabel.setVisible(false);
        this.buttonColor.setEnabled(false);
        this.colorSelector.setEnabled(false);
        this.formattedCheckbox.setEnabled(false);
        this.hiddenCheckbox.setEnabled(false);
        this.buttonDelete.setEnabled(false);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (this.playerArea.isVisible()) {
            int i = this.form.screenX() + 285;
            int j = this.form.screenY() + 175;

            if (this.playerArea.isHovered()) {
                GuiInventory.drawEntityOnScreen(i + 51, j + 75, 80, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - j - 25, this.mc.player);
            } else {
                GuiInventory.drawEntityOnScreen(i + 51, j + 75, 80, (float) (i + 51) - i - 25, (float) (j + 75 - 50) - j - 25, this.mc.player);
            }
        }

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_TAB) {
            if (this.idField.isFocused()) {
                this.idField.setFocused(false);
                this.permissionField.setFocused(true);
                this.permissionField.setCursorPosition(this.permissionField.getContentWidth() + 1, 0);  // Set position to last character
                return;
            }

            if (this.permissionField.isFocused()) {
                this.permissionField.setFocused(false);
                this.contentField.setFocused(true);
                this.contentField.setCursorPosition(0, 0);
                return;
            }
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            titleManager.setTitleContentForDisplay(null);
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

    // Ordi's or Grinch's GUI System is beyond retarded as the list reference NEVER CHANGES YET IT NEEDS TO BE RESET? REALLY?
    public void refreshTitles() {
        if (this.inEditMode) {
            this.titleList.clearItems().setItems(titleManager.getTitles());
            if (!titleManager.getTitles().isEmpty()) {
                this.titleList.setSelectedItem(titleManager.getTitles().get(0));
                this.mode = TitleModifyType.MODIFY;
            }
        }
    }

    // Lets keep the stupidity of the circus going shall we?!?!?
    public void refreshAvailableTitles() {
        if (!this.inEditMode) {
            this.titleList.clearItems().setItems(titleManager.getAvailableTitles());
            if (titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()) != null) {
                titleManager.setTitleContentForDisplay(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()));
                this.titleList.setSelectedItem(titleManager.getAvailableTitle(titleManager.getSelectedTitleFor(this.mc.player.getUniqueID()).getId()));
            } else {
                this.titleList.setSelectedItem(titleManager.getAvailableTitles().get(0));
                titleManager.setTitleContentForDisplay(titleManager.getAvailableTitles().get(0));
            }
        }
    }

    // TODO: Merge this with the ExchangeItemComponent class as a new default style
    public static class TitleItemComponent extends UIDynamicList.ItemComponent<Title> {

        private UIExpandingLabel titleLabel;

        TitleItemComponent(final MalisisGui gui, final Title title) {
            super(gui, title);

            // Set size
            this.setSize(UIComponent.INHERITED, 20);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            this.titleLabel = new UIExpandingLabel(gui, Text.of(TextColors.WHITE, item.getContent()));
            if (item.isHidden()) {
                this.titleLabel.setText(this.titleLabel.getText() + TextFormatting.WHITE + " [HIDDEN]");
            }
            this.titleLabel.setPosition(2, 0, Anchor.LEFT | Anchor.MIDDLE);

            this.add(this.titleLabel);
        }
    }
}
