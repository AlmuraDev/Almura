/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client;

import com.almuradev.almura.feature.menu.AlmuraScreens;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.menu.ListRows;
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.TitleMessages;
import com.almuradev.almura.feature.title.network.TitleMessages.ModifyType;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * The Titles window, as 3.5's {@code ManageTitlesGUI}: the titles the player may pick at the left, and at the right the player wearing the
 * one selected, with Apply and Remove. A title administrator's Edit Mode lists every title instead, with the ID, permission and content of
 * the one selected to change, and + and - to add and delete titles.
 * <p>
 * The content is typed with {@code &} colour codes (shown as they will look under it), as the Guide's editor; 3.5 switched the field between
 * the codes and the formatted text.
 */
final class TitlesScreen implements Screen {

    private static final float WIDTH = 370f, HEIGHT = 235f, PANE_WIDTH = 180f, BUTTON_HEIGHT = 16f, FIELD_HEIGHT = 16f, MARGIN = 10f;
    private static final float SMALL_WIDTH = 15f, ROW_HEIGHT = 20f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GREEN = 0xFF55FF55, RED = 0xFFFF5555;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);
    // 3.5's colour list: what it looks like, and its code
    private static final List<String> COLOURS = List.of("§1Dark Blue§f - &1", "§9Blue§f - &9", "§3Dark Aqua§f - &3", "§bAqua§f - &b",
        "§4Dark Red§f - &4", "§cRed§f - &c", "§eYellow§f - &e", "§6Gold§f - &6", "§2Dark Green§f - &2", "§aGreen§f - &a",
        "§5Dark Purple§f - &5", "§dLight Purple§f - &d", "§fWhite§f - &f", "§7Gray§f - &7", "§8Dark Gray§f - &8", "§0Black§f - &0",
        "§lBold§f - &l", "§mStrikethrough§f - &m", "§nUnderline§f - &n", "§oItalic§f - &o");

    private final boolean canChange, admin;
    private @Nullable ComponentFactory factory;
    private @Nullable ListView<Title> list;
    private @Nullable Label listLabel, modeLabel, showsAs;
    private @Nullable AnchorPanel playerPane, editPane;
    private @Nullable TextBox id, permission, content;
    private @Nullable CheckBox hidden;
    private @Nullable Button add, delete, save;
    private boolean editMode;
    private @Nullable ModifyType mode;
    // Selecting from code, not the player
    private boolean selecting;

    TitlesScreen(final boolean canChange, final boolean admin) {
        this.canChange = canChange;
        this.admin = admin;
    }

    @Override
    public Text title() {
        return Text.of("Title Manager");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final ListView<Title> list = factory.<Title>listView(title -> this.row(factory, title));
        list.setRowHeight(ROW_HEIGHT);
        ListRows.style(list);
        list.onEvent(Selection.SELECTED, event -> {
            if (!this.selecting) {
                this.onSelected();
            }
        });
        this.list = list;
        this.listLabel = factory.label(Text.of("Available Titles:").color(WHITE)).setTextScale(1.1f);

        // The player, wearing the title selected
        final Button apply = button(factory, "Apply", 50f, this::apply).setEnabled(this.canChange);
        apply.setTooltip(Text.of("Apply the selected title"));
        final Button remove = button(factory, "Remove", 50f, this::remove).setEnabled(this.canChange);
        remove.setTooltip(Text.of("Remove your title"));
        final AnchorPanel playerPane = pane(factory)
            .add(PlayerPreview.create(factory, PANE_WIDTH - 10f, HEIGHT - 70f, 55, TitlesScreen::playerName).setAnchor(Anchor.TOP_CENTER)
                .setMargin(0f, 4f, 0f, 0f))
            .add(remove.setAnchor(Anchor.BOTTOM_LEFT))
            .add(apply.setAnchor(Anchor.BOTTOM_RIGHT));
        this.playerPane = playerPane;

        // An administrator's title to change
        this.modeLabel = factory.label("").setTextScale(1.2f);
        this.id = field(factory, TitleMessages.MAX_ID);
        this.id.onEvent(TextInput.EDIT, event -> event.setValue(event.value().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "")));
        this.permission = field(factory, TitleMessages.MAX_PERMISSION);
        this.content = field(factory, TitleMessages.MAX_CONTENT);
        this.showsAs = factory.label("");
        this.content.onEvent(TextInput.EDITED, event -> this.updateShowsAs());
        final ComboBox<String> colours = factory.<String>comboBox(MinecraftText::of);
        colours.setItems(COLOURS);
        colours.select(0);
        colours.setSize(105f, BUTTON_HEIGHT);
        final Button addColour = button(factory, "Add", 40f, () -> colours.selected().ifPresent(colour -> {
            final TextBox field = this.content;
            if (field != null) {
                field.setText(field.text() + colour.substring(colour.length() - 2));
                this.updateShowsAs();
            }
        }));
        this.hidden = factory.checkBox(Text.of("Title Hidden from Players").color(WHITE)).setBoxSize(AlmuraScreens.CHECKBOX_SIZE);
        this.save = button(factory, "Save", 40f, this::save);
        final AnchorPanel editPane = pane(factory)
            .add(this.modeLabel.setAnchor(Anchor.TOP_CENTER).setMargin(0f, 2f, 0f, 0f))
            .add(label(factory, "ID:").setAnchor(Anchor.TOP_LEFT).setMargin(2f, 17f, 0f, 0f))
            .add(this.id.setAnchor(Anchor.TOP_LEFT).setMargin(5f, 28f, 5f, 0f).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(label(factory, "Permission:").setAnchor(Anchor.TOP_LEFT).setMargin(2f, 52f, 0f, 0f))
            .add(this.permission.setAnchor(Anchor.TOP_LEFT).setMargin(5f, 63f, 5f, 0f)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(label(factory, "Content:").setAnchor(Anchor.TOP_LEFT).setMargin(2f, 87f, 0f, 0f))
            .add(this.content.setAnchor(Anchor.TOP_LEFT).setMargin(5f, 98f, 5f, 0f)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(colours.setAnchor(Anchor.TOP_LEFT).setMargin(5f, 120f, 0f, 0f))
            .add(addColour.setAnchor(Anchor.TOP_LEFT).setMargin(130f, 120f, 0f, 0f))
            .add(this.showsAs.setAnchor(Anchor.TOP_LEFT).setMargin(5f, 142f, 0f, 0f))
            .add(this.hidden.setAnchor(Anchor.BOTTOM_LEFT).setMargin(5f, 0f, 0f, 4f))
            .add(this.save.setAnchor(Anchor.BOTTOM_RIGHT));
        editPane.setVisible(false);
        this.editPane = editPane;

        // + and -, Edit Mode and Close at the bottom
        this.add = small(factory, "+", GREEN, "Add New Title", this::startAdd);
        this.delete = small(factory, "-", RED, "Remove Title", this::confirmDelete);
        this.add.setVisible(false);
        this.delete.setVisible(false);
        final CheckBox editMode = factory.checkBox(Text.of("Edit Mode").color(WHITE)).setBoxSize(AlmuraScreens.CHECKBOX_SIZE);
        editMode.setVisible(this.admin);
        editMode.onEvent(CheckBox.CHANGED, event -> this.setEditMode(event.value() == CheckState.CHECKED));
        final Button close = button(factory, "Close", 40f, this::close);

        final float bottom = BUTTON_HEIGHT + 4f;
        final AnchorPanel content = factory.anchorPanel()
            .add(this.listLabel.setAnchor(Anchor.TOP_LEFT).setMargin(3f, 2f, 0f, 0f))
            .add(pane(factory).add(list.setAnchor(Anchor.FILL)).setAnchor(Anchor.FILL).setMargin(0f, 16f, 0f, bottom)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(PANE_WIDTH))
            .add(playerPane.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, bottom)
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH).setWidth(PANE_WIDTH))
            .add(editPane.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, bottom)
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH).setWidth(PANE_WIDTH))
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4f).add(this.add).add(this.delete).setAnchor(Anchor.BOTTOM_LEFT))
            .add(editMode.setAnchor(Anchor.BOTTOM_CENTER).setMargin(0f, 0f, 0f, 4f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));

        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window, this::close);
        this.editMode = false;
        this.refresh();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * New titles from the server: the list again, keeping the selected one (or, as 3.5, the player's own pick, else the first).
     */
    void refresh() {
        if (this.list == null) {
            return;
        }
        final List<Title> titles = this.editMode ? ClientTitles.titles() : ClientTitles.available();
        final Title wanted = this.list.selected().map(selected -> titles.stream().filter(title -> title.id().equals(selected.id())).findFirst()
            .orElse(null)).orElse(null);
        final Minecraft minecraft = Minecraft.getInstance();
        final Title own = minecraft.player == null ? null : ClientTitles.selectedFor(minecraft.player.getUUID());
        Title select = wanted;
        if (select == null && own != null && !this.editMode) {
            select = titles.stream().filter(title -> title.id().equals(own.id())).findFirst().orElse(null);
        }
        if (select == null && !titles.isEmpty()) {
            select = titles.getFirst();
        }
        this.selecting = true;
        try {
            this.list.setItems(titles);
            this.list.refresh();
            if (select != null) {
                this.list.selectItem(select);
            }
        } finally {
            this.selecting = false;
        }
        this.onSelected();
    }

    private void onSelected() {
        final Title title = this.list == null ? null : this.list.selected().orElse(null);
        if (!this.editMode) {
            ClientTitles.setPreview(title);
            return;
        }
        if (title != null && this.id != null && this.permission != null && this.content != null && this.hidden != null && this.modeLabel != null) {
            this.mode = ModifyType.MODIFY;
            this.modeLabel.setText(Text.of("Modify Title").color(WHITE));
            this.id.setText(title.id()).setReadOnly(true);
            this.permission.setText(title.permission()).setReadOnly(false);
            this.content.setText(title.content().replace('§', '&')).setReadOnly(false);
            this.hidden.setState(title.hidden() ? CheckState.CHECKED : CheckState.UNCHECKED).setEnabled(true);
            this.id.select(0, 0);
            this.permission.select(0, 0);
            this.content.select(0, 0);
            this.updateShowsAs();
        }
        this.updateButtons();
    }

    private void setEditMode(final boolean on) {
        this.editMode = on;
        if (this.listLabel != null) {
            this.listLabel.setText(Text.of(on ? "Server Titles:" : "Available Titles:").color(WHITE));
        }
        if (this.playerPane != null && this.editPane != null && this.add != null && this.delete != null) {
            this.playerPane.setVisible(!on);
            this.editPane.setVisible(on);
            this.add.setVisible(on);
            this.delete.setVisible(on);
        }
        if (on) {
            ClientTitles.setPreview(null);
        }
        this.refresh();
    }

    private void startAdd() {
        if (this.id == null || this.permission == null || this.content == null || this.hidden == null || this.modeLabel == null) {
            return;
        }
        this.mode = ModifyType.ADD;
        this.modeLabel.setText(Text.of("Add New Title").color(WHITE));
        this.id.setText("").setReadOnly(false);
        this.permission.setText("almura.title.").setReadOnly(false);
        this.content.setText("").setReadOnly(false);
        this.hidden.setState(CheckState.UNCHECKED).setEnabled(true);
        this.updateShowsAs();
        this.updateButtons();
    }

    private void save() {
        if (this.mode == null || this.id == null || this.permission == null || this.content == null || this.hidden == null) {
            return;
        }
        if (this.id.text().isBlank() || this.permission.text().isBlank() || this.content.text().isBlank()) {
            return;
        }
        ClientTitles.modify(this.mode, this.id.text().trim(), this.permission.text().trim(), fromEditor(this.content.text().trim()),
            this.hidden.state() == CheckState.CHECKED);
    }

    private void confirmDelete() {
        final Title title = this.list == null ? null : this.list.selected().orElse(null);
        if (this.factory == null || title == null) {
            return;
        }
        this.factory.overlays().dialog(this.factory.messageBox("Title Manager", "Delete the title '" + title.id() + "'?", MessageBox.Buttons.YES_NO)
            .onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    ClientTitles.modify(ModifyType.DELETE, title.id(), title.permission(), title.content(), title.hidden());
                }
            }));
    }

    private void apply() {
        final Title title = this.list == null ? null : this.list.selected().orElse(null);
        ClientTitles.setPreview(null);
        // The window stays open, showing the title once the server has it
        ClientTitles.select(title);
    }

    /**
     * Removes the player's title; the window stays open, showing them without one.
     */
    private void remove() {
        ClientTitles.setPreview(null);
        ClientTitles.select(null);
    }

    private void close() {
        ClientTitles.setPreview(null);
        Minecraft.getInstance().gui.screen().onClose();
    }

    private void updateButtons() {
        if (this.delete != null && this.save != null) {
            this.delete.setEnabled(this.editMode && this.mode == ModifyType.MODIFY);
            this.save.setEnabled(this.editMode && this.mode != null);
        }
    }

    private void updateShowsAs() {
        if (this.showsAs != null && this.content != null) {
            this.showsAs.setText(Text.of("Shows as: ").color(GRAY).append(MinecraftText.of(fromEditor(this.content.text()))));
        }
    }

    /**
     * A title in the list: as it looks, with its ID under it while editing.
     */
    private Component<?> row(final ComponentFactory factory, final Title title) {
        final StackPanel lines = factory.stackPanel().setSpacing(1f).add(factory.label(MinecraftText.of(title.content())));
        if (this.editMode) {
            lines.add(factory.label(Text.of(title.id() + (title.hidden() ? " (hidden)" : "")).color(GRAY)).setTextScale(0.8f));
        }
        lines.setHitTestVisible(false);
        final AnchorPanel row = factory.anchorPanel().add(lines.setAnchor(Anchor.MIDDLE_LEFT).setMargin(4f, 0f, 0f, 0f));
        ListRows.box(row, this.list != null && this.list.selected().filter(title::equals).isPresent());
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    private static net.minecraft.network.chat.Component playerName() {
        final Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? net.minecraft.network.chat.Component.empty() : minecraft.player.getDisplayName();
    }

    // ---- Helpers

    // & codes as section signs, as the Guide's editor
    private static String fromEditor(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    private static AnchorPanel pane(final ComponentFactory factory) {
        return factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(3f);
    }

    private static Label label(final ComponentFactory factory, final String text) {
        return factory.label(Text.of(text).color(WHITE));
    }

    private static TextBox field(final ComponentFactory factory, final int maxLength) {
        final TextBox field = factory.textBox().setMaxLength(maxLength).setReadOnly(true);
        field.setHeight(FIELD_HEIGHT);
        return field;
    }

    private static Button button(final ComponentFactory factory, final String text, final float width, final Runnable press) {
        return factory.button(Text.of(text)).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    private static Button small(final ComponentFactory factory, final String label, final int colour, final String tooltip, final Runnable press) {
        final Button button = factory.button(Text.of(label).color(colour)).setTextPadding(2f, 3f, 2f, 3f);
        button.setMinSize(0f, 0f);
        button.setSize(SMALL_WIDTH, BUTTON_HEIGHT).setTooltip(Text.of(tooltip));
        return button.onEvent(Button.PRESSED, event -> press.run());
    }
}
