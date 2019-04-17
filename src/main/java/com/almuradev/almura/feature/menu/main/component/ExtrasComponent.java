package com.almuradev.almura.feature.menu.main.component;

import com.almuradev.almura.feature.menu.component.CommonComponents;
import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.element.size.Sizes;
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
		UIButton shopButton = CommonComponents.shopButton(this, true)
											  .position(Position::bottomRight)
											  .build();
		//issues
		UIButton issuesButton = CommonComponents.issuesButton(this, true)
												.leftOf(shopButton, GuiConfig.PADDING)
												.bottomAligned()
												.build();

		//forums
		CommonComponents.forumsButton(this, true)
						.leftOf(issuesButton, GuiConfig.PADDING)
						.bottomAligned()
						.build();
	}
}
