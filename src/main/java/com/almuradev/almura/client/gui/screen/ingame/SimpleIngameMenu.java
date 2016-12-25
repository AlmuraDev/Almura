/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.ingame;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.client.gui.util.builder.UIButtonBuilder;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

import java.awt.AWTException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class SimpleIngameMenu extends SimpleScreen {

    private static final int PADDING = 4;

    @Override
    public void construct() {
        guiscreenBackground = true;

        final UIBackgroundContainer contentContainer = new UIBackgroundContainer(this);
        contentContainer.setBackgroundAlpha(0);
        contentContainer.setSize(220, 202);
        contentContainer.setAnchor(Anchor.MIDDLE | Anchor.CENTER);

        // Almura Header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(Constants.Gui.LOCATION_ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        final UIButton backButton = new UIButtonBuilder(this)
                .text("Back to game")
                .size(220, 20)
                .position(0, SimpleScreen.getPaddedY(almuraHeader, 10))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .container(contentContainer)
                .build("button.back");

        final UIBackgroundContainer shortcutContainer = new UIBackgroundContainer(this);
        shortcutContainer.setBackgroundAlpha(0);
        shortcutContainer.setSize(220, 24);
        shortcutContainer.setPosition(backButton.getX(), SimpleScreen.getPaddedY(backButton, PADDING));
        shortcutContainer.setAnchor(Anchor.TOP | Anchor.CENTER);

        final boolean lanAvaiable = Minecraft.getMinecraft().isSingleplayer() && !Minecraft.getMinecraft()
                .getIntegratedServer().getPublic();
        final boolean guideAvailable = Sponge.getPluginManager().getPlugin("guide").isPresent();

        final UIButton shopButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_SHOPPING_BAG)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Shop"))
                .build("button.shop");

        final UIButton guideButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_BOOK)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(shopButton, PADDING), shopButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Guide"))
                .enabled(guideAvailable)
                .build("button.guide");

        final UIButton mapButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_MAP)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(guideButton, PADDING), guideButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Map"))
                .build("button.instance");

        final UIButton statisticsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_PIE_CHART)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(mapButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Statistics"))
                .build("button.statistics");

        final UIButton achievementsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_TROPHY)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(statisticsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Achievements"))
                .build("button.achievements");

        final UIButton forumsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_ENJIN)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(achievementsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Forums"))
                .build("button.forums");

        final UIButton lanButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_SITEMAP)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(forumsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Open to LAN"))
                .enabled(lanAvaiable)
                .build("button.lan");

        final UIButton optionsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(Constants.Gui.ICON_FA_COG)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(lanButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("Options"))
                .build("button.options");

        final UIButton quitButton = new UIButtonBuilder(this)
                .container(contentContainer)
                .text(Text.of("Quit"))
                .fro(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_LIGHT_RED).shadow(true).build())
                .hoverFro(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(Constants.Gui.BUTTON_WIDTH_SHORT, Constants.Gui.BUTTON_HEIGHT)
                .position(0, SimpleScreen.getPaddedY(shortcutContainer, 25))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.quit");

        contentContainer.add(almuraHeader, shortcutContainer);

        this.addToScreen(contentContainer);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.back":
                this.close();
                break;
            case "button.shop":
                Desktop.getDesktop().browse(new URI(Constants.Gui.SHOP_URL));
                break;
            case "button.guide":
                if (this.mc.player != null) {
                    this.close();
                }
                break;
            case "button.instance":
                Desktop.getDesktop().browse(new URI(Constants.Gui.MAP_URL));
                break;
            case "button.statistics":
                Desktop.getDesktop().browse(new URI(Constants.Gui.STATISTICS_URL));
                break;
            case "button.achievements":
                close();
                if (this.mc.player != null) {
                    this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.player.getStatFileWriter()));
                }
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI(Constants.Gui.FORUM_URL));
                break;
            case "button.lan":
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case "button.options":
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case "button.quit":
                close();
                this.mc.world.sendQuittingDisconnectingPacket();
                this.mc.loadWorld(null);
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
