package com.almuradev.almura.feature.menu.main.component;

import com.almuradev.almura.feature.menu.main.ServerMenu;
import com.almuradev.almura.feature.menu.main.ServerMenu.Server;
import com.almuradev.almura.shared.util.Query;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.UIComponentBuilder;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.decoration.UITooltip;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.shape.GuiShape;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ServerComponent extends UIComponent
{
	private UILabel statusLabel;
	private UILabel playersLabel;

	private final ServerMenu menu;
	private final Server server;
	private final Query query;

	private ServerStatus status = ServerStatus.UPDATING;
	private int playerCount = 0;
	private int maxPlayers = 0;

	public ServerComponent(ServerMenu gui, Server server)
	{
		menu = gui;
		this.server = server;
		query = new Query(server.address, server.port);

		setSize(Size.of(196, 24));

		setBackground(GuiShape.builder(this)
							  .border(1, 0x808080)
							  .build());

		int y = 4;
		UILabel titleLabel = UILabel.builder()
									.parent(this)
									.text(server.name)
									.middleLeft(4, 0)
									.color(TextFormatting.WHITE)
									.shadow()
									.build();

		statusLabel = UILabel.builder()
							 .parent(this)
							 .text(() -> status == ServerStatus.ONLINE ? playerCount + "/" + maxPlayers : status.toString())
							 .color(TextFormatting.WHITE)
							 .shadow()
							 .middleRight(9, 0)
							 .build();

		GuiShape indicator = GuiShape.builder(this)
									 .rightAligned(1)
									 .y(1)
									 .size(5, 22)
									 .color(() -> status.color)
									 .build();

		setForeground(titleLabel.and(statusLabel)
								.and(indicator));

		onLeftClick(e -> {
			menu.select(this);
			return true;
		});
		onDoubleClick(e -> {
			connect();
			return true;
		});
	}

	public boolean isOnline()
	{
		return status == ServerStatus.ONLINE;
	}

	@Override
	public int getColor()
	{
		if (menu.isSelected(this))
			return 0x414141;
		return isHovered() ? 0x282828 : 0x000000;
	}

	public void connect()
	{
		if (!isOnline())
			return;

		ServerData data = new ServerData(name, server.address + ":" + server.port, false);
		FMLClientHandler.instance()
						.setupServerList();
		FMLClientHandler.instance()
						.connectToServer(MalisisGui.current(), data);
	}

	public void query()
	{
		if (!query.pingServer())
		{
			status = ServerStatus.OFFLINE;
			playerCount = 0;
			maxPlayers = 0;
			setTooltip((UITooltip) null);
			return;
		}

		query.sendQuery();

		if (query.getPlayers() == null || query.getMaxPlayers() == null)
		{
			status = ServerStatus.RESTARTING;
			playerCount = 0;
			maxPlayers = 0;
			setTooltip((UITooltip) null);
			return;
		}

		status = ServerStatus.ONLINE;
		playerCount = Integer.valueOf(query.getPlayers());
		maxPlayers = Integer.valueOf(query.getMaxPlayers());
		setTooltip(query.getMotd());
		if (menu.isSelected(this))
			menu.select(this);
	}

	public static ServerComponentBuilder builder(ServerMenu gui, Server server)
	{
		return new ServerComponentBuilder(gui, server);
	}

	public static class ServerComponentBuilder extends UIComponentBuilder<ServerComponentBuilder, ServerComponent>
	{
		private final ServerMenu gui;
		private final Server server;

		public ServerComponentBuilder(ServerMenu gui, Server server)
		{
			this.gui = gui;
			this.server = server;
			name(server.name);
		}

		@Override
		public ServerComponent build()
		{
			return build(new ServerComponent(gui, server));
		}
	}

	private enum ServerStatus
	{
		OFFLINE("almura.multiplayer.status.offline", TextFormatting.RED, 0xFF5555),
		ONLINE("almura.multiplayer.status.online", TextFormatting.GREEN, 0x055FF55),
		RESTARTING("almura.multiplayer.status.restarting", TextFormatting.YELLOW, 0xFFFFAA),
		UPDATING("almura.multiplayer.status.updating", TextFormatting.DARK_PURPLE, 0xAA00AA);

		private final String translationKey;
		private final TextFormatting textFormatting;
		private final int color;

		ServerStatus(String translationKey, TextFormatting textFormatting, int color)
		{
			this.translationKey = translationKey;
			this.textFormatting = textFormatting;
			this.color = color;
		}

		@Override
		public String toString()
		{
			return textFormatting + "{" + translationKey + "}";
		}
	}
}
