/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import com.almuradev.almura.feature.guide.network.GuideMessages.Abilities;
import com.almuradev.almura.feature.guide.network.GuideMessages.ChangeType;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.Entry;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundChange;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundMove;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundPage;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Guide, as 3.5's page view: the pages on the left (a server page's name split at " - " into its title and, in grey under it, its
 * subject; a player's guide marked as theirs or whose), with Add, Remove and Details under them and the arrows that move a page; the
 * selected page on the right, shown as it reads or, with Edit, as its text to change, with the formatter bar; and at the bottom Show Guide
 * on Login (and for the guide's administrators Show Player Guides), Edit, Save and Close. Selecting a page asks the server for it. Client
 * only.
 */
final class GuideScreen implements Screen {

    // 3.5's size, smaller on a small screen
    private static final float WIDTH = 582f, HEIGHT = 315f, LIST_WIDTH = 156f, ROW_HEIGHT = 25f, GAP = 4f, BOTTOM = 22f;
    private static final float BUTTON_WIDTH = 50f, SMALL_WIDTH = 12f, SMALL_HEIGHT = 15f, SMALL_GAP = 3f, BUTTON_HEIGHT = 16f, ARROW_WIDTH = 15f, ARROW_HEIGHT = 11f, MARGIN = 10f;
    private static final int GRAY = 0xFFAAAAAA, GREEN = 0xFF55FF55, GOLD = 0xFFFFAA00, ADD = 0xFF55FF55, REMOVE = 0xFFFF5555, DETAILS = 0xFFFFFF55;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    // 3.5's page boxes: black with a grey border, grey when it is the page shown, a gap between them
    private static final Paint ROW = Paint.solid(0xFF000000), ROW_SHOWN = Paint.solid(0xFF4A4A4A);
    private static final Border ROW_BORDER = Border.of(0xFFA0A0A0, 1f);
    private static final float ROW_SPACING = 3f;
    // The window's bottom padding (3) shows under the bottom row: half of it, to centre the row in all of it
    private static final float BOTTOM_NUDGE = 3f;
    private static final float ARROW_ICON_WIDTH = 9f, ARROW_ICON_HEIGHT = 5f;
    private static final Paint ARROW_BUTTON = Paint.nineSlice(org.inspirenxe.strata.api.theme.SpriteId.of(com.almuradev.almura.Almura.ID,
        "theme/button"), org.inspirenxe.strata.api.geometry.Insets.of(2f));
    private static final Paint ARROW_BUTTON_DISABLED = Paint.nineSlice(org.inspirenxe.strata.api.theme.SpriteId.of(
        com.almuradev.almura.Almura.ID, "theme/button_disabled"), org.inspirenxe.strata.api.geometry.Insets.of(1f));
    private static final org.inspirenxe.strata.api.theme.SpriteId ARROW_UP =
        org.inspirenxe.strata.api.theme.SpriteId.of(com.almuradev.almura.Almura.ID, "guide/arrow_up");
    private static final org.inspirenxe.strata.api.theme.SpriteId ARROW_DOWN =
        org.inspirenxe.strata.api.theme.SpriteId.of(com.almuradev.almura.Almura.ID, "guide/arrow_down");
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);

    private @Nullable ComponentFactory factory;
    private @Nullable ListView<Entry> list;
    private @Nullable ScrollPanel preview;
    private @Nullable TextArea editor;
    private @Nullable StackPanel pageButtons;
    // The list's width: 3.5's, wider to fit the longest name, the window widening with it
    private float listWidth = LIST_WIDTH;
    private @Nullable Component<?> editorPanel, bar;
    private @Nullable CheckBox hideBar;
    private @Nullable Button save;
    private @Nullable Button remove, details, add;
    private @Nullable CheckBox playerGuides;
    private @Nullable ClientboundPage page;
    private @Nullable String shown;
    private List<Entry> shownEntries = List.of();
    private Abilities abilities = Abilities.NONE;
    private boolean inEditor;
    // Selecting a page from the server's message, not the player
    private boolean selecting;

    @Override
    public Text title() {
        return Text.of("Guide");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        this.listWidth = listWidth(ClientGuide.entries());
        // Built again (the game's window resized, or back from the link prompt): on the page as it reads, as the new panels are
        this.inEditor = false;
        final ListView<Entry> list = factory.<Entry>listView(entry -> this.row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        // The rows draw themselves, as 3.5's boxes
        list.setRowBackgrounds(false);
        list.setRowSpacing(ROW_SPACING);
        list.onEvent(Selection.SELECTED, event -> list.selected().ifPresent(this::request));
        // A double click opens a page's details, as 3.5
        list.onEvent(Activatable.ACTIVATED, event -> this.openDetails());
        this.list = list;

        // The page as it reads
        final ScrollPanel preview = factory.scrollPanel();
        preview.setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f, 2f);
        // Its right click menu, as 3.5's: Edit page and Copy
        preview.setContextMenu(factory.menu());
        preview.onEvent(org.inspirenxe.strata.api.component.HasContextMenu.SHOW_CONTEXT_MENU, event -> event.setValue(this.previewMenu(null)));
        this.preview = preview;

        // The page's text to change, with the formatter bar
        final TextArea editor = factory.textArea().setMaxLength(GuideMessages.MAX_CONTENT).setWrap(true);
        // The page view's padding, so a long line breaks at the same place in both
        editor.setTextPadding(new org.inspirenxe.strata.api.geometry.Insets(2f, 2f, 2f, 2f));
        editor.setHighlighter(GuideEditor.HIGHLIGHTER);
        editor.onEvent(TextInput.EDITED, event -> this.updateButtons());
        // Its right click menu, as 3.5's: the link's commands, Cut, Copy, Paste, and back to the page as it reads
        editor.onEvent(org.inspirenxe.strata.api.component.HasContextMenu.SHOW_CONTEXT_MENU, event -> event.setValue(this.editorMenu()));
        this.editor = editor;
        // Over the page for those who may change it, as on 3.5; using it starts editing
        final StackPanel bar = GuideEditor.bar(factory, editor, this::enterEditor, area -> GuideDialogs.link(factory, area));
        // 3.5's # at its end: between the page formatted (as it reads) and raw (its text with the codes, to change)
        bar.add(factory.button(Text.of("#")).setSize(BUTTON_HEIGHT, BUTTON_HEIGHT).setTooltip(Text.of("Formatted / Raw"))
            .onEvent(Button.PRESSED, event -> this.toggleEditor()).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        this.bar = bar;
        final AnchorPanel editorPanel = factory.anchorPanel()
            .add(editor.setAnchor(Anchor.FILL));
        this.editorPanel = editorPanel;
        editorPanel.setVisible(false);

        // Add, Remove and Details, under the list
        this.add = small(factory, "+", ADD, "Add", () -> GuideDialogs.create(factory, this.abilities));
        this.remove = small(factory, "-", REMOVE, "Remove", () -> {
            if (this.page != null) {
                GuideDialogs.remove(factory, this.page.id(), this.page.playerGuide());
            }
        });
        this.details = small(factory, "?", DETAILS, "Details", this::openDetails);
        // As 3.5's: narrow, a few apart
        final StackPanel pageButtons = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(SMALL_GAP)
            .add(this.add).add(this.remove).add(this.details);
        this.pageButtons = pageButtons;

        final CheckBox onLogin = setting(factory, "Show Guide on Login", AlmuraClientConfig.DISPLAY_GUIDE_ON_LOGIN);
        final CheckBox playerGuides = setting(factory, "Show Player Guides", AlmuraClientConfig.DISPLAY_OTHER_PLAYER_GUIDES);
        playerGuides.onEvent(CheckBox.CHANGED, event -> this.showEntries(ClientGuide.entries(), this.abilities, null));
        this.playerGuides = playerGuides;
        final CheckBox hideBar = setting(factory, "Hide Formatter Bar", AlmuraClientConfig.HIDE_GUIDE_FORMATTER_BAR);
        hideBar.onEvent(CheckBox.CHANGED, event -> this.updateButtons());
        this.hideBar = hideBar;
        final StackPanel settings = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(10f).add(onLogin).add(hideBar).add(playerGuides);

        this.save = factory.button(Text.of("Save")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> this.saveText());
        final Button close = factory.button(Text.translatable("almura.button.close")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.close());
        final StackPanel actions = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(GAP).add(this.save).add(close);

        final AnchorPanel page = factory.anchorPanel()
            // The pages in their own box, as on 3.5
            .add(factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(3f)
                .add(list.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, BOTTOM)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(this.listWidth))
            .add(bar.setAnchor(Anchor.TOP_LEFT).setMargin(this.listWidth + GAP, 0f, 0f, 0f))
            .add(preview.setAnchor(Anchor.FILL).setMargin(this.listWidth + GAP, 0f, 0f, BOTTOM))
            .add(editorPanel.setAnchor(Anchor.FILL).setMargin(this.listWidth + GAP, 0f, 0f, BOTTOM))
            // The bottom row, as 3.5's: Add, Remove and Details at the left, the settings after them, Save and Close at the right, each
            // centred in the strip under the boxes (down by half the window's bottom padding, so centred in what shows of it)
            .add(factory.anchorPanel()
                .add(pageButtons.setAnchor(Anchor.MIDDLE_LEFT).setMargin(0f, BOTTOM_NUDGE, 0f, 0f))
                .add(settings.setAnchor(Anchor.MIDDLE_LEFT).setMargin(SMALL_WIDTH * 3 + SMALL_GAP * 2 + 8f, BOTTOM_NUDGE, 0f, 0f))
                .add(actions.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, BOTTOM_NUDGE, 0f, 0f))
                .setHeight(BOTTOM).setAnchor(Anchor.BOTTOM_LEFT).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.BOTTOM));

        final Minecraft minecraft = Minecraft.getInstance();
        final float width = Math.min(WIDTH + this.listWidth - LIST_WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN);
        final float height = Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN);
        final Window window = factory.window(this.title())
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(width, height)
            .setAnchor(Anchor.CENTER);
        // Its x closes the guide as Close does, asking about unsaved changes first
        com.almuradev.almura.feature.menu.InGameScreens.closesScreen(window, this::close);
        if (this.page != null) {
            editor.setText(GuideEditor.toEditor(this.page.content()));
            this.showText(this.page.content());
        }
        this.showEntries(ClientGuide.entries(), ClientGuide.abilities(), ClientGuide.wanted());
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * As 3.5's Add, Remove and Details: narrow, one coloured character. A button keeps itself 20 wide unless given a minimum of its own,
     * and is as wide as its caption and text padding: little padding, so the one character fits in 12.
     */
    private static Button small(final ComponentFactory factory, final String label, final int colour, final String tooltip,
        final Runnable press) {
        final Button button = factory.button(Text.of(label).color(colour)).setTextPadding(2f, 4f, 2f, 2f);
        button.setMinSize(0f, 0f);
        button.setSize(SMALL_WIDTH, SMALL_HEIGHT).setTooltip(Text.of(tooltip));
        return button.onEvent(Button.PRESSED, event -> press.run());
    }

    /**
     * As 3.5: the list as wide as its longest name needs (with the arrows and the scroll bar), never narrower than 156.
     */
    private static float listWidth(final List<Entry> entries) {
        final net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        int widest = 0;
        for (final Entry entry : entries) {
            final int split = entry.name().indexOf(" - ");
            final List<String> lines = entry.playerGuide() ? List.of(entry.name(), "Player guide by " + entry.owner())
                : split < 0 ? List.of(entry.name()) : List.of(entry.name().substring(0, split), entry.name().substring(split + 3));
            for (final String line : lines) {
                widest = Math.max(widest, font.width(GuideText.stripped(line)));
            }
        }
        // The row's margin, the arrows, the row's border, the scroll bar and the box's padding and border
        final float extra = 4f + 3f + ARROW_WIDTH + 3f + 2f + 8f + 8f;
        return Math.max(LIST_WIDTH, widest + extra);
    }

    private static CheckBox setting(final ComponentFactory factory, final String text,
        final net.neoforged.neoforge.common.ModConfigSpec.BooleanValue value) {
        final CheckBox box = factory.checkBox(text).setState(AlmuraClientConfig.get(value) ? CheckState.CHECKED : CheckState.UNCHECKED);
        box.onEvent(CheckBox.CHANGED, event -> AlmuraClientConfig.set(value, event.value() == CheckState.CHECKED));
        return box;
    }

    /**
     * A page in the list: a server page's name split at " - " into its title and its subject under it, in grey; a player's guide's name
     * with whose it is under it; the arrows that move it, for those who may.
     */
    private Component<?> row(final ComponentFactory factory, final Entry entry) {
        final StackPanel lines = factory.stackPanel().setSpacing(1f);
        final int split = entry.name().indexOf(" - ");
        if (entry.playerGuide()) {
            lines.add(factory.label(MinecraftText.of("§f" + entry.name())));
            lines.add(factory.label(entry.mine() ? Text.of("My guide").color(GREEN).italic()
                : Text.of("Player guide by " + entry.owner()).color(GOLD).italic()));
        } else if (split < 0) {
            lines.add(factory.label(MinecraftText.of("§f" + entry.name())));
        } else {
            lines.add(factory.label(MinecraftText.of("§f" + entry.name().substring(0, split))));
            lines.add(factory.label(Text.of(GuideText.stripped(entry.name().substring(split + 3))).color(GRAY)));
        }
        lines.setHitTestVisible(false);
        final AnchorPanel cell = factory.anchorPanel().add(lines.setAnchor(Anchor.MIDDLE_LEFT).setMargin(4f, 0f, 0f, 0f));
        cell.setBackground(entry.id().equals(this.shown) ? ROW_SHOWN : ROW).setBorder(ROW_BORDER);
        // The arrows that move a page, only while editing
        if (entry.movable() && this.inEditor) {
            final int at = this.shownEntries.indexOf(entry);
            final StackPanel arrows = factory.stackPanel().setSpacing(1f)
                .add(arrow(factory, ARROW_UP, "Move up", at > 0 && sameGroup(this.shownEntries.get(at - 1), entry), entry, true))
                .add(arrow(factory, ARROW_DOWN, "Move down", at >= 0 && at + 1 < this.shownEntries.size()
                    && sameGroup(this.shownEntries.get(at + 1), entry), entry, false));
            cell.add(arrows.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 3f, 0f));
        }
        return cell.setAnchor(Anchor.FILL);
    }

    /**
     * 3.5's arrow: its 9 by 5 triangle at its size, on a small button (greyed out where the page can't move that way).
     */
    private static Component<?> arrow(final ComponentFactory factory, final org.inspirenxe.strata.api.theme.SpriteId icon,
        final String tooltip, final boolean enabled, final Entry entry, final boolean up) {
        final AnchorPanel arrow = factory.anchorPanel()
            .setBackground(enabled ? ARROW_BUTTON : ARROW_BUTTON_DISABLED)
            .add(factory.image(icon).setImageSize(ARROW_ICON_WIDTH, ARROW_ICON_HEIGHT).setSize(ARROW_ICON_WIDTH, ARROW_ICON_HEIGHT)
                .setOpacity(enabled ? 1f : 0.35f).setAnchor(Anchor.CENTER));
        arrow.setSize(ARROW_WIDTH, ARROW_HEIGHT).setTooltip(Text.of(tooltip));
        if (enabled) {
            arrow.onEvent(org.inspirenxe.strata.api.event.input.InputEvents.CLICK, event -> {
                if (event.button() == org.inspirenxe.strata.api.input.MouseButton.LEFT) {
                    ClientPacketDistributor.sendToServer(new ServerboundMove(entry.id(), up));
                }
            });
        }
        return arrow;
    }

    // Server pages, the player's own guides, and each other player's guides move among themselves
    private static boolean sameGroup(final Entry a, final Entry b) {
        return a.playerGuide() == b.playerGuide() && a.mine() == b.mine() && Objects.equals(a.owner(), b.owner());
    }

    /**
     * Shows the pages, selecting the one to show: the one asked for, else the one shown if it's still there, else the first. Other
     * players' guides only for the guide's administrators who ask for them.
     */
    void showEntries(final List<Entry> entries, final Abilities abilities, final @Nullable String wanted) {
        this.abilities = abilities;
        if (this.list == null) {
            return;
        }
        final boolean others = abilities.admin() && this.playerGuides != null && this.playerGuides.state() == CheckState.CHECKED;
        final List<Entry> shown = new ArrayList<>();
        for (final Entry entry : entries) {
            if (!entry.playerGuide() || entry.mine() || others) {
                shown.add(entry);
            }
        }
        this.shownEntries = shown;
        this.selecting = true;
        try {
            this.list.setItems(shown);
            this.list.refresh();
            Entry select = null;
            for (final Entry entry : shown) {
                if (entry.id().equals(wanted) || select == null && entry.id().equals(this.shown)) {
                    select = entry;
                }
            }
            if (select == null && !shown.isEmpty()) {
                select = shown.getFirst();
            }
            if (select != null) {
                this.list.selectItem(select);
                if (!select.id().equals(this.shown) || this.page == null || select.id().equals(wanted)) {
                    this.load(select.id());
                }
            } else {
                this.shown = null;
                this.page = null;
                this.leaveEditor();
                this.showText("§7There are no pages for you to read yet.");
            }
        } finally {
            this.selecting = false;
        }
        this.updateButtons();
    }

    private void request(final Entry entry) {
        if (this.selecting || entry.id().equals(this.shown)) {
            return;
        }
        // 3.5 left unsaved changes behind when another page was picked: ask first
        if (this.isChanged() && this.factory != null) {
            GuideDialogs.ask(this.factory, "Changes detected to current guide, do you wish to save?", answer -> {
                if (answer.equals(MessageBox.Result.YES)) {
                    this.saveText();
                    this.load(entry.id());
                } else if (answer.equals(MessageBox.Result.NO)) {
                    this.load(entry.id());
                } else if (this.list != null && this.shown != null) {
                    this.selecting = true;
                    this.shownEntries.stream().filter(shown -> shown.id().equals(this.shown)).findFirst().ifPresent(this.list::selectItem);
                    this.selecting = false;
                }
            });
            return;
        }
        this.load(entry.id());
    }

    private void load(final String id) {
        this.shown = id;
        if (this.list != null) {
            this.list.refresh();
        }
        ClientPacketDistributor.sendToServer(new ServerboundPage(id));
    }

    /**
     * Shows a page the server sent, as it reads.
     */
    void showPage(final ClientboundPage page) {
        if (!page.id().equals(this.shown)) {
            return;
        }
        this.page = page;
        this.leaveEditor();
        this.showText(page.content());
        if (this.editor != null) {
            this.editor.setText(GuideEditor.toEditor(page.content()));
        }
        this.updateButtons();
    }

    /**
     * How a change went: once saved, the page is asked for again (the list comes with it); a removed one leaves the list's.
     */
    void changed(final ChangeType change, final String id) {
        if (change == ChangeType.REMOVE) {
            if (id.equals(this.shown)) {
                this.shown = null;
                this.page = null;
            }
            return;
        }
        if (id.equals(this.shown)) {
            // Asked for again, with what was saved
            this.page = null;
        }
    }

    private void showText(final String text) {
        if (this.preview == null || this.factory == null) {
            return;
        }
        this.preview.clear();
        this.preview.add(GuideText.build(this.factory, text, (label, index, address) -> {
            label.setContextMenu(this.factory.menu());
            label.onEvent(org.inspirenxe.strata.api.component.HasContextMenu.SHOW_CONTEXT_MENU,
                event -> event.setValue(this.previewMenu(new LinkAt(index, address))));
        }).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        this.preview.setScroll(0f, 0f);
    }

    /**
     * A link of the page shown: which (from 0) and its address.
     */
    private record LinkAt(int index, String address) {
    }

    /**
     * Where a link is in the text: its markup from {@code start} to {@code end}, its text and address.
     */
    private record LinkSpan(int start, int end, String text, String address) {
    }

    private static @Nullable LinkSpan linkNumber(final String text, final int index) {
        final java.util.regex.Matcher matcher = GuideText.LINK.matcher(text);
        for (int i = 0; matcher.find(); i++) {
            if (i == index) {
                return new LinkSpan(matcher.start(), matcher.end(), matcher.group(1), matcher.group(2));
            }
        }
        return null;
    }

    private static @Nullable LinkSpan linkAround(final String text, final int caret) {
        final java.util.regex.Matcher matcher = GuideText.LINK.matcher(text);
        while (matcher.find()) {
            if (caret >= matcher.start() && caret <= matcher.end()) {
                return new LinkSpan(matcher.start(), matcher.end(), matcher.group(1), matcher.group(2));
            }
        }
        return null;
    }

    private static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    private org.inspirenxe.strata.api.control.menu.MenuItem item(final String text, final boolean enabled, final Runnable action) {
        return Objects.requireNonNull(this.factory).menuItem(text).setEnabled(enabled).setAction(action);
    }

    /**
     * The page as it reads: on a link Open link, and for those who may change the page Edit link... and Remove link (changed and saved at
     * once, as on 3.5); Edit page; Copy (the page's text).
     */
    private org.inspirenxe.strata.api.control.menu.Menu previewMenu(final @Nullable LinkAt link) {
        final ComponentFactory factory = Objects.requireNonNull(this.factory);
        final org.inspirenxe.strata.api.control.menu.Menu menu = factory.menu();
        final boolean editable = this.page != null && this.page.editable() && this.editor != null;
        if (link != null) {
            menu.addItem(this.item("Open link", true, () -> GuideText.open(link.address())));
            if (editable) {
                menu.addItem(this.item("Edit link...", true, () -> this.editLinkNumber(link.index(), true)));
                menu.addItem(this.item("Remove link", true, () -> this.editLinkNumber(link.index(), false)));
            }
            menu.addSeparator();
        }
        if (editable) {
            menu.addItem(this.item("Edit page", true, this::enterEditor));
            menu.addSeparator();
        }
        menu.addItem(this.item("Copy", this.editor != null, () ->
            minecraft().keyboardHandler.setClipboard(GuideText.stripped(GuideEditor.fromEditor(Objects.requireNonNull(this.editor).text())))));
        return menu;
    }

    /**
     * From the page as it reads: edits or removes a link in the text, then saves, as 3.5's menu did.
     */
    private void editLinkNumber(final int index, final boolean edit) {
        final TextArea editor = Objects.requireNonNull(this.editor);
        final LinkSpan span = linkNumber(editor.text(), index);
        if (span == null || this.factory == null) {
            return;
        }
        if (edit) {
            GuideDialogs.link(this.factory, editor, span.start(), span.end(), span.text(), span.address(), this::saveAndShow);
        } else {
            final String text = editor.text();
            editor.setText(text.substring(0, span.start()) + span.text() + text.substring(span.end()));
            this.saveAndShow();
        }
    }

    private void saveAndShow() {
        this.saveText();
        if (this.editor != null && !this.inEditor) {
            this.showText(GuideEditor.fromEditor(this.editor.text()));
        }
    }

    /**
     * The text being changed: in a link Edit link... and Remove link, else Create link... (for selected text without brackets or line
     * breaks); Cut, Copy, Paste; and Preview page, back to the page as it reads.
     */
    private org.inspirenxe.strata.api.control.menu.Menu editorMenu() {
        final ComponentFactory factory = Objects.requireNonNull(this.factory);
        final TextArea editor = Objects.requireNonNull(this.editor);
        final org.inspirenxe.strata.api.control.menu.Menu menu = factory.menu();
        final LinkSpan span = linkAround(editor.text(), editor.caret());
        if (span != null) {
            menu.addItem(this.item("Edit link...", true, () ->
                GuideDialogs.link(factory, editor, span.start(), span.end(), span.text(), span.address(), () -> { })));
            menu.addItem(this.item("Remove link", true, () -> {
                final String text = editor.text();
                editor.setText(text.substring(0, span.start()) + span.text() + text.substring(span.end()));
            }));
        } else {
            menu.addItem(this.item("Create link...", GuideEditor.canLink(editor), () -> GuideDialogs.link(factory, editor)));
        }
        menu.addSeparator();
        final boolean selected = !editor.selectedText().isEmpty();
        menu.addItem(this.item("Cut", selected, () -> {
            minecraft().keyboardHandler.setClipboard(editor.selectedText());
            replaceSelection(editor, "");
        }));
        menu.addItem(this.item("Copy", selected, () -> minecraft().keyboardHandler.setClipboard(editor.selectedText())));
        menu.addItem(this.item("Paste", true, () -> replaceSelection(editor, minecraft().keyboardHandler.getClipboard())));
        menu.addSeparator();
        menu.addItem(this.item("Preview page", true, this::toggleEditor));
        return menu;
    }

    private static void replaceSelection(final TextArea editor, final String with) {
        final String text = editor.text();
        final int start = Math.min(editor.selectionStart(), editor.selectionEnd());
        final int end = Math.max(editor.selectionStart(), editor.selectionEnd());
        final String clean = with.replace("\r\n", "\n").replace('\r', '\n');
        editor.setText(text.substring(0, start) + clean + text.substring(end));
        editor.select(start + clean.length(), start + clean.length());
    }

    private void enterEditor() {
        if (!this.inEditor) {
            this.toggleEditor();
        }
    }

    private void toggleEditor() {
        if (this.page == null || !this.page.editable() || this.editor == null || this.preview == null || this.editorPanel == null) {
            return;
        }
        this.inEditor = !this.inEditor;
        this.editorPanel.setVisible(this.inEditor);
        this.preview.setVisible(!this.inEditor);
        if (!this.inEditor) {
            // Shows the changes as they'll read
            this.showText(GuideEditor.fromEditor(this.editor.text()));
        }
        if (this.list != null) {
            this.list.refresh();
        }
        this.updateButtons();
    }

    private void leaveEditor() {
        final boolean was = this.inEditor;
        this.inEditor = false;
        if (was && this.list != null) {
            this.list.refresh();
        }
        if (this.editorPanel != null) {
            this.editorPanel.setVisible(false);
        }
        if (this.preview != null) {
            this.preview.setVisible(true);
        }
    }

    private boolean isChanged() {
        return this.page != null && this.editor != null && !GuideEditor.fromEditor(this.editor.text()).equals(this.page.content());
    }

    private void saveText() {
        if (this.page == null || this.editor == null || !this.isChanged()) {
            return;
        }
        ClientPacketDistributor.sendToServer(new ServerboundChange(ChangeType.MODIFY, this.page.id(), this.page.index(), this.page.name(),
            GuideEditor.fromEditor(this.editor.text()), this.page.playerGuide()));
    }

    private void openDetails() {
        if (this.factory != null && this.page != null && this.page.editable()) {
            GuideDialogs.details(this.factory, this.page);
        }
    }

    private void close() {
        if (this.isChanged() && this.factory != null) {
            GuideDialogs.ask(this.factory, "Changes detected to current guide, do you wish to save?", answer -> {
                if (answer.equals(MessageBox.Result.YES)) {
                    this.saveText();
                }
                if (!answer.equals(MessageBox.Result.CANCEL)) {
                    Minecraft.getInstance().gui.screen().onClose();
                }
            });
            return;
        }
        Minecraft.getInstance().gui.screen().onClose();
    }

    /**
     * What may be pressed: 3.5's rules.
     */
    private void updateButtons() {
        final boolean edits = this.abilities.edits();
        final Entry entry = this.page == null ? null
            : this.shownEntries.stream().filter(shown -> shown.id().equals(this.page.id())).findFirst().orElse(null);
        final boolean editable = this.page != null && this.page.editable();
        if (this.pageButtons != null) {
            this.pageButtons.setVisible(edits);
        }
        if (this.add != null) {
            this.add.setEnabled(this.abilities.add() || this.abilities.addPlayer());
        }
        if (this.remove != null) {
            this.remove.setEnabled(entry != null && entry.removable() && (entry.playerGuide() || this.abilities.remove()));
        }
        if (this.details != null) {
            this.details.setEnabled(editable);
        }
        if (this.save != null) {
            this.save.setVisible(edits);
            this.save.setEnabled(editable && this.isChanged());
        }
        if (this.playerGuides != null) {
            this.playerGuides.setVisible(this.abilities.admin());
        }
        if (this.hideBar != null) {
            this.hideBar.setVisible(edits);
        }
        // The formatter bar over the page, for those who may change it, unless hidden
        final boolean barShown = editable && (this.hideBar == null || this.hideBar.state() != CheckState.CHECKED);
        final float top = barShown ? BUTTON_HEIGHT + GAP : 0f;
        if (this.bar != null) {
            this.bar.setVisible(barShown);
        }
        if (this.preview != null) {
            this.preview.setMargin(this.listWidth + GAP, top, 0f, BOTTOM);
        }
        if (this.editorPanel != null) {
            this.editorPanel.setMargin(this.listWidth + GAP, top, 0f, BOTTOM);
        }
    }
}
