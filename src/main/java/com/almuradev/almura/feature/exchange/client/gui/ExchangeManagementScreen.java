/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxResult;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.Store;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class ExchangeManagementScreen extends SimpleScreen {

    private static final int requiredScreenWidth = 375;
    private static final int requiredScreenHeight = 260;
    private static final String filter = "[^A-Za-z0-9_.]";
    private static final FontOptions defaultTextFieldFontOptions = FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build();
    private static final FontOptions readOnlyTextFieldFontOptions = FontOptions.builder().color(0xC5C5C5).shadow(false).build();

    @Inject private static ClientExchangeManager exchangeManager;
    @Inject private static ClientNotificationManager notificationManager;

    private UIButton buttonAdd, buttonDelete, buttonOpen, buttonSave;
    private UICheckBox hiddenCheckbox;
    private UIDynamicList<Exchange> exchangeList;
    private UILabel creatorNameLabel, creatorUniqueIdLabel, createdLabel;
    private UITextBox idTextBox, permissionTextBox, creatorNameTextBox, creatorUniqueIdTextBox, createdTextBox, titleTextBox;
    private UIForm form;

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        // Master Pane
        this.form = new UIForm(this, requiredScreenWidth, requiredScreenHeight, I18n.format("almura.title.exchange.management"));

        // LIST
        this.exchangeList = new UIDynamicList<>(this, 120, requiredScreenHeight - 40);
        this.exchangeList.setSelectConsumer(this.onSelect());
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
        container.setSize(244, requiredScreenHeight - 40);
        container.setBackgroundAlpha(0);

        // ID
        this.idTextBox = new UITextBox(this, "");
        this.idTextBox.setTabIndex(0);
        this.idTextBox.setOnEnter(tb -> this.save());
        this.idTextBox.setSize(140, 0);
        this.idTextBox.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        this.idTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.idTextBox.setEditable(false);
        this.idTextBox.register(this);
        this.idTextBox.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel idLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.id") + ":")
                .setPosition(0, this.idTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Title
        this.titleTextBox = new UITextBox(this, "");
        this.titleTextBox.setTabIndex(1);
        this.titleTextBox.setOnEnter(tb -> this.save());
        this.titleTextBox.setSize(140, 0);
        this.titleTextBox.setPosition(0, SimpleScreen.getPaddedY(this.idTextBox, 2), Anchor.RIGHT | Anchor.TOP);
        this.titleTextBox.setFontOptions(defaultTextFieldFontOptions);
        this.titleTextBox.register(this);

        final UILabel titleLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.title") + ":")
                .setPosition(0, this.titleTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Permission
        this.permissionTextBox = new UITextBox(this, "");
        this.permissionTextBox.setTabIndex(2);
        this.permissionTextBox.setOnEnter(tb -> this.save());
        this.permissionTextBox.setSize(140, 0);
        this.permissionTextBox.setPosition(0, SimpleScreen.getPaddedY(this.titleTextBox, 2), Anchor.RIGHT | Anchor.TOP);
        this.permissionTextBox.setFontOptions(defaultTextFieldFontOptions);
        this.permissionTextBox.register(this);
        this.permissionTextBox.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel permissionLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.permission") + ":")
                .setPosition(0, this.permissionTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (name)
        this.creatorNameTextBox = new UITextBox(this, "");
        this.creatorNameTextBox.setSize(140, 0);
        this.creatorNameTextBox.setPosition(0, SimpleScreen.getPaddedY(this.permissionTextBox, 2), Anchor.RIGHT | Anchor.TOP);
        this.creatorNameTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.creatorNameTextBox.setEditable(false);

        this.creatorNameLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.creator_name") + ":")
                .setPosition(0, this.creatorNameTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (Unique ID)
        this.creatorUniqueIdTextBox = new UITextBox(this, "");
        this.creatorUniqueIdTextBox.setSize(140, 0);
        this.creatorUniqueIdTextBox.setPosition(0, SimpleScreen.getPaddedY(this.creatorNameTextBox, 2), Anchor.RIGHT | Anchor.TOP);
        this.creatorUniqueIdTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.creatorUniqueIdTextBox.setEditable(false);

        this.creatorUniqueIdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.creator_uuid") + ":")
                .setPosition(0, this.creatorUniqueIdTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created on
        this.createdTextBox = new UITextBox(this, "");
        this.createdTextBox.setSize(140, 0);
        this.createdTextBox.setPosition(0, SimpleScreen.getPaddedY(this.creatorUniqueIdTextBox, 2), Anchor.RIGHT | Anchor.TOP);
        this.createdTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.createdTextBox.setEditable(false);

        this.createdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.created") + ":")
                .setPosition(0, this.createdTextBox.getY() + 3, Anchor.LEFT | Anchor.TOP);

        this.hiddenCheckbox = new UICheckBox(this);
        this.hiddenCheckbox.setText(TextFormatting.WHITE + I18n.format("almura.text.exchange.hidden"));
        this.hiddenCheckbox.setPosition(0, 0, Anchor.LEFT | Anchor.BOTTOM);
        this.hiddenCheckbox.setChecked(false);
        this.hiddenCheckbox.setName("checkbox.hidden");
        this.hiddenCheckbox.register(this);

        this.buttonSave = new UIButtonBuilder(this)
                .width(40)
                .text(I18n.format("almura.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .enabled(false)
                .onClick(this::save)
                .build("button.save");

        this.buttonOpen = new UIButtonBuilder(this)
                .width(40)
                .text(I18n.format("almura.button.open"))
                .position(SimpleScreen.getPaddedX(this.buttonSave, 2, Anchor.RIGHT), 0)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .enabled(false)
                .onClick(() -> {
                    if (this.exchangeList.getSelectedItem() == null) {
                        return;
                    }

                    exchangeManager.requestExchangeSpecificGui(this.exchangeList.getSelectedItem().getId());
                })
                .build("button.open");

        container.add(this.idTextBox, idLabel,
                      this.titleTextBox, titleLabel,
                      this.permissionTextBox, permissionLabel,
                      this.creatorNameTextBox, creatorNameLabel,
                      this.creatorUniqueIdTextBox, creatorUniqueIdLabel,
                      this.createdTextBox, createdLabel,
                      this.hiddenCheckbox, this.buttonOpen, this.buttonSave);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .text(Text.of(TextColors.GREEN, "+"))
                .width(15)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    this.exchangeList.setSelectedItem(null);
                    this.idTextBox.focus();
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
                        UIMessageBox.showDialog(this, I18n.format("almura.title.exchange.are_you_sure"),
                                I18n.format("almura.text.exchange.delete_exchange", selectedExchange.getId()),
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

    @Subscribe
    private void onValueChange(ComponentEvent.ValueChange event) {
        if (event.getComponent() instanceof UITextField) {
            this.validate(((String) event.getNewValue()));
        } else if (event.getComponent() instanceof UICheckBox) {
            this.validate(this.idTextBox.getText());
        }
    }

    public void refresh() {
        final String lastSelectedId = this.exchangeList.getSelectedItem() != null
                ? this.exchangeList.getSelectedItem().getId()
                : this.idTextBox.getText().toLowerCase();

        this.exchangeList.setItems(exchangeManager.getExchanges());

        final Exchange toSelect = this.exchangeList.getItems()
                .stream()
                .filter(exchange -> exchange.getId().equalsIgnoreCase(lastSelectedId))
                .findAny()
                .orElse(this.exchangeList.getItems().stream().findFirst().orElse(null));
        this.exchangeList.setSelectedItem(toSelect);
    }

    private void reset(boolean forNewExchange) {

        // ID
        this.idTextBox.setText("");
        this.idTextBox.setEditable(forNewExchange);
        this.idTextBox.setFontOptions(forNewExchange ? defaultTextFieldFontOptions : readOnlyTextFieldFontOptions);

        // Title
        this.titleTextBox.setText("");

        // Permission
        this.permissionTextBox.setText("");

        // Creator by (name)
        this.creatorNameTextBox.setText("");
        this.creatorNameTextBox.setEditable(false);
        this.creatorNameTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.creatorNameTextBox.setVisible(!forNewExchange);
        this.creatorNameLabel.setVisible(!forNewExchange);

        // Creator by (Unique ID)
        this.creatorUniqueIdTextBox.setText("");
        this.creatorUniqueIdTextBox.setEditable(false);
        this.creatorUniqueIdTextBox.setFontOptions(readOnlyTextFieldFontOptions);
        this.creatorUniqueIdTextBox.setVisible(!forNewExchange);
        this.creatorUniqueIdLabel.setVisible(!forNewExchange);

        // Created on
        this.createdTextBox.setText("");
        this.createdTextBox.setVisible(!forNewExchange);
        this.createdLabel.setVisible(!forNewExchange);

        // Hidden
        this.hiddenCheckbox.setChecked(false);

        this.buttonOpen.setEnabled(false);

        this.buttonSave.setEnabled(false);
    }

    private boolean validate(String newValue) {
        boolean isValid = !this.idTextBox.getText().isEmpty()
                && !this.titleTextBox.getText().isEmpty()
                && !this.permissionTextBox.getText().isEmpty()
                && !newValue.isEmpty(); // Because we don't have a post event

        // If this is a new project then ensure another with that ID doesn't exist
        if (this.exchangeList.getSelectedItem() == null && isValid) {
            isValid = this.exchangeList.getItems().stream().noneMatch(i -> i.getId().equalsIgnoreCase(this.idTextBox.getText()));
        }

        this.buttonSave.setEnabled(isValid);
        return isValid;
    }

    private void save() {
        if (!this.buttonSave.isEnabled()) {
            return;
        }

        final boolean isNew = this.exchangeList.getSelectedItem() == null;

        UIMessageBox.showDialog(this, I18n.format("almura.title.exchange.are_you_sure"),
                I18n.format(String.format("almura.text.exchange.%s_exchange", isNew ? "add" : "modify")), MessageBoxButtons.YES_NO,
                (result) -> {
                    if (result != MessageBoxResult.YES) return;

                    if (this.exchangeList.getSelectedItem() == null) { // It's a new listing if true
                        exchangeManager.addExchange(this.idTextBox.getText(), this.titleTextBox.getText(), this.permissionTextBox.getText(),
                                this.hiddenCheckbox.isChecked());
                    } else {
                        exchangeManager.modifyExchange(this.idTextBox.getText(), this.titleTextBox.getText(), this.permissionTextBox.getText(),
                                this.hiddenCheckbox.isChecked());
                    }
                });
    }

    private Consumer<Exchange> onSelect() {
        return exchange -> {

            this.reset(exchange == null);

            if (exchange == null) {
                return;
            }

            // ID
            this.idTextBox.setText(exchange.getId());
            this.idTextBox.setFontOptions(readOnlyTextFieldFontOptions);
            this.idTextBox.setEditable(false);

            // Title
            this.titleTextBox.setText(exchange.getName());

            // Permission
            this.permissionTextBox.setText(exchange.getPermission());

            // Creator by (Name)
            // This nonsense is brought to you by [sponge].
            final String name = Store.UNKNOWN_OWNER.equals(exchange.getCreator())
                    ? I18n.format("almura.text.exchange.unknown")
                    : exchange.getCreatorName().orElse(I18n.format("almura.text.exchange.unknown"));
            this.creatorNameTextBox.setText(name);

            // Creator by (Unique ID)
            this.creatorUniqueIdTextBox.setText(exchange.getCreator().toString());

            // Created on
            final DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());
            this.createdTextBox.setText(formatter.format(exchange.getCreated()));

            // Hidden
            this.hiddenCheckbox.setChecked(exchange.isHidden());

            this.buttonOpen.setEnabled(true);
        };
    }

    private static final class ExchangeItemComponent extends UIDynamicList.ItemComponent<Exchange> {

        public ExchangeItemComponent(MalisisGui gui, UIDynamicList<Exchange> parent, Exchange item) {
            super(gui, parent, item);
        }

        @Override
        public void construct(MalisisGui gui) {
            this.setSize(UIComponent.INHERITED, 20);
        }

        @Override
        public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.getName(), 2, (this.height - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT) / 2f, 0);
        }
    }
}
