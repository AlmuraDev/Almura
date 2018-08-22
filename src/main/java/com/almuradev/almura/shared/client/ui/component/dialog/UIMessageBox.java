/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component.dialog;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIMessageBox extends UIForm {
    private static final int buttonPadding = 4;
    private static final int bottomPadding = 23;
    private final MessageBoxButtons messageBoxButtons;
    private final String message;
    @Nullable private final MessageBoxConsumer consumer;
    @Nullable private MessageBoxResult result;

    private UIMessageBox(MalisisGui gui, String title, String message, MessageBoxButtons buttons, @Nullable MessageBoxConsumer consumer) {
        super(gui, 300, 115, title);
        this.messageBoxButtons = buttons;
        this.message = message;
        this.consumer = consumer;
        this.construct(gui);
        this.setClosable(true);
    }

    private void construct(MalisisGui gui) {
        setBottomPadding(bottomPadding);

        if (!this.message.isEmpty()) {
            final UILabel messageLabel = new UILabel(gui, this.message, true);
            messageLabel.setSize(SimpleScreen.getPaddedWidth(this), SimpleScreen.getPaddedHeight(this) - (buttonPadding * 2));
            messageLabel.setFontOptions(FontColors.WHITE_FO);

            final UISlimScrollbar scrollbar = new UISlimScrollbar(gui, messageLabel, UIScrollBar.Type.VERTICAL);
            scrollbar.setAutoHide(true);
            add(messageLabel.setAnchor(Anchor.CENTER | Anchor.MIDDLE));
        }

        final UIBackgroundContainer buttonContainer = new UIBackgroundContainer(gui);
        final List<UIButton> buttons = new ArrayList<>();
        switch (this.messageBoxButtons) {
            case OK:
                buttons.add(buildButton(gui, "button.ok"));
                break;
            case OK_CANCEL:
                buttons.add(buildButton(gui, "button.ok"));
                buttons.add(buildButton(gui, "button.cancel"));
                break;
            case YES_NO:
                buttons.add(buildButton(gui, "button.yes"));
                buttons.add(buildButton(gui, "button.no"));
                break;
            case YES_NO_CANCEL:
                buttons.add(buildButton(gui, "button.yes"));
                buttons.add(buildButton(gui, "button.no"));
                buttons.add(buildButton(gui, "button.cancel"));
                break;
            case CLOSE:
                buttons.add(buildButton(gui, "button.close"));
        }

        int width = 0;
        int x = 0;
        for (UIButton button : buttons) {
            buttonContainer.add(button.setPosition(x, 0));
            x += button.getWidth() + buttonPadding;
            width += button.getWidth();
        }

        width += buttonPadding * (buttons.size() - 1);
        buttonContainer.setSize(width, GuiConfig.Button.HEIGHT_TINY);
        buttonContainer.setPosition(0, bottomPadding + -buttonPadding, Anchor.BOTTOM | Anchor.RIGHT);
        buttonContainer.setBackgroundAlpha(0);
        add(buttonContainer);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.cancel":
                this.result = MessageBoxResult.CANCEL;
                onClose();
                break;
            case "button.close":
                this.result = MessageBoxResult.CLOSE;
                onClose();
                break;
            case "button.no":
                this.result = MessageBoxResult.NO;
                onClose();
                break;
            case "button.ok":
                this.result = MessageBoxResult.OK;
                onClose();
                break;
            case "button.yes":
                this.result = MessageBoxResult.YES;
                onClose();
                break;
            case "button.form.close":
                switch (messageBoxButtons) {
                    case OK:
                        this.result = MessageBoxResult.OK;
                        onClose();
                        break;
                    case OK_CANCEL:
                        this.result = MessageBoxResult.CANCEL;
                        onClose();
                        break;
                    case CLOSE:
                        this.result = MessageBoxResult.CLOSE;
                        onClose();
                        break;
                    case YES_NO:
                        this.result = MessageBoxResult.NO;
                        onClose();
                        break;
                    case YES_NO_CANCEL:
                        this.result = MessageBoxResult.CANCEL;
                        onClose();
                        break;
                }
        }
    }

    @Override
    public void onClose() {
        if (this.consumer != null) {
            try {
                this.consumer.accept(this.result);
            } catch (Exception e) {
                ClientStaticAccess.logger.error("An exception occurred while executing the consumer!", e);
            }
        }
        super.onClose();
    }

    private UIButton buildButton(MalisisGui gui, String name) {
        switch (name.toLowerCase()) {
            case "button.cancel":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.cancel")))
                        .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT_TINY)
                        .listener(this)
                        .build("button.cancel");
            case "button.close":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("almura.menu_button.close")))
                        .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT_TINY)
                        .listener(this)
                        .build("button.close");
            case "button.no":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.no")))
                        .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT_TINY)
                        .listener(this)
                        .build("button.no");
            case "button.yes":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.yes")))
                        .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT_TINY)
                        .listener(this)
                        .build("button.yes");
            default:
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("almura.menu_button.ok")))
                        .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT_TINY)
                        .listener(this)
                        .build("button.ok");
        }
    }

    public static void showDialog(@Nullable GuiScreen gui, String title, String message, MessageBoxButtons buttons) {
        showDialog(gui, title, message, buttons, null);
    }

    public static void showDialog(@Nullable GuiScreen gui, String title, String message, MessageBoxButtons buttons, @Nullable MessageBoxConsumer
            consumer) {
        final MessageBoxDialogScreen screen = new MessageBoxDialogScreen(gui, title, message, buttons, consumer);
        screen.display();
    }

    public static class MessageBoxDialogScreen extends SimpleScreen {

        @Nullable private final GuiScreen gui;
        private final String title;
        private final String message;
        private final MessageBoxButtons buttons;
        @Nullable private final MessageBoxConsumer consumer;

        public MessageBoxDialogScreen(@Nullable GuiScreen gui, String title, String message, MessageBoxButtons buttons, @Nullable MessageBoxConsumer
                consumer) {
            super(gui, true);
            this.gui = gui;
            this.title = title;
            this.message = message;
            this.buttons = buttons;
            this.consumer = consumer;
        }

        @Override
        public void construct() {
            guiscreenBackground = false;
            // Disable escape key press
            registerKeyListener((keyChar, keyCode) -> keyCode == Keyboard.KEY_ESCAPE);
            addToScreen(new UIMessageBox(this, title, message, buttons, consumer)
                    .setMovable(true)
                    .setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER)
                    .setZIndex(50)
            );
        }

        @Nullable
        public GuiScreen getParent() {
            return this.gui;
        }
    }
}
