/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.text.DecimalFormat;

public class IngameBlockWireframe extends AlmuraGui {

    private final World world;
    private final Block block;
    private final int x, y, z;
    private UITextField minXTextField, minYTextField, minZTextField, maxXTextField, maxYTextField, maxZTextField;
    private AxisAlignedBB wireframe;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added
     *
     * @param parent the {@link AlmuraGui} that we came from
     */
    public IngameBlockWireframe(AlmuraGui parent, World world, Block block, int x, int y, int z, AxisAlignedBB wireframe) {
        super(parent);
        this.world = world;
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
        this.wireframe = wireframe;
        setup();
    }

    @Override
    protected void setup() {
        guiscreenBackground = false;

        final UIForm form = new UIForm(this, 175, 150, "Wireframe Information");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final int padding = 5;
        final DecimalFormat decimal = new DecimalFormat("#.##");

        final UILabel minXLabel = new UILabel(this, ChatColor.GRAY + "Min X:");
        minXLabel.setPosition(padding, padding * 2, Anchor.LEFT | Anchor.TOP);

        minXTextField = new UITextField(this, decimal.format(block.minX));
        minXTextField.setPosition(getPaddedX(minXLabel, padding + 2), minXLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        minXTextField.setSize(50, 12);

        final UILabel minYLabel = new UILabel(this, ChatColor.GRAY + "Min Y:");
        minYLabel.setPosition(padding, getPaddedY(minXTextField, padding) + 2, Anchor.LEFT | Anchor.TOP);

        minYTextField = new UITextField(this, decimal.format(block.minY));
        minYTextField.setPosition(getPaddedX(minYLabel, padding + 2), minYLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        minYTextField.setSize(50, 12);

        final UILabel minZLabel = new UILabel(this, ChatColor.GRAY + "Min Z:");
        minZLabel.setPosition(padding, getPaddedY(minYTextField, padding) + 2, Anchor.LEFT | Anchor.TOP);

        minZTextField = new UITextField(this, decimal.format(block.minZ));
        minZTextField.setPosition(getPaddedX(minZLabel, padding + 2), minZLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        minZTextField.setSize(50, 12);

        final UILabel maxXLabel = new UILabel(this, ChatColor.GRAY + "Max X:");
        maxXLabel.setPosition(padding, getPaddedY(minZTextField, padding) + 2, Anchor.LEFT | Anchor.TOP);

        maxXTextField = new UITextField(this, decimal.format(block.maxX));
        maxXTextField.setPosition(getPaddedX(maxXLabel, padding), maxXLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        maxXTextField.setSize(50, 12);

        final UILabel maxYLabel = new UILabel(this, ChatColor.GRAY + "Max Y:");
        maxYLabel.setPosition(padding, getPaddedY(maxXTextField, padding) + 2, Anchor.LEFT | Anchor.TOP);

        maxYTextField = new UITextField(this, decimal.format(block.maxY));
        maxYTextField.setPosition(getPaddedX(maxYLabel, padding), maxYLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        maxYTextField.setSize(50, 12);

        final UILabel maxZLabel = new UILabel(this, ChatColor.GRAY + "Max Z:");
        maxZLabel.setPosition(padding, getPaddedY(maxYTextField, padding) + 2, Anchor.LEFT | Anchor.TOP);

        maxZTextField = new UITextField(this, decimal.format(block.maxZ));
        maxZTextField.setPosition(getPaddedX(maxZLabel, padding), maxZLabel.getY() - 2, Anchor.LEFT | Anchor.TOP);
        maxZTextField.setSize(50, 12);

        final UIButton applyButton = new UIButton(this, "Apply");
        applyButton.setSize(50, 14);
        applyButton.setPosition(-padding, -padding, Anchor.RIGHT | Anchor.BOTTOM);
        applyButton.setName("button.apply");
        applyButton.register(this);

        final UIButton cancelButton = new UIButton(this, "Cancel");
        cancelButton.setSize(50, 14);
        cancelButton.setPosition(-(getPaddedX(applyButton, padding) + padding), -padding, Anchor.RIGHT | Anchor.BOTTOM);
        cancelButton.setName("button.cancel");
        cancelButton.register(this);

        form.getContentContainer().add(minXLabel, minXTextField,
                minYLabel, minYTextField,
                minZLabel, minZTextField,
                maxXLabel, maxXTextField,
                maxYLabel, maxYTextField,
                maxZLabel, maxZTextField,
                cancelButton, applyButton);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.apply":
                try {
                    block.minX = Double.parseDouble(minXTextField.getText());
                    block.minY = Double.parseDouble(minYTextField.getText());
                    block.minZ = Double.parseDouble(minZTextField.getText());
                    block.maxX = Double.parseDouble(maxXTextField.getText());
                    block.maxY = Double.parseDouble(maxYTextField.getText());
                    block.maxZ = Double.parseDouble(maxZTextField.getText());
                } catch (Exception e) {
                    break;
                }
            case "button.cancel":
                close();
        }
    }
}
