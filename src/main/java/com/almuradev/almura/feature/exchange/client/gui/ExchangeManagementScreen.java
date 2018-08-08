/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxResult;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class ExchangeManagementScreen extends SimpleScreen {

    private static final int screenWidth = 375;
    private static final int screenHeight = 300;
    private static final String filter = "[^A-Za-z0-9_.]";
    private static final FontOptions defaultTextFieldFontOptions = FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build();
    private static final FontOptions readOnlyTextFieldFontOptions = FontOptions.builder().color(0xC5C5C5).shadow(false).build();

    @Inject private static ClientExchangeManager exchangeManager;
    @Inject private static ClientNotificationManager notificationManager;

    private UITextField idField, permissionField, creatorField, createdField, titleField;
    private UIButton buttonAdd, buttonDelete, buttonSave;
    private UIDynamicList<Exchange> exchangeList;
    private UICheckBox hiddenCheckbox;
    private UIFormContainer form;

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        // Master Pane
        this.form = new UIFormContainer(this, screenWidth, screenHeight, "Exchange - Management");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setPadding(3, 3);
        this.form.setTopPadding(20);

        // LIST
        this.exchangeList = new UIDynamicList<>(this, 120, 260);
        this.exchangeList.setItemComponentFactory(ExchangeItemComponent::new);
        this.exchangeList.setItemComponentSpacing(1);
        this.exchangeList.setCanDeselect(false);
        this.exchangeList.setBorder(0xFFFFFF, 1, 215);
        this.exchangeList.setPadding(2, 2);
        this.exchangeList.setPosition(1, 0);
        this.exchangeList.register(this);

        this.form.add(this.exchangeList);

        // MODIFY CONTAINER
        final UIContainer<?> container = new UIContainer(this);
        container.setBorder(0xFFFFFF, 1, 215);
        container.setPadding(4, 4);
        container.setAnchor(Anchor.RIGHT | Anchor.TOP);
        container.setPosition(-1, 0);
        container.setSize(244, 260);
        container.setBackgroundAlpha(0);

        // ID
        this.idField = new UITextField(this, "", false)
                .setSize(165, 0)
                .setPosition(0, 0, Anchor.RIGHT | Anchor.TOP)
                .setFontOptions(readOnlyTextFieldFontOptions)
                .setEditable(false)
                .register(this);
        this.idField.setFilter(s -> s.replaceAll(filter, "").toLowerCase());
        final UILabel idLabel = new UILabel(this, TextFormatting.WHITE + "ID:")
                .setPosition(0, this.idField.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Title
        this.titleField = new UITextField(this, "", false)
                .setSize(165, 0)
                .setPosition(0, SimpleScreen.getPaddedY(this.idField, 2), Anchor.RIGHT | Anchor.TOP)
                .setFontOptions(defaultTextFieldFontOptions)
                .register(this);
        final UILabel titleLabel = new UILabel(this, TextFormatting.WHITE + "Title:")
                .setPosition(0, this.titleField.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Permission
        this.permissionField = new UITextField(this, "", false)
                .setSize(165, 0)
                .setPosition(0, SimpleScreen.getPaddedY(this.titleField, 2), Anchor.RIGHT | Anchor.TOP)
                .setFontOptions(defaultTextFieldFontOptions)
                .register(this);
        this.permissionField.setFilter(s -> s.replaceAll(filter, "").toLowerCase());
        final UILabel permissionLabel = new UILabel(this, TextFormatting.WHITE + "Permission:")
                .setPosition(0, this.permissionField.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Last modified by
        this.creatorField = new UITextField(this, "", false)
                .setSize(165, 0)
                .setPosition(0, SimpleScreen.getPaddedY(this.permissionField, 2), Anchor.RIGHT | Anchor.TOP)
                .setFontOptions(readOnlyTextFieldFontOptions)
                .setEditable(false);
        final UILabel creatorLabel = new UILabel(this, TextFormatting.WHITE + "Creator:")
                .setPosition(0, this.creatorField.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Last modified date
        this.createdField = new UITextField(this, "", false)
                .setSize(165, 0)
                .setPosition(0, SimpleScreen.getPaddedY(this.creatorField, 2), Anchor.RIGHT | Anchor.TOP)
                .setFontOptions(readOnlyTextFieldFontOptions)
                .setEditable(false);
        final UILabel createdLabel = new UILabel(this, TextFormatting.WHITE + "Created:")
                .setPosition(0, this.createdField.getY() + 3, Anchor.LEFT | Anchor.TOP);

        this.hiddenCheckbox = new UICheckBox(this);
        this.hiddenCheckbox.setText(TextFormatting.WHITE + "Hidden");
        this.hiddenCheckbox.setPosition(0, 0, Anchor.LEFT | Anchor.BOTTOM);
        this.hiddenCheckbox.setChecked(false);
        this.hiddenCheckbox.setName("checkbox.hidden");
        this.hiddenCheckbox.register(this);

        this.buttonSave = new UIButtonBuilder(this)
                .width(30)
                .text(Text.of("almura.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .enabled(false)
                .onClick(() -> {
                    final boolean isNew = this.exchangeList.getSelectedItem() == null;

                    UIMessageBox.showDialog(this, "Are you sure?",
                            String.format("Are you sure you want to %s this Exchange?", isNew ? "add" : "modify"), MessageBoxButtons.YES_NO,
                            (result) -> {
                                if (result != MessageBoxResult.YES) return;

                                if (this.exchangeList.getSelectedItem() == null) { // It's a new listing if true
                                    exchangeManager.addExchange(this.idField.getText(), this.titleField.getText(), this.permissionField.getText(),
                                            this.hiddenCheckbox.isChecked());
                                } else {
                                    exchangeManager.modifyExchange(this.idField.getText(), this.titleField.getText(), this.permissionField.getText(),
                                            this.hiddenCheckbox.isChecked());
                                }
                            });
                })
                .build("button.save");

        container.add(this.idField, idLabel,
                      this.titleField, titleLabel,
                      this.permissionField, permissionLabel,
                      this.creatorField, creatorLabel,
                      this.createdField, createdLabel,
                      this.hiddenCheckbox, this.buttonSave);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .text(Text.of(TextColors.GREEN, "+"))
                .width(15)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    this.reset(false);
                    this.exchangeList.setSelectedItem(null);
                })
                .build("button.add");

        // Remove button
        this.buttonDelete = new UIButtonBuilder(this)
                .width(15)
                .x(SimpleScreen.getPaddedX(this.buttonAdd, 2))
                .text(Text.of(TextColors.RED, "-"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    final Exchange selectedExchange = this.exchangeList.getSelectedItem();
                    if (selectedExchange != null) {
                        UIMessageBox.showDialog(this, "Are you sure?",
                                String.format("Are you sure you want to delete the '%s' Exchange?", selectedExchange.getId()),
                                MessageBoxButtons.YES_NO, (result) -> {
                                   if (result != MessageBoxResult.YES) return;

                                    exchangeManager.deleteExchange(this.exchangeList.getSelectedItem().getId());
                                });
                    }
                })
                .build("button.delete");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.button.close"))
                .onClick(this::close)
                .build("button.close");

        this.form.add(this.exchangeList, container, this.buttonAdd, this.buttonDelete, buttonClose);

        this.addToScreen(this.form);

        this.refresh();
    }

    public void refresh() {
        final String lastSelectedId = this.exchangeList.getSelectedItem() != null ? this.exchangeList.getSelectedItem().getId() : "";

        this.exchangeList.setItems(exchangeManager.getExchanges());

        final Exchange toSelect = this.exchangeList.getItems()
                .stream()
                .filter(exchange -> exchange.getId().equalsIgnoreCase(lastSelectedId))
                .findAny()
                .orElse(this.exchangeList.getItems().stream().findFirst().orElse(null));
        this.exchangeList.setSelectedItem(toSelect);
    }

    private void reset(boolean markReadOnly) {
        final UITextField[] editableFields = {this.idField, this.titleField, this.permissionField};
        final UITextField[] readOnlyFields = {this.creatorField, this.createdField};

        Arrays.stream(editableFields).forEach(c -> {
            c.setText("");
            c.setEnabled(!markReadOnly);
        });

        Arrays.stream(readOnlyFields).forEach(c -> {
            c.setText("");
            c.setEnabled(!markReadOnly);
            c.setFontOptions(markReadOnly ? readOnlyTextFieldFontOptions : defaultTextFieldFontOptions);
        });

        this.idField.setText("");
        this.idField.setEditable(true);
        this.idField.setFontOptions(defaultTextFieldFontOptions);
        this.titleField.setText("");
        this.permissionField.setText("");
        this.creatorField.setText("");
        this.createdField.setText("");
        this.hiddenCheckbox.setChecked(false);
        this.validate("");
    }

    private void validate(String newValue) {
        final boolean isValid = !this.idField.getText().isEmpty()
                && !this.titleField.getText().isEmpty()
                && !this.permissionField.getText().isEmpty()
                && !newValue.isEmpty(); // Because we don't have a post event
        this.buttonSave.setEnabled(isValid);
    }

    @Subscribe
    private void onValueChange(ComponentEvent.ValueChange event) {
        if (event.getComponent() instanceof UITextField) {
            this.validate(((String) event.getNewValue()));
        }
    }

    @Subscribe
    private void onExchangeSelect(UIDynamicList.SelectEvent event) {
        this.reset(event.getNewValue() == null);

        if (event.getNewValue() != null) {
            final Exchange exchange = (Exchange) event.getNewValue();

            this.idField.setText(exchange.getId());
            this.idField.setFontOptions(readOnlyTextFieldFontOptions);
            this.idField.setEditable(false);
            this.titleField.setText(exchange.getName());
            this.permissionField.setText(exchange.getPermission());
            this.creatorField.setText(exchange.getCreator().toString());
            exchange.getCreatorName().ifPresent(name -> this.creatorField.setTooltip(name));

            final DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());
            this.createdField.setText(formatter.format(exchange.getCreated()));

            this.hiddenCheckbox.setChecked(exchange.isHidden());
        }
    }

    private static final class ExchangeItemComponent extends UIDynamicList.ItemComponent<Exchange> {

        public ExchangeItemComponent(MalisisGui gui, Exchange item) {
            super(gui, item);
        }

        @Override
        public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.getName(), 2, 3, 0);
        }
    }
}
