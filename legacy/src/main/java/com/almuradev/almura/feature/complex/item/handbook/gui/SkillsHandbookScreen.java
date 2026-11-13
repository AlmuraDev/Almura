/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.handbook.gui;

import com.almuradev.almura.feature.complex.item.handbook.SkillRule;
import com.almuradev.almura.feature.complex.item.handbook.network.ClientboundSkillsHandbookPacket;
import com.almuradev.almura.shared.client.ui.BottomRow;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * What each skill gates by level and rewards with experience, a tab per skill. Each row shows the block or item, what's done with it, the
 * level needed (green when the player has it) and the experience given. Clicking a column's heading sorts by it, again reverses it.
 */
@SideOnly(Side.CLIENT)
public final class SkillsHandbookScreen extends BasicScreen {

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,##0.##");
    private static final int FORM_WIDTH = 420;
    private static final int FORM_HEIGHT = 300;
    private static final int TAB_HEIGHT = 15;
    private static final int TAB_GAP = 2;
    private static final int ACTIVE_TAB_COLOR = 0xFFFF55;
    // Column headings, as the Exchange has them: a strip above the list
    private static final int HEADER_HEIGHT = 12;
    private static final int HEADER_TEXT_OFFSET = 2;
    // The name starts after the icon: list border and padding, the row's border, the icon and its gap
    private static final int HEADER_NAME_X = 24;
    // The columns on the right, right aligned in the rows at these insets; headings line up past the list's border, padding and scroll bar
    // (each far enough from the next that a sorted heading, with its arrow, doesn't run into the one beside it)
    private static final int MONEY_INSET = 4;
    private static final int XP_INSET = 54;
    private static final int LEVEL_INSET = 96;
    private static final int ACTION_INSET = 142;
    private static final int HEADER_EXTRA_INSET = 10;
    // The search box, and what a name stops short of: the widest action ("Use on"), a gap, and the list's scroll bar
    private static final int SEARCH_WIDTH = 120;
    private static final int ACTION_COLUMN_WIDTH = 40;
    private static final int NAME_GAP = 6;
    private static final int SCROLLBAR_WIDTH = 6;

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

    private final ClientboundSkillsHandbookPacket message;
    /** Each skill's rows, in the order of the tabs */
    private final Map<String, List<Entry>> entries = new LinkedHashMap<>();
    private final Map<String, UIButton> tabs = new LinkedHashMap<>();
    private final Map<Column, UILabel> headers = new EnumMap<>(Column.class);
    private String skill;
    private Column sortColumn = Column.LEVEL;
    private boolean ascending = true;

    private BasicList<Entry> list;
    private UILabel levelLabel;
    private UILabel emptyLabel;
    private BasicTextBox searchBox;

    private boolean unlockMouse = true;
    private int lastUpdate = 0;

    public SkillsHandbookScreen(final ClientboundSkillsHandbookPacket message) {
        this.message = message;
        for (final String skill : message.levels.keySet()) {
            this.entries.put(skill, new ArrayList<>());
        }
        // One row per thing shown: Skills lists some blocks once per state (a cocoa pod per facing) or twice (ripe nether wart gives
        // experience, any nether wart is only level gated), which look the same here, so the one giving the most experience is kept
        final Map<String, Entry> shown = new LinkedHashMap<>();
        for (final SkillRule rule : message.rules) {
            final Entry entry = new Entry(rule, message.levels.getOrDefault(rule.skill, -1));
            final String key = rule.skill + '|' + rule.action + '|' + entry.name + '|' + rule.level;
            final Entry existing = shown.get(key);
            if (existing == null || rule.xp > existing.rule.xp) {
                shown.put(key, entry);
            }
        }
        for (final Entry entry : shown.values()) {
            this.entries.computeIfAbsent(entry.rule.skill, k -> new ArrayList<>()).add(entry);
        }
        this.skill = this.entries.keySet().stream().findFirst().orElse("");
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        final BasicForm form = new BasicForm(FORM_WIDTH, FORM_HEIGHT, "Skills Handbook");
        final int paddedWidth = BasicScreen.getPaddedWidth(form);

        // A tab per skill
        final int skills = Math.max(1, this.entries.size());
        final int tabWidth = (paddedWidth - TAB_GAP * (skills - 1)) / skills;
        // (the last tab takes what the division leaves over, so it ends at the right edge with the search box below it)
        int tabX = 0;
        int tabIndex = 0;
        for (final String skill : this.entries.keySet()) {
            final UIButton tab = new UIButtonBuilder()
                    .width(tabIndex == skills - 1 ? paddedWidth - tabX : tabWidth)
                    .height(TAB_HEIGHT)
                    .position(tabX, 0)
                    .text(skillName(skill))
                    .onClick(() -> this.showSkill(skill))
                    .build("button.skill." + skill);
            this.tabs.put(skill, tab);
            form.add(tab);
            tabX += tabWidth + TAB_GAP;
            tabIndex++;
        }

        // A row under the tabs: the player's level in the shown skill, and a search box that filters the list as it's typed in
        final int rowY = TAB_HEIGHT + 4;
        this.searchBox = new BasicTextBox("");
        this.searchBox.setAcceptsReturn(false);
        this.searchBox.setSize(SEARCH_WIDTH, 0);
        this.searchBox.setPosition(0, rowY, Anchor.RIGHT | Anchor.TOP);
        this.searchBox.register(this);
        final UILabel searchLabel = new UILabel("Search:");
        searchLabel.setFontOptions(FontColors.WHITE_FO);
        searchLabel.setPosition(-(SEARCH_WIDTH + 4), rowY + (this.searchBox.getHeight() - searchLabel.getHeight()) / 2, Anchor.RIGHT | Anchor.TOP);

        this.levelLabel = new UILabel("Your level");
        this.levelLabel.setFontOptions(FontColors.WHITE_FO);
        this.levelLabel.setPosition(2, rowY + (this.searchBox.getHeight() - this.levelLabel.getHeight()) / 2);
        form.add(this.searchBox, searchLabel);

        // Column headings: click to sort by the column, ascending, click again for descending
        final int headerTop = rowY + this.searchBox.getHeight() + 3;
        final int headerY = headerTop + HEADER_TEXT_OFFSET;
        this.addHeader(form, Column.NAME, HEADER_NAME_X, headerY, Anchor.LEFT | Anchor.TOP);
        this.addHeader(form, Column.ACTION, -(ACTION_INSET + HEADER_EXTRA_INSET), headerY, Anchor.RIGHT | Anchor.TOP);
        this.addHeader(form, Column.LEVEL, -(LEVEL_INSET + HEADER_EXTRA_INSET), headerY, Anchor.RIGHT | Anchor.TOP);
        this.addHeader(form, Column.XP, -(XP_INSET + HEADER_EXTRA_INSET), headerY, Anchor.RIGHT | Anchor.TOP);
        this.addHeader(form, Column.MONEY, -(MONEY_INSET + HEADER_EXTRA_INSET), headerY, Anchor.RIGHT | Anchor.TOP);

        final UIButton closeButton = new UIButtonBuilder()
                .text("Close")
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .onClick(this::close)
                .build("button.close");

        final UILabel hintLabel = new UILabel(TextFormatting.GRAY + "Levels you have are green, ones you need are red.");
        hintLabel.setFontOptions(FontColors.WHITE_FO);
        hintLabel.setPosition(2, -(closeButton.getHeight() - hintLabel.getHeight()) / 2, Anchor.LEFT | Anchor.BOTTOM);

        // The list runs down to the same gap above the close button as below it
        final int listY = headerTop + HEADER_HEIGHT;
        this.list = new BasicList<>(paddedWidth, BasicScreen.getPaddedHeight(form) - listY - BottomRow.above(form, closeButton.getHeight()));
        this.list.setBorder(FontColors.WHITE, 1, 185);
        this.list.setPadding(2, 2);
        this.list.setItemComponentSpacing(1);
        this.list.setItemComponentFactory(EntryComponent::new);
        this.list.setPosition(0, listY);

        this.emptyLabel = new UILabel(TextFormatting.GRAY + "Nothing in this skill needs a level or gives experience.");
        this.emptyLabel.setFontOptions(FontColors.WHITE_FO);
        this.emptyLabel.setPosition(0, listY + 8, Anchor.TOP | Anchor.CENTER);

        // Names are shortened to end before the Action column (the tooltip has them in full): the row, less the icon and the right hand columns
        final int rowWidth = paddedWidth - Padding.of(this.list).horizontal() - this.list.getLeftBorderSize() - this.list.getRightBorderSize()
                - SCROLLBAR_WIDTH;
        final int nameWidth = rowWidth - HEADER_NAME_X - ACTION_INSET - ACTION_COLUMN_WIDTH - NAME_GAP;
        for (final List<Entry> skillEntries : this.entries.values()) {
            for (final Entry entry : skillEntries) {
                entry.displayName = shorten(entry.name, nameWidth);
            }
        }

        form.add(this.levelLabel, this.list, this.emptyLabel, hintLabel, closeButton);
        this.addToScreen(form);
        this.searchBox.focus();

        this.showSkill(this.skill);
        Mouse.setGrabbed(false);
    }

    @Override
    public void update() {
        super.update();
        if (this.unlockMouse && this.lastUpdate++ == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.
            this.unlockMouse = false;
        }
    }

    private void addHeader(final BasicForm form, final Column column, final int x, final int y, final int anchor) {
        final UILabel header = new UILabel(column.title);
        header.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(0.9F).build());
        header.setPosition(x, y, anchor);
        this.headers.put(column, header);
        form.add(header);
    }

    private void showSkill(final String skill) {
        this.skill = skill;
        for (final Map.Entry<String, UIButton> tab : this.tabs.entrySet()) {
            final boolean active = tab.getKey().equals(skill);
            tab.getValue().setText((active ? TextFormatting.YELLOW : TextFormatting.WHITE) + skillName(tab.getKey()));
        }
        final int level = this.message.levels.getOrDefault(skill, -1);
        this.levelLabel.setText(TextFormatting.WHITE + "Your " + skillName(skill) + " level: "
                + (level < 0 ? TextFormatting.GRAY + "unknown" : TextFormatting.YELLOW + String.valueOf(level)));
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
        final Comparator<Entry> byName = Comparator.comparing((Entry e) -> e.name, String.CASE_INSENSITIVE_ORDER);
        Comparator<Entry> order;
        switch (this.sortColumn) {
            case ACTION:
                order = Comparator.comparing((Entry e) -> e.rule.action.label);
                break;
            case LEVEL:
                order = Comparator.comparingInt((Entry e) -> Math.max(0, e.rule.level));
                break;
            case XP:
                order = Comparator.comparingDouble((Entry e) -> Math.max(0, e.rule.xp));
                break;
            case MONEY:
                order = Comparator.comparingDouble((Entry e) -> Math.max(0, e.rule.economy));
                break;
            default:
                order = byName;
        }
        if (!this.ascending) {
            order = order.reversed();
        }
        if (this.sortColumn != Column.NAME) {
            order = order.thenComparing(byName); // Rows that tie stay alphabetical
        }
        final String search = this.searchBox == null ? "" : this.searchBox.getText().trim().toLowerCase(Locale.ENGLISH);
        final List<Entry> rows = new ArrayList<>();
        for (final Entry entry : this.entries.getOrDefault(this.skill, new ArrayList<>())) {
            if (search.isEmpty() || entry.name.toLowerCase(Locale.ENGLISH).contains(search)) {
                rows.add(entry);
            }
        }
        rows.sort(order);
        this.list.setItems(rows);
        this.emptyLabel.setText(TextFormatting.GRAY + (search.isEmpty() ? "Nothing in this skill needs a level or gives experience." : "Nothing matches your search."));
        this.emptyLabel.setVisible(rows.isEmpty());

        // Mark the sorted column and its direction
        for (final Map.Entry<Column, UILabel> header : this.headers.entrySet()) {
            final boolean active = header.getKey() == this.sortColumn;
            header.getValue().setText(active ? TextFormatting.WHITE + header.getKey().title + (this.ascending ? " ▲" : " ▼")
                    : header.getKey().title);
        }
    }

    @Override
    protected void mousePressed(final UIComponent component, final int button) {
        final int x = MalisisGui.MOUSE_POSITION.x();
        final int y = MalisisGui.MOUSE_POSITION.y();
        if (button == 0) {
            for (final Map.Entry<Column, UILabel> header : this.headers.entrySet()) {
                if (header.getValue().isInsideBounds(x, y)) {
                    this.sortBy(header.getKey());
                    return;
                }
            }
        }
        super.mousePressed(component, button);
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        if (event.getComponent() == this.searchBox) {
            // The new text isn't in the box until this event is through
            Minecraft.getMinecraft().addScheduledTask(this::sort);
        }
    }

    /**
     * @return the text, cut short with "..." if it's wider than the given width
     */
    private static String shorten(final String text, final int width) {
        if (textWidth(text) <= width) {
            return text;
        }
        // The longest start that fits with the dots
        int low = 0, high = text.length();
        while (low < high) {
            final int mid = (low + high + 1) / 2;
            if (textWidth(text.substring(0, mid).trim() + "...") <= width) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return text.substring(0, low).trim() + "...";
    }

    private static int textWidth(final String text) {
        final UILabel label = new UILabel(text);
        label.setFontOptions(FontColors.WHITE_FO);
        return label.getWidth();
    }

    private static String skillName(final String skill) {
        return skill.isEmpty() ? skill : Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
    }

    private static String number(final double value) {
        return NUMBER_FORMAT.format(value);
    }

    /**
     * A rule as it's shown: the block or item resolved to something with an icon and a name.
     */
    static final class Entry {

        final SkillRule rule;
        final ItemStack icon;
        final String name;
        /** The name as the row shows it, shortened to fit */
        String displayName;
        final String levelText;
        final String xpText;
        final String moneyText;
        final String tooltip;

        Entry(final SkillRule rule, final int playerLevel) {
            this.rule = rule;
            ItemStack icon = ItemStack.EMPTY;
            String name = rule.id;
            if (rule.block) {
                final Block block = Block.getBlockFromName(rule.id);
                if (block != null) {
                    icon = iconFor(block, rule.meta);
                    // A block's own name, unless its icon is the block itself (a crop's icon is its seed, but it's the crop that's broken)
                    name = !icon.isEmpty() && icon.getItem() instanceof ItemBlock && ((ItemBlock) icon.getItem()).getBlock() == block
                            ? icon.getDisplayName() : block.getLocalizedName();
                }
            } else {
                final Item item = Item.getByNameOrId(rule.id);
                if (item != null) {
                    icon = new ItemStack(item, 1, rule.meta);
                    name = icon.getDisplayName();
                }
            }
            this.icon = icon;
            this.name = name;
            this.displayName = name;

            if (rule.level < 0) {
                this.levelText = TextFormatting.GRAY + "-";
            } else {
                final TextFormatting color = playerLevel < 0 ? TextFormatting.GRAY : playerLevel >= rule.level ? TextFormatting.GREEN : TextFormatting.RED;
                this.levelText = color + String.valueOf(rule.level);
            }
            this.xpText = rule.xp < 0 ? TextFormatting.GRAY + "-" : TextFormatting.AQUA + number(rule.xp);
            this.moneyText = rule.economy <= 0 ? TextFormatting.GRAY + "-" : TextFormatting.GOLD + "$" + number(rule.economy);

            final List<String> lines = new ArrayList<>();
            lines.add(TextFormatting.WHITE + name);
            lines.add(TextFormatting.GRAY + skillName(rule.skill) + ": " + TextFormatting.WHITE + rule.action.label);
            lines.add(TextFormatting.GRAY + "Level needed: " + (rule.level < 0 ? TextFormatting.GRAY + "none" : this.levelText));
            lines.add(TextFormatting.GRAY + "Experience: " + this.xpText);
            lines.add(TextFormatting.GRAY + "Money: " + this.moneyText);
            this.tooltip = String.join("\n", lines);
        }

        /**
         * @return what the block looks like in the inventory: what picking it gives (a crop's seed), else its own item
         */
        private static ItemStack iconFor(final Block block, final int meta) {
            final IBlockState state = block.getStateFromMeta(meta);
            final Minecraft client = Minecraft.getMinecraft();
            try {
                final ItemStack picked = block.getPickBlock(state, null, client.world, BlockPos.ORIGIN, client.player);
                if (!picked.isEmpty()) {
                    return picked;
                }
            } catch (final RuntimeException ignored) {
                // Some blocks need the world around them to work out what picking gives
            }
            final Item item = Item.getItemFromBlock(block);
            return item == null ? ItemStack.EMPTY : new ItemStack(item, 1, block.damageDropped(state));
        }
    }

    private static final class EntryComponent extends BasicList.ItemComponent<Entry> {

        EntryComponent(final MalisisGui gui, final BasicList<Entry> parent, final Entry item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            this.setSize(0, 20);

            final UISaneTooltip tooltip = new UISaneTooltip(this.item.tooltip);

            @Nullable UIImage image = null;
            if (!this.item.icon.isEmpty()) {
                image = new UIImage(this.item.icon);
                image.setPosition(1, 0, Anchor.LEFT | Anchor.MIDDLE);
                image.setTooltip(tooltip);
                this.add(image);
            }

            final UILabel nameLabel = new UILabel(this.item.displayName);
            nameLabel.setFontOptions(FontColors.WHITE_FO);
            nameLabel.setPosition(image == null ? 21 : BasicScreen.getPaddedX(image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);
            nameLabel.setTooltip(tooltip);

            final UILabel actionLabel = new UILabel(TextFormatting.GRAY + this.item.rule.action.label);
            actionLabel.setFontOptions(FontColors.WHITE_FO);
            actionLabel.setPosition(-ACTION_INSET, 0, Anchor.RIGHT | Anchor.MIDDLE);
            actionLabel.setTooltip(tooltip);

            final UILabel levelLabel = new UILabel(this.item.levelText);
            levelLabel.setFontOptions(FontColors.WHITE_FO);
            levelLabel.setPosition(-LEVEL_INSET, 0, Anchor.RIGHT | Anchor.MIDDLE);
            levelLabel.setTooltip(tooltip);

            final UILabel xpLabel = new UILabel(this.item.xpText);
            xpLabel.setFontOptions(FontColors.WHITE_FO);
            xpLabel.setPosition(-XP_INSET, 0, Anchor.RIGHT | Anchor.MIDDLE);
            xpLabel.setTooltip(tooltip);

            final UILabel moneyLabel = new UILabel(this.item.moneyText);
            moneyLabel.setFontOptions(FontColors.WHITE_FO);
            moneyLabel.setPosition(-MONEY_INSET, 0, Anchor.RIGHT | Anchor.MIDDLE);
            moneyLabel.setTooltip(tooltip);

            this.setTooltip(tooltip);
            this.add(nameLabel, actionLabel, levelLabel, xpLabel, moneyLabel);
        }
    }
}
