/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.almuradev.almura.feature.speed.FirstLaunchOptimization;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Inject;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.UIConstants;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.position.Positions;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.background.PanoramicBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GLContext;

@SideOnly(Side.CLIENT)
public class PanoramicMainMenu extends MalisisGui
{
	private static final int PADDING = 4;

	@Inject
	private static MappedConfiguration<ClientConfiguration> configAdapter = null;

	public PanoramicMainMenu()
	{
		renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
	}

	@Override
	public void construct()
	{
		GeneralCategory general = configAdapter.get().general;
		if (general.firstLaunch)
		{
			setupFirstLaunchEnvironment();
			general.firstLaunch = false;
			configAdapter.save();
		}

		setBackground(new PanoramicBackground(screen));

		addToScreen(mainContainer());
		addToScreen(extrasContainer());
		addToScreen(UILabel.builder()
						   .text("{almura.menu.main.trademark}\n{almura.menu.main.copyright}")
						   .color(TextFormatting.YELLOW)
						   .position(l -> Position.bottomLeft(l)
												  .offset(5, -5))
						   .build());

		// OpenGL Warning
		if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported())
		{
			addToScreen(UILabel.builder()
							   .text("{almura.menu.main.opengl.0}\n{almura.menu.main.opengl.1}")
							   .color(TextFormatting.DARK_RED)
							   .bold()
							   .position(l -> Position.topLeft(l)
													  .offset(5, 5))
							   .build());
		}

		//Todo: please tell me there is a better way to do this.
		//Why: the settings.saveOptions() method has a check to see if the game is still loading, thus the "FirstLaunched" settings are
		// not saved when they are initially ran.
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		settings.saveOptions();
		// End stupidity.
	}

	private UIContainer mainContainer()
	{
		UIContainer mainContainer = new UIContainer();
		mainContainer.setPosition(Position.middleCenter(mainContainer)
										  .offset(0, -10));
		mainContainer.setSize(Size.sizeOfContent(mainContainer));
		mainContainer.setPadding(Padding.of(PADDING));
		mainContainer.setClipContent(false);

		// Almura header
		UIImage almuraHeader = UIImage.builder()
									  .parent(mainContainer)
									  .icon(GuiConfig.Icon.ALMURA_LOGO)
									  .position(Position::topCenter)
									  .size(60, 99)
									  .build();

		//singleplayer
		UIButton spButton = UIButton.builder()
									.parent(mainContainer)
									.text("menu.singleplayer")
									.position(b -> Positions.leftAligned(b, PADDING), Positions.below(almuraHeader, PADDING))
									.size(UIConstants.Button.LONG)
									.onClick(() -> mc.displayGuiScreen(new GuiWorldSelection(this)))
									.build();

		//multiplayer
		UIButton mpButton = UIButton.builder()
									.parent(mainContainer)
									.text("menu.multiplayer")
									.position(b -> Position.below(b, spButton, PADDING))
									.size(UIConstants.Button.LONG)
									//.onClick(() -> new ServerMenu(this).display())
									.build();

		//options
		UIButton optionsButton = UIButton.builder()
										 .parent(mainContainer)
										 .text("options.title")
										 .position(b -> Position.below(b, mpButton, PADDING))
										 .size(UIConstants.Button.SHORT)
										 .onClick(() -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)))
										 .build();

		//mods
		UIButton modsButton = UIButton.builder()
									  .parent(mainContainer)
									  .text("almura.menu_button.mods")
									  .position(b -> Position.rightOf(b, optionsButton, PADDING))
									  .size(UIConstants.Button.SHORT)
									  .onClick(() -> mc.displayGuiScreen(new GuiModList(this)))
									  .build();

		//about
		UIButton.builder()
				.parent(mainContainer)
				.text("almura.menu_button.about")
				.position(b -> Position.rightOf(b, modsButton, PADDING))
				.size(UIConstants.Button.SHORT)
				.onClick(() -> new AboutMenu().display())
				.build();

		//quit
		UIButton.builder()
				.parent(mainContainer)
				.text(TextFormatting.RED + "{almura.menu_button.quit}")
				.position(b -> Position.below(b, optionsButton, PADDING))
				.size(UIConstants.Button.LONG)
				.onClick(mc::shutdown)
				.build();

		return mainContainer;
	}

	private UIContainer extrasContainer()
	{
		UIContainer extrasContainer = new UIContainer();
		extrasContainer.setPadding(Padding.of(5));
		extrasContainer.setPosition(Position.bottomRight(extrasContainer));
		extrasContainer.setSize(Size.sizeOfContent(extrasContainer));

		//forums
		UIButton forumsButton = UIButton.builder()
										.parent(extrasContainer)
										.content(new UIImage(GuiConfig.Icon.ENJIN))
										.size(UIConstants.Button.ICON)
										.tooltip("almura.menu_button.forums")
										.onClick(() -> MalisisGui.openLink(GuiConfig.Url.FORUM))
										.build();
		//issues
		UIButton issuesButton = UIButton.builder()
										.parent(extrasContainer)
										.content(new UIImage(GuiConfig.Icon.FA_GITHUB))
										.position(b -> Position.rightOf(b, forumsButton, PADDING))
										.size(UIConstants.Button.ICON)
										.tooltip("almura.menu_button.issues")
										.onClick(() -> MalisisGui.openLink(GuiConfig.Url.ISSUES))
										.build();

		//shop
		UIButton.builder()
				.parent(extrasContainer)
				.content(new UIImage(GuiConfig.Icon.FA_SHOPPING_BAG))
				.position(b -> Position.rightOf(b, issuesButton, PADDING))
				.size(UIConstants.Button.ICON)
				.tooltip("almura.menu_button.shop")
				.onClick(() -> MalisisGui.openLink(GuiConfig.Url.SHOP))
				.build();

		return extrasContainer;
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		if (keyCode == Keyboard.KEY_ESCAPE)
			return;
		super.keyTyped(keyChar, keyCode);
	}

	private void setupFirstLaunchEnvironment()
	{
		FirstLaunchOptimization.optimizeGame();
		ScaledResolution scaledresolution = new ScaledResolution(mc);
		int j = scaledresolution.getScaledWidth();
		int k = scaledresolution.getScaledHeight();
		setWorldAndResolution(mc, j, k);
	}
}
