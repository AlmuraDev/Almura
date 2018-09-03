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
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIWorldPanel extends AbstractPanel {

    @Inject private static HeadUpDisplay hudData;
    @Inject private static ClientClaimManager clientClaimManager;

    private final UILabel compassLabel, worldLabel, claimLabel;

    public UIWorldPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.worldLabel = new UILabel(gui, "");
        this.worldLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        this.worldLabel.setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .scale(0.9F)
                .build());

        this.claimLabel = new UILabel(gui, "");
        this.claimLabel.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        this.claimLabel.setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .scale(0.9F)
                .build());

        this.compassLabel = new UILabel(gui, "");
        this.compassLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
        this.compassLabel.setFontOptions(FontOptions.builder()
                .from(FontColors.WHITE_FO)
                .shadow(false)
                .scale(0.9F)
                .build());

        this.add(this.worldLabel, this.claimLabel, this.compassLabel);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.client.player == null || this.client.player.world == null) {
            return;
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateWorld();
        this.updateClaim();
        this.updateCompass();
    }

    private void updateWorld() {
        this.worldLabel.setText(hudData.worldName);
        this.worldLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        this.width = Math.max(this.worldLabel.getWidth() + 10, 60);
    }

    private void updateClaim() {
        //System.out.println("ClaimName: " + hudData.claimName);
        if (!clientClaimManager.isClaim || clientClaimManager.isWilderness) {
            if (!clientClaimManager.claimName.equalsIgnoreCase("Server Protected Area")) {
                this.claimLabel.setText(TextFormatting.GREEN + "Wilderness");
            } else {
                this.claimLabel.setText(TextFormatting.RED + clientClaimManager.claimName);
            }
        } else {
            if (clientClaimManager.claimName.isEmpty()){
                this.claimLabel.setText(TextFormatting.LIGHT_PURPLE + "claim name not set");
            } else {
                if (clientClaimManager.isTownClaim)
                    this.claimLabel.setText(TextFormatting.YELLOW + clientClaimManager.claimName);
                if (clientClaimManager.isAdminClaim)
                    this.claimLabel.setText(TextFormatting.BLUE + clientClaimManager.claimName);
                if (clientClaimManager.isBasicClaim)
                    this.claimLabel.setText(TextFormatting.DARK_AQUA + clientClaimManager.claimName);
                if (clientClaimManager.isSubdivision)
                    this.claimLabel.setText(TextFormatting.DARK_PURPLE + clientClaimManager.claimName);
            }
        }
        this.claimLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        if (this.width < Math.max(this.claimLabel.getWidth() + 10, 60)) {
            this.width = Math.max(this.claimLabel.getWidth() + 10, 60);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateCompass() {
        this.compassLabel.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(hudData.getCompass()));
        this.compassLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
    }
}
