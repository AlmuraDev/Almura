/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;

@SideOnly(Side.CLIENT)
public class DynamicAboutMenu extends SimpleGui {

    private final static String fieldText =
            "Almura is created and maintained by the AlmuraDev Team. http://www.github.com/AlmuraDev/"
                    + "\n\n"
                    + "The Beginning:"
                    + "\n\n"
                    + "Almura 1.0 began June 1st 2011 using the Spoutcraft Client version 1.73 beta. As the technologies changed"
                    + " we were forced to abandon the Spoutcraft client in favor of the Forge client. "
                    + " Zidane, our lead developer for AlmuraDev finally put his foot down and said enough if enough."
                    + " Thus began a 6 month journey to migrate the features we had been using within Spoutcraft into our new client."
                    + "\n\n"
                    + "Almura 2.0 first commit was on Sept 4th, 2014."
                    + "\n\n"
                    + "Almura has several custom state-of-the-art technologies that will not be found in any other mod that exists within the Minecraft Community.  Those features include but are not limited to:"
                    + "\n"
                    + "- ASM Mixin Technology built specifically for Minecraft version 1.8.9."
                    + "\n"
                    + "- yml / json content loading system for blocks, items, crops, trees and other tile entities."
                    + "\n"
                    + "- client / server security checking of Mods & Tweakers."
                    + "\n"
                    + "- multiple graphical user interfaces."
                    + "\n"
                    + "- information guide system displayed in-game with real-time updates."
                    + "\n"
                    + "- player accessorires system for Hats, Wings, Capes, Earrings and other stuch abilities."
                    + "\n\n"
                    + "Thank you to the following people:"
                    + "\n\n"
                    + TextFormatting.GOLD + "Zidane" + TextFormatting.RESET + " - Lead Developer and Co-Founder of Sponge, Lead Developer of "
                    + "AlmuraDev, Former Developer for Spout."
                    + "\n\n"
                    + TextFormatting.GOLD + "Grinch" + TextFormatting.RESET + " - Moderator and Developer for Sponge, Developer "
                    + "and Solder Server Administrator for AlmuraDev and former Developer for Spout."
                    + "\n\n"
                    + TextFormatting.GOLD + "Dockter" + TextFormatting.RESET + " - Developer, Lead Tester and Server Owner for Almura Public servers, CFO Sponge Foundation and former Developer for Spoutcraft."
                    + "\n\n"
                    + TextFormatting.GOLD + "blood_" + TextFormatting.RESET + " - Developer and Co-Founder of Sponge, former Developer for "
                    + "Cauldron."
                    + "\n\n"
                    + TextFormatting.GOLD + "Mumfrey" + TextFormatting.RESET + " - Developer for Sponge, Inventor of Mixin ASM Technology for Java."
                    + "\n\n"
                    + TextFormatting.GOLD + "Wifee" + TextFormatting.RESET + " - Lead Model and Graphic Artist for Almura's Custom Content."
                    + "\n\n"
                    + "Special Thanks to Zidane for all his time and patience over the years. Without this individual Almura and AlmuraDev would simply not exist."
                    + "\n\n"
                    + "As we enter our 6th year in operation soon we hope that all of our users have a positive experience with Almura.";

    public DynamicAboutMenu(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {

        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "About");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the build label
        final UILabel buildLabel = new UILabel(this);
        buildLabel.setText(TextFormatting.GRAY + "Almura: " + Almura.GUI_VERSION + " Forge: " + Almura.instance.container.getVersion().get());
        buildLabel.setPosition(0, 3, Anchor.CENTER | Anchor.TOP);
        
        // Create About us multi-line text field
        final UITextField aboutUsLabel = new UITextField(this, "", true);

        aboutUsLabel.setSize(290, form.getContentContainer().getHeight() - 40);
        aboutUsLabel.setPosition(0, 15, Anchor.CENTER);
        aboutUsLabel.setText(fieldText);
        aboutUsLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        aboutUsLabel.setName("multiline_label.about_us");

        // Create the mods button
        final UIButton modsButton = new UIButton(this, "Mods");
        modsButton.setSize(50, 16);
        modsButton.setPosition(5, -5, Anchor.LEFT | Anchor.BOTTOM);
        modsButton.setName("button.mods");
        modsButton.register(this);

        // Create the back button
        final UIButton backButton = new UIButton(this, "Back");
        backButton.setSize(50, 16);
        backButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
        backButton.setName("button.back");
        backButton.register(this);

        form.getContentContainer().add(buildLabel, aboutUsLabel, modsButton, backButton);

        addToScreen(new UIAnimatedBackground(this));
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.mods":
                mc.displayGuiScreen(new GuiModList(this));
                break;
            case "button.back":
                close();
                break;
        }
    }
}
