/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import static net.malisis.ego.gui.element.size.Sizes.fillHeight;
import static net.malisis.ego.gui.element.size.Sizes.fillWidth;

import com.almuradev.almura.feature.menu.main.IAboutData.Staff;
import com.almuradev.almura.shared.client.GuiConfig;
import com.google.common.collect.Lists;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.UIConstants.Button;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UISelectableList;
import net.malisis.ego.gui.component.scrolling.UIScrollBar.Type;
import net.malisis.ego.gui.component.scrolling.UISlimScrollbar;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.background.DirtBackground;
import net.malisis.ego.gui.render.background.OptionPanel;
import net.malisis.ego.gui.render.shape.GuiShape;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Sponge;

import java.util.List;
import java.util.Optional;

public class AboutMenu extends MalisisGui
{

	@SuppressWarnings("unchecked")
	@Override
	public void construct()
	{
		showParentOnClose = true;
		setBackground(new DirtBackground(screen));

		UILabel title = UILabel.builder()
							   .text("almura.menu_button.about")
							   .color(TextFormatting.WHITE)
							   .topCenter(0, 20)
							   .build();

		UIContainer mainContainer = UIContainer.builder()
											   .background(OptionPanel::new)
											   .topLeft(0, 36)
											   .size(s -> fillWidth(s, 0), s -> fillHeight(s, 66))
											   .padding(GuiConfig.PADDING)
											   .build();
		buildList(mainContainer);

		// Versions
		String sponge = ((Optional<String>) Sponge.getPlatform()
												  .asMap()
												  .get("ImplementationVersion")).orElse("dev");
		String forge = Sponge.getPluginManager()
							 .getPlugin("Forge")
							 .orElseThrow(NullPointerException::new)
							 .getVersion()
							 .orElse("dev");
		String almura = Sponge.getPluginManager()
							  .getPlugin("almura")
							  .orElseThrow(NullPointerException::new)
							  .getVersion()
							  .orElse("dev");

		UILabel versions = UILabel.builder()
								  .text("SpongeForge: {SPONGE}\nForge: {FORGE}\nAlmura: {ALMURA}")
								  .color(TextFormatting.WHITE)
								  .bind("SPONGE", sponge)
								  .bind("FORGE", forge)
								  .bind("ALMURA", almura)
								  .bottomLeft(GuiConfig.PADDING, GuiConfig.PADDING)
								  .build();

		UIButton doneButton = UIButton.builder()
									  .text("gui.done")
									  .bottomCenter(0, 15)
									  .size(Button.HALF)
									  .onClick(this::close)
									  .build();

		addToScreen(title, mainContainer, versions, doneButton);
	}

	private void buildList(UIContainer mainContainer)
	{
		List<IAboutData> list = Lists.asList(IAboutData.ABOUT_ALMURA, Staff.values());

		UISelectableList<IAboutData> aboutList = new UISelectableList<>();
		aboutList.setElementSpacing(4);
		aboutList.setComponentFactory(AboutDataComponent::new);
		aboutList.setSize(Size.of(125, fillHeight(aboutList, 0)));
		aboutList.setElements(list);
		aboutList.select(IAboutData.ABOUT_ALMURA);

		UILabel label = UILabel.builder()
							   .parent(mainContainer)
							   .text(() -> aboutList.selected() != null ?
										   aboutList.selected()
													.description() :
										   "")
							   .rightOf(aboutList, GuiConfig.PADDING)
							   .topAligned()
							   .size(Size::fill)
							   .color(TextFormatting.WHITE)
							   .build();

		new UISlimScrollbar(aboutList, Type.VERTICAL);
		new UISlimScrollbar(label, Type.VERTICAL);

		mainContainer.add(aboutList);
	}

	private class AboutDataComponent extends UIComponent
	{
		@SuppressWarnings("unchecked")
		public AboutDataComponent(IAboutData data)
		{
			boolean isAlmura = data == IAboutData.ABOUT_ALMURA;
			UIImage image = UIImage.builder()
								   .parent(this)//required for correct positioning
								   .texture(data.texture())
								   .middleLeft(GuiConfig.PADDING, 0)
								   .size(isAlmura ? 23 : 32, 32)
								   .build();

			UILabel label = UILabel.builder()
								   .parent(this)//required for correct positioning
								   .text(data.title())
								   .color(data.color())
								   .rightOf(image, isAlmura ? 13 : 4)
								   .middleAligned()
								   .build();

			setSize(Size.of(fillWidth(this, GuiConfig.PADDING), 38));

			GuiShape shape = GuiShape.builder(this)
									 .color(0x414141)
									 .border(1, 0x808080)
									 .alpha(() -> ((UISelectableList<IAboutData>) getParent()).isSelected(data) ? 255 : 0)
									 .build();
			setBackground(shape);
			setForeground(image.and(label));
		}
	}

}
