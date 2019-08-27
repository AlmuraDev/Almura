package com.almuradev.almura.feature.menu.component;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.GuiConfig.Icon;
import com.almuradev.almura.shared.client.GuiConfig.Url;
import net.malisis.ego.gui.UIConstants.Button;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UIImage.UIImageBuilder;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UIButton.UIButtonBuilder;
import net.malisis.ego.gui.render.GuiIcon;

public class CommonComponents
{
	public static UIImageBuilder almuraHeader(UIContainer parent)
	{
		return UIImage.builder()
					  .parent(parent)
					  .icon(Icon.ALMURA_LOGO)
					  .topCenter()
					  .size(60, 99);

	}

	public static UIButtonBuilder shopButton(UIContainer parent, boolean expand)
	{
		return iconButton(parent, Icon.FA_SHOPPING_BAG, "almura.menu_button.shop", Url.SHOP, expand);
	}

	public static UIButtonBuilder forumsButton(UIContainer parent, boolean expand)
	{
		return iconButton(parent, Icon.ENJIN, "almura.menu_button.forums", Url.FORUM, expand);
	}

	public static UIButtonBuilder issuesButton(UIContainer parent, boolean expand)
	{
		return iconButton(parent, Icon.FA_GITHUB, "almura.menu_button.issues", Url.ISSUES, expand);
	}

	private static UIButtonBuilder iconButton(UIContainer parent, GuiIcon icon, String text, String url, boolean expand)
	{
		UIButtonBuilder builder = UIButton.builder()
										  .parent(parent)
										  .link(url);

		if (!expand)
		{
			return builder.icon(icon)
						  .size(Button.ICON)
						  .tooltip(text);
		}

		UIContainer container = UIContainer.builder()
										   .build();

		UIImage image = UIImage.builder()
							   .parent(container)
							   .icon(icon)
							   .size(16, 16)
							   .build();

		UILabel label = UILabel.builder()
							   .name(text)
							   .parent(container)
							   .text(text)
							   .rightOf(image, GuiConfig.PADDING)
							   .middleAlignedTo(image, 1) //don't middleAlign() because parent size depends on label position
							   .textColor(0xFFFFA0)
							   .shadow()
							   .visible(false)
							   .build();

		return builder.content(container)
					  .onMouseOver(e -> {
						  label.setVisible(true);
						  return true;
					  })
					  .onMouseOut(e -> {
						  label.setVisible(false);
						  return true;
					  });

	}
}
