/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.singleplayer;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.screen.PanoramicScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

/**
 * A yes/no question on the menus' panorama, in place of vanilla's GuiYesNo (deleting a world, opening one from a newer version). Cancel goes
 * back to the screen it came from; confirm runs the action, which decides where to go next.
 */
@SideOnly(Side.CLIENT)
public final class MenuConfirmScreen extends PanoramicScreen {

    private static final int WIDTH = 280, PADDING = 6;

    private final String title, message, confirmText;
    private final boolean dangerous;
    private final Runnable onConfirm;

    /**
     * @param parent the screen to go back to on cancel
     * @param title the question
     * @param message the explanation under it
     * @param confirmText the confirm button's text
     * @param dangerous whether confirming can't be undone (its button is red)
     * @param onConfirm what confirming does
     */
    public MenuConfirmScreen(final BasicScreen parent, final String title, final String message, final String confirmText, final boolean dangerous,
        final Runnable onConfirm) {
        super(parent);
        this.title = title;
        this.message = message;
        this.confirmText = confirmText;
        this.dangerous = dangerous;
        this.onConfirm = onConfirm;
    }

    @Override
    public void construct() {
        this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);

        final BasicContainer<?> box = new BasicContainer<>(WIDTH, 100);
        box.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        box.setColor(0x000000);
        box.setBackgroundAlpha(185);
        box.setBorder(0xFFFFFF, 1, 185);
        box.setPadding(PADDING);

        final UILabel titleLabel = new UILabel(TextFormatting.WHITE + this.title);
        titleLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        final UILabel messageLabel = new UILabel(true);
        messageLabel.setSize(WIDTH - PADDING * 2 - 2, 40);
        messageLabel.setText(TextFormatting.GRAY + this.message);
        messageLabel.setPosition(0, BasicScreen.getPaddedY(titleLabel, 6), Anchor.TOP | Anchor.CENTER);

        final UIButtonBuilder confirm = new UIButtonBuilder()
            .text(this.confirmText)
            .width(UIConstants.Button.WIDTH_TINY + 20)
            .height(UIConstants.Button.HEIGHT)
            .position(-(UIConstants.Button.WIDTH_TINY + 20) / 2 - 2, 0)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .onClick(this.onConfirm);
        if (this.dangerous) {
            confirm.fontOptions(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
                .hoverFontOptions(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build());
        }

        final UIButtonBuilder cancel = new UIButtonBuilder()
            .text(I18n.format("gui.cancel"))
            .width(UIConstants.Button.WIDTH_TINY + 20)
            .height(UIConstants.Button.HEIGHT)
            .position((UIConstants.Button.WIDTH_TINY + 20) / 2 + 2, 0)
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .onClick(this::close);

        box.add(titleLabel, messageLabel, confirm.build("button.confirm"), cancel.build("button.cancel"));
        this.addToScreen(box);
    }
}
