package com.almuradev.almura.feature.complex.item.almanac.gui;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class IngameFarmersAlmanac extends SimpleScreen {

    private final Block block;
    private final int metadata;
    private final boolean fertile;
    private final float temp;
    private final float rain;
    private final int areaBlockLight;
    private final int sunlight;
    public static final ResourceLocation BLOCKS_TEXTURE = TextureMap.LOCATION_BLOCKS_TEXTURE;

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
            groundTemp = FontColors.DARK_RED_FO + "Frozen";
        if (temp >= 0.15 && temp <= 0.2)
            groundTemp = FontColors.BLUE_FO + "Chilly";
        if (temp >= 0.21 && temp <= 0.5)
            groundTemp = FontColors.GREEN_FO + "Slightly Warm";
        if (temp >= 0.51 && temp <= 1.2)
            groundTemp = FontColors.DARK_GREEN_FO + "Warm";
        if (temp >= 1.21 && temp <= 1.9)
            groundTemp = FontColors.GOLD_FO + "Very Warm";
        if (temp >= 1.91)
            groundTemp = FontColors.RED_FO + "Hot";

        if (this.fertile) {
            groundMoisture = FontColors.DARK_GREEN_FO + "Fertile";
        } else {
            groundMoisture = FontColors.RED_FO + "Too Dry";
        }

        if (rain > 0.4) {
            rainAmount = "" + FontColors.DARK_GREEN_FO + rain;
        } else {
            rainAmount = "" + FontColors.RED_FO + rain;
        }

        if (sunlight < 6) {
            sunlightText = "" + FontColors.RED_FO + sunlight;
        } else {
            sunlightText = "" + FontColors.DARK_GREEN_FO + sunlight;
        }

        if (areaBlockLight < 6) {
            if (sunlight < 6) {
                areaBlockLightText = "" + FontColors.RED_FO + areaBlockLight;
            } else {
                areaBlockLightText = "" + FontColors.YELLOW_FO + areaBlockLight;
            }
        } else {
            areaBlockLightText = "" + FontColors.DARK_GREEN_FO + areaBlockLight;
        }

        final UIForm form = new UIForm(this, formWidth, formHeight, "Farmers Almanac");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        final UIImage blockImage = new UIImage(this, new ItemStack(block));
        blockImage.setPosition(xPadding, (yPadding + 3) * 2, Anchor.LEFT | Anchor.TOP);

        final String localized = block.getLocalizedName();
        UILabel localizedNameLabel = new UILabel(this, FontColors.WHITE_FO + getFormattedString(localized, 22, "..."));
        localizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), blockImage.getY(), Anchor.LEFT | Anchor.TOP);

        final UILabel unlocalizedNameLabel = new UILabel(this, FontColors.GRAY_FO + getFormattedString(block.getUnlocalizedName(), 60, "..."));
        unlocalizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), getPaddedY(localizedNameLabel, 0), Anchor.LEFT | Anchor.TOP);
        //unlocalizedNameLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_070);

        final UILabel metadataLabel = new UILabel(this, "");

        if (block instanceof BlockFarmland) {
            //metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + "N/A");
        } else {
            if (block instanceof BlockCrops) {
                metadataLabel.setText(FontColors.GRAY_FO + "Growth Stage: " + FontColors.BLUE_FO + metadata + " of " + ("MAX_AGE"));  //Todo: fix MAX_AGE
            }
        }

        metadataLabel.setPosition(xPadding, getPaddedY(unlocalizedNameLabel, yPadding+5), Anchor.LEFT | Anchor.TOP);

        final UILabel moisturedataLabel = new UILabel(this, FontColors.GRAY_FO + "Ground Moisture: " + FontColors.BLUE_FO + groundMoisture);
        moisturedataLabel.setPosition(xPadding, getPaddedY(metadataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel temperaturedataLabel = new UILabel(this, FontColors.GRAY_FO + "Ground Temperature: " + FontColors.BLUE_FO + groundTemp);
        temperaturedataLabel.setPosition(xPadding, getPaddedY(moisturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel biomeRainLabel = new UILabel(this, FontColors.GRAY_FO + "Biome Rain: " + rainAmount);
        biomeRainLabel.setPosition(xPadding, getPaddedY(temperaturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel sunlightValueLabel = new UILabel(this, FontColors.GRAY_FO + "Sunlight Value: " + sunlightText);
        sunlightValueLabel.setPosition(xPadding, getPaddedY(biomeRainLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel areaLightValueLabel = new UILabel(this, FontColors.GRAY_FO + "Area Light Value: " + areaBlockLightText);
        areaLightValueLabel.setPosition(xPadding, getPaddedY(sunlightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        UILabel modelNameLabel = null;

        /*
        if (block instanceof IModelContainer && block instanceof IPackObject) {

            final IModelContainer modelContainer = (IModelContainer) block;
            final UILabel textureNameLabel = new UILabel(this, Colors.GRAY + "Texture Name: " + Colors.BLUE + block.getTextureName());
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

            modelNameLabel = new UILabel(this, Colors.GRAY + "Model Name: " + Colors.BLUE + modelName);
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
        } */
        final IBlockState state = block.getStateFromMeta(metadata);
        final String harvestTool = block.getHarvestTool(state);
        if (harvestTool != null && !harvestTool.isEmpty()) {
            final UILabel harvestToolLabel = new UILabel(this, FontColors.GRAY_FO + "Harvest tool: " + FontColors.BLUE_FO + harvestTool);
            if (block instanceof BlockCrops) {
                harvestToolLabel.setPosition(xPadding, getPaddedY(modelNameLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            } else {
                harvestToolLabel.setPosition(xPadding, getPaddedY(areaLightValueLabel, yPadding), Anchor.LEFT | Anchor.TOP);
            }
            formHeight += 10;
            form.add(harvestToolLabel);
        }

        form.setSize(formWidth, formHeight);

        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 14);
        closeButton.setPosition(-4, -4, Anchor.RIGHT | Anchor.BOTTOM);
        closeButton.setName("button.close");
        closeButton.register(this);

        form.add(blockImage, localizedNameLabel, unlocalizedNameLabel, metadataLabel, moisturedataLabel, temperaturedataLabel, biomeRainLabel, sunlightValueLabel, areaLightValueLabel, closeButton);

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
