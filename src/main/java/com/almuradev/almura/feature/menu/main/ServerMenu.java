/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.feature.menu.main.component.ExtrasComponent;
import com.almuradev.almura.feature.menu.main.component.ServerComponent;
import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.UIConstants.Button;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.background.PanoramicBackground;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public class ServerMenu extends MalisisGui
{
	private ScheduledFuture<?> future;

	@Override
	public void construct()
	{
		//if reconstructed
		if (future != null)
			future.cancel(true);
		showParentOnClose = true;
		renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);
		setBackground(new PanoramicBackground(screen));

		UIContainer mainContainer = UIContainer.builder()
											   .centered()
											   .middleAligned(-10)
											   .size(Size::sizeOfContent)
											   .padding(GuiConfig.PADDING)
											   .noClipContent()
											   .build();

		UIImage almuraHeader = UIImage.builder()
									  .parent(mainContainer)
									  .icon(GuiConfig.Icon.ALMURA_LOGO)
									  .position(Position::topCenter)
									  .size(60, 99)
									  .build();

		ServerComponent publicServer = ServerComponent.builder("Public server")
													  .address("srv1.almuramc.com")
													  .port(25566)
													  .parent(mainContainer)
													  .leftAligned()
													  .below(almuraHeader, GuiConfig.PADDING * 2)
													  .build();

		ServerComponent devServer = ServerComponent.builder("Dev server")
												   .address("dev.almuramc.com")
												   .port(25566)
												   .parent(mainContainer)
												   .leftAligned()
												   .below(publicServer, GuiConfig.PADDING)
												   .build();

		UIButton otherMP = UIButton.builder()
								   .parent(mainContainer)
								   .text("Other servers")
								   .centered()
								   .below(devServer, GuiConfig.PADDING * 2)
								   .size(Button.LONG)
								   .onClick(() -> mc.displayGuiScreen(new GuiMultiplayer(this)))
								   .build();

		UIButton back = UIButton.builder()
								.parent(mainContainer)
								.text("Back")
								.centered()
								.below(otherMP, GuiConfig.PADDING)
								.size(Button.LONG)
								.onClick(this::close)
								.build();

		addToScreen(mainContainer);
		addToScreen(new ExtrasComponent());

		future = Executors.newSingleThreadScheduledExecutor()
						  .scheduleAtFixedRate(() -> {
							  publicServer.query();
							  devServer.query();
						  }, 0, 3, TimeUnit.SECONDS);
	}

	@Override
	public void close()
	{
		if (future != null)
			future.cancel(true);
		super.close();
	}
}


