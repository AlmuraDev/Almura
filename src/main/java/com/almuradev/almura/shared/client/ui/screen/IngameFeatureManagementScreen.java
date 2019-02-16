/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.screen;

import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
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
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class IngameFeatureManagementScreen<T extends IngameFeature> extends BasicScreen {

    private static final int requiredScreenWidth = 375;
    private static final int requiredScreenHeight = 215;
    private static final String filter = "[^A-Za-z0-9_.]";

    private final String title, name;
    private final BiConsumer<IngameFeatureManagementScreen<T>, Optional<T>> onDelete, onOpen, onSave;
    private final Consumer<IngameFeatureManagementScreen<T>> onRefresh;

    private UIButton buttonAdd, buttonDelete, buttonOpen, buttonSave;
    private UICheckBox hiddenCheckbox;
    private BasicList<T> featureList;
    private BasicForm form;
    private UILabel creatorNameLabel, creatorUniqueIdLabel, createdLabel;
    private BasicTextBox tbId, tbPermission, tbCreatorName, tbCreatorUniqueId, tbCreated, tbTitle;

    public IngameFeatureManagementScreen(final String title, final String name,
            final Consumer<IngameFeatureManagementScreen<T>> onRefresh,
            final BiConsumer<IngameFeatureManagementScreen<T>, Optional<T>> onDelete,
            final BiConsumer<IngameFeatureManagementScreen<T>, Optional<T>> onOpen,
            final BiConsumer<IngameFeatureManagementScreen<T>, Optional<T>> onSave) {
        this.title = title;
        this.name = name;
        this.onRefresh = onRefresh;
        this.onDelete = onDelete;
        this.onOpen = onOpen;
        this.onSave = onSave;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new BasicForm(this, requiredScreenWidth, requiredScreenHeight, this.title);

        this.featureList = new BasicList<>(this, 120, requiredScreenHeight - 40);
        this.featureList.setSelectConsumer(this.onSelect());
        this.featureList.setItemComponentFactory((g, l, i) -> new IngameFeatureItemComponent(this, l, i));
        this.featureList.setItemComponentSpacing(1);
        this.featureList.setCanDeselect(false);
        this.featureList.setBorder(0xFFFFFF, 1, 215);
        this.featureList.setPadding(2, 2);
        this.featureList.setPosition(1, 0);
        this.featureList.register(this);

        this.form.add(this.featureList);

        final BasicContainer<?> container = new BasicContainer(this);
        container.setBorder(0xFFFFFF, 1, 215);
        container.setPadding(4, 4);
        container.setAnchor(Anchor.RIGHT | Anchor.TOP);
        container.setPosition(-1, 0);
        container.setSize(244, requiredScreenHeight - 40);
        container.setBackgroundAlpha(0);

        // ID
        this.tbId = new BasicTextBox(this, "");
        this.tbId.setAcceptsReturn(false);
        this.tbId.setAcceptsTab(false);
        this.tbId.setTabIndex(0);
        this.tbId.setOnEnter(tb -> this.save());
        this.tbId.setSize(140, 0);
        this.tbId.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        this.tbId.setEditable(false);
        this.tbId.register(this);
        this.tbId.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel idLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.id") + ":")
                .setPosition(0, this.tbId.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Title
        this.tbTitle = new BasicTextBox(this, "");
        this.tbTitle.setAcceptsReturn(false);
        this.tbTitle.setAcceptsTab(false);
        this.tbTitle.setTabIndex(1);
        this.tbTitle.setOnEnter(tb -> this.save());
        this.tbTitle.setSize(140, 0);
        this.tbTitle.setPosition(0, BasicScreen.getPaddedY(this.tbId, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbTitle.register(this);

        final UILabel titleLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.title") + ":")
                .setPosition(0, this.tbTitle.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Permission
        this.tbPermission = new BasicTextBox(this, "");
        this.tbPermission.setAcceptsReturn(false);
        this.tbPermission.setAcceptsTab(false);
        this.tbPermission.setTabIndex(2);
        this.tbPermission.setOnEnter(tb -> this.save());
        this.tbPermission.setSize(140, 0);
        this.tbPermission.setPosition(0, BasicScreen.getPaddedY(this.tbTitle, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbPermission.register(this);
        this.tbPermission.setFilter(s -> s.replaceAll(filter, "").toLowerCase());

        final UILabel permissionLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.permission") + ":")
                .setPosition(0, this.tbPermission.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (name)
        this.tbCreatorName = new BasicTextBox(this, "");
        this.tbCreatorName.setSize(140, 0);
        this.tbCreatorName.setPosition(0, BasicScreen.getPaddedY(this.tbPermission, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreatorName.setEditable(false);

        this.creatorNameLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.creator_name") + ":")
                .setPosition(0, this.tbCreatorName.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created by (Unique ID)
        this.tbCreatorUniqueId = new BasicTextBox(this, "");
        this.tbCreatorUniqueId.setSize(140, 0);
        this.tbCreatorUniqueId.setPosition(0, BasicScreen.getPaddedY(this.tbCreatorName, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreatorUniqueId.setEditable(false);

        this.creatorUniqueIdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.creator_uuid") + ":")
                .setPosition(0, this.tbCreatorUniqueId.getY() + 3, Anchor.LEFT | Anchor.TOP);

        // Created on
        this.tbCreated = new BasicTextBox(this, "");
        this.tbCreated.setSize(140, 0);
        this.tbCreated.setPosition(0, BasicScreen.getPaddedY(this.tbCreatorUniqueId, 2), Anchor.RIGHT | Anchor.TOP);
        this.tbCreated.setEditable(false);

        this.createdLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.created") + ":")
                .setPosition(0, this.tbCreated.getY() + 3, Anchor.LEFT | Anchor.TOP);

        this.hiddenCheckbox = new UICheckBox(this);
        this.hiddenCheckbox.setText(TextFormatting.WHITE + I18n.format("almura.feature.common.text.hidden"));
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
                .position(BasicScreen.getPaddedX(this.buttonSave, 2, Anchor.RIGHT), 0)
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
                .text(TextFormatting.GREEN + "+")
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    this.featureList.setSelectedItem(null);
                    this.tbId.focus();
                })
                .build("button.add");

        // Remove button
        this.buttonDelete = new UIButtonBuilder(this)
                .x(BasicScreen.getPaddedX(this.buttonAdd, 2))
                .text(TextFormatting.RED + "-")
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .onClick(() -> {
                    final IngameFeature selectedFeature = this.featureList.getSelectedItem();
                    if (selectedFeature != null) {
                        BasicMessageBox.showDialog(this, I18n.format("almura.feature.common.title.are_you_sure"),
                                I18n.format("almura.feature.common.text.delete_feature", selectedFeature.getId(), name),
                                MessageBoxButtons.YES_NO, (result) -> {
                                    if (result != MessageBoxResult.YES) return;

                                    this.onDelete.accept(this, Optional.ofNullable(this.featureList.getSelectedItem()));
                                });
                    }
                })
                .build("button.delete");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(I18n.format("almura.button.close"))
                .onClick(this::close)
                .build("button.close");

        this.form.add(this.featureList, container, this.buttonAdd, this.buttonDelete, buttonClose);

        this.addToScreen(this.form);

        this.refresh();
    }

    @Subscribe
    private void onValueChange(final ComponentEvent.ValueChange event) {
        if (event.getComponent() instanceof UITextField) {
            this.updateControls(((String) event.getNewValue()));
        } else if (event.getComponent() instanceof UICheckBox) {
            this.updateControls(this.tbId.getText());
        }
    }

    public String getPendingId() {
        return this.tbId.getText();
    }

    public String getPendingTitle() {
        return this.tbTitle.getText();
    }

    public String getPendingPermission() {
        return this.tbPermission.getText();
    }

    public boolean getPendingHiddenState() {
        return this.hiddenCheckbox.isChecked();
    }

    public void setItems(final List<T> features) {
        this.featureList.setItems(features);
    }

    public void refresh() {
        final String lastSelectedId = this.featureList.getSelectedItem() != null
                ? this.featureList.getSelectedItem().getId()
                : this.tbId.getText().toLowerCase();

        this.onRefresh.accept(this);

        final T toSelect = this.featureList.getItems()
                .stream()
                .filter(feature -> feature.getId().equalsIgnoreCase(lastSelectedId))
                .findAny()
                .orElse(this.featureList.getItems().stream().findFirst().orElse(null));
        this.featureList.setSelectedItem(toSelect);
    }

    private void open() {
        if (this.featureList.getSelectedItem() == null) {
            return;
        }

        this.onOpen.accept(this, Optional.ofNullable(this.featureList.getSelectedItem()));
    }

    private void reset(final boolean forNewFeature) {

        // ID
        this.tbId.setText("");
        this.tbId.setEditable(forNewFeature);

        // Title
        this.tbTitle.setText("");

        // Permission
        this.tbPermission.setText("");

        // Creator by (name)
        this.tbCreatorName.setText("");
        this.tbCreatorName.setEditable(false);
        this.tbCreatorName.setVisible(!forNewFeature);
        this.creatorNameLabel.setVisible(!forNewFeature);

        // Creator by (Unique ID)
        this.tbCreatorUniqueId.setText("");
        this.tbCreatorUniqueId.setEditable(false);
        this.tbCreatorUniqueId.setVisible(!forNewFeature);
        this.creatorUniqueIdLabel.setVisible(!forNewFeature);

        // Created on
        this.tbCreated.setText("");
        this.tbCreated.setVisible(!forNewFeature);
        this.createdLabel.setVisible(!forNewFeature);

        // Hidden
        this.hiddenCheckbox.setChecked(false);

        this.buttonOpen.setEnabled(false);

        this.buttonSave.setEnabled(false);
    }

    private boolean updateControls(final String newValue) {
        boolean isValid = !this.tbId.getText().isEmpty()
                && !this.tbTitle.getText().isEmpty()
                && !this.tbPermission.getText().isEmpty()
                && !newValue.isEmpty(); // Because we don't have a post event

        // If this is a new project then ensure another with that ID doesn't exist
        if (this.featureList.getSelectedItem() == null && isValid) {
            isValid = this.featureList.getItems().stream().noneMatch(i -> i.getId().equalsIgnoreCase(this.tbId.getText()));
        }

        this.buttonSave.setEnabled(isValid);
        return isValid;
    }

    private void save() {
        if (!this.buttonSave.isEnabled()) {
            return;
        }

        final boolean isNew = this.featureList.getSelectedItem() == null;

        BasicMessageBox.showDialog(this, I18n.format("almura.feature.common.title.are_you_sure"),
                I18n.format(String.format("almura.feature.common.text.%s_feature", isNew ? "add" : "modify"), this.name),
                MessageBoxButtons.YES_NO,
                (result) -> {
                    if (result != MessageBoxResult.YES) return;

                    this.onSave.accept(this, Optional.ofNullable(this.featureList.getSelectedItem()));
                });
    }

    private Consumer<T> onSelect() {
        return feature -> {

            this.reset(feature == null);

            if (feature == null) {
                return;
            }

            // ID
            this.tbId.setText(feature.getId());
            this.tbId.setEditable(false);

            // Title
            this.tbTitle.setText(feature.getName());

            // Permission
            this.tbPermission.setText(feature.getPermission());

            // Creator by (Name)
            // This nonsense is brought to you by [sponge].
            final String name = FeatureConstants.UNKNOWN_OWNER.equals(feature.getCreator())
                    ? I18n.format("almura.feature.common.text.unknown")
                    : feature.getCreatorName().orElse(I18n.format("almura.feature.common.text.unknown"));
            this.tbCreatorName.setText(name);

            // Creator by (Unique ID)
            this.tbCreatorUniqueId.setText(feature.getCreator().toString());

            // Created on
            final DateTimeFormatter formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getJavaLocale())
                    .withZone(ZoneId.systemDefault());
            this.tbCreated.setText(formatter.format(feature.getCreated()));

            // Hidden
            this.hiddenCheckbox.setChecked(feature.isHidden());

            this.buttonOpen.setEnabled(true);
        };
    }

    private static final class IngameFeatureItemComponent<T extends IngameFeature> extends BasicList.ItemComponent<T> {

        IngameFeatureItemComponent(final IngameFeatureManagementScreen<T> screen, final BasicList<T> parent, final T item) {
            super(screen, parent, item);
            this.setOnDoubleClickConsumer(i -> screen.open());
        }

        @Override
        public void construct(final MalisisGui gui) {
            this.setSize(UIComponent.INHERITED, 20);
        }

        @Override
        public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            renderer.drawText(TextFormatting.WHITE + item.getName(), 4, (this.height - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT) / 2f, 0);
        }
    }
}
