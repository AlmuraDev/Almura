package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.shared.client.GuiConfig;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import net.malisis.ego.gui.render.GuiRemoteTexture;
import net.malisis.ego.gui.render.GuiTexture;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public interface IAboutData
{
	public static IAboutData ABOUT_ALMURA = new AboutAlmura();

	public String title();

	public TextFormatting color();

	public String description();

	public GuiTexture texture();

	public class AboutAlmura implements IAboutData
	{
		private final String title = "Almura";
		private final String story = loadStory();
		private final GuiTexture texture = new GuiTexture(GuiConfig.Location.ALMURA_MAN, 23, 23);

		private static String loadStory()
		{
			try
			{
				return Resources.toString(AboutConfig.class.getResource("/assets/almura/text/about/story.txt"), StandardCharsets.UTF_8);
			}
			catch (IOException e)
			{
				return "";
			}
		}

		@Override
		public String title()
		{
			return title;
		}

		@Override
		public TextFormatting color()
		{
			return TextFormatting.WHITE;
		}

		@Override
		public String description()
		{
			return story;
		}

		@Override
		public GuiTexture texture()
		{
			return texture;
		}
	}

	public enum Staff implements IAboutData
	{
		ZIDANE("zidane", UUID.fromString("85271de5-8380-4db5-9f05-ada3b4aa785c"), TextFormatting.BLUE, 3),
		KASHIKE("kashike", UUID.fromString("eb121687-8b1a-4944-bd4d-e0a818d9dfe2"), TextFormatting.GRAY, 2),
		DOCKTER("dockter", UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696"), TextFormatting.GOLD, 3),
		GRINCH("grinch", UUID.fromString("7c104888-df99-4224-a8ba-2c4e15dbc777"), TextFormatting.DARK_GREEN, 3),
		WIFEE("wifee", UUID.fromString("5f757396-8bc7-4dff-8b1f-37fd454a86b7"), TextFormatting.LIGHT_PURPLE, 1),
		CHIMWOLFEYE("chimwolfeye", UUID.fromString("fa1ee43f-8949-41c6-ab61-e50bd864943a"), TextFormatting.RED, 2),
		MUMFREY("mumfrey", UUID.fromString("e8e0361e-9b3b-481a-b06a-5c314a6c1ef0"), TextFormatting.GRAY, 2),
		BLOOD("blood", UUID.fromString("87caf570-b1fc-4100-bd95-3e7f1fa2e153"), TextFormatting.DARK_RED, 2);

		private final String id;
		private final String name;
		private final UUID uuid;
		private final String description;
		private final TextFormatting color;
		private final List<String> titles = Lists.newArrayList();

		Staff(String id, UUID uuid, TextFormatting color, int titles)
		{
			this.id = id;
			name = String.format("almura.menu.about.person.%s.name", id);
			this.uuid = uuid;
			description = String.format("almura.menu.about.person.%s.description", id);
			this.color = color;
			for (int i = 0; i < titles; i++)
				this.titles.add(String.format("almura.menu.about.person.%s.titles.%d", id, i));
		}

		@Override
		public String title()
		{
			return name;
		}

		@Override
		public TextFormatting color()
		{
			return color;
		}

		@Override
		public String description()
		{
			return description;
		}

		@Override
		public GuiTexture texture()
		{
			return new GuiRemoteTexture(String.format(GuiConfig.Url.SKINS, uuid, 32), GuiConfig.Location.GENERIC_AVATAR, 32, 32);
		}

	}
}
