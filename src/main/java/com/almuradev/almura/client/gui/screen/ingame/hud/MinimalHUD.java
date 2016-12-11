/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame.hud;

import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.format.TextColors;

@SideOnly(Side.CLIENT)
public class MinimalHUD extends AbstractHUD {

    private static final FontOptions FRO_HUD;
    private static final int PADDING = 1;

    static {
        FRO_HUD = FontOptions.builder().color(TextColors.WHITE.getColor().getRgb()).shadow().build();
    }

    private UILabel clockLabel;
    private UILabel economyLabel;
    private UILabel compassLabel;
    private UILabel coordinatesXLabel;
    private UILabel coordinatesYLabel;
    private UILabel coordinatesZLabel;
    private UILabel worldLabel;

    @Override
    public void construct() {
        guiscreenBackground = false;

        final UIBackgroundContainer overlayContainer = new UIBackgroundContainer(this, UIComponent.INHERITED, UIComponent.INHERITED);
        overlayContainer.setBackgroundAlpha(0);
        overlayContainer.setClipContent(false);

        // Display name
        final UILabel nameLabel = new UILabel(this, mc.player.getDisplayName().getFormattedText());
        nameLabel.setPosition(PADDING, PADDING, Anchor.TOP | Anchor.LEFT);
        nameLabel.setFontOptions(FRO_HUD);

        // Economy
        economyLabel = new UILabel(this, "$0.00");
        economyLabel.setPosition(PADDING, SimpleScreen.getPaddedY(nameLabel, PADDING), Anchor.TOP | Anchor.LEFT);
        economyLabel.setFontOptions(FRO_HUD);

        // Clock
        final UIImage clockImage = new UIImage(this, GuiConstants.TEXTURE_SPRITESHEET, GuiConstants.LEGACY_ICON_CLOCK);
        clockImage.setPosition(PADDING, SimpleScreen.getPaddedY(economyLabel, PADDING), Anchor.TOP | Anchor.LEFT);
        clockImage.setSize(7, 7);

        clockLabel = new UILabel(this, "12:00AM");
        clockLabel.setPosition(SimpleScreen.getPaddedX(clockImage, PADDING), SimpleScreen.getPaddedY(economyLabel, PADDING), clockImage.getAnchor());
        clockLabel.setFontOptions(FRO_HUD);

        // Compass
        final UIBackgroundContainer compassContainer = new UIBackgroundContainer(this, 50, 10);
        compassContainer.setBackgroundAlpha(180);
        compassContainer.setColor(0);
        compassContainer.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        compassLabel = new UILabel(this, "");
        compassLabel.setPosition(1, 1, Anchor.CENTER | Anchor.MIDDLE);
        compassLabel.setFontOptions(FontOptions.builder().from(FRO_HUD).shadow(false).build());

        compassContainer.add(compassLabel);

        // World
        worldLabel = new UILabel(this, "World");
        worldLabel.setPosition(0, SimpleScreen.getPaddedY(compassContainer, PADDING), Anchor.TOP | Anchor.CENTER);
        worldLabel.setFontOptions(FRO_HUD);

        // Coordinates
        coordinatesXLabel = new UILabel(this, (int) mc.player.posX + " X");
        coordinatesXLabel.setPosition(-PADDING, PADDING, Anchor.TOP | Anchor.RIGHT);
        coordinatesXLabel.setFontOptions(FRO_HUD);

        coordinatesYLabel = new UILabel(this, (int) mc.player.posY + " Y");
        coordinatesYLabel.setPosition(-PADDING, SimpleScreen.getPaddedY(coordinatesXLabel, PADDING), Anchor.TOP | Anchor.RIGHT);
        coordinatesYLabel.setFontOptions(FRO_HUD);

        coordinatesZLabel = new UILabel(this, (int) mc.player.posZ + " Z");
        coordinatesZLabel.setPosition(-PADDING, SimpleScreen.getPaddedY(coordinatesYLabel, PADDING), Anchor.TOP | Anchor.RIGHT);
        coordinatesZLabel.setFontOptions(FRO_HUD);

        overlayContainer.add(nameLabel, economyLabel, clockImage, clockLabel, compassContainer, worldLabel, coordinatesXLabel, coordinatesYLabel,
                coordinatesZLabel);

        addToScreen(overlayContainer);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        // Clock
        clockLabel.setText(HUDData.getTime());

        // Compass
        compassLabel.setText(HUDData.getCompass());

        // World
        worldLabel.setText(HUDData.WORLD_NAME);

        // Economy
        economyLabel.setText(HUDData.PLAYER_CURRENCY);

        // Coordinates
        coordinatesXLabel.setText((int) mc.player.posX + " X");
        coordinatesYLabel.setText((int) mc.player.posY + " Y");
        coordinatesZLabel.setText((int) mc.player.posZ + " Z");
    }

    @Override
    public int getOriginBossBarOffsetY() {
        return 25;
    }

    @Override
    public int getTabMenuOffsetY() {
        return 0;
    }

    @Override
    public int getPotionOffsetY() {
        return 0;
    }
}
