/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.shared.client.GuiConfig;
import com.google.common.eventbus.Subscribe;
import com.google.inject.internal.cglib.reflect.$FastMember;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
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
import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SideOnly(Side.CLIENT)
public class PurchaseConfirmGui extends BasicScreen {

    private static final int padding = 4;

    private String membership, value;
    private int function;

    public PurchaseConfirmGui(@Nullable GuiScreen parent, String membership, String value, int function) {
        super(parent, true);
        this.function = function;
        this.membership = membership;
        this.value = value;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(this, 150, 125, "Confirmation");
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        final UILabel label0 = new UILabel(this, "Do you wish to purchase:");
        label0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        label0.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        final UILabel membershipLabel = new UILabel(this, membership);
        membershipLabel.setFontOptions(FontOptions.builder().from(FontColors.GOLD_FO).shadow(true).scale(1.0F).build());
        membershipLabel.setPosition(0, label0.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UILabel amountLabel = new UILabel(this, "Amount: ");
        amountLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        amountLabel.setPosition(-25, membershipLabel.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UILabel valueLabel = new UILabel(this, value);
        valueLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        valueLabel.setPosition(amountLabel.getX() + amountLabel.getWidth() + 5, membershipLabel.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UISeparator belowAmountSeparator = new UISeparator(this);
        belowAmountSeparator.setSize(form.getWidth() -5, 1);
        belowAmountSeparator.setPosition(0, -45, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonPurchase = new UIButtonBuilder(this)
                //.text(I18n.format("almura.button.close"))
                .text("Purchase")
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(0, -25)
                .width(40)
                .onClick(this::close)
                .build("button.purchase");

        final UISeparator aboveCloseSeparator = new UISeparator(this);
        aboveCloseSeparator.setSize(form.getWidth() -5, 1);
        aboveCloseSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonClose = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(0, 0)
                .width(40)
                .onClick(this::close)
                .build("button.cancel");

        form.add(buttonClose, label0, membershipLabel, amountLabel, valueLabel, aboveCloseSeparator, buttonPurchase, belowAmountSeparator);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                this.close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
