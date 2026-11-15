/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook.client;

import com.almuradev.almura.content.registry.ContentCrop;
import com.almuradev.almura.feature.handbook.HandbookRule;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook.SkillLevel;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.menu.ListRows;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.input.MouseButton;
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
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Skills Handbook, as 3.5's {@code SkillsHandbookScreen}: a tab per skill, the player's level in it and a search box, and the skill's
 * rules with their item, action, level (green when the player has it, red when not), experience and money, sorted by a heading's click.
 */
final class HandbookScreen implements Screen {

    // 3.5's size, smaller on a small screen
    private static final float WIDTH = 420f, HEIGHT = 300f, MARGIN = 10f, TAB_HEIGHT = 15f, TAB_GAP = 2f, BUTTON_HEIGHT = 16f;
    private static final float SEARCH_WIDTH = 120f, HEADER_HEIGHT = 12f, ROW_HEIGHT = 20f, ICON = 16f;
    // The right hand columns, right aligned in the rows at these insets; headings line up past the list's border, padding and scroll bar
    private static final float MONEY_INSET = 4f, XP_INSET = 54f, LEVEL_INSET = 96f, ACTION_INSET = 142f, HEADER_EXTRA_INSET = 10f;
    // A name stops short of the widest action ("Use on") and a gap
    private static final float ACTION_COLUMN_WIDTH = 40f, NAME_X = 22f, NAME_GAP = 6f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, YELLOW = 0xFFFFFF55, GREEN = 0xFF55FF55, RED = 0xFFFF5555,
        AQUA = 0xFF55FFFF, GOLD = 0xFFFFAA00;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);
    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0.##");

    private enum Column {
        NAME("Item"),
        ACTION("Action"),
        LEVEL("Level"),
        XP("XP"),
        MONEY("Money");

        final String title;

        Column(final String title) {
            this.title = title;
        }
    }

    private final Map<String, Integer> levels = new LinkedHashMap<>();
    // Each skill's rows, in the order of the tabs
    private final Map<String, List<Entry>> entries = new LinkedHashMap<>();
    private final Map<String, Button> tabs = new LinkedHashMap<>();
    private final Map<Column, Label> headers = new EnumMap<>(Column.class);
    private @Nullable ListView<Entry> list;
    private @Nullable Label levelLabel, emptyLabel;
    private @Nullable TextBox search;
    private String skill;
    private Column sortColumn = Column.LEVEL;
    private boolean ascending = true;

    HandbookScreen(final ClientboundHandbook handbook) {
        for (final SkillLevel level : handbook.levels()) {
            this.levels.put(level.skill(), level.level());
            this.entries.put(level.skill(), new ArrayList<>());
        }
        // One row per thing shown: a block listed once per state, or twice (ripe nether wart gives experience, any nether wart is only level
        // gated), looks the same here, so the one giving the most experience is kept
        final Map<String, Entry> shown = new LinkedHashMap<>();
        for (final HandbookRule rule : handbook.rules()) {
            final Entry entry = new Entry(rule, this.levels.getOrDefault(rule.skill(), -1));
            final String key = rule.skill() + '|' + rule.action() + '|' + entry.name + '|' + rule.level();
            final Entry existing = shown.get(key);
            if (existing == null || rule.xp() > existing.rule.xp()) {
                shown.put(key, entry);
            }
        }
        for (final Entry entry : shown.values()) {
            this.entries.computeIfAbsent(entry.rule.skill(), key -> new ArrayList<>()).add(entry);
        }
        this.skill = this.entries.keySet().stream().findFirst().orElse("");
    }

    @Override
    public Text title() {
        return Text.of("Skills Handbook");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Minecraft minecraft = Minecraft.getInstance();
        final float width = Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN);
        final float height = Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN);

        // A tab per skill, sharing the width
        final StackPanel tabRow = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(TAB_GAP);
        final int count = Math.max(1, this.entries.size());
        final float inner = width - 7f;
        final float tabWidth = (inner - TAB_GAP * (count - 1)) / count;
        this.tabs.clear();
        for (final String skill : this.entries.keySet()) {
            final Button tab = factory.button(Text.of(skillName(skill))).setSize(tabWidth, TAB_HEIGHT)
                .onEvent(Button.PRESSED, event -> this.showSkill(skill));
            this.tabs.put(skill, tab);
            tabRow.add(tab);
        }

        // Under the tabs: the player's level in the skill, and a search box that filters the list as it's typed in
        final float rowY = TAB_HEIGHT + 4f;
        final Label levelLabel = factory.label("");
        this.levelLabel = levelLabel;
        final TextBox search = factory.textBox().setMaxLength(64);
        search.setSize(SEARCH_WIDTH, BUTTON_HEIGHT);
        search.onEvent(TextInput.EDITED, event -> this.sort());
        this.search = search;

        // Column headings: a click sorts by the column, again for the other way
        final float headerTop = rowY + BUTTON_HEIGHT + 3f;
        this.headers.clear();
        final AnchorPanel page = factory.anchorPanel()
            .add(tabRow.setAnchor(Anchor.TOP_LEFT))
            .add(levelLabel.setAnchor(Anchor.TOP_LEFT).setMargin(2f, rowY + 4f, 0f, 0f))
            .add(factory.label(Text.of("Search:").color(WHITE)).setAnchor(Anchor.TOP_RIGHT).setMargin(0f, rowY + 4f, SEARCH_WIDTH + 4f, 0f))
            .add(search.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, rowY, 0f, 0f))
            .add(this.header(factory, Column.NAME).setAnchor(Anchor.TOP_LEFT).setMargin(NAME_X + 2f, headerTop + 2f, 0f, 0f))
            .add(this.header(factory, Column.ACTION).setAnchor(Anchor.TOP_RIGHT).setMargin(0f, headerTop + 2f, ACTION_INSET + HEADER_EXTRA_INSET, 0f))
            .add(this.header(factory, Column.LEVEL).setAnchor(Anchor.TOP_RIGHT).setMargin(0f, headerTop + 2f, LEVEL_INSET + HEADER_EXTRA_INSET, 0f))
            .add(this.header(factory, Column.XP).setAnchor(Anchor.TOP_RIGHT).setMargin(0f, headerTop + 2f, XP_INSET + HEADER_EXTRA_INSET, 0f))
            .add(this.header(factory, Column.MONEY).setAnchor(Anchor.TOP_RIGHT).setMargin(0f, headerTop + 2f, MONEY_INSET + HEADER_EXTRA_INSET, 0f));

        // The rules, in the lighter box, down to the same gap above Close as below it
        final float nameWidth = inner - 4f - 2f - 8f - NAME_X - ACTION_INSET - ACTION_COLUMN_WIDTH - NAME_GAP;
        for (final List<Entry> skillEntries : this.entries.values()) {
            for (final Entry entry : skillEntries) {
                entry.displayName = shorten(entry.name, nameWidth);
            }
        }
        final ListView<Entry> list = factory.<Entry>listView(entry -> this.row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        ListRows.style(list);
        this.list = list;
        final Label emptyLabel = factory.label("");
        emptyLabel.setHitTestVisible(false);
        this.emptyLabel = emptyLabel;
        page.add(factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f)
                .add(list.setAnchor(Anchor.FILL))
                .add(emptyLabel.setAnchor(Anchor.TOP_CENTER).setMargin(0f, 8f, 0f, 0f))
                .setAnchor(Anchor.FILL).setMargin(0f, headerTop + HEADER_HEIGHT, 0f, BUTTON_HEIGHT + 6f))
            .add(factory.label(Text.of("Levels you have are green, ones you need are red.").color(GRAY)).setAnchor(Anchor.BOTTOM_LEFT)
                .setMargin(2f, 0f, 0f, 4f))
            .add(factory.button(Text.translatable("almura.button.close")).setSize(40f, BUTTON_HEIGHT)
                .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose()).setAnchor(Anchor.BOTTOM_RIGHT));

        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(width, height)
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        this.showSkill(this.skill);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private AnchorPanel header(final ComponentFactory factory, final Column column) {
        final Label label = factory.label(Text.of(column.title).color(GRAY)).setTextScale(0.9f);
        label.setHitTestVisible(false);
        this.headers.put(column, label);
        final AnchorPanel clickable = factory.anchorPanel().add(label.setAnchor(Anchor.TOP_LEFT));
        clickable.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT) {
                this.sortBy(column);
            }
        });
        return clickable;
    }

    private void showSkill(final String skill) {
        this.skill = skill;
        for (final Map.Entry<String, Button> tab : this.tabs.entrySet()) {
            tab.getValue().setText(Text.of(skillName(tab.getKey())).color(tab.getKey().equals(skill) ? YELLOW : WHITE));
        }
        if (this.levelLabel != null) {
            final int level = this.levels.getOrDefault(skill, -1);
            this.levelLabel.setText(Text.of("Your " + skillName(skill) + " level: ").color(WHITE)
                .append(level < 0 ? Text.of("unknown").color(GRAY) : Text.of(String.valueOf(level)).color(YELLOW)));
        }
        this.sort();
    }

    private void sortBy(final Column column) {
        if (this.sortColumn == column) {
            this.ascending = !this.ascending;
        } else {
            this.sortColumn = column;
            this.ascending = true;
        }
        this.sort();
    }

    private void sort() {
        if (this.list == null || this.emptyLabel == null) {
            return;
        }
        final Comparator<Entry> byName = Comparator.comparing((Entry entry) -> entry.name, String.CASE_INSENSITIVE_ORDER);
        Comparator<Entry> order = switch (this.sortColumn) {
            case ACTION -> Comparator.comparing((Entry entry) -> entry.rule.action().label);
            case LEVEL -> Comparator.comparingInt((Entry entry) -> Math.max(0, entry.rule.level()));
            case XP -> Comparator.comparingDouble((Entry entry) -> Math.max(0, entry.rule.xp()));
            case MONEY -> Comparator.comparingDouble((Entry entry) -> Math.max(0, entry.rule.money()));
            default -> byName;
        };
        if (!this.ascending) {
            order = order.reversed();
        }
        if (this.sortColumn != Column.NAME) {
            // Rows that tie stay alphabetical
            order = order.thenComparing(byName);
        }
        final String search = this.search == null ? "" : this.search.text().trim().toLowerCase(Locale.ROOT);
        final List<Entry> rows = new ArrayList<>();
        for (final Entry entry : this.entries.getOrDefault(this.skill, List.of())) {
            if (search.isEmpty() || entry.name.toLowerCase(Locale.ROOT).contains(search)) {
                rows.add(entry);
            }
        }
        rows.sort(order);
        this.list.setItems(rows);
        this.list.refresh();
        this.emptyLabel.setText(Text.of(search.isEmpty() ? "Nothing in this skill needs a level or gives experience."
            : "Nothing matches your search.").color(GRAY));
        this.emptyLabel.setVisible(rows.isEmpty());
        // The sorted column, and which way
        for (final Map.Entry<Column, Label> header : this.headers.entrySet()) {
            final boolean active = header.getKey() == this.sortColumn;
            header.getValue().setText(active ? Text.of(header.getKey().title + (this.ascending ? " ▲" : " ▼")).color(WHITE)
                : Text.of(header.getKey().title).color(GRAY));
        }
    }

    private Component<?> row(final ComponentFactory factory, final Entry entry) {
        final AnchorPanel row = factory.anchorPanel();
        if (!entry.icon.isEmpty()) {
            row.add(factory.image(MinecraftVisuals.item(entry.icon)).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f));
        }
        row.add(factory.label(Text.of(entry.displayName).color(WHITE)).setAnchor(Anchor.MIDDLE_LEFT).setMargin(NAME_X, 0f, 0f, 0f))
            .add(factory.label(Text.of(entry.rule.action().label).color(GRAY)).setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, ACTION_INSET, 0f))
            .add(factory.label(entry.levelText).setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, LEVEL_INSET, 0f))
            .add(factory.label(entry.xpText).setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, XP_INSET, 0f))
            .add(factory.label(entry.moneyText).setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, MONEY_INSET, 0f));
        for (final Component<?> child : row.children()) {
            child.setHitTestVisible(false);
        }
        row.setTooltip(entry.tooltip);
        ListRows.box(row, this.list != null && this.list.selected().filter(entry::equals).isPresent());
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    /**
     * @return the text, cut short with "..." if wider than the width
     */
    private static String shorten(final String text, final float width) {
        final var font = Minecraft.getInstance().font;
        if (font.width(text) <= width) {
            return text;
        }
        int low = 0, high = text.length();
        while (low < high) {
            final int mid = (low + high + 1) / 2;
            if (font.width(text.substring(0, mid).trim() + "...") <= width) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return text.substring(0, low).trim() + "...";
    }

    private static String skillName(final String skill) {
        return skill.isEmpty() ? skill : Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
    }

    /**
     * A rule as it's shown: its block or item resolved to an icon and a name.
     */
    private static final class Entry {

        final HandbookRule rule;
        final ItemStack icon;
        final String name;
        // The name as the row shows it, shortened to fit
        String displayName;
        final Text levelText, xpText, moneyText, tooltip;

        Entry(final HandbookRule rule, final int playerLevel) {
            this.rule = rule;
            ItemStack icon = ItemStack.EMPTY;
            String name = rule.id();
            final Identifier id = Identifier.tryParse(rule.id());
            if (rule.block()) {
                final Block block = id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
                if (block != Blocks.AIR) {
                    icon = iconFor(block);
                    // A block's own name, unless its icon isn't the block itself (a crop's icon is its seed, but it's the crop that's broken)
                    name = !icon.isEmpty() && icon.getItem() instanceof final BlockItem item && item.getBlock() == block
                        ? icon.getHoverName().getString() : block.getName().getString();
                }
            } else {
                final Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
                if (item != Items.AIR) {
                    icon = new ItemStack(item);
                    name = icon.getHoverName().getString();
                }
            }
            this.icon = icon;
            this.name = name;
            this.displayName = name;
            this.levelText = rule.level() < 0 ? Text.of("-").color(GRAY)
                : Text.of(String.valueOf(rule.level())).color(playerLevel < 0 ? GRAY : playerLevel >= rule.level() ? GREEN : RED);
            this.xpText = rule.xp() < 0 ? Text.of("-").color(GRAY) : Text.of(NUMBER.format(rule.xp())).color(AQUA);
            this.moneyText = rule.money() <= 0 ? Text.of("-").color(GRAY) : Text.of("$" + NUMBER.format(rule.money())).color(GOLD);
            this.tooltip = Text.concat(
                Text.of(name).color(WHITE),
                Text.of("\n" + skillName(rule.skill()) + ": ").color(GRAY), Text.of(rule.action().label).color(WHITE),
                Text.of("\nLevel needed: ").color(GRAY), rule.level() < 0 ? Text.of("none").color(GRAY) : this.levelText,
                Text.of("\nExperience: ").color(GRAY), this.xpText,
                Text.of("\nMoney: ").color(GRAY), this.moneyText);
        }

        /**
         * @return what the block looks like in the inventory: a crop's seed, otherwise its own item
         */
        private static ItemStack iconFor(final Block block) {
            if (block instanceof final ContentCrop crop) {
                final Identifier seed = Identifier.tryParse(crop.def().seed());
                final Item item = seed == null ? Items.AIR : BuiltInRegistries.ITEM.getOptional(seed).orElse(Items.AIR);
                if (item != Items.AIR) {
                    return new ItemStack(item);
                }
            }
            final Item item = block.asItem();
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
    }
}
