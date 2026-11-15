/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.DARK_GRAY;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.DARK_GREEN;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.GRAY;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.GREEN;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.RED;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.TWO;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.WHITE;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.YELLOW;

import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.registry.ContentCrop;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops.CropLevels;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
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
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Farmer's Almanac's list of every crop, as 3.5's: Almura's crops and vanilla's, by name, each with its seed; hovering one shows the
 * light and ground temperature it needs (green where the player stands is warm enough, red where not) and whether a harvesting sack picks
 * it. The ground temperature where the player stands is at the bottom, with Close.
 * <p>
 * When the server runs the Skills plugin, as on 3.5, each crop also shows the farming level to plant and to harvest it (green when the player
 * has it, red when not), and the list sorts by the Crop or Farming Lv heading's click. 3.5 also opened a crop's growth stages, drawn as 3D
 * models, on a double click (not ported yet).
 */
final class AlmanacCropsScreen implements Screen {

    // The Farming Lv column, right aligned in the rows; its heading lines up past the list's border, padding and scroll bar
    private static final float HEADER_HEIGHT = 12f, HEADER_NAME_X = 24f, LEVEL_INSET = 4f, HEADER_EXTRA_INSET = 10f;
    private static final float WIDTH = 220f, HEIGHT = 250f, ROW_HEIGHT = 20f, ICON = 16f, CLOSE_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    private static final DecimalFormat LIGHT = new DecimalFormat("0.##");

    // Vanilla's crops, as 3.5 listed them
    private static final List<Entry> VANILLA = List.of(
        vanilla("minecraft:wheat", "Wheat Crop", Items.WHEAT_SEEDS, true, null),
        vanilla("minecraft:carrots", "Carrot Crop", Items.CARROT, true, null),
        vanilla("minecraft:potatoes", "Potato Crop", Items.POTATO, true, null),
        vanilla("minecraft:beetroots", "Beetroot Crop", Items.BEETROOT_SEEDS, true, null),
        vanilla("minecraft:melon_stem", "Melon", Items.MELON_SEEDS, true, "Fruit grows on a free block next to the stem"),
        vanilla("minecraft:pumpkin_stem", "Pumpkin", Items.PUMPKIN_SEEDS, true, "Fruit grows on a free block next to the stem"),
        vanilla("minecraft:nether_wart", "Nether Wart", Items.NETHER_WART, false, "Planted on soul sand"),
        vanilla("minecraft:cocoa", "Cocoa", Items.COCOA_BEANS, false, "Planted on jungle logs"),
        vanilla("minecraft:sugar_cane", "Sugar Cane", Items.SUGAR_CANE, false, "Needs water next to the block it's planted on"));

    private final ClientboundAlmanacCrops crops;
    // Each crop's farming levels by its planted block's id, when the server runs Skills
    private final Map<String, CropLevels> levels = new HashMap<>();
    private @Nullable ListView<Entry> list;
    private @Nullable Label nameHeader, levelHeader;
    private List<Entry> entries = List.of();
    private boolean byLevel;
    private boolean ascending = true;

    AlmanacCropsScreen(final ClientboundAlmanacCrops crops) {
        this.crops = crops;
        for (final CropLevels levels : crops.levels()) {
            this.levels.put(levels.id(), levels);
        }
    }

    @Override
    public Text title() {
        return Text.of("Farmer's Almanac");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final List<Entry> entries = new ArrayList<>(VANILLA);
        for (final Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof final ContentCrop crop) {
                entries.add(this.almura(crop));
            }
        }
        this.entries = entries;

        final ListView<Entry> list = factory.<Entry>listView(entry -> this.row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        this.list = list;

        final Component<?> count = factory.label(Text.of("Crops: ").color(WHITE).append(Text.of(Integer.toString(entries.size())).color(GRAY)));
        final Component<?> ground = factory.label(Text.of("Ground Temperature: ").color(WHITE)
            .append(Text.of(TWO.format(this.crops.temperature())).color(DARK_GREEN)));
        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        // With Skills, as 3.5: column headings over the list, a click sorting by the column, again for the other way
        final float listTop = this.crops.skills() ? 14f + HEADER_HEIGHT : 14f;
        final AnchorPanel page = factory.anchorPanel()
            .add(count.setAnchor(Anchor.TOP_LEFT));
        if (this.crops.skills()) {
            this.nameHeader = factory.label("").setTextScale(0.9f);
            this.levelHeader = factory.label("").setTextScale(0.9f);
            page.add(clickable(factory, this.nameHeader, () -> this.sortBy(false)).setAnchor(Anchor.TOP_LEFT).setMargin(HEADER_NAME_X, 16f, 0f, 0f))
                .add(clickable(factory, this.levelHeader, () -> this.sortBy(true)).setAnchor(Anchor.TOP_RIGHT)
                    .setMargin(0f, 16f, LEVEL_INSET + HEADER_EXTRA_INSET, 0f));
        }
        // In 3.5's lighter box with a white border
        page.add(factory.anchorPanel().setBackground(AlmanacFieldScreen.BOX).setBorder(AlmanacFieldScreen.BOX_BORDER).setPadding(2f)
                .add(list.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(0f, listTop, 0f, BUTTON_HEIGHT + 6f))
            .add(ground.setAnchor(Anchor.BOTTOM_LEFT).setMargin(0f, 0f, 0f, 4f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));

        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(WIDTH, HEIGHT)
            .setAnchor(Anchor.CENTER);
        // Its x closes the almanac, as Close does
        InGameScreens.closesScreen(window);
        this.sort();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private static AnchorPanel clickable(final ComponentFactory factory, final Label label, final Runnable click) {
        label.setHitTestVisible(false);
        final AnchorPanel panel = factory.anchorPanel().add(label.setAnchor(Anchor.TOP_LEFT));
        panel.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT) {
                click.run();
            }
        });
        return panel;
    }

    private void sortBy(final boolean byLevel) {
        if (this.byLevel == byLevel) {
            this.ascending = !this.ascending;
        } else {
            this.byLevel = byLevel;
            this.ascending = true;
        }
        this.sort();
    }

    private void sort() {
        if (this.list == null) {
            return;
        }
        final Comparator<Entry> byName = Comparator.comparing(entry -> entry.name.toLowerCase(Locale.ROOT));
        Comparator<Entry> order = this.byLevel ? Comparator.comparingInt(this::sortLevel) : byName;
        if (!this.ascending) {
            order = order.reversed();
        }
        if (this.byLevel) {
            // Crops that tie stay alphabetical
            order = order.thenComparing(byName);
        }
        final List<Entry> sorted = new ArrayList<>(this.entries);
        sorted.sort(order);
        this.list.setItems(sorted);
        this.list.refresh();
        if (this.nameHeader != null && this.levelHeader != null) {
            this.nameHeader.setText(header("Crop", !this.byLevel));
            this.levelHeader.setText(header("Farming Lv", this.byLevel));
        }
    }

    private Text header(final String title, final boolean active) {
        return active ? Text.of(title + (this.ascending ? " \u25B2" : " \u25BC")).color(WHITE) : Text.of(title).color(GRAY);
    }

    // The higher of the plant and harvest levels, 0 when neither is needed
    private int sortLevel(final Entry entry) {
        final CropLevels levels = this.levels.get(entry.id);
        return levels == null ? 0 : Math.max(0, Math.max(levels.plant(), levels.harvest()));
    }

    /**
     * @return a level, green when the player has it, red when not, gray when their level isn't known; "-" when none is needed
     */
    private Text level(final int level) {
        if (level < 0) {
            return Text.of("-").color(GRAY);
        }
        final int farming = this.crops.farmingLevel();
        return Text.of(String.valueOf(level)).color(farming < 0 ? GRAY : farming >= level ? GREEN : RED);
    }

    private Component<?> row(final ComponentFactory factory, final Entry entry) {
        final StackPanel line = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(6f);
        if (!entry.seed.isEmpty()) {
            line.add(factory.image(MinecraftVisuals.item(entry.seed)).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        }
        line.add(factory.label(Text.of(entry.name).color(WHITE)).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        // Hovering anywhere on the row shows its tooltip
        line.setHitTestVisible(false);
        final AnchorPanel row = factory.anchorPanel().add(line.setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f));
        Text tooltip = entry.tooltip;
        if (this.crops.skills()) {
            final CropLevels levels = this.levels.get(entry.id);
            final int plant = levels == null ? -1 : levels.plant();
            final int harvest = levels == null ? -1 : levels.harvest();
            final Text column = plant == harvest || plant < 0 || harvest < 0 ? this.level(Math.max(plant, harvest))
                : this.level(plant).append(Text.of(" / ").color(GRAY)).append(this.level(harvest));
            final Label levelLabel = factory.label(column);
            levelLabel.setHitTestVisible(false);
            row.add(levelLabel.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, LEVEL_INSET, 0f));
            tooltip = Text.concat(tooltip, Text.of("\nFarming level to plant: ").color(GRAY), this.level(plant),
                Text.of("\nFarming level to harvest: ").color(GRAY), this.level(harvest));
        }
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH).setTooltip(tooltip);
    }

    /**
     * An Almura crop: its light and temperature from the first stage that names them.
     */
    private Entry almura(final ContentCrop crop) {
        Range light = null, temperature = null;
        for (final CropStageDef stage : crop.def().stages()) {
            if (stage.light() != null || stage.temperature() != null) {
                light = stage.light();
                temperature = stage.temperature();
                break;
            }
        }
        final String name = crop.getName().getString();
        final Item seed = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(crop.def().seed())).orElse(Items.AIR);
        final ItemStack icon = seed != Items.AIR ? new ItemStack(seed) : new ItemStack(crop.asItem());

        final double ground = Math.round(this.crops.temperature() * 100f) / 100.0;
        final Text tooltip = Text.concat(
            Text.of(name).color(WHITE),
            Text.of("\nLight required: ").color(GRAY),
            light == null ? Text.of("Any").color(GRAY) : Text.of(LIGHT.format(light.min()) + "-" + LIGHT.format(light.max())).color(YELLOW),
            Text.of("\nGround temperature required: ").color(GRAY),
            temperature == null ? Text.of("Any").color(GRAY)
                : Text.of(TWO.format(temperature.min()) + "-" + TWO.format(temperature.max())).color(temperature.contains(ground) ? GREEN : RED),
            Text.of("\nHarvesting sack: ").color(GRAY),
            crop.def().sackResetAge() >= 0 ? Text.of("Yes").color(GREEN) : Text.of("No").color(RED));
        return new Entry(BuiltInRegistries.BLOCK.getKey(crop).toString(), name, icon, tooltip);
    }

    private static Entry vanilla(final String id, final String name, final Item seed, final boolean needsLight, final @Nullable String note) {
        final List<Text> parts = new ArrayList<>(List.of(
            Text.of(name).color(WHITE), Text.of(" (Minecraft)").color(DARK_GRAY),
            Text.of("\nLight required: ").color(GRAY), needsLight ? Text.of("9-15").color(YELLOW) : Text.of("Any").color(GRAY),
            Text.of("\nGround temperature required: ").color(GRAY), Text.of("Any").color(GRAY),
            Text.of("\nHarvesting sack: ").color(GRAY), Text.of("No").color(RED)));
        if (note != null) {
            parts.add(Text.of("\n" + note).color(YELLOW));
        }
        return new Entry(id, name, new ItemStack(seed), Text.concat(parts));
    }

    // A crop: its planted block's id (its levels are known by), name, seed and tooltip
    private record Entry(String id, String name, ItemStack seed, Text tooltip) {
    }
}
