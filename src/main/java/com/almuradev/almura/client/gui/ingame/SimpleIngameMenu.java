/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.util.builders.FontRenderOptionsBuilder;
import com.almuradev.almura.client.gui.util.builders.UIButtonBuilder;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Color;

import java.awt.AWTException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class SimpleIngameMenu extends SimpleGui {

    private static final String MAP_URL = "http://srv1.almuramc.com:8123";
    private static final String STATISTICS_URL = "http://srv1.almuramc.com:8080";
    private static final String FORUM_URL = "http://www.almuramc.com";

    @Override
    public void construct() {
        guiscreenBackground = true;

        final int padding = 4;

        final UIBackgroundContainer contentContainer = new UIBackgroundContainer(this);
        contentContainer.setBackgroundAlpha(0);
        contentContainer.setSize(220, 202);
        contentContainer.setAnchor(Anchor.MIDDLE | Anchor.CENTER);

        // Almura Header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(ALMURA_HEADER_LOCATION), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        final UIButton backButton = new UIButtonBuilder(this)
                .name("button.back")
                .text("Back to game")
                .size(220, 20)
                .position(0, SimpleGui.getPaddedY(almuraHeader, 10))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();

        final UIBackgroundContainer shortcutContainer = new UIBackgroundContainer(this);
        shortcutContainer.setBackgroundAlpha(0);
        shortcutContainer.setSize(220, 24);
        shortcutContainer.setPosition(backButton.getX(), SimpleGui.getPaddedY(backButton, padding));
        shortcutContainer.setAnchor(Anchor.TOP | Anchor.CENTER);

        final boolean lanAvaiable = Minecraft.getMinecraft().isSingleplayer() && !Minecraft.getMinecraft()
                .getIntegratedServer().getPublic();
        final boolean guideAvailable = Sponge.getPluginManager().getPlugin("guide").isPresent();

        final UIButton shopButton = new UIButtonBuilder(this)
                .name("button.shop")
                .image(SimpleGui.ICON_FA_SHOPPING_BAG)
                .size(24, 24)
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Shop", 15))
                .build();

        final UIButton guideButton = new UIButtonBuilder(this)
                .name("button.guide")
                .image(SimpleGui.ICON_FA_BOOK)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(shopButton, padding), shopButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Guide", 15))
                .enabled(guideAvailable)
                .build();

        final UIButton mapButton = new UIButtonBuilder(this)
                .name("button.map")
                .image(SimpleGui.ICON_FA_MAP)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(guideButton, padding), guideButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Map", 15))
                .build();

        final UIButton statisticsButton = new UIButtonBuilder(this)
                .name("button.statistics")
                .image(SimpleGui.ICON_FA_PIE_CHART)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(mapButton, padding), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Statistics", 15))
                .build();

        final UIButton achievementsButton = new UIButtonBuilder(this)
                .name("button.achievements")
                .image(SimpleGui.ICON_FA_TROPHY)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(statisticsButton, padding), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Achievements", 15))
                .build();

        final UIButton forumsButton = new UIButtonBuilder(this)
                .name("button.forums")
                .image(SimpleGui.ICON_FORUM)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(achievementsButton, padding), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Forums", 15))
                .build();

        final UIButton lanButton = new UIButtonBuilder(this)
                .name("button.lan")
                .image(SimpleGui.ICON_FA_SITEMAP)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(forumsButton, padding), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Open to LAN", 15))
                .enabled(lanAvaiable)
                .build();

        final UIButton optionsButton = new UIButtonBuilder(this)
                .name("button.options")
                .image(SimpleGui.ICON_FA_COG)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(lanButton, padding), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(new UITooltip(this, "Options", 15))
                .build();

        final UIButton quitButton = new UIButtonBuilder(this)
                .name("button.quit")
                .text("Quit")
                .fro(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_RED).shadow(true).build())
                .hoverFro(FontRenderOptionsBuilder.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(98, 20)
                .position(0, SimpleGui.getPaddedY(shortcutContainer, 25))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();

        shortcutContainer.add(shopButton, guideButton, mapButton, statisticsButton, achievementsButton, forumsButton, lanButton, optionsButton);
        contentContainer.add(almuraHeader, backButton, shortcutContainer, quitButton);

        this.addToScreen(contentContainer);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.back":
                close();
                break;
            case "button.shop":
                // TODO: Shopping URL
                //Desktop.getDesktop().browse(new URI(""));
                break;
            case "button.guide":
                if (this.mc.thePlayer != null) {
                    close();
                }
                break;
            case "button.map":
                Desktop.getDesktop().browse(new URI(MAP_URL));
                break;
            case "button.statistics":
                Desktop.getDesktop().browse(new URI(STATISTICS_URL));
                break;
            case "button.achievements":
                close();
                if (this.mc.thePlayer != null) {
                    this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                }
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI(FORUM_URL));
                break;
            case "button.lan":
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;
            case "button.options":
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case "button.quit":
                close();
                this.mc.theWorld.sendQuittingDisconnectingPacket();
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
