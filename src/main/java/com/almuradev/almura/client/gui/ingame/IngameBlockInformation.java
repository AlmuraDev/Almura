/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;

public class IngameBlockInformation extends AlmuraGui {

    private final Block block;
    private final int metadata;
    private final float hardness;

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public IngameBlockInformation(AlmuraGui parent, Block block, int metadata, float hardness) {
        super(parent);
        this.block = block;
        this.metadata = metadata;
        this.hardness = hardness;
        setup();
    }

    @Override
    protected void setup() {
        final UIBackgroundContainer window = new UIBackgroundContainer(this, 175, 150);
        window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        window.setColor(Integer.MIN_VALUE);
        window.setBackgroundAlpha(125);

        final int padding = 5;
        final DecimalFormat decimal = new DecimalFormat("#.##");

        final UIImage blockImage = new UIImage(this, new GuiTexture(UIImage.BLOCKS_TEXTURE), block.getIcon(0, metadata));
        blockImage.setPosition(padding, padding, Anchor.LEFT | Anchor.TOP);

        final String localized = block.getLocalizedName();
        final UILabel localizedNameLabel = new UILabel(this, ChatColor.WHITE + getFormattedString(localized, 22, "..."));
        localizedNameLabel.setPosition(getPaddedX(blockImage, padding), padding + 3, Anchor.LEFT | Anchor.TOP);

        final UILabel unlocalizedNameLabel = new UILabel(this, ChatColor.GRAY + getFormattedString(block.getUnlocalizedName(), 60, "..."));
        unlocalizedNameLabel.setPosition(getPaddedX(blockImage, padding), getPaddedY(localizedNameLabel, 0), Anchor.LEFT | Anchor.TOP);
        unlocalizedNameLabel.setFontScale(0.5f);

        final UILabel soundLabel = new UILabel(this, ChatColor.GRAY + "Step sound: " + block.stepSound.soundName);
        soundLabel.setPosition(padding, getPaddedY(unlocalizedNameLabel, padding), Anchor.LEFT | Anchor.TOP);

        final UILabel metadataLabel = new UILabel(this, ChatColor.GRAY + "Metadata: " + metadata);
        metadataLabel.setPosition(padding, getPaddedY(soundLabel, padding), Anchor.LEFT | Anchor.TOP);

        final UILabel lightOpacityLabel = new UILabel(this, ChatColor.GRAY + "Light opacity: " + block.getLightOpacity());
        lightOpacityLabel.setPosition(padding, getPaddedY(metadataLabel, padding), Anchor.LEFT | Anchor.TOP);

        final UILabel lightValueLabel = new UILabel(this, ChatColor.GRAY + "Light value: " + block.getLightValue());
        lightValueLabel.setPosition(padding, getPaddedY(lightOpacityLabel, padding), Anchor.LEFT | Anchor.TOP);

        final UILabel hardnessLabel = new UILabel(this, ChatColor.GRAY + "Hardness: " + decimal.format(hardness));
        hardnessLabel.setPosition(padding, getPaddedY(lightValueLabel, padding), Anchor.LEFT | Anchor.TOP);

        final UILabel
                blockBoundsLabel =
                new UILabel(this, ChatColor.GRAY + String
                        .format("Block bounds: %1$sx%2$sx%3$s", decimal.format(block.getBlockBoundsMaxX()),
                                decimal.format(block.getBlockBoundsMaxY()), decimal.format(block.getBlockBoundsMaxZ())));
        blockBoundsLabel.setPosition(padding, getPaddedY(hardnessLabel, padding), Anchor.LEFT | Anchor.TOP);

        final String harvestTool = block.getHarvestTool(metadata);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            final UILabel harvestToolLabel = new UILabel(this, ChatColor.GRAY + "Harvest tool: " + harvestTool);
            harvestToolLabel.setPosition(padding, getPaddedY(blockBoundsLabel, padding), Anchor.LEFT | Anchor.TOP);
            window.add(harvestToolLabel);
        }

        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 14);
        closeButton.setPosition(-padding, -padding, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        window.add(blockImage, localizedNameLabel, unlocalizedNameLabel, soundLabel, metadataLabel, lightOpacityLabel, lightValueLabel, hardnessLabel,
                   blockBoundsLabel, closeButton);

        new UIMoveHandle(this, window);

        addToScreen(window);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }

    private static String getFormattedString(String raw, int length, String suffix) {
        if (raw.trim().length() <= length) {
            return raw;
        } else {
            return raw.substring(0, Math.min(raw.length(), length)).trim() + suffix;
        }
    }
}
