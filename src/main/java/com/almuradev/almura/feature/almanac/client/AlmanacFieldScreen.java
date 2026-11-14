/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import com.almuradev.almura.content.def.ContentDefs.CropStageDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationBlockDef;
import com.almuradev.almura.content.def.ContentDefs.HydrationDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import com.almuradev.almura.content.registry.ContentCrop;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Farmer's Almanac on a spot of a field, as 3.5's: the block (its item, name and id), then what the crop there needs and what the spot
 * gives it (hydration, its stage, the soil, rain, the ground temperature, the light, whether it can die, a heat lamp, irrigation), and at the
 * bottom whether it is growing, with Close. Read from the client's world and the content, with what only the server knows from its message.
 */
final class AlmanacFieldScreen implements Screen {

    static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, DARK_GRAY = 0xFF555555, DARK_GREEN = 0xFF00AA00, GREEN = 0xFF55FF55,
        RED = 0xFFFF5555, YELLOW = 0xFFFFFF55, GOLD = 0xFFFFAA00;
    private static final float WIDTH = 230f, LIST_MAX_HEIGHT = 120f, LINE_HEIGHT = 11f, CLOSE_WIDTH = 40f, BUTTON_HEIGHT = 16f, ICON = 16f;
    static final DecimalFormat TWO = new DecimalFormat("0.00");
    static final org.inspirenxe.strata.api.theme.Paint BOX = org.inspirenxe.strata.api.theme.Paint.solid(0x23FFFFFF);
    static final org.inspirenxe.strata.api.theme.Border BOX_BORDER = org.inspirenxe.strata.api.theme.Border.of(0xB9FFFFFF, 1f);

    private final ClientboundAlmanacField field;
    private final List<Text> lines = new ArrayList<>();
    private boolean growing = true, canDie, ready, statusShown = true;

    AlmanacFieldScreen(final ClientboundAlmanacField field) {
        this.field = field;
    }

    @Override
    public Text title() {
        return Text.of("Farmer's Almanac");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final ClientLevel level = Minecraft.getInstance().level;
        final BlockPos pos = this.field.pos();
        final BlockState state = level == null ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : level.getBlockState(pos);
        if (level != null) {
            this.describe(level, pos, state);
        }

        // The block: its item, its name and under it its id
        final ItemStack pick = level == null ? ItemStack.EMPTY : state.getCloneItemStack(level, pos, false);
        final StackPanel names = factory.stackPanel().setSpacing(1f)
            .add(factory.label(Text.of(state.getBlock().getName().getString()).color(WHITE)))
            .add(factory.label(Text.of(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()).color(GRAY)));
        final StackPanel header = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4f);
        if (!pick.isEmpty()) {
            header.add(factory.image(MinecraftVisuals.item(pick)).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        }
        header.add(names.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));

        // What it needs and gets, scrolling past 120 high
        final StackPanel properties = factory.stackPanel().setSpacing(2f);
        for (final Text line : this.lines) {
            properties.add(factory.label(line).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        }
        // 3.5's property box: lighter than the window (white at alpha 35) with a white border at alpha 185
        final ScrollPanel scroll = factory.scrollPanel();
        scroll.add(properties.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        scroll.setBackground(BOX).setBorder(BOX_BORDER).setPadding(4f, 2f);
        scroll.setHeight(Math.min(LIST_MAX_HEIGHT, this.lines.size() * (LINE_HEIGHT + 2f) + 8f));

        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        final AnchorPanel bar = factory.anchorPanel().setHeight(BUTTON_HEIGHT);
        if (this.statusShown) {
            bar.add(factory.label(this.status()).setAnchor(Anchor.MIDDLE_LEFT));
        }
        bar.add(close.setAnchor(Anchor.MIDDLE_RIGHT));

        final StackPanel column = factory.stackPanel().setSpacing(4f)
            .add(header.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP))
            .add(scroll.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(bar.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));

        final Window window = factory.window(this.title())
            .setClosable(false)
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private Text status() {
        if (this.ready) {
            return Text.of("Ready for Harvest!").color(GREEN);
        }
        if (this.growing) {
            return Text.of("Growing...").color(DARK_GREEN);
        }
        return this.canDie ? Text.of("Dying!").color(RED) : Text.of("Not Growing...").color(YELLOW);
    }

    /**
     * 3.5's lines, in its order, working out whether the crop grows as it goes.
     */
    private void describe(final ClientLevel level, final BlockPos pos, final BlockState state) {
        final boolean pipe = this.field.pipe();
        final boolean farmland = state.getBlock() instanceof FarmlandBlock;
        final CropStageDef stage = state.getBlock() instanceof final ContentCrop crop ? crop.stage(state) : null;

        // Hydration: what waters it, and whether one is there
        final HydrationDef hydration = stage == null ? null : stage.hydration();
        if (hydration != null) {
            this.lines.add(Text.of("Hydrated by:").color(WHITE));
            for (final HydrationBlockDef block : hydration.blocks()) {
                final String name = BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.Identifier.tryParse(block.block()))
                    .map(found -> found.getName().getString()).orElse(block.block());
                this.lines.add(Text.of("  - ").color(WHITE).append(Text.of(name).color(YELLOW)));
                for (final Map.Entry<String, Range> property : block.properties().entrySet()) {
                    this.lines.add(generic("      - " + property.getKey(), TWO.format(property.getValue().min()) + "-"
                        + TWO.format(property.getValue().max())));
                }
                if (hydration.radius() > 0) {
                    this.lines.add(generic("      - radius", Integer.toString(hydration.radius())));
                    final boolean found = this.hydrationFound(level, pos.below(), hydration);
                    this.lines.add(Text.of("      - found: ").color(WHITE).append(found ? Text.of("Yes").color(DARK_GREEN) : Text.of("No").color(RED)));
                    if (!found) {
                        this.growing = false;
                    }
                }
            }
        }

        if (!pipe) {
            if (state.getBlock() instanceof final CropBlock crop) {
                final int age = crop.getAge(state);
                this.lines.add(generic("Growth Stage", String.format("%d of %d", age + 1, crop.getMaxAge() + 1)));
                this.ready = age >= crop.getMaxAge();
            }
            // The farmland's moisture: under a crop, or the farmland itself
            final BlockState soil = state.getBlock() instanceof CropBlock ? level.getBlockState(pos.below()) : state;
            final int moisture = soil.getBlock() instanceof FarmlandBlock ? soil.getValue(FarmlandBlock.MOISTURE) : 0;
            final boolean inWater = state.getBlock() instanceof final ContentCrop crop && crop.isInWater(state);
            final boolean fertile = moisture > 0 || inWater || hydration != null && this.growing;
            this.lines.add(Text.of("Soil Quality: ").color(WHITE).append(fertile ? Text.of("Fertile").color(DARK_GREEN) : Text.of("Too dry").color(RED)));
            if (fertile) {
                this.lines.add(Text.of("  Moisture Level: ").color(WHITE).append(Text.of(Integer.toString(moisture)).color(moisture > 3 ? DARK_GREEN : YELLOW)));
            } else {
                this.growing = false;
            }
            this.lines.add(Text.of("  Rain: ").color(WHITE).append(Text.of(TWO.format(this.field.rainfall())).color(this.field.rainfall() > 0.5f ? DARK_GREEN : GOLD)));
        }

        // The ground temperature, against the crop's range (a heat lamp keeps a cold crop growing)
        final Range temperature = stage == null ? null : stage.temperature();
        final float ground = this.field.temperature();
        if (temperature != null) {
            final double rounded = BigDecimal.valueOf(ground).setScale(2, RoundingMode.HALF_UP).doubleValue();
            final int color;
            if (temperature.contains(rounded)) {
                color = DARK_GREEN;
            } else if (this.field.heatLamp()) {
                color = YELLOW;
            } else {
                color = RED;
                this.growing = false;
            }
            this.lines.add(Text.of("Ground Temperature: ").color(WHITE).append(Text.of(TWO.format(ground)).color(color)));
            this.lines.add(generic("  Required", TWO.format(temperature.min()) + "-" + TWO.format(temperature.max())));
        } else {
            this.lines.add(Text.of("Ground Temperature: ").color(WHITE).append(Text.of(TWO.format(ground)).color(DARK_GREEN)));
        }

        // The light, against the crop's range
        final Range light = stage == null ? null : stage.light();
        final int combined = this.field.combinedLight();
        final boolean night = !this.field.daytime();
        if (light != null) {
            final boolean inRange = light.contains(combined);
            if (!inRange && night && !this.field.seesSky()) {
                this.growing = false;
            }
            final int color = night && this.field.seesSky() ? YELLOW : inRange ? DARK_GREEN : RED;
            this.lines.add(Text.of("Combined Light: ").color(WHITE).append(Text.of(Integer.toString(combined)).color(color)));
            if (night && this.field.seesSky() && !this.ready) {
                this.lines.add(Text.of("   [Night Time Growth]").color(GRAY));
            }
            this.lines.add(generic("  Required", TWO.format(light.min()) + "-" + TWO.format(light.max())));
            this.lines.add(Text.of("    Sunlight: ").color(WHITE).append(Text.of(Integer.toString(this.field.skyLight())).color(YELLOW)));
            this.lines.add(Text.of("    Area light: ").color(WHITE).append(Text.of(Integer.toString(this.field.blockLight())).color(YELLOW)));
        } else {
            this.lines.add(Text.of("Combined Light: ").color(WHITE).append(Text.of(Integer.toString(combined)).color(DARK_GREEN)));
        }

        if (!pipe) {
            if (!farmland && !this.ready && stage != null) {
                this.canDie = stage.canRollback();
                this.lines.add(Text.of("Can Die: ").color(WHITE).append(this.canDie ? Text.of("Yes").color(RED) : Text.of("No").color(DARK_GREEN)));
            }
            if (farmland) {
                this.statusShown = false;
            }
        }

        if (this.field.heatLamp()) {
            this.lines.add(Text.of("Special: ").color(WHITE));
            this.lines.add(Text.of("    Heat Lamp Found!").color(YELLOW));
        }

        if (pipe) {
            this.lines.add(Text.of("Irrigation Water Range: ").color(WHITE).append(Text.of("6").color(DARK_GREEN)));
            this.statusShown = false;
        } else {
            this.lines.add(Text.of("In-Range of Irrigation: ").color(WHITE).append(this.field.pipeNear() ? Text.of("Yes").color(DARK_GREEN)
                : Text.of("No").color(this.field.rainfall() > 0.5f ? DARK_GREEN : GOLD)));
        }
    }

    /**
     * Whether one of the hydration's blocks is within its radius of the soil, on the soil's level.
     */
    private boolean hydrationFound(final ClientLevel level, final BlockPos soil, final HydrationDef hydration) {
        final int radius = hydration.radius();
        for (final BlockPos at : BlockPos.betweenClosed(soil.offset(-radius, 0, -radius), soil.offset(radius, 0, radius))) {
            if (level.isLoaded(at) && ContentCrop.matches(hydration, level.getBlockState(at))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 3.5's plain line: the name in white, the value in grey.
     */
    static Text generic(final String key, final String value) {
        return Text.of(key + ": ").color(WHITE).append(Text.of(value).color(GRAY));
    }
}
