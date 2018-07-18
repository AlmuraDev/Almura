/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.awt.AWTException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

public class SimpleConfirmRemove extends SimpleScreen {

    public SimpleConfirmRemove(@Nullable GuiScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        // Create the form
        final UIForm form = new UIForm(this, 250, 60, I18n.format("almura.guide.view.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Confirmation Window Text
        final UILabel line1 = new UILabel(this, "Do you wish to delete the selected guide?");
        line1.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);

        final UIButton yesButton = new UIButton(this, "Yes");
        yesButton.setSize(30, 10);
        yesButton.setPosition(-45, 0, Anchor.RIGHT | Anchor.BOTTOM);
        yesButton.setName("button.yes");
        yesButton.register(this);

        // Cancel
        final UIButton noButton = new UIButton(this, "No");
        noButton.setSize(30, 10);
        noButton.setPosition(-10, 0, Anchor.RIGHT | Anchor.BOTTOM);
        noButton.setName("button.no");
        noButton.register(this);        

        form.add(line1, yesButton, noButton);
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.yes":
                SimplePageView.manager.requestRemovePage(SimplePageView.manager.getPage().getId());
                close();
                break;
            case "button.no":
                close();
                break;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            close();
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof SimpleConfirmRemove)) {
            this.close();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
