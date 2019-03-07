/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin;

import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.almuradev.almura.feature.hud.screen.AbstractHUD;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIBossBarPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UINotificationPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPlayerListPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIUserPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.InformationDebugPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.LookingDebugPanel;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.util.MathUtil;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.Game;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class OriginHUD extends AbstractHUD
{
	private static final int PADDING = 1;
	private final Minecraft client = Minecraft.getMinecraft();
	private final Game game;
	private final MappedConfiguration<ClientConfiguration> configAdapter;
	private final ClientNotificationManager manager;
	private UIBossBarPanel bossBarPanel;
	private InformationDebugPanel debugDetailsPanel;
	private LookingDebugPanel debugBlockPanel;
	private UIDetailsPanel detailsPanel;
	private UIPlayerListPanel playerListPanel;
	private UIUserPanel userPanel;
	private UIWorldPanel worldPanel;
	public UINotificationPanel notificationPanel;

	@Inject
	private OriginHUD(Game game, MappedConfiguration<ClientConfiguration> configAdapter, ClientNotificationManager manager)
	{
		this.game = game;
		this.configAdapter = configAdapter;
		this.manager = manager;
	}

	@Override
	public void construct()
	{
		guiscreenBackground = false;

		renderer.setDefaultTexture(null /*GuiConfig.SpriteSheet.VANILLA_CONTAINER_INVENTORY*/);

		// User panel
		userPanel = new UIUserPanel(this, 124, 27);
		userPanel.setPosition(0, 0);

		// Debug block panel
		debugBlockPanel = new LookingDebugPanel(this, 124, 45);
		debugBlockPanel.setPosition(0, BasicScreen.getPaddedY(userPanel, PADDING));

		// Notifications panel
		notificationPanel = new UINotificationPanel(this, 124, 26, manager);
		notificationPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
		notificationPanel.setAlpha(0); // Hide this initially.

		// World panel
		worldPanel = new UIWorldPanel(this);
		worldPanel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

		// Details panel
		detailsPanel = new UIDetailsPanel(this, 124, 37);
		detailsPanel.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

		// Debug details panel
		debugDetailsPanel = new InformationDebugPanel(this, 155, 64, game);
		debugDetailsPanel.setPosition(0, BasicScreen.getPaddedY(detailsPanel, PADDING), Anchor.TOP | Anchor.RIGHT);

		// Boss bar panel
		bossBarPanel = new UIBossBarPanel(this, 124, 33);
		bossBarPanel.setPosition(0, BasicScreen.getPaddedY(worldPanel, PADDING), Anchor.TOP | Anchor.CENTER);

		// Player list panel
		playerListPanel = new UIPlayerListPanel(this, 150, 16);
		playerListPanel.setPosition(0, 40, Anchor.TOP | Anchor.CENTER);

		addToScreen(userPanel,
					debugBlockPanel,
					notificationPanel,
					worldPanel,
					detailsPanel,
					debugDetailsPanel,
					bossBarPanel,
					playerListPanel);
	}

	public boolean handleScroll()
	{
		if (playerListPanel != null && playerListPanel.isVisible())
		{
			playerListPanel.onScrollWheel(Mouse.getEventX(), Mouse.getEventY(), MathUtil.squashi(Mouse.getEventDWheel(), -1, 1));
			return true;
		}
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		GeneralCategory general = configAdapter.get().general;

		userPanel.setAlpha(general.originHudOpacity);

		detailsPanel.setVisible(general.displayLocationWidget);
		detailsPanel.setAlpha(general.originHudOpacity);

		worldPanel.setVisible(general.displayWorldCompassWidget);

		if (manager.getCurrent() == null)
		{
			notificationPanel.setAlpha(Math.max(notificationPanel.getAlpha() - 2, 0));
			worldPanel.setAlpha(Math.min(worldPanel.getAlpha() + 2, general.originHudOpacity));
		}
		else
		{
			notificationPanel.setAlpha(Math.min(notificationPanel.getAlpha() + 5, 255));
			worldPanel.setAlpha(Math.max(worldPanel.getAlpha() - 20, 0));

			if ((notificationPanel.notificationTitle.getWidth()) < (notificationPanel.notificationLabel.getWidth()))
			{
				notificationPanel.setSize(notificationPanel.notificationLabel.getContentWidth() + 10, notificationPanel.getHeight());
			}
			else
			{
				notificationPanel.setSize(notificationPanel.notificationTitle.getContentWidth() + 10, notificationPanel.getHeight());
			}
		}

		// Show debug panels if necessary
		boolean isDebugEnabled = client.gameSettings.showDebugInfo;
		debugDetailsPanel.setVisible(isDebugEnabled);
		if (isDebugEnabled)
		{
			// Get proper position based on what potion effects are being shown
			int yOffset = BasicScreen.getPaddedY(detailsPanel, PADDING);
			if (client.player.getActivePotionEffects().stream().anyMatch(potion -> potion.getPotion().isBeneficial()))
			{
				yOffset += 25; // 24 for potion icon, 1 for padding
			}
			if (client.player.getActivePotionEffects().stream().anyMatch(potion -> !potion.getPotion().isBeneficial()))
			{
				yOffset += 25; // 24 for potion icon, 1 for padding
			}
			// Debug block panel
			debugBlockPanel.setPosition(0, BasicScreen.getPaddedY(userPanel, PADDING));
			debugBlockPanel.setAlpha(general.originHudOpacity);

			// Debug details panel
			debugDetailsPanel.setPosition(0, yOffset);
			debugDetailsPanel.setAlpha(general.originHudOpacity);
		}

		// Show boss panel if necessary
		GuiIngame guiIngame = client.ingameGUI;
		if (guiIngame != null)
		{
			bossBarPanel.setVisible(!((IMixinGuiBossOverlay) guiIngame.getBossOverlay()).getBossInfo().isEmpty());
			bossBarPanel.setAlpha(general.originHudOpacity);
		}

		// Show player list if necessary
		playerListPanel.setVisible(client.gameSettings.keyBindPlayerList.isKeyDown());

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public int getTabMenuOffsetY()
	{
		return worldPanel.getHeight() + PADDING;
	}

	@Override
	public int getPotionOffsetY()
	{
		return detailsPanel.getHeight() + PADDING;
	}
}
