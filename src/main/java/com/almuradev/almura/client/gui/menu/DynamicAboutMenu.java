/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.util.Colors;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.GuiModList;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;

public class DynamicAboutMenu extends SimpleGui {

    public DynamicAboutMenu(SimpleGui parent) {
        super(parent);
    }

    @Override
    public void construct() {

        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "About");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create About us multi-line label
        final UITextField aboutUsLabel = new UITextField(this, "", true);

        String fieldText =
                "Almura 2.0 began June 1st, 2014.  Based on the idea that we could finally get away from the broken and abandoned Spoutcraft client"
                        + " a brilliant developer came to Almura and said, \"Why don't you get rid of that out of date client and move into the "
                        + "present?\" This brilliant developer's name is " + Colors.AQUA + "Zidane" + Colors.RESET + ". Along with him and another "
                        + "outstanding developer " + Colors.AQUA + "Grinch" + Colors.RESET
                        + ", Almura 2.0 was born.  Using the forge client as our basis these two developers, along with " + Colors.GOLD + "Dockter's"
                        + Colors.RESET + " content and gui abilities, built, in our opinion, one of the best content loading /"
                        + " gui enabled Minecraft experiences ever conceived.\n\n" + Colors.LIGHT_PURPLE + "More info to follow...";

        aboutUsLabel.setSize(290, form.getContentContainer().getHeight() - 30);
        aboutUsLabel.setPosition(0, 5, Anchor.CENTER);
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

        form.getContentContainer().add(aboutUsLabel, modsButton, backButton);

        addToScreen(new UIAnimatedBackground(this));
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.mods":
                mc.displayGuiScreen(new GuiModList(this));
                break;
            case "button.back":
                close();
                break;
        }
    }
}
