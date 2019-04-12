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
import net.malisis.ego.gui.UIConstants;
import net.malisis.ego.gui.UIConstants.Button;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.background.PanoramicBackground;
import net.malisis.ego.gui.render.shape.GuiShape;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public class ServerMenu extends MalisisGui
{

	public enum Server
	{
		LIVE("almura.menu.server.live.name", "srv1.almuramc.com", 25566),
		DEV("almura.menu.server.dev.name", "dev.almuramc.com", 25566);

		public String name;
		public String address;
		public int port;

		Server(String name, String address, int port)
		{
			this.name = name;
			this.address = address;
			this.port = port;
		}
	}

	private UIButton joinButton;
	private ScheduledExecutorService executor;
	private ServerComponent selected;

	@Override
	public void construct()
	{
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

		UIContainer servers = UIContainer.builder()
										 .parent(mainContainer)
										 .leftAligned()
										 .below(almuraHeader, GuiConfig.PADDING)
										 .size(UIConstants.Button.WIDTH, 53)
										 .padding(2)
										 .background((UIContainer c) -> GuiShape.builder(c)
																				.color(0x000000)
																				.alpha(185)
																				.border(1, 0xFFFFFF, 185)
																				.build())
										 .build();

		ServerComponent publicServer = ServerComponent.builder(this, Server.LIVE)
													  .parent(servers)
													  .topLeft()
													  .build();

		ServerComponent devServer = ServerComponent.builder(this, Server.DEV)
												   .parent(servers)
												   .leftAligned()
												   .below(publicServer, 1)
												   .build();

		joinButton = UIButton.builder()
							 .parent(mainContainer)
							 .text("almura.menu_button.join")
							 .leftAligned()
							 .below(servers, GuiConfig.PADDING)
							 .size(Button.DEFAULT)
							 .enabled(false)
							 .onClick(() -> {
								 if (selected != null)
									 selected.connect();
							 })
							 .build();

		UIButton back = UIButton.builder()
								.parent(mainContainer)
								.text("gui.back")
								.leftAligned()
								.below(joinButton, GuiConfig.PADDING)
								.size(Button.HALF)
								.onClick(this::close)
								.build();

		UIButton.builder()
				.parent(mainContainer)
				.text("almura.menu_button.other")
				.rightOf(back, GuiConfig.PADDING)
				.below(joinButton, GuiConfig.PADDING)
				.size(Button.HALF)
				.onClick(() -> mc.displayGuiScreen(new GuiMultiplayer(this)))
				.build();

		addToScreen(mainContainer);
		addToScreen(new ExtrasComponent());

		if (executor != null)//reconstructed
			executor.shutdown();
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			publicServer.query();
			devServer.query();
		}, 0, 3, TimeUnit.SECONDS);

		select(publicServer);
	}

	public void select(ServerComponent component)
	{
		selected = component;
		joinButton.setEnabled(component.isOnline());
	}

	public boolean isSelected(ServerComponent component)
	{
		return component == selected;
	}

	@Override
	public void close()
	{
		executor.shutdown();
		super.close();
	}
}


