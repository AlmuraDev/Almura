/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.client.PermissionsHelper;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.network.play.C00PageInformation;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.util.Predicates;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;

import java.text.ParseException;

@SideOnly(Side.CLIENT)
public class CreatePageGui extends SimpleGui {

    private UIButton buttonSave, buttonCancel;
    private UILabel labelFileName, labelIndex, labelTitle;
    private UITextField textFieldFileName, textFieldIndex, textFieldTitle;

    public CreatePageGui(ViewPagesGui parent) {
        super(parent);
    }

    @Override
    public void construct() {
        final int textFieldTopPadding = 1;
        final int padding = 4;

        final UIForm form = new UIForm(this, 150, 114, "Guide - Create Page");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setName("form.guide.create");
        form.setColor(ViewPagesGui.CONTROL.getGuiColorCode());
        form.setBackgroundAlpha(255);

        labelFileName = new UILabel(this, "File Name");
        labelFileName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelFileName.setPosition(padding, padding);
        labelFileName.getFontRenderOptions().shadow = false;

        textFieldFileName = new UITextField(this, "");
        textFieldFileName.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldFileName.setPosition(padding, SimpleGui.getPaddedY(labelFileName, textFieldTopPadding));
        textFieldFileName.setSize(form.getWidth() - (padding * 2), 0);
        textFieldFileName.setTooltip(new UITooltip(this, "File must not exist already.", 20));
        textFieldFileName.setFocused(true);
        textFieldFileName.getFontRenderOptions().shadow = false;

        labelIndex = new UILabel(this, "Index");
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(padding, textFieldFileName.isVisible() ? SimpleGui.getPaddedY(textFieldFileName, padding) : padding);
        labelIndex.getFontRenderOptions().shadow = false;

        textFieldIndex = new UITextField(this, "" + 0);
        textFieldIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldIndex.setPosition(padding, SimpleGui.getPaddedY(labelIndex, textFieldTopPadding));
        textFieldIndex.setSize(form.getWidth() - 8, 0);
        textFieldIndex.setTooltip(new UITooltip(this, "Value must be equal to or greater than 0.", 20));
        textFieldIndex.setValidator(new Predicates.IntegerFilterPredicate());
        textFieldIndex.getFontRenderOptions().shadow = false;

        labelTitle = new UILabel(this, "Title");
        labelTitle.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelTitle.setPosition(padding, textFieldIndex.isVisible() ? SimpleGui.getPaddedY(textFieldIndex, padding) : padding);
        labelTitle.getFontRenderOptions().shadow = false;

        textFieldTitle = new UITextField(this, "");
        textFieldTitle.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldTitle.setPosition(padding, SimpleGui.getPaddedY(labelTitle, textFieldTopPadding));
        textFieldTitle.setSize(form.getWidth() - (padding * 2), 0);
        textFieldTitle.setTooltip(new UITooltip(this, "Name must not be greater than 100 characters.", 20));
        textFieldTitle.setValidator(new Predicates.StringLengthPredicate(1, 100));
        textFieldTitle.getFontRenderOptions().shadow = false;

        buttonCancel = new UIButton(this, "Cancel");
        buttonCancel.setAnchor(Anchor.BOTTOM | Anchor.RIGHT);
        buttonCancel.setPosition(-padding, -padding);
        buttonCancel.setSize(0, 15);
        buttonCancel.setName("form.guide.create.button.cancel");
        buttonCancel.getFontRenderOptions().shadow = false;
        buttonCancel.getHoveredFontRendererOptions().shadow = false;
        buttonCancel.register(this);

        buttonSave = new UIButton(this, "Save");
        buttonSave.setAnchor(Anchor.BOTTOM | Anchor.RIGHT);
        buttonSave.setPosition(SimpleGui.getPaddedX(buttonCancel, 2, Anchor.RIGHT), -padding);
        buttonSave.setSize(0, 10);
        buttonSave.setName("form.guide.create.button.save");
        buttonSave.getFontRenderOptions().shadow = false;
        buttonSave.getHoveredFontRendererOptions().shadow = false;
        buttonSave.register(this);

        form.getContentContainer().add(labelFileName, textFieldFileName, labelIndex, textFieldIndex, labelTitle,
                textFieldTitle, buttonSave, buttonCancel);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) throws ParseException {
        switch (event.getComponent().getName()) {
            case "form.guide.create.button.save":
                if (PermissionsHelper.hasPermission(PermissionsHelper.PERMISSIBLE_GUIDE, "create")) {
                    textFieldFileName.setText(textFieldFileName.getText().trim());
                    if (textFieldFileName.getText().isEmpty() || textFieldIndex.getText().isEmpty() || textFieldTitle.getText().isEmpty()) {
                        break;
                    }
                    if (PageRegistry.getPage(textFieldFileName.getText()).isPresent()) {
                        break;
                    }
                    CommonProxy.NETWORK_FORGE.sendToServer(
                            new C00PageInformation(textFieldFileName.getText().replace(".yml", ""),
                                    Integer.parseInt(textFieldIndex.getText()), textFieldTitle.getText(), ""));
                }
                close();
            case "form.guide.create.button.cancel":
                close();
                break;
        }
    }
}
