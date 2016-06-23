/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame.hud;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.util.FontRenderOptionsBuilder;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.format.TextColors;

public class MinimalHUD extends SimpleGui {

    private static final FontRenderOptions FRO_HUD;
    private static final int PADDING = 1;
    private static final String COMPASS_CHARACTERS = "S.......W.......N.......E.......";
    private static final String TIME_FORMAT = "%2d:%02d%s";
    private UILabel clockLabel;
    private UILabel economyLabel;
    private UILabel compassLabel;
    private UILabel coordinatesXLabel;
    private UILabel coordinatesYLabel;
    private UILabel coordinatesZLabel;
    private UILabel worldLabel;

    static {
        FRO_HUD = new FontRenderOptions();
        FRO_HUD.color = TextColors.WHITE.getColor().getRgb();
        FRO_HUD.shadow = true;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        final UIBackgroundContainer overlayContainer = new UIBackgroundContainer(this, UIComponent.INHERITED, UIComponent.INHERITED);
        overlayContainer.setBackgroundAlpha(0);
        overlayContainer.setClipContent(false);

        // Display name
        final UILabel nameLabel = new UILabel(this, mc.thePlayer.getDisplayName().getFormattedText());
        nameLabel.setPosition(PADDING, PADDING, Anchor.TOP | Anchor.LEFT);
        nameLabel.setFontRenderOptions(FRO_HUD);

        // Economy
        economyLabel = new UILabel(this, "$0.00");
        economyLabel.setPosition(PADDING, getPaddedY(nameLabel, PADDING), Anchor.TOP | Anchor.LEFT);
        economyLabel.setFontRenderOptions(FRO_HUD);

        // Clock
        final UIImage clockImage = new UIImage(this, TEXTURE_SPRITESHEET, ICON_CLOCK);
        clockImage.setPosition(PADDING, getPaddedY(economyLabel, PADDING), Anchor.TOP | Anchor.LEFT);
        clockImage.setSize(7, 7);

        clockLabel = new UILabel(this, "12:00AM");
        clockLabel.setPosition(getPaddedX(clockImage, PADDING), getPaddedY(economyLabel, PADDING), clockImage.getAnchor());
        clockLabel.setFontRenderOptions(FRO_HUD);

        // Compass
        final UIBackgroundContainer compassContainer = new UIBackgroundContainer(this, 50, 10);
        compassContainer.setBackgroundAlpha(180);
        compassContainer.setColor(0);
        compassContainer.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        compassLabel = new UILabel(this, "");
        compassLabel.setPosition(1, 1, Anchor.CENTER | Anchor.MIDDLE);
        compassLabel.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FRO_HUD).shadow(false).build());

        compassContainer.add(compassLabel);

        // World
        worldLabel = new UILabel(this, "World");
        worldLabel.setPosition(0, getPaddedY(compassContainer, PADDING), Anchor.TOP | Anchor.CENTER);
        worldLabel.setFontRenderOptions(FRO_HUD);

        // Coordinates
        coordinatesXLabel = new UILabel(this, (int) mc.thePlayer.posX + " X");
        coordinatesXLabel.setPosition(-PADDING, PADDING, Anchor.TOP | Anchor.RIGHT);
        coordinatesXLabel.setFontRenderOptions(FRO_HUD);

        coordinatesYLabel = new UILabel(this, (int) mc.thePlayer.posY + " Y");
        coordinatesYLabel.setPosition(-PADDING, getPaddedY(coordinatesXLabel, PADDING), Anchor.TOP | Anchor.RIGHT);
        coordinatesYLabel.setFontRenderOptions(FRO_HUD);

        coordinatesZLabel = new UILabel(this, (int) mc.thePlayer.posZ + " Z");
        coordinatesZLabel.setPosition(-PADDING, getPaddedY(coordinatesYLabel, PADDING), Anchor.TOP | Anchor.RIGHT);
        coordinatesZLabel.setFontRenderOptions(FRO_HUD);

        overlayContainer.add(nameLabel, economyLabel, clockImage, clockLabel, compassContainer, worldLabel, coordinatesXLabel, coordinatesYLabel,
                coordinatesZLabel);

        addToScreen(overlayContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        // Clock
        clockLabel.setText(getTime());

        // Compass
        compassLabel.setText(getCompass());

        // World
        worldLabel.setText(HUDData.WORLD_NAME);

        // Economy
        economyLabel.setText(HUDData.PLAYER_CURRENCY);

        // Coordinates
        coordinatesXLabel.setText((int) mc.thePlayer.posX + " X");
        coordinatesYLabel.setText((int) mc.thePlayer.posY + " Y");
        coordinatesZLabel.setText((int) mc.thePlayer.posZ + " Z");
    }

    private String getCompass() {
        final int position = (int) ((((mc.thePlayer.rotationYaw + 11.25) % 360 + 360) % 360) / 360 * 32);

        return "" + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 8) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 7) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 6) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 5) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 4) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 3) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position - 2) & 31)
                + TextFormatting.GRAY + COMPASS_CHARACTERS.charAt((position - 1) & 31)
                + TextFormatting.WHITE + COMPASS_CHARACTERS.charAt((position) & 31)
                + TextFormatting.GRAY + COMPASS_CHARACTERS.charAt((position + 1) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 2) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 3) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 4) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 5) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 6) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 7) & 31)
                + TextFormatting.DARK_GRAY + COMPASS_CHARACTERS.charAt((position + 8) & 31);
    }

    private String getTime() {
        final int minute = (int) Math.floor((mc.thePlayer.worldObj.getWorldTime() % 1000) / 1000.0 * 60);
        final int hour = (int) ((Math.floor(mc.thePlayer.worldObj.getWorldTime() / 1000.0) + 6) % 24);

        if (hour == 0) {
            return String.format(TIME_FORMAT, hour + 12, minute, "AM");
        } else if (hour == 12) {
            return String.format(TIME_FORMAT, hour, minute, "PM");
        } else if (hour > 12) {
            return String.format(TIME_FORMAT, hour - 12, minute, "PM");
        } else {
            return String.format(TIME_FORMAT, hour, minute, "AM");
        }
    }
}
