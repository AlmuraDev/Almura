/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Comparator;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIWorldPanel extends AbstractPanel {

  private static final int minWidth = 50;

  @Inject private static HeadUpDisplay hudData;
  @Inject private static ClientClaimManager clientClaimManager;

  private final UILabel compassLabel, worldLabel, claimLabel;

  public UIWorldPanel(final MalisisGui gui) {
    super(gui, minWidth, 35);

    this.worldLabel = new UILabel(gui, "");
    this.worldLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
    this.worldLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

    this.claimLabel = new UILabel(gui, "");
    this.claimLabel.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
    this.claimLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

    this.compassLabel = new UILabel(gui, "");
    this.compassLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
    this.compassLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

    this.add(this.claimLabel, this.compassLabel);
  }

  @Override
  public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
    if (this.client.player == null || this.client.player.world == null) {
      return;
    }
    super.drawForeground(renderer, mouseX, mouseY, partialTick);
    this.updateWorld();
    this.updateClaim();
    this.updateCompass();
    this.updateSize();
  }

  private void updateSize() {
    this.width = Math.max(minWidth, this.getContentWidth());
    this.height = this.getContentHeight();
  }

  private void updateWorld() {
    if (hudData.worldName.isEmpty()) {
      this.remove(this.worldLabel);
    } else {
      if (!this.components.contains(this.worldLabel)) {
        this.add(this.worldLabel);
      }
      this.worldLabel.setText(hudData.worldName);
      this.worldLabel.setPosition(1, 2, Anchor.TOP | Anchor.CENTER);
    }
  }

  private void updateClaim() {
    if (!this.components.contains(this.claimLabel)) {
      this.add(this.claimLabel);
    }
    if (!clientClaimManager.isClaim || clientClaimManager.isWilderness) {
      if (!clientClaimManager.claimName.equalsIgnoreCase("Server Protected Area")) {
        this.claimLabel.setText(TextFormatting.GREEN + "Wilderness");
      } else {
        this.claimLabel.setText(TextFormatting.GRAY + clientClaimManager.claimName);
      }
    } else {
      if (clientClaimManager.claimName.isEmpty()) {
        this.claimLabel.setText(TextFormatting.LIGHT_PURPLE + "claim name not set");
      } else {
        if (clientClaimManager.isTownClaim) {
          this.claimLabel.setText(TextFormatting.YELLOW + clientClaimManager.claimName);
        }
        if (clientClaimManager.isAdminClaim) {
          this.claimLabel.setText(TextFormatting.BLUE + clientClaimManager.claimName);
        }
        if (clientClaimManager.isBasicClaim) {
          this.claimLabel.setText(TextFormatting.DARK_AQUA + clientClaimManager.claimName);
        }
        if (clientClaimManager.isSubdivision) {
          this.claimLabel.setText(TextFormatting.DARK_PURPLE + clientClaimManager.claimName);
        }
      }
    }

    final int y = this.components.contains(this.worldLabel) ? SimpleScreen.getPaddedY(this.worldLabel, 2) : 2;
    this.claimLabel.setPosition(1, y, Anchor.TOP | Anchor.CENTER);
  }

  @SuppressWarnings("deprecation")
  private void updateCompass() {
    this.compassLabel.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(hudData.getCompass()));

    final UIComponent lowestComponent = this.components
      .stream()
      .filter(c -> this.compassLabel != c)
      .max(Comparator.comparingInt(UIComponent::getY)).orElse(null);
    this.compassLabel.setPosition(1, lowestComponent != null ? SimpleScreen.getPaddedY(lowestComponent, 2) : 2, Anchor.TOP | Anchor.CENTER);
  }
}
