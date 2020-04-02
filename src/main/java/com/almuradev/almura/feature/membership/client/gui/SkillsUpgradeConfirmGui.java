/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client.gui;

import com.almuradev.almura.feature.membership.ClientMembershipManager;
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
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SkillsUpgradeConfirmGui extends BasicScreen {

    private static final int padding = 4;
    @Inject private static ClientMembershipManager membershipManager;
    private String membership, value;
    private int membershipLevel;

    public SkillsUpgradeConfirmGui(@Nullable GuiScreen parent, String membership, String value, int membershipLevel) {
        super(parent, true);
        this.membershipLevel = membershipLevel;
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

        final UILabel label0 = new UILabel(this, "Do you wish to upgrade to:");
        label0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        label0.setPosition(0, 10, Anchor.CENTER | Anchor.TOP);

        final UILabel membershipLabel = new UILabel(this, membership);
        membershipLabel.setFontOptions(FontOptions.builder().from(FontColors.GOLD_FO).shadow(true).scale(1.0F).build());
        membershipLabel.setPosition(0, label0.getY() + 15, Anchor.CENTER | Anchor.TOP);

        final UISeparator belowAmountSeparator = new UISeparator(this);
        belowAmountSeparator.setSize(form.getWidth() -5, 1);
        belowAmountSeparator.setPosition(0, -45, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonPurchase = new UIButtonBuilder(this)
                //.text(I18n.format("almura.button.close"))
                .text("Upgrade Me!")
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .position(0, -25)
                .width(40)
                .listener(this)
                .build("button.upgrade");

        final UISeparator aboveCloseSeparator = new UISeparator(this);
        aboveCloseSeparator.setSize(form.getWidth() -5, 1);
        aboveCloseSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonClose = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(0, 0)
                .width(40)
                .listener(this)
                .build("button.close");

        form.add(buttonClose, label0, membershipLabel, aboveCloseSeparator, buttonPurchase, belowAmountSeparator);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.upgrade":
                membershipManager.requestMembershipPurchase(membershipLevel, 2);
                this.close();
                break;

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
