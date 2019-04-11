package com.almuradev.almura.feature.menu.main.component;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.GuiConfig.Url;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UIButton.UIButtonBuilder;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.element.size.Sizes;
import net.malisis.ego.gui.render.GuiIcon;
import net.minecraft.util.text.TextFormatting;

public class ExtrasComponent extends UIContainer
{
	public ExtrasComponent()
	{
		setPadding(Padding.of(GuiConfig.PADDING));
		setPosition(Position.bottomLeft(this));
		setSize(Size.of(Sizes.parentWidth(this, 1.0F, 0), 32));

		UILabel.builder()
			   .parent(this)
			   .text("{almura.menu.main.trademark}\n{almura.menu.main.copyright}")
			   .position(Position::bottomLeft)
			   .color(TextFormatting.YELLOW)
			   .build();

		//right to left
		//shop
		UIButton shopButton = buttonBuilder(GuiConfig.Icon.FA_SHOPPING_BAG, "almura.menu_button.shop", Url.SHOP).position(
				Position::bottomRight)

																														  .build();
		//issues
		UIButton issuesButton = buttonBuilder(GuiConfig.Icon.FA_GITHUB, "almura.menu_button.issues", Url.ISSUES).leftOf(shopButton,
																														GuiConfig.PADDING)
																												.bottomAligned()
																												.build();

		//forums
		buttonBuilder(GuiConfig.Icon.ENJIN, "almura.menu_button.forums", Url.FORUM).leftOf(issuesButton, GuiConfig.PADDING)
																							 .bottomAligned()
																							 .build();
	}

	private UIButtonBuilder buttonBuilder(GuiIcon icon, String text, String url)
	{
		UIContainer container = UIContainer.builder()
										   .size(Size::sizeOfContent)
										   .build();

		UIImage image = UIImage.builder()
							   .parent(container)
							   .icon(icon)
							   .build();

		UILabel label = UILabel.builder()
							   .name(text)
							   .parent(container)
							   .text(text)
							   .rightOf(image, 4)
							   .middleAligned(1)
							   .color(0xFFFFA0)
							   .shadow()
							   .visible(false)
							   .build();

		return UIButton.builder()
					   .parent(this)
					   .content(container)
					   .onClick(() -> MalisisGui.openLink(Url.SHOP))
					   .onMouseOver(e -> {
						   label.setVisible(true);
						   container.onContentUpdate();
						   return true;
					   })
					   .onMouseOut(e -> {
						   label.setVisible(false);
						   container.onContentUpdate();
						   return true;
					   });

	}
}
