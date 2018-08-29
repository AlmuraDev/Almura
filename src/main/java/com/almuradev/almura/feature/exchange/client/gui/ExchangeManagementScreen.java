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
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxResult;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.FeatureConstants;
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
import net.malisis.core.util.MouseButton;
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
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public final class ExchangeManagementScreen extends SimpleScreen {

    private static final int requiredScreenWidth = 375;
    private static final int requiredScreenHeight = 260;
    private static final String filter = "[^A-Za-z0-9_.]";

    @Inject private static ClientExchangeManager exchangeManager;

    private UIButton buttonAdd, buttonDelete, buttonOpen, buttonSave;
    private UICheckBox hiddenCheckbox;
    private UIDynamicList<Exchange> exchangeList;
    private UILabel creatorNameLabel, creatorUniqueIdLabel, createdLabel;
    private UITextBox tbId, tbPermission, tbCreatorName, tbCreatorUniqueId, tbCreated, tbTitle;
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
        this.tbId = new UITextBox(this, "");
        this.tbId.setAcceptsReturn(false);
        this.tbId.setAcceptsTab(false);
        this.tbId.setTabIndex(0);
        this.tbId.setOnEnter(tb -> this.save());
        this.tbId.setSize(140, 0);
        this.tbId.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        this.tbId.setEditable(false);
        this.tbId.register(this);
        this.tbId.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel idLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.id") + ":")
                .setPosition(0, this.tbId.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Title
        this.tbTitle = new UITextBox(this, "");
        this.tbTitle.setAcceptsReturn(false);
        this.tbTitle.setAcceptsTab(false);
        this.tbTitle.setTabIndex(1);
        this.tbTitle.setOnEnter(tb -> this.save());
        this.tbTitle.setSize(140, 0);
        this.tbTitle.setPosition(0, SimpleScreen.getPaddedY(this.tbId, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbTitle.register(this);

        final UILabel titleLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.title") + ":")
                .setPosition(0, this.tbTitle.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Permission
        this.tbPermission = new UITextBox(this, "");
        this.tbPermission.setAcceptsReturn(false);
        this.tbPermission.setAcceptsTab(false);
        this.tbPermission.setTabIndex(2);
        this.tbPermission.setOnEnter(tb -> this.save());
        this.tbPermission.setSize(140, 0);
        this.tbPermission.setPosition(0, SimpleScreen.getPaddedY(this.tbTitle, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbPermission.register(this);
        this.tbPermission.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel permissionLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.permission") + ":")
                .setPosition(0, this.tbPermission.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (name)
        this.tbCreatorName = new UITextBox(this, "");
        this.tbCreatorName.setSize(140, 0);
        this.tbCreatorName.setPosition(0, SimpleScreen.getPaddedY(this.tbPermission, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreatorName.setEditable(false);

        this.creatorNameLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.creator_name") + ":")
                .setPosition(0, this.tbCreatorName.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (Unique ID)
        this.tbCreatorUniqueId = new UITextBox(this, "");
        this.tbCreatorUniqueId.setSize(140, 0);
        this.tbCreatorUniqueId.setPosition(0, SimpleScreen.getPaddedY(this.tbCreatorName, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreatorUniqueId.setEditable(false);

        this.creatorUniqueIdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.creator_uuid") + ":")
                .setPosition(0, this.tbCreatorUniqueId.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created on
        this.tbCreated = new UITextBox(this, "");
        this.tbCreated.setSize(140, 0);
        this.tbCreated.setPosition(0, SimpleScreen.getPaddedY(this.tbCreatorUniqueId, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreated.setEditable(false);

        this.createdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.text.exchange.created") + ":")
                .setPosition(0, this.tbCreated.getY() + 3, Anchor.LEFT | Anchor.TOP);

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
                .onClick(this::open)
                .build("button.open");

        container.add(this.tbId, idLabel,
                      this.tbTitle, titleLabel,
                      this.tbPermission, permissionLabel,
                      this.tbCreatorName, creatorNameLabel,
                      this.tbCreatorUniqueId, creatorUniqueIdLabel,
                      this.tbCreated, createdLabel,
                      this.hiddenCheckbox, this.buttonOpen, this.buttonSave);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .text(Text.of(TextColors.GREEN, "+"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    this.exchangeList.setSelectedItem(null);
                    this.tbId.focus();
                })
                .build("button.add");

        // Remove button
        this.buttonDelete = new UIButtonBuilder(this)
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
    private void onValueChange(final ComponentEvent.ValueChange event) {
        if (event.getComponent() instanceof UITextField) {
            this.validate(((String) event.getNewValue()));
        } else if (event.getComponent() instanceof UICheckBox) {
            this.validate(this.tbId.getText());
        }
    }

    public void refresh() {
        final String lastSelectedId = this.exchangeList.getSelectedItem() != null
                ? this.exchangeList.getSelectedItem().getId()
                : this.tbId.getText().toLowerCase();

        this.exchangeList.setItems(exchangeManager.getExchanges());

        final Exchange toSelect = this.exchangeList.getItems()
                .stream()
                .filter(exchange -> exchange.getId().equalsIgnoreCase(lastSelectedId))
                .findAny()
                .orElse(this.exchangeList.getItems().stream().findFirst().orElse(null));
        this.exchangeList.setSelectedItem(toSelect);
    }

    private void open() {
        if (this.exchangeList.getSelectedItem() == null) {
            return;
        }

        exchangeManager.requestExchangeSpecificGui(this.exchangeList.getSelectedItem().getId());
    }

    private void reset(final boolean forNewExchange) {

        // ID
        this.tbId.setText("");
        this.tbId.setEditable(forNewExchange);

        // Title
        this.tbTitle.setText("");

        // Permission
        this.tbPermission.setText("");

        // Creator by (name)
        this.tbCreatorName.setText("");
        this.tbCreatorName.setEditable(false);
        this.tbCreatorName.setVisible(!forNewExchange);
        this.creatorNameLabel.setVisible(!forNewExchange);

        // Creator by (Unique ID)
        this.tbCreatorUniqueId.setText("");
        this.tbCreatorUniqueId.setEditable(false);
        this.tbCreatorUniqueId.setVisible(!forNewExchange);
        this.creatorUniqueIdLabel.setVisible(!forNewExchange);

        // Created on
        this.tbCreated.setText("");
        this.tbCreated.setVisible(!forNewExchange);
        this.createdLabel.setVisible(!forNewExchange);

        // Hidden
        this.hiddenCheckbox.setChecked(false);

        this.buttonOpen.setEnabled(false);

        this.buttonSave.setEnabled(false);
    }

    private boolean validate(final String newValue) {
        boolean isValid = !this.tbId.getText().isEmpty()
                && !this.tbTitle.getText().isEmpty()
                && !this.tbPermission.getText().isEmpty()
                && !newValue.isEmpty(); // Because we don't have a post event

        // If this is a new project then ensure another with that ID doesn't exist
        if (this.exchangeList.getSelectedItem() == null && isValid) {
            isValid = this.exchangeList.getItems().stream().noneMatch(i -> i.getId().equalsIgnoreCase(this.tbId.getText()));
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
                        exchangeManager.addExchange(this.tbId.getText(), this.tbTitle.getText(), this.tbPermission.getText(),
                                this.hiddenCheckbox.isChecked());
                    } else {
                        exchangeManager.modifyExchange(this.tbId.getText(), this.tbTitle.getText(), this.tbPermission.getText(),
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
            this.tbId.setText(exchange.getId());
            this.tbId.setEditable(false);

            // Title
            this.tbTitle.setText(exchange.getName());

            // Permission
            this.tbPermission.setText(exchange.getPermission());

            // Creator by (Name)
            // This nonsense is brought to you by [sponge].
            final String name = FeatureConstants.UNKNOWN_OWNER.equals(exchange.getCreator())
                    ? I18n.format("almura.text.exchange.unknown")
                    : exchange.getCreatorName().orElse(I18n.format("almura.text.exchange.unknown"));
            this.tbCreatorName.setText(name);

            // Creator by (Unique ID)
            this.tbCreatorUniqueId.setText(exchange.getCreator().toString());

            // Created on
            final DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getJavaLocale())
                    .withZone(ZoneId.systemDefault());
            this.tbCreated.setText(formatter.format(exchange.getCreated()));

            // Hidden
            this.hiddenCheckbox.setChecked(exchange.isHidden());

            this.buttonOpen.setEnabled(true);
        };
    }

    private static final class ExchangeItemComponent extends UIDynamicList.ItemComponent<Exchange> {

        public ExchangeItemComponent(final MalisisGui gui, final UIDynamicList<Exchange> parent, final Exchange item) {
            super(gui, parent, item);
        }

        @Override
        public void construct(final MalisisGui gui) {
            this.setSize(UIComponent.INHERITED, 20);
        }

        @Override
        public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.getName(), 4, (this.height - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT) / 2f, 0);
        }

        @Override
        public boolean onDoubleClick(final int x, final int y, final MouseButton button) {
            if (button != MouseButton.LEFT) {
                return super.onDoubleClick(x, y, button);
            }

            final UIComponent<?> componentAt = this.getComponentAt(x, y);
            final UIComponent<?> parentComponentAt = componentAt == null ? null : componentAt.getParent();
            if (!(componentAt instanceof UIDynamicList.ItemComponent) && !(parentComponentAt instanceof UIDynamicList.ItemComponent)) {
                return super.onDoubleClick(x, y, button);
            }

            ((ExchangeManagementScreen) this.getGui()).open();

            return true;
        }
    }
}
