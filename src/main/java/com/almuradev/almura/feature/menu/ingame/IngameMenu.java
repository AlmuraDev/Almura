package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.feature.menu.component.CommonComponents;
import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.ego.gui.MalisisGui;
import net.malisis.ego.gui.UIConstants.Button;
import net.malisis.ego.gui.component.container.UIContainer;
import net.malisis.ego.gui.component.decoration.UIImage;
import net.malisis.ego.gui.component.interaction.UIButton;
import net.malisis.ego.gui.element.size.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.util.text.TextFormatting;

public class IngameMenu extends MalisisGui
{
	@Override
	public void construct()
	{
		renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);

		UIContainer container = UIContainer.builder()
										   .middleCenter()
										   .size(Size::sizeOfContent)
										   //.size(220, 202)
										   .build();

		// Almura Header
		UIImage almuraHeader = UIImage.builder()
									  .parent(container)
									  .icon(GuiConfig.Icon.ALMURA_LOGO)
									  .topCenter()
									  .size(60, 99)
									  .build();

		UIButton backButton = UIButton.builder()
									  .parent(container)
									  .text("menu.returnToGame")
									  .below(almuraHeader, GuiConfig.PADDING)
									  .size(192, 20)
									  .onClick(this::close)
									  .build();

		UIButton shopButton = CommonComponents.shopButton(container, false)
											  .below(backButton, GuiConfig.PADDING)
											  .build();

		UIButton mapButton = UIButton.builder()
									 .parent(container)
									 .icon(GuiConfig.Icon.FA_MAP)
									 .rightOf(shopButton, GuiConfig.PADDING)
									 .below(backButton, GuiConfig.PADDING)
									 .size(Button.ICON)
									 .tooltip("item.map.name")
									 //.onClick()
									 .build();

		UIButton statisticsButton = UIButton.builder()
											.parent(container)
											.icon(GuiConfig.Icon.FA_PIE_CHART)
											.rightOf(mapButton, GuiConfig.PADDING)
											.below(backButton, GuiConfig.PADDING)
											.size(Button.ICON)
											.tooltip("gui.stats")
											.link(GuiConfig.Url.STATISTICS)
											.build();

		UIButton advancementsButton = UIButton.builder()
											  .parent(container)
											  .icon(GuiConfig.Icon.FA_TROPHY)
											  .rightOf(statisticsButton, GuiConfig.PADDING)
											  .below(backButton, GuiConfig.PADDING)
											  .size(Button.ICON)
											  .tooltip("gui.advancements")
											  .onClick(() -> mc.displayGuiScreen(
													  new GuiScreenAdvancements(mc.player.connection.getAdvancementManager())))
											  .build();

		UIButton forumsButton = CommonComponents.forumsButton(container, false)
												.rightOf(advancementsButton, GuiConfig.PADDING)
												.below(backButton, GuiConfig.PADDING)
												.build();

		UIButton lanButton = UIButton.builder()
									 .parent(container)
									 .icon(GuiConfig.Icon.FA_SITEMAP)
									 .rightOf(forumsButton, GuiConfig.PADDING)
									 .below(backButton, GuiConfig.PADDING)
									 .size(Button.ICON)
									 .enabled(Minecraft.getMinecraft()
													   .isSingleplayer() && !Minecraft.getMinecraft()
																					  .getIntegratedServer()
																					  .getPublic())
									 .tooltip("gui.shareToLan")
									 .onClick(() -> mc.displayGuiScreen(new GuiShareToLan(this)))
									 .build();

		UIButton.builder()
				.parent(container)
				.icon(GuiConfig.Icon.FA_COG)
				.rightOf(lanButton, GuiConfig.PADDING)
				.below(backButton, GuiConfig.PADDING)
				.size(Button.ICON)
				.tooltip("menu.options")
				.onClick(() -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)))
				.build();

		UIButton.builder()
				.parent(container)
				.text("almura.menu_button.quit")
				.below(shopButton, GuiConfig.PADDING)
				.size(192, 20)
				.color(TextFormatting.RED)
				.shadow(true)
				.onClick(() -> {
					close();
					mc.world.sendQuittingDisconnectingPacket();
					mc.loadWorld(null);
					mc.displayGuiScreen(new GuiMainMenu());
				})
				.build();

		addToScreen(container);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return true;
	}
}

