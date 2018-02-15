package com.almuradev.almura.feature.complex.item.almanac.gui;

import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundWorldPositionInformationPacket;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.Colors;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

public class IngameFarmersAlmanac extends SimpleScreen {

    private final Block block;
    private final IBlockState state;
    private final int metadata;
    private final boolean fertile;
    private final float temp;
    private final float rain;
    private final int areaBlockLight;
    private final int sunlight;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;

    public IngameFarmersAlmanac(ClientboundWorldPositionInformationPacket message, World worldIn, BlockPos pos, IBlockState state) {
        this.block = state.getBlock();
        this.state = state;
        this.metadata = block.getMetaFromState(state);

        // Get Ground Moisture value
        BlockPos underBlockPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        IBlockState underBlockState = worldIn.getBlockState(underBlockPos);
        int underBlockMeta = underBlockState.getBlock().getMetaFromState(underBlockState);
        if (underBlockMeta > 0) {
            fertile = true;
        } else {
            fertile = false;
        }

        rain = message.biomeRainfall;
        temp = message.biomeTemperature;

        if (block instanceof BlockCrops) {
            areaBlockLight = worldIn.getLightFor(EnumSkyBlock.BLOCK, pos);
            sunlight = worldIn.getLightFor(EnumSkyBlock.SKY, pos) - worldIn.getSkylightSubtracted();
        } else {
            areaBlockLight = worldIn.getLightFor(EnumSkyBlock.BLOCK, underBlockPos);
            sunlight = worldIn.getLightFor(EnumSkyBlock.SKY, underBlockPos) - worldIn.getSkylightSubtracted();
        }
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
        int formWidth = 180;


        String groundTemp = "";
        String groundMoisture = "";
        String rainAmount = "";
        String sunlightText;
        String areaBlockLightText;

        //Frozen = Temp =< 0.14

        if (temp <= 0.14) {
            groundTemp = Colors.DARK_RED + "Frozen";
        }
        if (temp >= 0.15 && temp <= 0.2) {
            groundTemp = Colors.BLUE + "Chilly";
        }
        if (temp >= 0.21 && temp <= 0.5) {
            groundTemp = Colors.GREEN + "Slightly Warm";
        }
        if (temp >= 0.51 && temp <= 1.2) {
            groundTemp = Colors.DARK_GREEN + "Warm";
        }
        if (temp >= 1.21 && temp <= 1.9) {
            groundTemp = Colors.GOLD + "Very Warm";
        }
        if (temp >= 1.91) {
            groundTemp = Colors.RED + "Hot";
        }

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

        final UIFormContainer form = new UIFormContainer(this, formWidth, formHeight, "");

        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(false);
        form.setBorder(FontColors.WHITE, 1, 185);


        form.setBackgroundAlpha(185);
        form.setPosition(0,25, Anchor.TOP | Anchor.CENTER);

        UILabel titleLabel = new UILabel(this, "Farmer's Almanac");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        final UIImage blockImage = new UIImage(this, new ItemStack(block, 1, block.getMetaFromState(state)));
        blockImage.setPosition(xPadding, (yPadding + 3) * 2, Anchor.LEFT | Anchor.TOP);

        final String localized = block.getLocalizedName();
        UILabel localizedNameLabel = new UILabel(this, getFormattedString(localized, 22, "..."));
        localizedNameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.3F).build());
        localizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), blockImage.getY(), Anchor.LEFT | Anchor.TOP);

        final UILabel unlocalizedNameLabel = new UILabel(this, "§7" + getFormattedString(block.getUnlocalizedName(), 60, "..."));
        unlocalizedNameLabel.setPosition(getPaddedX(blockImage, xPadding), getPaddedY(localizedNameLabel, 0), Anchor.LEFT | Anchor.TOP);
        //unlocalizedNameLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_SCALE_070);

        final UILabel metadataLabel = new UILabel(this, "");

        if (block instanceof BlockFarmland) {
            //metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + "N/A");
        } else {
            if (block instanceof BlockCrops) {
                metadataLabel.setText(Colors.GRAY + "Growth Stage: " + Colors.BLUE + metadata + " of " + ("MAX_AGE"));  //Todo: fix MAX_AGE
            }
        }

        metadataLabel.setPosition(xPadding, getPaddedY(unlocalizedNameLabel, yPadding + 5), Anchor.LEFT | Anchor.TOP);

        final UILabel moisturedataLabel = new UILabel(this, Colors.GRAY + "Ground Moisture: " + Colors.BLUE + groundMoisture);
        moisturedataLabel.setPosition(xPadding, getPaddedY(metadataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel temperaturedataLabel = new UILabel(this, Colors.GRAY + "Ground Temperature: " + Colors.BLUE + groundTemp);
        temperaturedataLabel.setPosition(xPadding, getPaddedY(moisturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel biomeRainLabel = new UILabel(this, Colors.GRAY + "Biome Rain: " + rainAmount);
        biomeRainLabel.setPosition(xPadding, getPaddedY(temperaturedataLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel sunlightValueLabel = new UILabel(this, Colors.GRAY + "Sunlight Value: " + sunlightText);
        sunlightValueLabel.setPosition(xPadding, getPaddedY(biomeRainLabel, yPadding), Anchor.LEFT | Anchor.TOP);

        final UILabel areaLightValueLabel = new UILabel(this, Colors.GRAY + "Area Light Value: " + areaBlockLightText);
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
            final UILabel harvestToolLabel = new UILabel(this, Colors.GRAY + "Harvest tool: " + Colors.BLUE + harvestTool);
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

        form.add(titleLabel, blockImage, localizedNameLabel, unlocalizedNameLabel, metadataLabel, moisturedataLabel, temperaturedataLabel, biomeRainLabel, sunlightValueLabel, areaLightValueLabel, closeButton);

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

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {

        }
    }
}
