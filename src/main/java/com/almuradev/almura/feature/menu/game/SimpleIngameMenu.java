/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.menu.game;

import com.almuradev.shared.client.GuiConfig;
import com.almuradev.shared.client.ui.FontColors;
import com.almuradev.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.shared.client.ui.screen.SimpleScreen;
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
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.resources.I18n;
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
        this.guiscreenBackground = true;

        final UIBackgroundContainer contentContainer = new UIBackgroundContainer(this);
        contentContainer.setBackgroundAlpha(0);
        contentContainer.setSize(220, 202);
        contentContainer.setAnchor(Anchor.MIDDLE | Anchor.CENTER);

        // Almura Header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        final UIButton backButton = new UIButtonBuilder(this)
                .text(I18n.format("menu.returnToGame"))
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
                .icon(GuiConfig.Icon.FA_SHOPPING_BAG)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("almura.menu.shop")))
                .build("button.shop");

        final UIButton guideButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_BOOK)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(shopButton, PADDING), shopButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("almura.menu.guide")))
                .enabled(guideAvailable)
                .build("button.guide");

        final UIButton mapButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_MAP)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(guideButton, PADDING), guideButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("item.map.name")))
                .build("button.instance");

        final UIButton statisticsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_PIE_CHART)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(mapButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("gui.stats")))
                .build("button.statistics");

        final UIButton advancementsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_TROPHY)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(statisticsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of("gui.advancements"))
                .build("button.advancements");

        final UIButton forumsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.ENJIN)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(advancementsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("almura.menu.forums")))
                .build("button.forums");

        final UIButton lanButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_SITEMAP)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(forumsButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("menu.shareToLan")))
                .enabled(lanAvaiable)
                .build("button.lan");

        final UIButton optionsButton = new UIButtonBuilder(this)
                .container(shortcutContainer)
                .icon(GuiConfig.Icon.FA_COG)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(lanButton, PADDING), mapButton.getY())
                .anchor(Anchor.MIDDLE | Anchor.LEFT)
                .listener(this)
                .tooltip(Text.of(I18n.format("menu.options")))
                .build("button.options");

        final UIButton quitButton = new UIButtonBuilder(this)
                .container(contentContainer)
                .text(Text.of(I18n.format("almura.menu.quit")))
                .fro(FontOptions.builder().from(FontColors.FRO_LIGHT_RED).shadow(true).build())
                .hoverFro(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(GuiConfig.Button.WIDTH_SHORT, GuiConfig.Button.HEIGHT)
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
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.SHOP));
                break;
            case "button.guide":
                if (this.mc.player != null) {
                    this.close();
                }
                break;
            case "button.instance":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.MAP));
                break;
            case "button.statistics":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.STATISTICS));
                break;
            case "button.advancements":
                close();
                if (this.mc.player != null) {
                    this.mc.displayGuiScreen(new GuiScreenAdvancements(this.mc.player.connection.getAdvancementManager()));
                }
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.FORUM));
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
