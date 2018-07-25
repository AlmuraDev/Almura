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
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ManageTitlesGUI extends SimpleScreen {

    @Inject private static ClientTitleManager titleManager;
    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static PluginContainer container;

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel titleLabel,  functionNameLabel, titleNameLabel, permissionNodeLabel, titleIdLabel, titleContextLabel;
    private UIFormContainer form;
    private UITextField nameField, permissionField, titleIdField, titleContextField;
    private UIButton buttonAdd, buttonRemove, buttonRefresh, saveChangesButton;
    private UIDynamicList<Title> titleList;
    private List<Title> masterTitleList = null;
    private UICheckBox hiddenCheckbox;

    //Todo: WTF.
    //private final BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> titleListFactory;

    private int screenWidth = 450;
    private int screenHeight = 300;

    private TitleModifyType mode;

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

        this.titleLabel = new UILabel(this, "Title Manager")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Title List Area
        final UIFormContainer listArea = new UIFormContainer(this, 220, 260, "");
        listArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        listArea.setMovable(false);
        listArea.setClosable(false);
        listArea.setBorder(FontColors.WHITE, 1, 185);
        listArea.setBackgroundAlpha(215);
        listArea.setBottomPadding(3);
        listArea.setRightPadding(3);
        listArea.setTopPadding(3);
        listArea.setLeftPadding(3);

        // Create left container
        final UIContainer<?> titleContainer = new UIContainer(this, 200, UIComponent.INHERITED);
        titleContainer.setBackgroundAlpha(0);
        titleContainer.setPosition(0,0, Anchor.CENTER | Anchor.TOP);
        titleContainer.setPadding(4, 4);
        titleContainer.setTopPadding(20);

        this.masterTitleList = Lists.newArrayList(titleManager.getTitles());

        System.out.println("All Titles: " + masterTitleList.size());

        this.titleList = new UIDynamicList<>(this, UIComponent.INHERITED, UIComponent.INHERITED);
        this.titleList.setItemComponentFactory(TitleItemComponent::new);
        this.titleList.setItemComponentSpacing(1);
        this.titleList.setCanDeselect(true);
        this.titleList.setName("list.left");
        this.titleList.setItems(this.masterTitleList);
        this.titleList.register(this);

        titleContainer.add(this.titleList);
        listArea.add(titleContainer);

        // Edit Container
        final UIFormContainer editArea = new UIFormContainer(this, 220, 260, "");
        editArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        editArea.setMovable(false);
        editArea.setClosable(false);
        editArea.setBorder(FontColors.WHITE, 1, 185);
        editArea.setBackgroundAlpha(215);
        editArea.setBottomPadding(3);
        editArea.setRightPadding(3);
        editArea.setTopPadding(3);
        editArea.setLeftPadding(3);

        this.functionNameLabel = new UILabel(this, "Modify Title")
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)
                        .shadow(true)
                        .underline(false)
                        .scale(1.2F)
                        .build()
                )
                .setPosition(0, 05, Anchor.CENTER | Anchor.TOP);

        this.titleNameLabel = new UILabel(this, "Name:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.0F)
                .build()
            )
            .setPosition(0, 15, Anchor.LEFT | Anchor.TOP);

        this.nameField = new UITextField(this, "", false)
            .setSize(200, 0)
            .setPosition(10, 30, Anchor.LEFT | Anchor.TOP)
            .setEditable(false)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        this.permissionNodeLabel = new UILabel(this, "Permission:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.0F)
                .build()
            ).setPosition(0, 55, Anchor.LEFT | Anchor.TOP);

        this.permissionField = new UITextField(this, "", false)
            .setSize(200, 0)
            .setPosition(10, 70, Anchor.LEFT | Anchor.TOP)
            .setEditable(false)
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .build()
            );

        this.titleIdLabel = new UILabel(this, "ID:")
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)
                        .shadow(true)
                        .scale(1.0F)
                        .build()
                ).setPosition(0, 95, Anchor.LEFT | Anchor.TOP);

        this.titleIdField = new UITextField(this, "", false)
                .setSize(200, 0)
                .setPosition(10, 110, Anchor.LEFT | Anchor.TOP)
                .setEditable(false)
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)
                        .shadow(false)
                        .build()
                );

        this.titleContextLabel = new UILabel(this, "Context:")
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)
                        .shadow(true)
                        .scale(1.0F)
                        .build()
                ).setPosition(0, 135, Anchor.LEFT | Anchor.TOP);

        this.titleContextField = new UITextField(this, "", false)
                .setSize(200, 0)
                .setPosition(10, 150, Anchor.LEFT | Anchor.TOP)
                .setEditable(true)
                .setFontOptions(FontOptions.builder()
                        .from(FontColors.WHITE_FO)

                        .shadow(false)
                        .build()
                );

        hiddenCheckbox = new UICheckBox(this);
        hiddenCheckbox.setText(TextFormatting.WHITE + "Hidden");
        hiddenCheckbox.setPosition(10, -7, Anchor.LEFT | Anchor.BOTTOM);
        hiddenCheckbox.setChecked(false);
        hiddenCheckbox.setName("checkbox.hidden");
        hiddenCheckbox.register(this);


        // Save Changes button
        this.saveChangesButton = new UIButtonBuilder(this)
                .width(30)
                .text(Text.of("almura.button.save"))
                .position(0,-5)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.save");

        editArea.add(this.functionNameLabel, this.titleNameLabel, this.nameField, this.permissionNodeLabel, this.permissionField, this.titleIdLabel, this.titleIdField, this.titleContextLabel, this.titleContextField, this.hiddenCheckbox, this
                .saveChangesButton);

        final UILabel titleSelectionLabel = new UILabel(this, "Server Titles:")
            .setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(true)
                .scale(1.1F)
                .build()
            )
            .setPosition(5, 10, Anchor.LEFT | Anchor.TOP);

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.GREEN, "+"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .tooltip("Add New Title")
                .listener(this)
                .build("button.add");

        // Remove button
        this.buttonRemove = new UIButtonBuilder(this)
                .width(10)
                .x(15)
                .text(Text.of(TextColors.RED, "-"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .tooltip("Remove Title")
                .listener(this)
                .build("button.remove");

        // Refresh button
        this.buttonRefresh = new UIButtonBuilder(this)
                .width(10)
                .x(35)
                .text(Text.of(TextColors.WHITE, "Refresh List"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .tooltip("Refresh Titles List")
                .listener(this)
                .build("button.refresh");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.button.close"))
                .listener(this)
                .build("button.close");

        this.form.add(this.titleLabel, listArea, editArea, titleSelectionLabel, this.buttonAdd, this.buttonRemove, this.buttonRefresh, buttonClose);

        this.addToScreen(this.form);
    }

    @Subscribe
    public void onValueChange(ComponentEvent.ValueChange event) {
        switch (event.getComponent().getName()) {
            case "checkbox.hidden":
                break;
        }
    }

    @Subscribe
    public void onUIListClickEvent(UIDynamicList.SelectEvent<Title> event) {
        this.nameField.setText(event.getNewValue().getName());
        this.nameField.setEditable(false);
        this.permissionField.setText(event.getNewValue().getPermission());
        this.permissionField.setEditable(true);
        this.titleIdField.setText(event.getNewValue().getId());
        this.titleIdField.setEditable(false);
        this.titleContextField.setText(event.getNewValue().getContent());
        this.titleContextField.setEditable(true);
        this.functionNameLabel.setText("Modify Title");
        this.hiddenCheckbox.setChecked(event.getNewValue().isHidden());
        this.mode = TitleModifyType.MODIFY;
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.add":
                this.functionNameLabel.setText("Add New Title");
                // Clear Fields & Unlock
                this.nameField.setText("");
                this.nameField.setEditable(true);
                this.titleIdField.setText("");
                this.titleIdField.setEditable(true);
                this.permissionField.setText("");
                this.permissionField.setEditable(true);
                this.titleContextField.setText("");
                this.titleContextField.setEditable(true);
                this.mode = TitleModifyType.ADD;
                break;
            case "button.refresh":
                final List<Title> titleList = Lists.newArrayList(titleManager.getTitles());
                this.titleList.setItems(titleList);
                notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Refreshing Titles List..."), 2));
                break;
            case "button.remove":
                this.mode = TitleModifyType.DELETE;

                notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Removing selected title"), 2));
                titleManager.deleteTitle(this.titleIdField.getText().toLowerCase().trim());
                break;
            case "button.save":
                switch (this.mode) {
                    case ADD:
                        notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Adding new Title"), 2));

                        titleManager.addTitle(this.titleIdField.getText().toLowerCase().trim(), this.nameField.getText().trim(), this.permissionField
                            .getText().toLowerCase().trim(), this.titleContextField.getText().trim(), this.hiddenCheckbox.isChecked());

                        Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("refreshList")).submit(container);
                        break;
                    case MODIFY:
                        notificationManager.queuePopup(new PopupNotification(Text.of("Title Manager"), Text.of("Saving Title Changes"), 2));

                        titleManager.modifyTitle(this.titleIdField.getText().toLowerCase().trim(), this.nameField.getText().trim(), this.permissionField.getText
                            ().toLowerCase().trim(), this.titleContextField.getText().trim(), this.hiddenCheckbox.isChecked());

                        Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("refreshList")).submit(container);
                        break;
                }
                break;
            case "button.close":
                this.close();
                break;
        }
    }

    private Consumer<Task> delayedTask(String details) {
        return task -> {
            if (details.equalsIgnoreCase("refreshList")) {
                this.masterTitleList = Lists.newArrayList(titleManager.getTitles());
                this.titleList.setItems(masterTitleList);
            }
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
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

    // TODO: Merge this with the ExchangeItemComponent class as a new default style
    public static class TitleItemComponent extends UIDynamicList.ItemComponent<Title> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();
        private static final int INNER_HOVER_COLOR = org.spongepowered.api.util.Color.ofRgb(40, 40, 40).getRgb();
        private static final int INNER_SELECTED_COLOR = org.spongepowered.api.util.Color.ofRgb(65, 65, 65).getRgb();

        private UIExpandingLabel titleLabel;

        public TitleItemComponent(final MalisisGui gui, final Title title) {
            super(gui, title);

            // Set padding
            this.setPadding(3, 3);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);

            // Set default size
            this.setSize(0, 20);

            this.construct(gui, item);
        }

        @SuppressWarnings("deprecation")
        protected void construct(final MalisisGui gui, final Title title) {

            this.titleLabel = new UIExpandingLabel(gui, Text.of(TextColors.WHITE, title.getContent()));
            if (title.isHidden()) {
                this.titleLabel.setText(this.titleLabel.getText() + TextFormatting.WHITE + " [HIDDEN]");
            }
            this.titleLabel.setPosition(2, 0, Anchor.LEFT | Anchor.MIDDLE);

            this.add(this.titleLabel);
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UIDynamicList) {
                final UIDynamicList parent = (UIDynamicList) this.parent;

                final int width = parent.getWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 5 : 0);

                this.setSize(width, getHeight());

                if (parent.getSelectedItem() == this.item) {
                    this.setColor(INNER_SELECTED_COLOR);
                } else if (this.isHovered()) {
                    this.setColor(INNER_HOVER_COLOR);
                } else {
                    this.setColor(INNER_COLOR);
                }

                super.drawBackground(renderer, mouseX, mouseY, partialTick);
            }
        }
    }
}
