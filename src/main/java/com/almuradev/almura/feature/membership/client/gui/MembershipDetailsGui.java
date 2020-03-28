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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class MembershipDetailsGui extends BasicScreen {

    private static final int padding = 4;
    @Inject private static ClientMembershipManager membershipManager;
    private String membership, value;
    private int membershipLevel;

    public MembershipDetailsGui(@Nullable GuiScreen parent, String membership, String value, int membershipLevel) {
        super(parent, true);
        this.membershipLevel = membershipLevel;
        this.membership = membership;
        this.value = value;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(this, 250, 200, membership);
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        final UILabel label0 = new UILabel(this, "Unlocks Worlds:");
        label0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        label0.setPosition(25, 10, Anchor.LEFT | Anchor.TOP);
        form.add(label0);

        final UILabel world1 = new UILabel(this, "Cemaria");
        world1.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        world1.setPosition(label0.getX() + label0.getWidth() + 5, label0.getY(), Anchor.LEFT | Anchor.TOP);
        form.add(world1);

        if (membershipLevel >= 2) {
            final UILabel world2 = new UILabel(this, "Atlantis");
            world2.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
            world2.setPosition(label0.getX() + label0.getWidth() + 5, world1.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(world2);

            final UILabel world3 = new UILabel(this, "Keystone");
            world3.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
            world3.setPosition(label0.getX() + label0.getWidth() + 5, world2.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(world3);

            if (membershipLevel == 3) {
                final UILabel world4 = new UILabel(this, "Zeal");
                world4.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
                world4.setPosition(label0.getX() + label0.getWidth() + 5, world3.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(world4);

                final UILabel world5 = new UILabel(this, "Tollana");
                world5.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
                world5.setPosition(label0.getX() + label0.getWidth() + 5, world4.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(world5);
            }
        }

        final UILabel label2 = new UILabel(this, "Features: ");
        label2.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        if (membershipLevel == 1) {
            label2.setPosition(25, 25, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 140);
        }
        if (membershipLevel == 2) {
            label2.setPosition(25, 45, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 180);
        }
        if (membershipLevel == 3) {
            label2.setPosition(25, 65, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 260);
        }

        form.add(label2);

        final UILabel label3 = new UILabel(this, "- Keep Inventory on Death");
        label3.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        label3.setPosition(35, label2.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(label3);

        final UILabel label4 = new UILabel(this, "- Keep Experience on Death");
        label4.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        label4.setPosition(label3.getX(), label3.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(label4);

        final UILabel label5 = new UILabel(this, "- 90 day Grief Prevention Leases");
        label5.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        label5.setPosition(label3.getX(), label4.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(label5);

        final UILabel label6 = new UILabel(this, "- Priority Player Technical Support");
        label6.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        label6.setPosition(label3.getX(), label5.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(label6);

        final UILabel label7 = new UILabel(this, "- Early access to new content");
        label7.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        label7.setPosition(label3.getX(), label6.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(label7);

        if (membershipLevel >= 2) {
            final UILabel label8 = new UILabel(this, "- Ability to Fly!");
            label8.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
            label8.setPosition(label3.getX(), label7.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(label8);

            if (membershipLevel == 2) {
                final UILabel label9 = new UILabel(this, "- 500 item slots on Exchange");
                label9.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label9.setPosition(label3.getX(), label8.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label9);
            }

            if (membershipLevel == 3) {
                final UILabel label9 = new UILabel(this, "- 5,000 item slots on Exchange");
                label9.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label9.setPosition(label3.getX(), label8.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label9);

                final UILabel commandsLabel = new UILabel(this, "Additional Commands: ");
                commandsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
                commandsLabel.setPosition(25, label9.getY() + 15, Anchor.LEFT | Anchor.TOP);
                form.add(commandsLabel);

                final UILabel label10 = new UILabel(this, "- /back");
                label10.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label10.setPosition(label3.getX(), commandsLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label10);

                final UILabel label11 = new UILabel(this, "- /nickname");
                label11.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label11.setPosition(label3.getX(), label10.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label11);

                final UILabel label12 = new UILabel(this, "- /anvil");
                label12.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label12.setPosition(label3.getX(), label11.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label12);

                final UILabel label13 = new UILabel(this, "- /enchantingtable");
                label13.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                label13.setPosition(label3.getX(), label12.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(label13);
            }
        }

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

        form.add(buttonClose, aboveCloseSeparator);

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
