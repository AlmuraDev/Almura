/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.GRAY;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.GREEN;
import static com.almuradev.almura.feature.almanac.client.AlmanacFieldScreen.WHITE;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.menu.StrataGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.Spacer;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A crop at every stage of its growth, from planted to ready for harvest, drawn as 3D models in a light grey grid, as 3.5's Almanac showed
 * them on a double click in its crop list. Back (or the window's x) goes back to the list.
 */
final class AlmanacStagesScreen implements Screen {

    private static final int COLUMNS = 4;
    private static final float MODEL_SIZE = 56f, CELL_WIDTH = 76f, CELL_HEIGHT = 74f, BACK_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    // Room the window takes around its contents: its title bar and padding
    private static final float CHROME_WIDTH = 12f, CHROME_HEIGHT = 17f, MIN_WIDTH = 150f;
    // Solid light grey, like an inventory, so the crops stand out
    private static final Paint GRID = Paint.solid(0xFFC6C6C6);
    private static final Border GRID_BORDER = Border.of(0xB9FFFFFF, 1f);
    private static final int LABEL = 0xFF404040;

    private final String title;
    private final List<BlockState> states;

    private AlmanacStagesScreen(final String title, final List<BlockState> states) {
        this.title = title;
        this.states = states;
    }

    /**
     * @return the planted block's age, when it has stages to show; sugar cane's age only counts towards its next block, so it has none
     */
    static @Nullable IntegerProperty age(final @Nullable Block block) {
        if (block == null || block == Blocks.SUGAR_CANE) {
            return null;
        }
        final Property<?> property = block.getStateDefinition().getProperty("age");
        return property instanceof final IntegerProperty age && age.getPossibleValues().size() > 1 ? age : null;
    }

    /**
     * Opens the stages of the planted block, over the screen showing now (the crop list), which Back returns to.
     */
    static void open(final String title, final Block block) {
        final IntegerProperty age = age(block);
        if (age == null) {
            return;
        }
        final List<BlockState> states = new ArrayList<>();
        for (final Integer value : age.getPossibleValues()) {
            states.add(block.defaultBlockState().setValue(age, value));
        }
        InGameScreens.open(new AlmanacStagesScreen(title, states), Minecraft.getInstance().gui.screen());
    }

    @Override
    public Text title() {
        return Text.of(this.title);
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final int stages = this.states.size();
        final int columns = Math.min(COLUMNS, stages);
        final int rows = (stages + COLUMNS - 1) / COLUMNS;
        final float gridWidth = columns * CELL_WIDTH + 2f, gridHeight = rows * CELL_HEIGHT + 2f;

        // Every stage shares one view, so they're drawn at the same size and the crop can be seen growing
        final CropStageView view = CropStageView.fitting(this.states);
        final AnchorPanel grid = factory.anchorPanel().setBackground(GRID).setBorder(GRID_BORDER).setPadding(1f);
        grid.setSize(gridWidth, gridHeight);
        for (int index = 0; index < stages; index++) {
            grid.add(this.cell(factory, index, stages, view)
                .setAnchor(Anchor.TOP_LEFT).setMargin((index % COLUMNS) * CELL_WIDTH, (index / COLUMNS) * CELL_HEIGHT, 0f, 0f));
        }

        final Button back = factory.button(Text.of("Back"))
            .setSize(BACK_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        final AnchorPanel page = factory.anchorPanel()
            .add(grid.setAnchor(Anchor.TOP_CENTER))
            .add(back.setAnchor(Anchor.BOTTOM_RIGHT));

        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(Math.max(MIN_WIDTH, gridWidth + CHROME_WIDTH), gridHeight + 6f + BUTTON_HEIGHT + CHROME_HEIGHT)
            .setAnchor(Anchor.CENTER);
        // Its x goes back to the list, as Back does
        InGameScreens.closesScreen(window);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * A stage: its model, and "Stage n" under it. Stages are numbered from 1, as the Almanac's "Growth Stage 1 of 8" does.
     */
    private AnchorPanel cell(final ComponentFactory factory, final int index, final int stages, final CropStageView view) {
        final BlockState state = this.states.get(index);
        Text tooltip = Text.of("Stage " + (index + 1) + " of " + stages).color(WHITE);
        if (index == 0) {
            tooltip = Text.concat(tooltip, Text.of("\nJust planted").color(GRAY));
        }
        if (index == stages - 1) {
            tooltip = Text.concat(tooltip, Text.of("\nReady for harvest").color(GREEN));
        }

        final Spacer model = factory.spacer(MODEL_SIZE, MODEL_SIZE);
        // Drawn as Strata paints its space, so the tooltips it paints after go over it
        model.onEvent(Component.PAINTED, event -> {
            final GuiGraphicsExtractor graphics = StrataGraphics.of(event.painter());
            if (graphics != null) {
                draw(graphics, model.bounds(), state, view);
            }
        });
        // Dark grey on the light grid, without a shadow
        final Label label = factory.label(Text.of("Stage " + (index + 1))).setStyle(TextStyle.DEFAULT.withColor(LABEL).withShadow(false));
        label.setHitTestVisible(false);

        final AnchorPanel cell = factory.anchorPanel()
            .add(model.setAnchor(Anchor.TOP_CENTER).setMargin(0f, 3f, 0f, 0f))
            .add(label.setAnchor(Anchor.TOP_CENTER).setMargin(0f, MODEL_SIZE + 5f, 0f, 0f));
        cell.setSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setTooltip(tooltip);
        return cell;
    }

    private static void draw(final GuiGraphicsExtractor graphics, final Rect bounds, final BlockState state, final CropStageView view) {
        if (bounds.width() <= 0f || bounds.height() <= 0f) {
            return;
        }
        final int x0 = Math.round(bounds.x()), y0 = Math.round(bounds.y());
        final int x1 = Math.round(bounds.x() + bounds.width()), y1 = Math.round(bounds.y() + bounds.height());
        graphics.submitPictureInPictureRenderState(new CropStageRenderState(state, view, x0, y0, x1, y1, (x1 - x0) * view.scale()));
    }
}
