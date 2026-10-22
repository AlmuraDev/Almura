/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.client.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

/**
 * Asks for an account name (new account, rename).
 */
@SideOnly(Side.CLIENT)
public final class BankNameScreen extends BasicScreen implements BankScreen.Child {

    private static final int MAX_NAME_LENGTH = 32;

    private final BankScreen bankScreen;
    private final String title, initialName;
    private final Consumer<String> onSave;
    private BasicTextBox nameTextBox;

    BankNameScreen(final BankScreen parent, final String title, final String initialName, final Consumer<String> onSave) {
        super(parent, true);
        this.bankScreen = parent;
        this.title = title;
        this.initialName = initialName;
        this.onSave = onSave;
    }

    @Override
    public BankScreen getBankScreen() {
        return this.bankScreen;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(this, 160, 62, this.title);
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        this.nameTextBox = new BasicTextBox(this, this.initialName);
        this.nameTextBox.setSize(UIComponent.INHERITED, 0);
        this.nameTextBox.setAcceptsReturn(false);
        this.nameTextBox.setAcceptsTab(false);
        this.nameTextBox.setFilter(s -> s.substring(0, Math.min(s.length(), MAX_NAME_LENGTH)));
        this.nameTextBox.setOnEnter(tb -> this.save());

        final UIButton buttonSave = new UIButtonBuilder(this)
            .text(I18n.format("almura.button.save"))
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::save)
            .build("button.save");

        final UIButton buttonCancel = new UIButtonBuilder(this)
            .text(I18n.format("almura.button.cancel"))
            .width(40)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .position(BasicScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
            .onClick(this::close)
            .build("button.cancel");

        form.add(this.nameTextBox, buttonSave, buttonCancel);
        this.addToScreen(form);

        this.nameTextBox.focus();
        this.nameTextBox.selectAll();
    }

    private void save() {
        final String name = this.nameTextBox.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        this.onSave.accept(name);
        this.close();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
