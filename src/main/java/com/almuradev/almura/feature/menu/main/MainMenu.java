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
import com.almuradev.almura.feature.menu.component.CommonComponents;
import com.almuradev.almura.feature.menu.main.component.ExtrasComponent;
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
public class MainMenu extends MalisisGui
{
	@Inject
	private static MappedConfiguration<ClientConfiguration> configAdapter = null;

	public MainMenu()
	{
	}

	@Override
	public void construct()
	{
		renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
		GeneralCategory general = configAdapter.get().general;
		if (general.firstLaunch)
		{
			setupFirstLaunchEnvironment();
			general.firstLaunch = false;
			configAdapter.save();
		}

		setBackground(new PanoramicBackground(screen));

		addToScreen(mainContainer());
		addToScreen(new ExtrasComponent());

		// OpenGL Warning
		if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported())
		{
			addToScreen(UILabel.builder()
							   .text("{almura.menu.main.opengl.0}\n{almura.menu.main.opengl.1}")
							   .textColor(TextFormatting.DARK_RED)
							   .bold()
							   .topLeft(GuiConfig.PADDING, GuiConfig.PADDING)
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
		UIContainer mainContainer = UIContainer.builder()
											   .middleCenter(0, -10)
											   .size(Size::sizeOfContent)
											   .padding(GuiConfig.PADDING)
											   .noClipContent()
											   .build();

		// Almura header
		UIImage almuraHeader = CommonComponents.almuraHeader(mainContainer)
											   .build();

		//singleplayer
		UIButton spButton = UIButton.builder()
									.parent(mainContainer)
									.text("menu.singleplayer")
									.leftAligned()
									.below(almuraHeader, GuiConfig.PADDING)
									.size(UIConstants.Button.DEFAULT)
									.onClick(() -> mc.displayGuiScreen(new GuiWorldSelection(this)))
									.build();

		//multiplayer
		UIButton mpButton = UIButton.builder()
									.parent(mainContainer)
									.text("menu.multiplayer")
									.leftAligned()
									.below(spButton, GuiConfig.PADDING)
									.size(UIConstants.Button.DEFAULT)
									.onClick(() -> new ServerMenu().display())
									.build();

		//options
		UIButton optionsButton = UIButton.builder()
										 .parent(mainContainer)
										 .text("options.title")
										 .leftAligned()
										 .below(mpButton, GuiConfig.PADDING)
										 .size(UIConstants.Button.THIRD)
										 .onClick(() -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)))
										 .build();

		//mods
		UIButton modsButton = UIButton.builder()
									  .parent(mainContainer)
									  .text("almura.menu_button.mods")
									  .rightOf(optionsButton, GuiConfig.PADDING)
									  .topAlignedTo(optionsButton)
									  .size(UIConstants.Button.THIRD)
									  .onClick(() -> mc.displayGuiScreen(new GuiModList(this)))
									  .build();

		//about
		UIButton.builder()
				.parent(mainContainer)
				.text("almura.menu_button.about")
				.rightOf(modsButton, GuiConfig.PADDING)
				.topAlignedTo(optionsButton)
				.size(UIConstants.Button.THIRD)
				.onClick(() -> new AboutMenu().display())
				.build();

		//quit
		UIButton.builder()
				.parent(mainContainer)
				.text(TextFormatting.RED + "{almura.menu_button.quit}")
				.leftAligned()
				.below(optionsButton, GuiConfig.PADDING)
				.size(UIConstants.Button.DEFAULT)
				.onClick(mc::shutdown)
				.build();

		return mainContainer;
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
