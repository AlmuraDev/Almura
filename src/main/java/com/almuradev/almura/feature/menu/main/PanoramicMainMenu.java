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
import net.malisis.ego.gui.render.background.DirtBackground;
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

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class PanoramicMainMenu extends MalisisGui
{
	private static final int PADDING = 4;

	@Inject
	private static MappedConfiguration<ClientConfiguration> configAdapter = null;

	public PanoramicMainMenu(@Nullable MalisisGui parent)
	{
		super();
		renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
		setBackground(new DirtBackground(getScreen()));
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

		UIContainer mainContainer = mainContainer();
		UIContainer extrasContainer = extrasContainer();

		UILabel tmLabel = new UILabel(TextFormatting.YELLOW + "{almura.menu.main.trademark}\n{almura.menu.main.copyright}");
		tmLabel.setPosition(Position.bottomLeft(tmLabel).offset(5, -5));

		setBackground(new PanoramicBackground(getScreen()));

		addToScreen(mainContainer);
		addToScreen(extrasContainer);
		addToScreen(tmLabel);

		// Disable escape key press

		// OpenGL Warning
		if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported())
		{
			UILabel glWarning = new UILabel(
					"" + TextFormatting.BOLD + TextFormatting.DARK_RED + "{almura.menu.main.opengl.0}\n{almura.menu.main.opengl.1}");
			glWarning.setPosition(Position.topLeft(glWarning).offset(5, 5));
			addToScreen(glWarning);
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
		mainContainer.setPosition(Position.middleCenter(mainContainer).offset(0, -10));
		mainContainer.setSize(Size.sizeOfContent(mainContainer));
		mainContainer.setPadding(Padding.of(PADDING));

		// Almura header
		UIImage almuraHeader = new UIImage(GuiConfig.Icon.ALMURA_LOGO);
		almuraHeader.setPosition(Position.topCenter(almuraHeader));
		almuraHeader.setSize(Size.of(60, 99));
		mainContainer.add(almuraHeader);

		//singleplayer
		UIButton spButton = new UIButton();
		spButton.setText("menu.singleplayer");
		spButton.setPosition(Position.of(Positions.leftAligned(spButton, PADDING), Positions.below(almuraHeader, PADDING)));
		spButton.setSize(UIConstants.Button.LONG);
		spButton.onClick(() -> mc.displayGuiScreen(new GuiWorldSelection(this)));
		mainContainer.add(spButton);

		//multiplayer
		UIButton mpButton = new UIButton();
		mpButton.setText("menu.multiplayer");
		mpButton.setPosition(Position.below(mpButton, spButton, PADDING));
		mpButton.setSize(UIConstants.Button.LONG);
		//mpButton.click(() -> new ServerMenu(this).display());
		mainContainer.add(mpButton);

		//options
		UIButton optionsButton = new UIButton();
		optionsButton.setText("options.title");
		optionsButton.setPosition(Position.below(optionsButton, mpButton, PADDING));
		optionsButton.setSize(UIConstants.Button.SHORT);
		optionsButton.onClick(() -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)));
		mainContainer.add(optionsButton);

		//mods
		UIButton modsButton = new UIButton();
		modsButton.setText("almura.menu_button.mods");
		modsButton.setPosition(Position.rightOf(modsButton, optionsButton, PADDING));
		modsButton.setSize(UIConstants.Button.SHORT);
		modsButton.onClick(() -> mc.displayGuiScreen(new GuiModList(this)));
		mainContainer.add(modsButton);

		//about
		UIButton aboutButton = new UIButton();
		aboutButton.setText("almura.menu_button.about");
		aboutButton.setPosition(Position.rightOf(aboutButton, modsButton, PADDING));
		aboutButton.setSize(UIConstants.Button.SHORT);
		aboutButton.onClick(() -> new AboutMenu().display());
		mainContainer.add(aboutButton);

		//quit
		UIButton quitButton = new UIButton();
		quitButton.setText(TextFormatting.RED + "{almura.menu_button.quit}");
		quitButton.setPosition(Position.below(quitButton, optionsButton, PADDING));
		quitButton.setSize(UIConstants.Button.LONG);
		quitButton.onClick(mc::shutdown);
		mainContainer.add(quitButton);

		return mainContainer;
	}

	private UIContainer extrasContainer()
	{
		UIContainer extrasContainer = new UIContainer();
		extrasContainer.setPadding(Padding.of(5));
		extrasContainer.setPosition(Position.bottomRight(extrasContainer));
		extrasContainer.setSize(Size.sizeOfContent(extrasContainer));

		UIButton forumsButton = new UIButton();
		forumsButton.setContent(new UIImage(GuiConfig.Icon.ENJIN));
		forumsButton.setSize(UIConstants.Button.ICON);
		forumsButton.setTooltip("almura.menu_button.forums");
		forumsButton.onClick(() -> MalisisGui.openLink(GuiConfig.Url.FORUM));
		extrasContainer.add(forumsButton);

		UIButton issuesButton = new UIButton();
		issuesButton.setContent(new UIImage(GuiConfig.Icon.FA_GITHUB));
		issuesButton.setPosition(Position.rightOf(issuesButton, forumsButton, PADDING));
		issuesButton.setSize(UIConstants.Button.ICON);
		issuesButton.setTooltip("almura.menu_button.issues");
		issuesButton.onClick(() -> MalisisGui.openLink(GuiConfig.Url.ISSUES));
		extrasContainer.add(issuesButton);

		UIButton shopButton = new UIButton();
		shopButton.setContent(new UIImage(GuiConfig.Icon.FA_SHOPPING_BAG));
		shopButton.setPosition(Position.rightOf(shopButton, issuesButton, PADDING));
		shopButton.setSize(UIConstants.Button.ICON);
		shopButton.setTooltip("almura.menu_button.shop");
		shopButton.onClick(() -> MalisisGui.openLink(GuiConfig.Url.SHOP));
		extrasContainer.add(shopButton);

		return extrasContainer;
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		if(keyCode == Keyboard.KEY_ESCAPE)
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
