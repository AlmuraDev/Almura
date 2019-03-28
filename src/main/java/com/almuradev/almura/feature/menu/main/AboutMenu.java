/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import static net.malisis.ego.gui.element.position.Positions.middleAligned;
import static net.malisis.ego.gui.element.position.Positions.rightOf;
import static net.malisis.ego.gui.element.position.Positions.topAligned;
import static net.malisis.ego.gui.element.size.Sizes.fillHeight;
import static net.malisis.ego.gui.element.size.Sizes.fillWidth;

import com.almuradev.almura.feature.menu.main.IAboutData.Staff;
import com.google.common.collect.Lists;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UISelectableList;
import net.malisis.ego.gui.component.scrolling.UIScrollBar.Type;
import net.malisis.ego.gui.component.scrolling.UISlimScrollbar;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
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
		setBackground(new DirtBackground(getScreen()));

		UILabel title = UILabel.builder()
							   .text("almura.menu_button.about")
							   .color(TextFormatting.WHITE)
							   .position(l -> Position.topCenter(l)
													  .offset(0, 20))
							   .build();

		UIContainer mainContainer = new UIContainer();
		mainContainer.setBackground(new OptionPanel(mainContainer));
		mainContainer.setPosition(Position.topLeft(mainContainer)
										  .offset(0, 36));
		mainContainer.setSize(Size.of(fillWidth(mainContainer, 0), fillHeight(mainContainer, -66)));
		mainContainer.setPadding(Padding.of(4));

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
								  .position(l -> (Position.bottomLeft(l)
														  .offset(5, -5)))
								  .build();

		UIButton doneButton = UIButton.builder()
									  .text("gui.done")
									  .position(b -> Position.bottomCenter(b)
															 .offset(0, -15))
									  .size(98, 20)
									  .onClick(() -> new PanoramicMainMenu(null).display())
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
		aboutList.select(IAboutData.ABOUT_ALMURA);//to do after change callback set

		UILabel label = UILabel.builder()
							   .position(l -> Position.of(rightOf(aboutList, 5), topAligned(l, 0)))
							   .text(() -> aboutList.selected() != null ?
										   aboutList.selected()
													.description() :
										   "")
							   .size(Size::fill)
							   .color(TextFormatting.WHITE)
							   .build();

		//handled by the label directly
		//aboutList.select(IAboutData.ABOUT_ALMURA);//to do after change callback set

		new UISlimScrollbar(aboutList, Type.VERTICAL);
		new UISlimScrollbar(label, Type.VERTICAL);

		mainContainer.add(aboutList, label);
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
								   .position(i -> Position.middleLeft(i)
														  .offset(4, 0))
								   .size(isAlmura ? 23 : 32, 32)
								   .build();

			UILabel label = UILabel.builder()
								   .parent(this)//required for correct positioning
								   .text(data.title())
								   .color(data.color())
								   .position(l -> rightOf(image, isAlmura ? 13 : 4), l -> middleAligned(l, 0))
								   .build();

			setSize(Size.of(fillWidth(this, -4), 38));

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
