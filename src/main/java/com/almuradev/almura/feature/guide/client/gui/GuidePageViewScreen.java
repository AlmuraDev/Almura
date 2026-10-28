/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.shared.client.ui.component.WYSIWYGTextBox;
import com.almuradev.almura.shared.util.TextUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class GuidePageViewScreen extends BasicScreen {

    @Inject private static PluginContainer container;
    @Inject public static ClientPageManager manager;
    private static final int CHECKBOX_SPACING = 10;
    // The window's normal size, which it widens from when the page list needs more room
    private static final int BASE_FORM_WIDTH = 582, BASE_LIST_WIDTH = 156;
    // Room a list entry needs besides its text: text inset 2, entry padding 3 + 3, list padding 2 + 2, borders 2, scroll bar 11
    private static final int ENTRY_TEXT_RESERVE = 2 + 6 + 4 + 2 + 11;
    // The move arrows and a gap before them
    private static final int ENTRY_ARROW_RESERVE = 15 + 4;
    private static final int SCREEN_MARGIN = 10;
    private static final ResourceLocation ARROW_UP = new ResourceLocation(Almura.ID, "textures/gui/guide_arrow_up.png");
    private static final ResourceLocation ARROW_DOWN = new ResourceLocation(Almura.ID, "textures/gui/guide_arrow_down.png");
    // Outline included; two of these sharing an outline row are 21 high, leaving a pixel inside the entry's border above and below
    private static final int ARROW_BUTTON_HEIGHT = 11;
    // canModify: guide admin (server guides and everyone's player guides), canAddPlayer: may create player guides
    private boolean canAdd, canRemove, canModify, canAddPlayer;
    private float snapshotScrollOffset;
    private int snapshotCursorPositionX, snapshotCursorPositionY;
    // Only a text box that had focus gets it back, so a page just opened shows no cursor
    private boolean snapshotFocused;
    private BasicForm form;
    private List<UICheckBox> checkBoxes = new ArrayList<>();
    private BasicList<PageListEntry> pagesList;
    private WYSIWYGTextBox tbContent;
    @Nullable private UIButton buttonRemove, buttonAdd, buttonDetails, buttonSave;
    // Whether the formatter bar was last shown, null before the first update
    @Nullable private Boolean toolbarShown;

    public GuidePageViewScreen(final boolean canAdd, final boolean canRemove, final boolean canModify, final boolean canAddPlayer) {
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
        this.canAddPlayer = canAddPlayer;
    }

    /**
     * @return True if this player can create or edit any guide (the add/remove/details and save buttons)
     */
    private boolean hasEditControls() {
        return this.canModify || this.canAddPlayer;
    }

    /**
     * @return True if the open page may be edited: server guides by admins, player guides by their owner and admins
     */
    private boolean canEditPage() {
        final PageListEntry entry = manager.getPageEntry();
        if (entry == null) {
            return false;
        }
        return entry.isPlayerGuide() ? entry.isMine() || this.canModify : this.canModify;
    }

    private boolean canRemovePage() {
        final PageListEntry entry = manager.getPageEntry();
        if (entry == null) {
            return false;
        }
        return entry.isPlayerGuide() ? entry.isMine() || this.canModify : this.canRemove;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(BASE_FORM_WIDTH, 315, TextFormatting.WHITE + I18n.format("almura.guide.view.form.title"));
        this.form = form;

        // Page list (widened to fit its entries by fitToPages)
        this.pagesList = new BasicList<>(BASE_LIST_WIDTH, BasicScreen.getPaddedHeight(form));
        this.pagesList.setBorder(0xFFFFFF, 1, 215);
        this.pagesList.setPadding(2, 2);
        this.pagesList.setPosition(1, 0);
        this.pagesList.setItemComponentSpacing(1);
        this.pagesList.setItemComponentFactory(PageListEntryItemComponent::new);
        this.pagesList.setItems(manager.getPageEntries());
        this.pagesList.setSelectConsumer(i -> {
            if (i == null) {
                this.tbContent.getTextBox().setText("");
            } else {
                if (manager.getPage() != null && !manager.getPage().getId().equalsIgnoreCase(i.getId())) {
                    this.tbContent.getTextBox().setFocused(false);
                    this.clearCursorSnapshot();
                    this.restoreCursorSnapshot();
                }
                manager.requestPage(i.getId());
            }
        });

        if (this.hasEditControls()) {
            this.pagesList.setHeight(this.pagesList.getHeight() - 17);

            this.buttonAdd = new UIButtonBuilder()
                    .text(TextFormatting.GREEN + "+")
                    .onClick(() -> this.runScheduledTask("add"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(0, 0)
                    .visible(this.canAdd || this.canAddPlayer)
                    .tooltip(I18n.format("almura.guide.view.button.add.tooltip"))
                    .build("button.add");

            this.buttonRemove = new UIButtonBuilder()
                    .text(TextFormatting.RED + "-")
                    .onClick(() -> this.runScheduledTask("remove"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(BasicScreen.getPaddedX(this.buttonAdd, 2), 0)
                    .visible(this.canRemove || this.canAddPlayer)
                    .tooltip(I18n.format("almura.guide.view.button.remove.tooltip"))
                    .build("button.remove");

            this.buttonDetails = new UIButtonBuilder()
                    .text(TextFormatting.YELLOW + "?")
                    .onClick(() -> this.runScheduledTask("details"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(BasicScreen.getPaddedX(this.buttonRemove, 2), 0)
                    .visible(this.hasEditControls())
                    .tooltip(I18n.format("almura.guide.view.button.details.tooltip"))
                    .build("button.details");
        }

        this.tbContent = new WYSIWYGTextBox(this, BasicScreen.getPaddedHeight(form), "");
        this.tbContent.setPosition(BasicScreen.getPaddedX(this.pagesList, 2), 0);
        this.tbContent.getTextBox().setColors(0x555555, 0xc8c8c8, 0x00000);
        // Editing and the formatter bar follow the open page (updateEditing)
        this.tbContent.getTextBox().setEditable(false);
        // Save is only clickable once the text differs from the page as loaded
        this.tbContent.getTextBox().register(this);

        final UIButton buttonClose = new UIButtonBuilder()
            .width(50)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)

            .text(I18n.format("almura.button.close"))
            .onClick(this::close)
            .build("button.close");

        form.add(this.pagesList, this.tbContent, this.buttonAdd, buttonRemove, buttonDetails, buttonClose);

        this.tbContent.setHeight(BasicScreen.getPaddedHeight(form) - 17);
        if (this.hasEditControls()) {
            this.buttonSave = new UIButtonBuilder()
                .width(50)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(BasicScreen.getPaddedX(buttonClose, 2, Anchor.RIGHT), 0)
                //.position(-20,0)
                .text(I18n.format("almura.button.save"))
                .onClick(() -> {
                    if (manager.getPage() != null) {
                        if (!manager.preSnapshot.equalsIgnoreCase(this.tbContent.getTextBox().getText())) {
                            manager.getPage().setContent(this.tbContent.getTextBox().getText());
                            manager.requestSavePage();
                        }
                    }
                })
                .build("button.save");
            form.add(buttonSave);
        }

        // Options, centred along the bottom: the formatter bar one for those who get it, the player guides one for admins
        final GeneralCategory general = ClientStaticAccess.configAdapter.get().general;
        final List<UICheckBox> checkBoxes = new ArrayList<>();
        checkBoxes.add(this.createCheckBox("checkbox.show_on_login", I18n.format("almura.guide.view.checkbox.show_on_login"),
                general.displayGuideOnLogin));
        if (this.hasEditControls()) {
            checkBoxes.add(this.createCheckBox("checkbox.hide_formatter_bar", I18n.format("almura.guide.view.checkbox.hide_formatter_bar"),
                    general.guideHideFormatterBar));
        }
        if (this.canModify) {
            checkBoxes.add(this.createCheckBox("checkbox.show_player_guides", I18n.format("almura.guide.view.checkbox.show_player_guides"),
                    general.guideShowPlayerGuides));
        }
        checkBoxes.forEach(form::add);
        this.checkBoxes = checkBoxes;
        this.layoutCheckBoxes();

        this.addToScreen(form);
        this.fitToPages();
        this.updateEditing();
        this.updateSaveButton();
    }

    private void layoutCheckBoxes() {
        int checkBoxesWidth = CHECKBOX_SPACING * (this.checkBoxes.size() - 1);
        for (final UICheckBox checkBox : this.checkBoxes) {
            checkBoxesWidth += checkBox.getWidth();
        }
        // Centred in the space between the edit buttons on the left and Save/Close on the right
        final int left = this.buttonDetails != null && this.buttonDetails.isVisible() ? BasicScreen.getPaddedX(this.buttonDetails, CHECKBOX_SPACING) : 0;
        final int right = BasicScreen.getPaddedWidth(this.form) - 50 - (this.buttonSave != null ? 52 : 0) - CHECKBOX_SPACING;
        int checkBoxX = left + (right - left - checkBoxesWidth) / 2;
        for (final UICheckBox checkBox : this.checkBoxes) {
            // Level with the middle of the 15 high buttons
            checkBox.setPosition(checkBoxX, -(15 - checkBox.getHeight()) / 2, Anchor.BOTTOM | Anchor.LEFT);
            checkBoxX += checkBox.getWidth() + CHECKBOX_SPACING;
        }
    }

    /**
     * Widens the page list to fit its widest entry (both lines, and the move arrows where it has them) and the window with it, so the
     * text box keeps its size. Never narrower than the normal size, nor wider than the screen.
     */
    private void fitToPages() {
        int widest = 0;
        for (final PageListEntry entry : manager.getPageEntries()) {
            int textWidth = 0;
            for (final String line : entryLines(entry)) {
                textWidth = Math.max(textWidth, (int) Math.ceil(MalisisFont.minecraftFont.getStringWidth(line, FontColors.WHITE_FO)));
            }
            widest = Math.max(widest, textWidth + (this.isMovable(entry) ? ENTRY_ARROW_RESERVE : 0));
        }

        final int extra = BASE_FORM_WIDTH - BASE_LIST_WIDTH;
        int listWidth = Math.max(BASE_LIST_WIDTH, widest + ENTRY_TEXT_RESERVE);
        listWidth = Math.min(listWidth, Math.max(BASE_LIST_WIDTH, this.width - SCREEN_MARGIN - extra));
        if (listWidth == this.pagesList.getWidth()) {
            return;
        }

        this.pagesList.setSize(listWidth, this.pagesList.getHeight());
        this.form.setSize(extra + listWidth, this.form.getHeight());
        this.tbContent.setPosition(BasicScreen.getPaddedX(this.pagesList, 2), 0);
        this.layoutCheckBoxes();
    }

    /**
     * @return The lines a list entry shows: a player guide's name and whose it is, or a server guide's "Category - Name" halves
     */
    private static String[] entryLines(final PageListEntry entry) {
        if (entry.isPlayerGuide()) {
            return new String[] {entry.getName(), TextFormatting.ITALIC + (entry.isMine()
                    ? I18n.format("almura.guide.view.label.my_guide")
                    : I18n.format("almura.guide.view.label.player_guide", entry.getOwnerName()))};
        }
        return entry.getName().split(" - ");
    }

    /**
     * @return True if the entry gets move arrows: server guides for admins, player guides for their owner
     */
    private boolean isMovable(final PageListEntry entry) {
        return entry.isPlayerGuide() ? entry.isMine() : this.canModify;
    }

    private UICheckBox createCheckBox(final String name, final String text, final boolean checked) {
        final UICheckBox checkBox = new UICheckBox(text);
        checkBox.setName(name);
        checkBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        checkBox.setChecked(checked);
        checkBox.register(this);
        return checkBox;
    }

    @Subscribe
    public void onCheckBoxChange(final UICheckBox.CheckEvent event) {
        final GeneralCategory general = ClientStaticAccess.configAdapter.get().general;
        final boolean checked = event.getNewValue();
        switch (event.getComponent().getName()) {
            case "checkbox.show_on_login":
                general.displayGuideOnLogin = checked;
                break;
            case "checkbox.hide_formatter_bar":
                general.guideHideFormatterBar = checked;
                this.updateEditing();
                break;
            case "checkbox.show_player_guides":
                general.guideShowPlayerGuides = checked;
                manager.refreshVisiblePages();
                this.refreshPageEntries(null);
                break;
            default:
                return;
        }
        ClientStaticAccess.configAdapter.save();
    }

    /**
     * Lets the open page be edited (with the formatter bar unless hidden) if this player may edit it.
     */
    private void updateEditing() {
        final boolean editable = this.canEditPage();
        this.tbContent.getTextBox().setEditable(editable);

        final boolean showToolbar = editable && !ClientStaticAccess.configAdapter.get().general.guideHideFormatterBar;
        // Only on a change, so a collapsed bar stays collapsed between pages
        if (this.toolbarShown == null || this.toolbarShown != showToolbar) {
            if (showToolbar) {
                this.tbContent.showToolbar();
            } else {
                this.tbContent.hideToolbar();
            }
            this.toolbarShown = showToolbar;
        }
    }

    private void runScheduledTask(final String details) {
        // Null this so we don't see it drawing on any screens we open
        this.tooltip = null;

        Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(task -> {
            switch (details.toUpperCase()) {
                case "ADD":
                    new GuidePageCreateScreen(this, this.canAdd, this.canAddPlayer).display();
                    break;
                case "DETAILS":
                    new GuidePageDetailsScreen(this).display();
                    break;
                case "REMOVE":
                    if (manager.getPage() == null) {
                        return;
                    }

                    BasicMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Do you wish to delete the selected guide?",
                            MessageBoxButtons.YES_NO,
                            messageBoxResult -> {
                                if (messageBoxResult == MessageBoxResult.YES && manager.getPage() != null) {
                                    manager.requestRemovePage(manager.getPage().getId());
                                }
                            });
                    break;
                case "CLOSE":
                    if (Minecraft.getMinecraft().currentScreen instanceof GuidePageViewScreen) {
                        ((GuidePageViewScreen) Minecraft.getMinecraft().currentScreen).close();
                    }
                    break;
            }
        }).submit(container);
    }

    public void close() {
        if (!canModify || manager.getPage() != null) {
            super.close();
            return;
        }

        if (manager.preSnapshot.equals(this.tbContent.getTextBox().getText())) {
            super.close();
            return;
        }

        // Save the postSnapshot to the manager so it can be accessed within manager.
        manager.postSnapshot = this.tbContent.getTextBox().getText();
        BasicMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Changes detected to current guide, do you wish to save?",
                MessageBoxButtons.YES_NO_CANCEL, messageBoxResult -> {
                    if (messageBoxResult == MessageBoxResult.YES) {
                        manager.getPage().setContent(manager.postSnapshot);
                        manager.requestSavePage();
                    } else {
                        this.runScheduledTask("close");
                    }
                });
    }

    public void refreshPageEntries(final String switchToPage) {
        this.pagesList.setItems(manager.getPageEntries());
        this.fitToPages();

        if (switchToPage != null) {
            manager.getPageEntries()
                    .stream()
                    .filter(en -> en.getId().equalsIgnoreCase(switchToPage))
                    .findFirst()
                    .ifPresent(en -> this.pagesList.setSelectedItem(en));
        } else if (manager.getPage() != null) {
            // Keep the open page highlighted (e.g. after a move) without opening it again, which would lose unsaved edits
            final PageListEntry open = manager.getPageEntry();
            if (open != null) {
                final Consumer<PageListEntry> onSelect = this.pagesList.getSelectConsumer();
                this.pagesList.setSelectConsumer(null);
                this.pagesList.setSelectedItem(open);
                this.pagesList.setSelectConsumer(onSelect);
            }
        }
    }

    public void refreshPage() {
        Keyboard.enableRepeatEvents(true);
        this.snapshotCursor();
        this.tbContent.getTextBox().setText(TextUtil.asFriendlyText(manager.getPage() == null ? "" : manager.getPage().getContent()));
        this.tbContent.getTextBox().getCursorPosition().jumpToStart();
        manager.preSnapshot = this.tbContent.getTextBox().getText();

        this.updateButtons();
        this.restoreCursorSnapshot();
        this.clearCursorSnapshot();
    }

    private void snapshotCursor() {
        this.snapshotScrollOffset = this.tbContent.getTextBox().getScrollbar().offset();
        this.snapshotCursorPositionX = this.tbContent.getTextBox().getCursorPosition().index(); // the text index
        this.snapshotCursorPositionY = 0;
        this.snapshotFocused = this.tbContent.getTextBox().isFocused();
    }

    private void restoreCursorSnapshot() {
        if (this.snapshotFocused) {
            this.tbContent.getTextBox().focus();
        }
        this.tbContent.getTextBox().getScrollbar().scrollTo(this.snapshotScrollOffset);
        this.tbContent.getTextBox().getCursorPosition().jumpTo(this.snapshotCursorPositionX);
    }

    private void clearCursorSnapshot() {
        this.snapshotScrollOffset = 0;
        this.snapshotCursorPositionX = 0;
        this.snapshotCursorPositionY = 0;
        this.snapshotFocused = false;
    }

    private void updateButtons() {
        if (this.buttonAdd != null) {
            this.buttonAdd.setEnabled(this.canAdd || this.canAddPlayer);
        }

        if (this.buttonRemove != null) {
            this.buttonRemove.setEnabled(this.canRemovePage());
        }

        if (this.buttonDetails != null) {
            this.buttonDetails.setEnabled(this.canEditPage());
        }

        this.updateSaveButton();
        this.updateEditing();
    }

    @Subscribe
    public void onContentUpdate(final ContentUpdateEvent<?> event) {
        if (this.tbContent != null && event.getComponent() == this.tbContent.getTextBox()) {
            this.updateSaveButton();
        }
    }

    /**
     * Save is clickable when the open page may be edited and its text has changed since it was loaded (or last saved).
     */
    private void updateSaveButton() {
        if (this.buttonSave == null) {
            return;
        }
        this.buttonSave.setEnabled(this.canEditPage() && manager.preSnapshot != null
                && !manager.preSnapshot.equals(this.tbContent.getTextBox().getText()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Drawn behind the create/details screens and dialogs: no hover or tooltip of its own (the tooltip of the button that opened them
        // would otherwise stay under the mouse, on top of theirs)
        if (Minecraft.getMinecraft().currentScreen != this) {
            this.tooltip = null;
            mouseX = -1;
            mouseY = -1;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    private static final class PageListEntryItemComponent extends BasicList.ItemComponent<PageListEntry> {

        PageListEntryItemComponent(final MalisisGui gui, final BasicList<PageListEntry> parent, final PageListEntry item) {
            super(gui, parent, item);
            this.setHeight(25);
            this.setOnDoubleClickConsumer(i -> {
                final GuidePageViewScreen parentScreen = (GuidePageViewScreen) this.getGui();
                if (parentScreen.canEditPage()) {
                    parentScreen.runScheduledTask("details");
                }
            });
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            final UIImage icon = null;//this.item.getIcon() == null ? null : new UIImage(this.item.getIcon());
            if (icon != null) {
                icon.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);
                this.add(icon);
            }

            this.addMoveArrows(gui);

            // Ensure all text lines up if one happens to have an image
            final int xPos = 2; //manager.getPageEntries().stream().anyMatch(entry -> entry.getIcon() != null);

            // Player guides: the name, then whose it is (they aren't "Category - Name" like server guides)
            if (this.item.isPlayerGuide()) {
                final UILabel nameLabel = new UILabel(this.item.getName());
                nameLabel.setPosition(xPos, 0, Anchor.TOP | Anchor.LEFT);
                nameLabel.setFontOptions(FontColors.WHITE_FO);

                final UILabel ownerLabel = new UILabel(TextFormatting.ITALIC + (this.item.isMine()
                        ? I18n.format("almura.guide.view.label.my_guide")
                        : I18n.format("almura.guide.view.label.player_guide", this.item.getOwnerName())));
                ownerLabel.setPosition(xPos, BasicScreen.getPaddedY(nameLabel, 2));
                ownerLabel.setFontOptions(this.item.isMine() ? FontColors.GREEN_FO : FontColors.GOLD_FO);
                this.add(nameLabel, ownerLabel);
                return;
            }

            final String[] splitName = this.item.getName().split(" - ");
            final UILabel categoryLabel = new UILabel(splitName[0]);
            categoryLabel.setPosition(xPos, 0, Anchor.MIDDLE | Anchor.LEFT);
            categoryLabel.setFontOptions(FontColors.WHITE_FO);
            this.add(categoryLabel);

            if (splitName.length > 1) {
                categoryLabel.setAnchor(Anchor.TOP | Anchor.LEFT);

                final UILabel nameLabel = new UILabel(splitName[1]);
                nameLabel.setPosition(xPos, BasicScreen.getPaddedY(categoryLabel, 2));
                nameLabel.setFontOptions(FontColors.GRAY_FO);
                this.add(nameLabel);
            }
        }

        /**
         * Up and down arrows that move this page among the pages of its kind: server guides for admins, a player's own guides for them.
         */
        private void addMoveArrows(final MalisisGui gui) {
            final GuidePageViewScreen screen = (GuidePageViewScreen) gui;
            final boolean movable = this.item.isPlayerGuide() ? this.item.isMine() : screen.canModify;
            if (!movable) {
                return;
            }

            final List<PageListEntry> group = manager.getPageEntries()
                    .stream()
                    .filter(entry -> this.item.isPlayerGuide() ? entry.isPlayerGuide() && entry.isMine() : !entry.isPlayerGuide())
                    .collect(Collectors.toList());
            final boolean first = group.isEmpty() || group.get(0).equals(this.item);
            final boolean last = group.isEmpty() || group.get(group.size() - 1).equals(this.item);

            // Stacked on the entry's middle, sharing the outline row between them (the entry's height isn't set yet while it is built)
            final int half = ARROW_BUTTON_HEIGHT / 2;
            this.add(this.arrowButton(gui, ARROW_UP, -1, -half, !first),
                    this.arrowButton(gui, ARROW_DOWN, 1, ARROW_BUTTON_HEIGHT - 1 - half, !last));
        }

        private UIButton arrowButton(final MalisisGui gui, final ResourceLocation texture, final int direction, final int y,
                final boolean enabled) {
            final UIImage arrow = new UIImage(GuiConfig.wholeTexture(texture));
            arrow.setSize(9, 5);
            final UIButton button = new UIButtonBuilder()
                    .image(arrow)
                    .anchor(Anchor.MIDDLE | Anchor.RIGHT)
                    .position(0, y)
                    .enabled(enabled)
                    .tooltip(I18n.format(direction < 0 ? "almura.guide.view.button.move_up.tooltip" : "almura.guide.view.button.move_down.tooltip"))
                    .onClick(() -> manager.movePage(this.item.getId(), direction))
                    .build("button.move." + (direction < 0 ? "up" : "down"));
            // The button graphic has a 1 pixel black outline: at 15 x 11 the 9 x 5 arrow sits in the middle of the grey inside it
            button.setSize(15, ARROW_BUTTON_HEIGHT);
            return button;
        }
    }
}
