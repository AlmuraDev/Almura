/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.game;

import com.almuradev.almura.shared.client.GuiConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public final class SimpleIngameMenu extends BasicScreen
{

	private static final int PADDING = 4;

	@Override
	public void construct()
	{
		renderer.setDefaultTexture(null /*GuiConfig.SpriteSheet.ALMURA*/);

		guiscreenBackground = true;

		BasicContainer<?> contentContainer = new BasicContainer<>(this);
		contentContainer.setBackgroundAlpha(0);
		contentContainer.setSize(220, 202);
		contentContainer.setAnchor(Anchor.MIDDLE | Anchor.CENTER);

		// Almura Header
		UIImage almuraHeader = new UIImage(this, new GuiTexture((ResourceLocation) null /*GuiConfig.Location.ALMURA_LOGO*/), null);
		almuraHeader.setSize(60, 99);
		almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

		UIButton backButton = new UIButtonBuilder(this).text(I18n.format("menu.returnToGame"))
													   .size(192, 20)
													   .position(0, BasicScreen.getPaddedY(almuraHeader, 10))
													   .anchor(Anchor.TOP | Anchor.CENTER)
													   .listener(this)
													   .container(contentContainer)
													   .build("button.back");

		BasicContainer<?> shortcutContainer = new BasicContainer<>(this);
		shortcutContainer.setBackgroundAlpha(0);
		shortcutContainer.setSize(192, 24);
		shortcutContainer.setPosition(backButton.getX(), BasicScreen.getPaddedY(backButton, PADDING));
		shortcutContainer.setAnchor(Anchor.TOP | Anchor.CENTER);

		boolean lanAvaiable = Minecraft.getMinecraft().isSingleplayer() && !Minecraft.getMinecraft().getIntegratedServer().getPublic();

		UIButton shopButton = new UIButtonBuilder(this).container(shortcutContainer)
													   // .icon(GuiConfig.Icon.FA_SHOPPING_BAG)
													   .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
													   .anchor(Anchor.MIDDLE | Anchor.LEFT)
													   .listener(this)
													   .tooltip(I18n.format("almura.menu_button.shop"))
													   .build("button.shop");

		UIButton mapButton = new UIButtonBuilder(this).container(shortcutContainer)
													  //     .icon(GuiConfig.Icon.FA_MAP)
													  .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
													  .position(BasicScreen.getPaddedX(shopButton, PADDING), shopButton.getY())
													  .anchor(Anchor.MIDDLE | Anchor.LEFT)
													  .listener(this)
													  .tooltip(I18n.format("item.map.name"))
													  .build("button.instance");

		UIButton statisticsButton = new UIButtonBuilder(this).container(shortcutContainer)
															 // .icon(GuiConfig.Icon.FA_PIE_CHART)
															 .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
															 .position(BasicScreen.getPaddedX(mapButton, PADDING), mapButton.getY())
															 .anchor(Anchor.MIDDLE | Anchor.LEFT)
															 .listener(this)
															 .tooltip(I18n.format("gui.stats"))
															 .build("button.statistics");

		UIButton advancementsButton = new UIButtonBuilder(this).container(shortcutContainer)
															   //  .icon(GuiConfig.Icon.FA_TROPHY)
															   .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
															   .position(BasicScreen.getPaddedX(statisticsButton, PADDING),
																		 mapButton.getY())
															   .anchor(Anchor.MIDDLE | Anchor.LEFT)
															   .listener(this)
															   .tooltip(I18n.format("gui.advancements"))
															   .build("button.advancements");

		UIButton forumsButton = new UIButtonBuilder(this).container(shortcutContainer)
														 //  .icon(GuiConfig.Icon.ENJIN)
														 .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
														 .position(BasicScreen.getPaddedX(advancementsButton, PADDING), mapButton.getY())
														 .anchor(Anchor.MIDDLE | Anchor.LEFT)
														 .listener(this)
														 .tooltip(I18n.format("almura.menu_button.forums"))
														 .build("button.forums");

		UIButton lanButton = new UIButtonBuilder(this).container(shortcutContainer)
													  //  .icon(GuiConfig.Icon.FA_SITEMAP)
													  .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
													  .position(BasicScreen.getPaddedX(forumsButton, PADDING), mapButton.getY())
													  .anchor(Anchor.MIDDLE | Anchor.LEFT)
													  .listener(this)
													  .tooltip(I18n.format("menu.shareToLan"))
													  .enabled(lanAvaiable)
													  .build("button.lan");

		UIButton optionsButton = new UIButtonBuilder(this).container(shortcutContainer)
														  //    .icon(GuiConfig.Icon.FA_COG)
														  .size(UIConstants.Button.WIDTH_ICON, UIConstants.Button.HEIGHT_ICON)
														  .position(BasicScreen.getPaddedX(lanButton, PADDING), mapButton.getY())
														  .anchor(Anchor.MIDDLE | Anchor.LEFT)
														  .listener(this)
														  .tooltip(I18n.format("menu.options"))
														  .build("button.options");

		UIButton quitButton = new UIButtonBuilder(this).container(contentContainer)
													   .text(I18n.format("almura.menu_button.quit"))
													   .fontOptions(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
													   .hoverFontOptions(FontOptions.builder()
																					.color(Color.ofRgb(255, 89, 89).getRgb())
																					.shadow(true)
																					.build())
													   .size(UIConstants.Button.WIDTH_SHORT, UIConstants.Button.HEIGHT)
													   .position(0, BasicScreen.getPaddedY(shortcutContainer, 25))
													   .anchor(Anchor.TOP | Anchor.CENTER)
													   .listener(this)
													   .build("button.quit");

		contentContainer.add(almuraHeader, shortcutContainer);

		addToScreen(contentContainer);
	}

	@Subscribe
	public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException
	{
		switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH))
		{
			case "button.back":
				close();
				break;
			case "button.shop":
				Desktop.getDesktop().browse(new URI(GuiConfig.Url.SHOP));
				break;
			case "button.guide":
				if (mc.player != null)
				{
					close();
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
				if (mc.player != null)
				{
					mc.displayGuiScreen(new GuiScreenAdvancements(mc.player.connection.getAdvancementManager()));
				}
				break;
			case "button.forums":
				Desktop.getDesktop().browse(new URI(GuiConfig.Url.FORUM));
				break;
			case "button.lan":
				mc.displayGuiScreen(new GuiShareToLan(this));
				break;
			case "button.options":
				mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
				break;
			case "button.quit":
				close();
				mc.world.sendQuittingDisconnectingPacket();
				mc.loadWorld(null);
				mc.displayGuiScreen(new GuiMainMenu());
				break;
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return true;
	}
}
