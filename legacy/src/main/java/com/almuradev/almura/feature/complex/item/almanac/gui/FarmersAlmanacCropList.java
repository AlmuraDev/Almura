/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.gui;

import com.almuradev.almura.shared.client.ui.BottomRow;
import com.almuradev.almura.feature.complex.item.almanac.VanillaCrops;
import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundCropListOpenPacket;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.util.MathUtil;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.block.type.crop.processor.growth.Growth;
import com.almuradev.content.type.block.type.crop.state.CropBlockStateDefinition;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Every crop on the server, with its seed. Hovering a crop shows what it needs to grow, double clicking it shows the crop at each stage of
 * its growth. When the server runs the Skills plugin, each crop also shows the farming level needed to plant and harvest it. Clicking a
 * column's heading sorts by it; clicking it again reverses the order.
 */
@SideOnly(Side.CLIENT)
public final class FarmersAlmanacCropList extends BasicScreen {

    private static final NumberFormat TEMPERATURE_FORMAT = new DecimalFormat("0.00");
    private static final NumberFormat LIGHT_FORMAT = new DecimalFormat("0.##");
    // Column headings, as the Exchange has them: the heading text sits in a strip above the list
    private static final int HEADER_HEIGHT = 12;
    private static final int HEADER_TEXT_OFFSET = 2;
    // The crop name starts after the seed: list border and padding, the row's border, the seed and its gap
    private static final int HEADER_NAME_X = 24;
    // Levels are right aligned in the rows; the heading lines up with them past the list's border, padding and scroll bar
    private static final int ROW_LEVEL_INSET = 4;
    private static final int HEADER_LEVEL_INSET = ROW_LEVEL_INSET + 10;

    private enum Column {
        NAME,
        LEVEL
    }

    private final ClientboundCropListOpenPacket message;
    private final List<CropEntry> crops = new ArrayList<>();
    private BasicList<CropEntry> cropList;
    private UILabel nameHeader;
    @Nullable private UILabel levelHeader;
    private Column sortColumn = Column.NAME;
    private boolean ascending = true;

    private boolean unlockMouse = true;
    private int lastUpdate = 0;

    public FarmersAlmanacCropList(final ClientboundCropListOpenPacket message) {
        this.message = message;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.crops.clear();
        this.crops.addAll(this.collectCrops());

        final BasicForm form = new BasicForm(220, 250, "Farmer's Almanac");

        final UILabel countLabel = new UILabel(TextFormatting.WHITE + "Crops: " + TextFormatting.GRAY + this.crops.size()
                + TextFormatting.DARK_GRAY + "  (double click for stages)");
        countLabel.setPosition(2, 0, Anchor.TOP | Anchor.LEFT);

        // Column headings: click to sort by the column, ascending, click again for descending
        final int headerTop = BasicScreen.getPaddedY(countLabel, 3);
        final int headerY = headerTop + HEADER_TEXT_OFFSET;
        this.nameHeader = createHeader("Crop");
        this.nameHeader.setPosition(HEADER_NAME_X, headerY, Anchor.LEFT | Anchor.TOP);
        form.add(this.nameHeader);

        if (this.message.skills) {
            this.levelHeader = createHeader("Farming Lv");
            this.levelHeader.setPosition(-HEADER_LEVEL_INSET, headerY, Anchor.RIGHT | Anchor.TOP);
            form.add(this.levelHeader);
        }

        final int listY = headerTop + HEADER_HEIGHT;
        this.cropList = new BasicList<>(BasicScreen.getPaddedWidth(form), 100);
        this.cropList.setBorder(FontColors.WHITE, 1, 185);
        this.cropList.setPadding(2, 2);
        this.cropList.setItemComponentSpacing(1);
        this.cropList.setItemComponentFactory(CropComponent::new);
        this.cropList.setPosition(0, listY);

        // The ground temperature where the player is standing, from the server
        final UILabel temperatureLabel = new UILabel(TextFormatting.WHITE + "Ground Temperature: " + TextFormatting.DARK_GREEN
                + TEMPERATURE_FORMAT.format(this.message.groundTemperature));
        temperatureLabel.setFontOptions(FontColors.WHITE_FO);
        temperatureLabel.setPosition(1, -3, Anchor.BOTTOM | Anchor.LEFT);

        final UIButton closeButton = new UIButtonBuilder()
                .text("Close")
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .onClick(this::close)
                .build("button.close");

        // The list runs down to just above the close button
        this.cropList.setSize(this.cropList.getWidth(), BasicScreen.getPaddedHeight(form) - listY - BottomRow.above(form, closeButton.getHeight()));

        form.add(countLabel, this.cropList, temperatureLabel, closeButton);
        this.addToScreen(form);

        this.sort();

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
        final Comparator<CropEntry> byName = Comparator.comparing((CropEntry entry) -> entry.name, String.CASE_INSENSITIVE_ORDER);
        Comparator<CropEntry> order = this.sortColumn == Column.LEVEL ? Comparator.comparingInt((CropEntry entry) -> entry.sortLevel) : byName;
        if (!this.ascending) {
            order = order.reversed();
        }
        if (this.sortColumn == Column.LEVEL) {
            order = order.thenComparing(byName); // Crops at the same level stay alphabetical
        }
        this.crops.sort(order);
        this.cropList.setItems(new ArrayList<>(this.crops));

        // Mark the sorted column and its direction
        this.setHeader(this.nameHeader, "Crop", Column.NAME);
        if (this.levelHeader != null) {
            this.setHeader(this.levelHeader, "Farming Lv", Column.LEVEL);
        }
    }

    private static UILabel createHeader(final String text) {
        final UILabel header = new UILabel(text);
        header.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(0.9F).build());
        return header;
    }

    private void setHeader(final UILabel header, final String text, final Column column) {
        final boolean active = this.sortColumn == column;
        header.setText(active ? TextFormatting.WHITE + text + (this.ascending ? " ▲" : " ▼") : text);
    }

    @Override
    protected void mousePressed(final UIComponent component, final int button) {
        final int x = MalisisGui.MOUSE_POSITION.x();
        final int y = MalisisGui.MOUSE_POSITION.y();
        if (button == 0 && this.nameHeader != null) {
            if (this.nameHeader.isInsideBounds(x, y)) {
                this.sortBy(Column.NAME);
                return;
            }
            if (this.levelHeader != null && this.levelHeader.isInsideBounds(x, y)) {
                this.sortBy(Column.LEVEL);
                return;
            }
        }
        super.mousePressed(component, button);
    }

    private List<CropEntry> collectCrops() {
        // Requirements can differ per biome, so show them for the biome the player is standing in. Biomes the client doesn't know (from
        // TerrainControl) can't have their own requirements anyway, so any biome gives the crop's usual ones.
        @Nullable Biome biome = this.message.biomeId.isEmpty() ? null : ForgeRegistries.BIOMES.getValue(new ResourceLocation(this.message.biomeId));
        if (biome == null) {
            biome = Biomes.PLAINS;
        }

        final List<CropEntry> crops = new ArrayList<>();
        for (final Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (block instanceof CropBlockImpl) {
                crops.add(new CropEntry((CropBlockImpl) block, biome, this.message));
            }
        }
        // And Minecraft's own crops
        for (final VanillaCrops.VanillaCrop crop : VanillaCrops.ALL) {
            crops.add(new CropEntry(crop, this.message));
        }
        return crops;
    }

    private static String formatRange(@Nullable final DoubleRange range, final NumberFormat format, final TextFormatting color) {
        if (range == null) {
            return TextFormatting.GRAY + "Any";
        }
        return color + format.format(range.min()) + "-" + format.format(range.max());
    }

    static final class CropEntry {

        final String name;
        final ItemStack seed;
        final String tooltip;
        /** The level text shown on the right, or null when the server doesn't run Skills */
        @Nullable final String levelText;
        /** The higher of the plant and harvest levels, 0 when neither is needed */
        final int sortLevel;
        /** The planted block and its age, for the growth stages window; null when it has no stages to show */
        @Nullable final Block stageBlock;
        @Nullable final PropertyInteger stageAge;

        /**
         * One of Minecraft's own crops. They grow by Minecraft's rules: no temperature, and field crops and stems need light 9 or more.
         */
        CropEntry(final VanillaCrops.VanillaCrop crop, final ClientboundCropListOpenPacket message) {
            this.name = crop.name;
            final Item seedItem = Item.getByNameOrId(crop.seedItem);
            this.seed = seedItem == null ? ItemStack.EMPTY : new ItemStack(seedItem, 1, crop.seedMeta);

            final Block plant = Block.getBlockFromName(crop.plantBlock);
            @Nullable PropertyInteger age = null;
            if (plant != null) {
                for (final IProperty<?> property : plant.getBlockState().getProperties()) {
                    if (property instanceof PropertyInteger && "age".equals(property.getName())) {
                        age = (PropertyInteger) property;
                    }
                }
            }
            // Sugar cane's age only counts towards its next block, it doesn't change how it looks
            final boolean showStages = plant != null && age != null && !"minecraft:reeds".equals(crop.id);
            this.stageBlock = showStages ? plant : null;
            this.stageAge = showStages ? age : null;

            final List<String> lines = new ArrayList<>();
            lines.add(TextFormatting.WHITE + this.name + TextFormatting.DARK_GRAY + " (Minecraft)");
            lines.add(TextFormatting.GRAY + "Light required: " + (crop.needsLight ? TextFormatting.YELLOW + "9-15" : TextFormatting.GRAY + "Any"));
            lines.add(TextFormatting.GRAY + "Ground temperature required: " + TextFormatting.GRAY + "Any");
            lines.add(TextFormatting.GRAY + "Harvesting sack: " + TextFormatting.RED + "No");
            if (crop.note != null) {
                lines.add(TextFormatting.YELLOW + crop.note);
            }

            final String[] levels = skillLevels(lines, crop.id, message);
            this.levelText = levels[0];
            this.sortLevel = Integer.parseInt(levels[1]);
            if (showStages) {
                lines.add(TextFormatting.DARK_GRAY + "Double click to see its growth stages");
            }
            this.tooltip = String.join("\n", lines);
        }

        CropEntry(final CropBlockImpl crop, final Biome biome, final ClientboundCropListOpenPacket message) {
            this.name = crop.getLocalizedName();
            this.seed = seedOf(crop);
            this.stageBlock = crop;
            this.stageAge = crop.getAgeProperty();

            @Nullable final Growth growth = growthOf(crop);
            @Nullable final DoubleRange light = growth == null ? null : growth.getOrLoadLightRangeForBiome(biome);
            @Nullable final DoubleRange temperature = growth == null ? null : growth.getOrLoadTemperatureRequiredRangeForBiome(biome);
            // Green when it would grow at the ground temperature here (without a heat lamp), red when it wouldn't
            final double roundedTemperature = Math.round(message.groundTemperature * 100.0) / 100.0;
            final TextFormatting temperatureColor = temperature == null || MathUtil.withinRange(roundedTemperature, temperature.min(), temperature.max())
                    ? TextFormatting.GREEN : TextFormatting.RED;

            final List<String> lines = new ArrayList<>();
            lines.add(TextFormatting.WHITE + this.name);
            lines.add(TextFormatting.GRAY + "Light required: " + formatRange(light, LIGHT_FORMAT, TextFormatting.YELLOW));
            lines.add(TextFormatting.GRAY + "Ground temperature required: " + formatRange(temperature, TEMPERATURE_FORMAT, temperatureColor));
            lines.add(TextFormatting.GRAY + "Harvesting sack: " + (crop.isSackHarvestable() ? TextFormatting.GREEN + "Yes" : TextFormatting.RED + "No"));

            final String[] levels = skillLevels(lines, crop.getRegistryName() == null ? "" : crop.getRegistryName().toString(), message);
            this.levelText = levels[0];
            this.sortLevel = Integer.parseInt(levels[1]);

            lines.add(TextFormatting.DARK_GRAY + "Double click to see its growth stages");
            this.tooltip = String.join("\n", lines);
        }

        /**
         * Adds the farming levels to the tooltip lines, when the server runs Skills.
         *
         * @return the text for the level column (null without Skills) and the level to sort by
         */
        private static String[] skillLevels(final List<String> lines, final String id, final ClientboundCropListOpenPacket message) {
            if (!message.skills) {
                return new String[] {null, "0"};
            }
            final int[] levels = message.levels.get(id);
            final int plant = levels == null ? -1 : levels[0];
            final int harvest = levels == null ? -1 : levels[1];
            lines.add(TextFormatting.GRAY + "Farming level to plant: " + levelText(plant, message.farmingLevel));
            lines.add(TextFormatting.GRAY + "Farming level to harvest: " + levelText(harvest, message.farmingLevel));
            final String text = plant == harvest || plant < 0 || harvest < 0
                    ? levelText(Math.max(plant, harvest), message.farmingLevel)
                    : levelText(plant, message.farmingLevel) + TextFormatting.GRAY + " / " + levelText(harvest, message.farmingLevel);
            return new String[] {text, String.valueOf(Math.max(0, Math.max(plant, harvest)))};
        }

        /**
         * @return the level, green if the player has it, red if not, gray if the player's level isn't known; "-" when none is needed
         */
        private static String levelText(final int level, final int farmingLevel) {
            if (level < 0) {
                return TextFormatting.GRAY + "-";
            }
            final TextFormatting color = farmingLevel < 0 ? TextFormatting.GRAY : farmingLevel >= level ? TextFormatting.GREEN : TextFormatting.RED;
            return color + String.valueOf(level);
        }

        private static ItemStack seedOf(final CropBlockImpl crop) {
            try {
                final Item seed = crop.getSeed();
                if (seed != null) {
                    return new ItemStack(seed);
                }
            } catch (final RuntimeException ignored) {
                // A crop whose seed item is missing from the content; fall back to the crop itself
            }
            return new ItemStack(crop);
        }

        /**
         * @return the growth settings of the first stage that has them, which is the planted stage for every crop we have
         */
        @Nullable
        private static Growth growthOf(final CropBlockImpl crop) {
            for (int age = 0; age <= crop.getMaxAge(); age++) {
                final CropBlockStateDefinition definition = crop.state(age);
                if (definition != null && definition.growth != null) {
                    return definition.growth;
                }
            }
            return null;
        }
    }

    private static final class CropComponent extends BasicList.ItemComponent<CropEntry> {

        CropComponent(final MalisisGui gui, final BasicList<CropEntry> parent, final CropEntry item) {
            super(gui, parent, item);
            this.setOnDoubleClickConsumer(entry -> {
                if (entry.stageBlock != null && entry.stageAge != null) {
                    new FarmersAlmanacCropStages((FarmersAlmanacCropList) this.getGui(), entry.stageBlock, entry.stageAge, entry.name).display();
                }
            });
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            this.setSize(0, 20);

            final UISaneTooltip tooltip = new UISaneTooltip(this.item.tooltip);

            final UIImage image = new UIImage(this.item.seed);
            image.setPosition(1, 0, Anchor.LEFT | Anchor.MIDDLE);
            image.setTooltip(tooltip);

            final UILabel nameLabel = new UILabel(this.item.name);
            nameLabel.setFontOptions(FontColors.WHITE_FO);
            nameLabel.setPosition(BasicScreen.getPaddedX(image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);
            nameLabel.setTooltip(tooltip);

            this.setTooltip(tooltip);
            this.add(image, nameLabel);

            // The farming level, in the right hand column under its heading
            if (this.item.levelText != null) {
                final UILabel levelLabel = new UILabel(this.item.levelText);
                levelLabel.setFontOptions(FontColors.WHITE_FO);
                levelLabel.setPosition(-ROW_LEVEL_INSET, 0, Anchor.RIGHT | Anchor.MIDDLE);
                levelLabel.setTooltip(tooltip);
                this.add(levelLabel);
            }
        }
    }
}
