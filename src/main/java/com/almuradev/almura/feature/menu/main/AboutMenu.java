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
import net.malisis.ego.font.FontOptions;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.component.UIComponent;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.decoration.UILabel;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.component.interaction.UISelectableList;
import net.malisis.ego.gui.component.interaction.builder.UIButtonBuilder;
import net.malisis.ego.gui.component.scrolling.UIScrollBar.Type;
import net.malisis.ego.gui.component.scrolling.UISlimScrollbar;
import net.malisis.ego.gui.element.Padding;
import net.malisis.ego.gui.element.position.Position;
import net.malisis.ego.gui.element.size.Size;
import net.malisis.ego.gui.render.GuiIcon;
import net.malisis.ego.gui.render.background.DirtBackground;
import net.malisis.ego.gui.render.background.OptionPanel;
import net.malisis.ego.gui.render.shape.GuiShape;
import net.malisis.ego.gui.text.GuiText;
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

		UILabel title = new UILabel(TextFormatting.WHITE + "{almura.menu_button.about}");
		title.setPosition(Position.topCenter(title).offset(0, 20));

		UIContainer mainContainer = new UIContainer();
		mainContainer.setBackground(new OptionPanel(mainContainer));
		mainContainer.setPosition(Position.topLeft(mainContainer).offset(0, 36));
		mainContainer.setSize(Size.of(fillWidth(mainContainer, 0), fillHeight(mainContainer, -66)));
		mainContainer.setPadding(Padding.of(4));

		buildList(mainContainer);

		// Versions
		String sponge = ((Optional<String>) Sponge.getPlatform().asMap().get("ImplementationVersion")).orElse("dev");
		String forge = Sponge.getPluginManager().getPlugin("Forge").orElseThrow(NullPointerException::new).getVersion().orElse("dev");
		String almura = Sponge.getPluginManager().getPlugin("almura").orElseThrow(NullPointerException::new).getVersion().orElse("dev");

		UILabel versions = new UILabel(GuiText.builder()
											  .text(TextFormatting.WHITE + "SpongeForge: {SPONGE}\nForge: {FORGE}\nAlmura: {ALMURA}")
											  .bind("SPONGE", sponge)
											  .bind("FORGE", forge)
											  .bind("ALMURA", almura)
											  .build());
		versions.setPosition(Position.bottomLeft(versions).offset(5, -5));

		UIButton doneButton = new UIButtonBuilder("button.done").text("gui.done")
																.position(b -> Position.bottomCenter(b).offset(0, -15))
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

		UILabel label = new UILabel(true);
		label.setPosition(Position.of(rightOf(aboutList, 5), topAligned(label, 0)));
		label.setSize(Size.of(fillWidth(label, 0), fillHeight(label, 0)));
		label.setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow(false).build());

		aboutList.onChange((IAboutData data) -> label.setText(data.description()));//to do after label init
		aboutList.select(IAboutData.ABOUT_ALMURA);//to do after change callback set

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
			UIImage image = new UIImage(new GuiIcon(data.texture()));
			image.setParent(this);//required for correct positioning
			image.setPosition(Position.middleLeft(image).offset(4, 0));
			image.setSize(Size.of(isAlmura ? 23 : 32, 32));

			UILabel label = new UILabel(data.color() + "{" + data.title() + "}");
			label.setParent(this);//required for correct positioning
			label.setPosition(Position.of(rightOf(image, isAlmura ? 13 : 4), middleAligned(label, 0)));

			setSize(Size.of(fillWidth(this, -4), 38));

			GuiShape shape = GuiShape.builder(this).color(0x414141).border(1, 0x808080).build();
			setBackground(() -> ((UISelectableList<IAboutData>) getParent()).isSelected(data) ? shape : null);
			setForeground(image.and(label));
		}
	}

}
