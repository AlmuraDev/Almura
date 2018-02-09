/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageCreate extends SimpleScreen {

    private static final int padding = 4;

    @Inject
    private static ClientPageManager manager;

    private UITextField textFieldId, textFieldIndex, textFieldName;

    public SimplePageCreate(@Nullable GuiScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        final UIForm form = new UIForm(this, 150, 125, I18n.format("almura.guide.create.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setZIndex(50);

        // File name
        final UILabel labelId = new UILabel(this, I18n.format("almura.guide.label.id"));
        labelId.setAnchor(Anchor.TOP | Anchor.LEFT);

        this.textFieldId = new UITextField(this, "");
        this.textFieldId.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldId.setPosition(0, SimpleScreen.getPaddedY(labelId, 1));
        this.textFieldId.setSize(UIComponent.INHERITED, 0);
        this.textFieldId.setFocused(true);
        this.textFieldId.setFilter(String::toLowerCase);

        // Index
        final UILabel labelIndex = new UILabel(this, I18n.format("almura.guide.label.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, this.textFieldId.isVisible() ? SimpleScreen.getPaddedY(this.textFieldId, padding) : padding);

        this.textFieldIndex = new UITextField(this, Integer.toString(0));
        this.textFieldIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldIndex.setPosition(0, SimpleScreen.getPaddedY(labelIndex, 1));
        this.textFieldIndex.setSize(UIComponent.INHERITED, 0);
        this.textFieldIndex.setFilter(s -> s.replaceAll("[^\\d]", ""));

        // Title
        final UILabel labelName = new UILabel(this, I18n.format("almura.guide.label.name"));
        labelName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelName.setPosition(0, this.textFieldIndex.isVisible() ? SimpleScreen.getPaddedY(this.textFieldIndex, padding) : padding);

        this.textFieldName = new UITextField(this, "");
        this.textFieldName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldName.setPosition(0, SimpleScreen.getPaddedY(labelName, 1));
        this.textFieldName.setSize(UIComponent.INHERITED, 0);
        this.textFieldName.setFilter(s -> s.substring(0, Math.min(s.length(), 50)));

        // Save/Cancel
        final UIButton buttonSave = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .width(40)
                .listener(this)
                .build("button.save");

        final UIButton buttonCancel = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(SimpleScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
                .width(40)
                .listener(this)
                .build("button.cancel");

        form.add(labelId, this.textFieldId,
                labelIndex, this.textFieldIndex,
                labelName, this.textFieldName,
                buttonCancel, buttonSave);

        addToScreen(form);
        this.textFieldId.setFocused(true);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.cancel":
                close();
                break;
            case "button.save":
                final String id = this.textFieldId.getText().trim();
                final String rawIndex = this.textFieldIndex.getText();
                final int index = rawIndex.isEmpty() ? 0 : Integer.valueOf(rawIndex);
                final String name = this.textFieldName.getText().trim();
                manager.requestNewPage(id, index, name);

                close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == 15) {  //Tab
            if (textFieldId.isFocused()) {
                this.textFieldId.setFocused(false);
                this.textFieldIndex.setFocused(true);
                return;
            }

            if (textFieldIndex.isFocused()) {
                this.textFieldIndex.setFocused(false);
                this.textFieldName.setFocused(true);
                return;
            }

            if (this.textFieldName.isFocused()) {
                this.textFieldName.setFocused(false);
                this.textFieldId.setFocused(true);
                return;
            }
        }
        super.keyTyped(keyChar, keyCode);
    }
}
