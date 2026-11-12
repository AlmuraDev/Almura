/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.gui;

import com.almuradev.almura.shared.client.ui.BottomRow;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows a crop at every stage of its growth, from planted to ready for harvest.
 */
@SideOnly(Side.CLIENT)
public final class FarmersAlmanacCropStages extends BasicScreen {

    private static final int COLUMNS = 4;
    private static final int MODEL_SIZE = 56;
    private static final int CELL_WIDTH = 76;
    private static final int CELL_HEIGHT = 74;

    private final Block block;
    private final PropertyInteger age;
    private final String title;
    private boolean unlockMouse = true;
    private int lastUpdate = 0;

    /**
     * @param block the planted block, shown at each value of its age
     */
    public FarmersAlmanacCropStages(final GuiScreen parent, final Block block, final PropertyInteger age, final String title) {
        super(parent);
        this.block = block;
        this.age = age;
        this.title = title;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        final int stages = Collections.max(this.age.getAllowedValues()) + 1;
        final int columns = Math.min(COLUMNS, stages);
        final int rows = (stages + COLUMNS - 1) / COLUMNS;

        final BasicForm form = new BasicForm(Math.max(150, columns * CELL_WIDTH + 12), rows * CELL_HEIGHT + 60, this.title);

        // Every stage shares one view, so they're drawn at the same size and the crop can be seen growing
        final List<IBlockState> states = new ArrayList<>();
        for (int age = 0; age < stages; age++) {
            states.add(this.block.getDefaultState().withProperty(this.age, age));
        }
        final UIBlockStateModel.View view = UIBlockStateModel.View.fitting(states);

        final BasicContainer<?> grid = new BasicContainer<>(columns * CELL_WIDTH + 2, rows * CELL_HEIGHT + 2);
        grid.setBorder(FontColors.WHITE, 1, 185);
        // Solid light grey, like an inventory, so the crops stand out
        grid.setColor(0xC6C6C6);
        grid.setBackgroundAlpha(255);
        grid.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        for (int age = 0; age < stages; age++) {
            final IBlockState state = states.get(age);
            final int x = (age % COLUMNS) * CELL_WIDTH;
            final int y = (age / COLUMNS) * CELL_HEIGHT;
            final boolean ripe = age == stages - 1;
            // Stages are numbered from 1 here, as the Almanac's "Growth Stage 1 of 8" does
            final String tooltip = TextFormatting.WHITE + "Stage " + (age + 1) + " of " + stages
                    + (age == 0 ? "\n" + TextFormatting.GRAY + "Just planted" : "")
                    + (ripe ? "\n" + TextFormatting.GREEN + "Ready for harvest" : "");

            final UIBlockStateModel model = new UIBlockStateModel(state, MODEL_SIZE, view);
            model.setPosition(x + (CELL_WIDTH - MODEL_SIZE) / 2, y + 3);
            model.setTooltip(new UISaneTooltip(tooltip));

            final UILabel label = new UILabel("Stage " + (age + 1));
            label.setFontOptions(FontOptions.builder().color(0x404040).build());
            label.setPosition(x + (CELL_WIDTH - label.getWidth()) / 2, y + MODEL_SIZE + 5);
            label.setTooltip(new UISaneTooltip(tooltip));

            grid.add(model, label);
        }

        final UIButton backButton = new UIButtonBuilder()
                .text("Back")
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .onClick(this::close)
                .build("button.back");

        form.add(grid, backButton);
        // Fit the form to the grid and the button below it, without empty space between them
        form.setSize(form.getWidth(), form.getHeight() - BasicScreen.getPaddedHeight(form) + grid.getHeight() + BottomRow.above(form, backButton.getHeight()));
        this.addToScreen(form);

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
}
