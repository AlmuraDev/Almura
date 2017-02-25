/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.pack.IModelContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.crop.Stage;
import com.almuradev.almura.util.Colors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;

public class IngameFarmersAlmanac extends SimpleGui {

    private final Block block;
    private final int metadata;
    private final boolean fertile;
    private final float temp;
    private final float rain;
    private final int areaBlockLight;
    private final int sunlight;

    public IngameFarmersAlmanac(Block block, int metadata, boolean fertile, float temp, float rain, int areaBlockLight, int sunlight) {
        this.block = block;        
        this.metadata = metadata;
        this.fertile = fertile;
        this.temp = temp;
        this.rain = rain;
        this.areaBlockLight = areaBlockLight;
        this.sunlight = sunlight;
        construct();
    }

    private static String getFormattedString(String raw, int length, String suffix) {
        if (raw.trim().length() <= length) {
            return raw;
        } else {
            return raw.substring(0, Math.min(raw.length(), length)).trim() + suffix;
        }
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        final int xPadding = 10;
        final int yPadding = 1;
        int formHeight = 135;
        int formWidth = 150;
        int newWindowWidth = 150;
        
        String groundTemp = "";
        String groundMoisture = "";
        String rainAmount = "";
        String sunlightText;
        String areaBlockLightText;
        
        //Frozen = Temp =< 0.14
        
        if (temp <= 0.14) 
            groundTemp = Colors.RED + "Frozen";
        if (temp >= 0.15 && temp <= 0.2)
            groundTemp = Colors.BLUE + "Chilly";
        if (temp >= 0.21 && temp <= 0.5)
            groundTemp = Colors.GREEN + "Slightly Warm";
        if (temp >= 0.51 && temp <= 1.2)
            groundTemp = Colors.DARK_GREEN + "Warm";
        if (temp >= 1.21 && temp <= 1.9)
            groundTemp = Colors.GOLD + "Very Warm";
        if (temp >= 1.91)
            groundTemp = Colors.RED + "Hot";
        
        if (this.fertile) {
            groundMoisture = Colors.DARK_GREEN + "Fertile";
        } else {
            groundMoisture = Colors.RED + "Too Dry";
        }
        
        if (rain > 0.4) {
            rainAmount = "" + Colors.DARK_GREEN + rain;
        } else {
            rainAmount = "" + Colors.RED + rain;
        }
        
        if (sunlight < 6) {
            sunlightText = "" + Colors.RED + sunlight;
        } else {
            sunlightText = "" + Colors.DARK_GREEN + sunlight;
        }
        
        if (areaBlockLight < 6) {
            if (sunlight < 6) {
                areaBlockLightText = "" + Colors.RED + areaBlockLight;
            } else {
                areaBlockLightText = "" + Colors.YELLOW + areaBlockLight;
            }
        } else {
            areaBlockLightText = "" + Colors.DARK_GREEN + areaBlockLight;
        }

        final UIForm form = new UIForm(this, formWidth, formHeight, "Farmers Almanac");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final UIImage blockImage = new UIImage(this, new GuiTexture(UIImage.BLOCKS_TEXTURE), block.getIcon(0, metadata));
        blockImage.setPosition(xPadding, (yPadding + 3) * 2, Anchor.LEFT | Anchor.TOP);

        String localized = block.getLocalizedName();
        if (localized.equalsIgnoreCase("Crops")) {
            localized = "Wheat"; //Wheat is normally displayed as Crops, well not here...
        }
        UILabel localizedNameLabel = new UILabel(this, Colors.WHITE + getFormattedString(localized, 22, "..."));
        localizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), blockImage.getY(), Anchor.LEFT | Anchor.TOP);
        localizedNameLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_110);

        //final UILabel unlocalizedNameLabel = new UILabel(this, Colors.GRAY + getFormattedString(block.getUnlocalizedName(), 60, "..."));
        final UILabel unlocalizedNameLabel = new UILabel(this, Colors.GRAY + getFormattedString("", 60, "..."));  //Temporarily disabled.
        unlocalizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), getPaddedY(localizedNameLabel, 0), Anchor.LEFT | Anchor.TOP);
        unlocalizedNameLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_070);

        final UILabel metadataLabel = new UILabel(this, "");
        
        if (block instanceof BlockFarmland) {
            //metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + "N/A");
        } else {
            if (block instanceof BlockCrops) {
                metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + metadata + " of 7");
            }
            
            if (block instanceof PackCrops) {
                final PackCrops crop = (PackCrops) block;
                metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + metadata + " of " + (crop.getStages().size()-1));
            }
        }
        
        metadataLabel.setPosition(xPadding, getPaddedY(unlocalizedNameLabel, yPadding+5), Anchor.LEFT | Anchor.TOP);
        
        final UILabel moisturedataLabel = new UILabel(this, Colors.GRAY + "Ground Moisture: " + Colors.BLUE + groundMoisture);
        moisturedataLabel.setPosition(xPadding, getPaddedY(metadataLabel, yPadding), Anchor.LEFT | Anchor.TOP);
        
        final UILabel temperaturedataLabel = new UILabel(this, Colors.GRAY + "Ground Temperature: " + Colors.BLUE + groundTemp);
        temperaturedataLabel.setPosition(xPadding, getPaddedY(moisturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel biomeRainLabel = new UILabel(this, Colors.GRAY + "Biome Rain: " + rainAmount);
        biomeRainLabel.setPosition(xPadding, getPaddedY(temperaturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel sunlightValueLabel = new UILabel(this, Colors.GRAY + "Natural Sunlight Value: " + sunlightText);
        sunlightValueLabel.setPosition(xPadding, getPaddedY(biomeRainLabel, yPadding), Anchor.LEFT | Anchor.TOP);
        
        final UILabel areaLightValueLabel = new UILabel(this, Colors.GRAY + "Artificial Light Value: " + areaBlockLightText);
        areaLightValueLabel.setPosition(xPadding, getPaddedY(sunlightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);
        
        if (block instanceof BlockCocoa) {
            metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + metadata + " of 9");
            // The following values are hidden because growth of Cocoa isn't dependent on these values.
            moisturedataLabel.setText("");
            temperaturedataLabel.setText("");
            biomeRainLabel.setText("");
            sunlightValueLabel.setText("");
            areaLightValueLabel.setText("");
        }
        
        UILabel modelNameLabel = null;

        if (block instanceof IModelContainer && block instanceof IPackObject) {

            final IModelContainer modelContainer = (IModelContainer) block;
            final UILabel textureNameLabel = new UILabel(this, Colors.GRAY + "Texture Name: " + Colors.BLUE + block.getTextureName()+".png");
            textureNameLabel.setPosition(xPadding, getPaddedY(areaLightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            form.getContentContainer().add(textureNameLabel);

            String modelName;

            if (block instanceof PackCrops) {
                final PackCrops crop = (PackCrops) block;
                final Stage stage = crop.getStages().get(metadata);
                modelName = stage.getModelName();
            } else {
                modelName = modelContainer.getModelName();
            }

            modelNameLabel = new UILabel(this, Colors.GRAY + "Model Name: " + Colors.BLUE + modelName+".shape");
            modelNameLabel.setPosition(xPadding, getPaddedY(textureNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            form.getContentContainer().add(modelNameLabel);

            final UILabel packNameLabel =
                    new UILabel(this, Colors.GRAY + "Pack Name: " + Colors.BLUE + ((IPackObject) block).getPack().getName());
            packNameLabel.setPosition(xPadding, getPaddedY(modelNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            form.getContentContainer().add(packNameLabel);

            if (modelNameLabel.getWidth() >= (formWidth - 20)) {
                newWindowWidth = modelNameLabel.getWidth() + 20;
            }

            if (textureNameLabel.getWidth() >= (formWidth - 20)) {
                newWindowWidth = textureNameLabel.getWidth() + 20;
            }

            formWidth = newWindowWidth;
            formHeight += 30;
        }

        final String harvestTool = block.getHarvestTool(metadata);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            final UILabel harvestToolLabel = new UILabel(this, Colors.GRAY + "Harvest tool: " + Colors.BLUE + harvestTool);
            if (block instanceof PackBlock) {
                harvestToolLabel.setPosition(xPadding, getPaddedY(modelNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            } else {
                harvestToolLabel.setPosition(xPadding, getPaddedY(areaLightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            }
            formHeight += 10;
            form.getContentContainer().add(harvestToolLabel);
        }

        form.setSize(formWidth, formHeight);

        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 14);
        closeButton.setPosition(-4, -4, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        form.getContentContainer().add(blockImage, localizedNameLabel, unlocalizedNameLabel, metadataLabel, moisturedataLabel, temperaturedataLabel, biomeRainLabel, sunlightValueLabel, areaLightValueLabel, closeButton);

        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }
}
