/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Asks for the web address of a guide link, over the guide.
 */
@SideOnly(Side.CLIENT)
final class GuideLinkScreen extends BasicScreen {

    private static final int WIDTH = 220;
    private static final int PADDING = 4;

    private final String linkText;
    private final String url;
    private final Consumer<String> onDone;
    @Nullable private final Runnable onCancel;
    private boolean confirmed;
    private BasicTextBox tbUrl;
    private UILabel errorLabel;

    /**
     * @param linkText The text the link is on
     * @param url The current address, empty for a new link
     * @param onDone Given the address (checked, http:// added when missing) once the player confirms it, after this closes
     * @param onCancel Run when this closes without an address (cancel, escape), or null
     */
    GuideLinkScreen(final GuiScreen parent, final String linkText, final String url, final Consumer<String> onDone,
            @Nullable final Runnable onCancel) {
        super(parent, true);
        this.linkText = linkText;
        this.url = url;
        this.onDone = onDone;
        this.onCancel = onCancel;
    }

    @Override
    public void close() {
        super.close();
        if (!this.confirmed && this.onCancel != null) {
            this.onCancel.run();
        }
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(WIDTH, 90, this.url.isEmpty() ? "Create Link" : "Edit Link");
        form.setZIndex(10);
        form.setBackgroundAlpha(255);
        final int fieldWidth = BasicScreen.getPaddedWidth(form);

        final UILabel textLabel = new UILabel(TextFormatting.GRAY + "Text: " + TextFormatting.WHITE
                + TextFormatting.getTextWithoutFormattingCodes(this.linkText));
        textLabel.setAnchor(Anchor.TOP | Anchor.LEFT);

        final UILabel urlLabel = new UILabel("Web address:");
        urlLabel.setFontOptions(FontColors.WHITE_FO);
        urlLabel.setAnchor(Anchor.TOP | Anchor.LEFT);
        urlLabel.setPosition(0, BasicScreen.getPaddedY(textLabel, PADDING));

        this.tbUrl = new BasicTextBox(this.url);
        this.tbUrl.setAcceptsReturn(false);
        this.tbUrl.setAcceptsTab(false);
        this.tbUrl.setOnEnter(tb -> this.done());
        // Text copied from the guide carries its colour codes; an address has none ('&' stays, it is common in addresses)
        this.tbUrl.setFilter(TextFormatting::getTextWithoutFormattingCodes);
        this.tbUrl.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbUrl.setPosition(0, BasicScreen.getPaddedY(urlLabel, 1));
        this.tbUrl.setSize(fieldWidth, 0);

        this.errorLabel = new UILabel("");
        this.errorLabel.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.errorLabel.setPosition(0, BasicScreen.getPaddedY(this.tbUrl, 2));

        final UIButton buttonDone = new UIButtonBuilder()
                .text(I18n.format("almura.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .width(40)
                .onClick(this::done)
                .build("button.done");

        final UIButton buttonCancel = new UIButtonBuilder()
                .text(I18n.format("almura.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(BasicScreen.getPaddedX(buttonDone, 2, Anchor.RIGHT), 0)
                .width(40)
                .onClick(this::close)
                .build("button.cancel");

        form.add(textLabel, urlLabel, this.tbUrl, this.errorLabel, buttonDone, buttonCancel);
        this.addToScreen(form);
        this.tbUrl.focus();
    }

    private void done() {
        final String address = GuideLinks.toWebAddress(this.tbUrl.getText());
        if (address == null) {
            this.errorLabel.setText(TextFormatting.RED + "Enter a web address (http or https).");
            return;
        }
        this.confirmed = true;
        this.close();
        this.onDone.accept(address);
    }
}
