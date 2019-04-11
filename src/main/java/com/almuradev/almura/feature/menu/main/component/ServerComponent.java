package com.almuradev.almura.feature.menu.main.component;

import com.almuradev.almura.shared.util.Query;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponentBuilder;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.size.Size;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;

public class ServerComponent extends UIContainer
{
	private UILabel titleLabel;
	private UILabel statusLabel;
	private UIButton joinButton;
	private UILabel playersLabel;
	private UILabel ping;

	private final ServerData serverConnect;
	private final Query query;

	private String status = TextFormatting.YELLOW + "Updating...";
	private int playerCount = 0;
	private int maxPlayers = 0;

	protected ServerComponent(String name, String address, int port)
	{
		serverConnect = new ServerData(name, address + ":" + port, false);
		query = new Query(new ServerData(name, address, false), port);

		setSize(Size.of(240, 16));

		int y = 4;
		titleLabel = UILabel.builder()
							.parent(this)
							.text(name + " :")
							.position(0, y)
							.color(TextFormatting.WHITE)
							.shadow()
							.build();

		statusLabel = UILabel.builder()
							 .parent(this)
							 .text(() -> status)
							 .shadow()
							 .position(90, y)
							 .build();

		joinButton = UIButton.builder()
							 .parent(this)
							 .text("Join")
							 .position(150, 0)
							 .size(40, 16)
							 .onClick(this::connect)
							 .visible(false)
							 .build();

		playersLabel = UILabel.builder()
							  .parent(this)
							  .text("({PLAYER_COUNT}/{MAX_PLAYERS})")
							  .color(TextFormatting.DARK_BLUE)
							  .bind("PLAYER_COUNT", () -> playerCount)
							  .bind("MAX_PLAYERS", () -> maxPlayers)
							  .position(200, y)
							  .visible(false)
							  .build();

		statusLabel.setTooltip((String) null);

	}

	public void deactivate()
	{
		playerCount = 0;
		maxPlayers = 0;
		joinButton.setVisible(false);
		playersLabel.setVisible(false);
	}

	public void connect()
	{
		FMLClientHandler.instance()
						.setupServerList();
		FMLClientHandler.instance()
						.connectToServer(MalisisGui.current(), serverConnect);
	}

	public void query()
	{
		statusLabel.setTooltip((String) null);

		if (!query.pingServer())
		{
			status = TextFormatting.RED + "Offline";
			deactivate();
			return;
		}

		try
		{
			query.sendQuery();
		}
		catch (IOException e)
		{
			status = TextFormatting.ITALIC + (TextFormatting.RED + "Query failed");
			statusLabel.setTooltip(e.getMessage());
			deactivate();
			return;
		}

		if (query.getPlayers() == null || query.getMaxPlayers() == null)
		{
			status = TextFormatting.YELLOW + "Restarting...";
			deactivate();
			return;
		}

		status = TextFormatting.GREEN + "Online";
		playerCount = Integer.valueOf(query.getPlayers());
		maxPlayers = Integer.valueOf(query.getMaxPlayers());
		joinButton.setVisible(true);
		playersLabel.setVisible(true);
	}

	public static ServerComponentBuilder builder(String name)
	{
		return new ServerComponentBuilder().name(name);
	}

	public static class ServerComponentBuilder extends UIComponentBuilder<ServerComponentBuilder, ServerComponent>
	{
		private String address;
		private int port;

		public ServerComponentBuilder address(String address)
		{
			this.address = address;
			return this;
		}

		public ServerComponentBuilder port(int port)
		{
			this.port = port;
			return this;
		}

		@Override
		public ServerComponent build()
		{
			return build(new ServerComponent(name, address, port));
		}
	}
}
