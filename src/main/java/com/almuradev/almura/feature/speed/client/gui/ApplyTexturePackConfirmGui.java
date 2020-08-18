/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.speed.client.gui;

import com.almuradev.almura.feature.speed.AlmuraSettings;
import com.almuradev.almura.feature.menu.main.PanoramicMainMenu;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ApplyTexturePackConfirmGui extends BasicScreen {

    private static final int padding = 4;

    public ApplyTexturePackConfirmGui(@Nullable GuiScreen parent) {
        super(parent, false);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(this, 350, 125, "Almura Resource Loader");
        form.setZIndex(10);
        form.setClosable(false);
        form.setBackgroundAlpha(255);

        final UILabel label0 = new UILabel(this, "We noticed you're not using the preferred texture pack.");
        label0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        label0.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        final UILabel label1 = new UILabel(this, "Do you want to apply the following texture pack?");
        label1.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        label1.setPosition(0, label0.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UILabel label2 = new UILabel(this, AlmuraSettings.getPreferredTexturePack());
        label2.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        label2.setPosition(0, label1.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UISeparator belowAmountSeparator = new UISeparator(this);
        belowAmountSeparator.setSize(form.getWidth() -5, 1);
        belowAmountSeparator.setPosition(0, -45, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonYes = new UIButtonBuilder(this)
                .text("Yes")
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(-20, -25)
                .width(40)
                .listener(this)
                .build("button.yes");

        final UIButton buttonNo = new UIButtonBuilder(this)
            .text("No")
            .anchor(Anchor.BOTTOM | Anchor.CENTER)
            .position(20, -25)
            .width(40)
            .listener(this)
            .build("button.close");

        final UISeparator aboveCloseSeparator = new UISeparator(this);
        aboveCloseSeparator.setSize(form.getWidth() -5, 1);
        aboveCloseSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonClose = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.close"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(0, 0)
                .width(40)
                .listener(this)
                .build("button.close");

        form.add(buttonClose, label0, label1, label2, aboveCloseSeparator, buttonYes, buttonNo, belowAmountSeparator);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.yes":
                AlmuraSettings.applyPreferredTexturePack();
                new PanoramicMainMenu(null).display();
                break;

            case "button.close":
                new PanoramicMainMenu(null).display();
                this.close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
