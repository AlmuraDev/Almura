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
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.window.Window;
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
import java.util.List;
import java.util.Locale;

/**
 * The Farmer's Almanac's list of every crop, as 3.5's: Almura's crops and vanilla's, by name, each with its seed; hovering one shows the
 * light and ground temperature it needs (green where the player stands is warm enough, red where not) and whether a harvesting sack picks
 * it. The ground temperature where the player stands is at the bottom, with Close.
 * <p>
 * 3.5 also showed the Skills plugin's farming levels (Skills has no 26.3 version yet) and opened a crop's growth stages, drawn as 3D models,
 * on a double click (not ported yet).
 */
final class AlmanacCropsScreen implements Screen {

    private static final float WIDTH = 220f, HEIGHT = 250f, ROW_HEIGHT = 20f, ICON = 16f, CLOSE_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    private static final DecimalFormat LIGHT = new DecimalFormat("0.##");

    // Vanilla's crops, as 3.5 listed them
    private static final List<Entry> VANILLA = List.of(
        vanilla("Wheat Crop", Items.WHEAT_SEEDS, true, null),
        vanilla("Carrot Crop", Items.CARROT, true, null),
        vanilla("Potato Crop", Items.POTATO, true, null),
        vanilla("Beetroot Crop", Items.BEETROOT_SEEDS, true, null),
        vanilla("Melon", Items.MELON_SEEDS, true, "Fruit grows on a free block next to the stem"),
        vanilla("Pumpkin", Items.PUMPKIN_SEEDS, true, "Fruit grows on a free block next to the stem"),
        vanilla("Nether Wart", Items.NETHER_WART, false, "Planted on soul sand"),
        vanilla("Cocoa", Items.COCOA_BEANS, false, "Planted on jungle logs"),
        vanilla("Sugar Cane", Items.SUGAR_CANE, false, "Needs water next to the block it's planted on"));

    private final ClientboundAlmanacCrops crops;

    AlmanacCropsScreen(final ClientboundAlmanacCrops crops) {
        this.crops = crops;
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
        entries.sort(Comparator.comparing(entry -> entry.name.toLowerCase(Locale.ROOT)));

        final ListView<Entry> list = factory.<Entry>listView(entry -> row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        list.setItems(entries);

        final Component<?> count = factory.label(Text.of("Crops: ").color(WHITE).append(Text.of(Integer.toString(entries.size())).color(GRAY)));
        final Component<?> ground = factory.label(Text.of("Ground Temperature: ").color(WHITE)
            .append(Text.of(TWO.format(this.crops.temperature())).color(DARK_GREEN)));
        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        final AnchorPanel page = factory.anchorPanel()
            .add(count.setAnchor(Anchor.TOP_LEFT))
            // In 3.5's lighter box with a white border
            .add(factory.anchorPanel().setBackground(AlmanacFieldScreen.BOX).setBorder(AlmanacFieldScreen.BOX_BORDER).setPadding(2f)
                .add(list.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(0f, 14f, 0f, BUTTON_HEIGHT + 6f))
            .add(ground.setAnchor(Anchor.BOTTOM_LEFT).setMargin(0f, 0f, 0f, 4f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));

        final Window window = factory.window(this.title())
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(WIDTH, HEIGHT)
            .setAnchor(Anchor.CENTER);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private static Component<?> row(final ComponentFactory factory, final Entry entry) {
        final StackPanel line = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(6f);
        if (!entry.seed.isEmpty()) {
            line.add(factory.image(MinecraftVisuals.item(entry.seed)).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        }
        line.add(factory.label(Text.of(entry.name).color(WHITE)).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        // Hovering anywhere on the row shows its tooltip
        line.setHitTestVisible(false);
        return factory.anchorPanel().add(line.setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f)).setAnchor(Anchor.FILL)
            .setTooltip(entry.tooltip);
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
        final Item seed = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.Identifier.tryParse(crop.def().seed())).orElse(Items.AIR);
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
        return new Entry(name, icon, tooltip);
    }

    private static Entry vanilla(final String name, final Item seed, final boolean needsLight, final @Nullable String note) {
        final List<Text> parts = new ArrayList<>(List.of(
            Text.of(name).color(WHITE), Text.of(" (Minecraft)").color(DARK_GRAY),
            Text.of("\nLight required: ").color(GRAY), needsLight ? Text.of("9-15").color(YELLOW) : Text.of("Any").color(GRAY),
            Text.of("\nGround temperature required: ").color(GRAY), Text.of("Any").color(GRAY),
            Text.of("\nHarvesting sack: ").color(GRAY), Text.of("No").color(RED)));
        if (note != null) {
            parts.add(Text.of("\n" + note).color(YELLOW));
        }
        return new Entry(name, new ItemStack(seed), Text.concat(parts));
    }

    private record Entry(String name, ItemStack seed, Text tooltip) {
    }
}
