/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.dialog;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.UIExpandingLabel;
import com.almuradev.almura.client.gui.component.UIForm;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.builder.UIButtonBuilder;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class UIMessageBox extends UIForm {
    private static final int buttonPadding = 4;
    private static final int topPadding = 20;
    private static final int bottomPadding = 30;
    private final MessageBoxConsumer consumer;
    private final MessageBoxButtons messageBoxButtons;
    private final String message;
    private MessageBoxResult result;

    private UIMessageBox(MalisisGui gui, String title, String message, MessageBoxButtons buttons, @Nullable MessageBoxConsumer
            consumer) {
        super(gui, 0, 0, title);
        this.messageBoxButtons = buttons;
        this.message = message;
        this.consumer = consumer;
        this.construct(gui);
    }

    private void construct(MalisisGui gui) {
        this.setTopPadding(topPadding);
        this.setBottomPadding(bottomPadding);

        if (!message.isEmpty()) {
            final UIExpandingLabel messageLabel = new UIExpandingLabel(gui, this.message, true);
            final int width = gui.width == 0 ? Math.max(messageLabel.getWidth() + 3, 250) : Math.min(Math.max(messageLabel.getWidth() + 3, 250),
                    gui.width);
            setSize(width, topPadding + messageLabel.getContentHeight() +bottomPadding + 10);

            add(messageLabel);
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
        buttonContainer.setSize(width, Constants.Gui.BUTTON_HEIGHT);
        buttonContainer.setPosition(0, 25, Anchor.BOTTOM | Anchor.CENTER);
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
        }
    }

    @Override
    public void onClose() {
        if (consumer != null) {
            try {
                consumer.accept(result);
            } catch (Exception e) {
                Almura.instance.logger.error("An exception occurred while executing the consumer!", e);
            }
        }
        super.onClose();
    }

    @Override
    public UIMessageBox setClosable(boolean closable) {
        throw new UnsupportedOperationException("This component does not support the ability to close via a X button.");
    }

    private UIButton buildButton(MalisisGui gui, String name) {
        switch (name.toLowerCase()) {
            case "button.cancel":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.cancel")))
                        .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                        .listener(this)
                        .build("button.cancel");
            case "button.close":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("almura.menu.close")))
                        .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                        .listener(this)
                        .build("button.close");
            case "button.no":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.no")))
                        .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                        .listener(this)
                        .build("button.no");
            case "button.yes":
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("gui.yes")))
                        .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                        .listener(this)
                        .build("button.yes");
            default:
                return new UIButtonBuilder(gui)
                        .text(Text.of(I18n.format("almura.menu.ok")))
                        .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                        .listener(this)
                        .build("button.ok");
        }
    }

    public static void showDialog(SimpleScreen gui, String title, String message, MessageBoxButtons buttons) {
        showDialog(gui, title, message, buttons, null);
    }

    public static void showDialog(SimpleScreen gui, String title, String message, MessageBoxButtons buttons, @Nullable MessageBoxConsumer consumer) {
        final SimpleScreen screen = new SimpleScreen(gui) {
            @Override
            public void construct() {
                guiscreenBackground = false;
                // Disable escape key press
                registerKeyListener((keyChar, keyCode) -> keyCode == Keyboard.KEY_ESCAPE);
                addToScreen(new UIMessageBox(this, title, message, buttons, consumer)
                        .setMovable(true)
                        .setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER)
                );
            }

            @Override
            public void drawScreen(int mouseX, int mouseY, float partialTick) {
                parent.ifPresent(screen -> screen.drawScreen(mouseX, mouseY, partialTick));
                super.drawScreen(mouseX, mouseY, partialTick);
            }

            @Override
            public void updateScreen() {
                parent.ifPresent(MalisisGui::updateScreen);
                super.updateScreen();
            }

            @Override
            public void onResize(Minecraft minecraft, int width, int height) {
                parent.ifPresent(screen -> screen.onResize(minecraft, width, height));
                super.onResize(minecraft, width, height);
            }

            @Override
            public void update(int mouseX, int mouseY, float partialTick) {
                parent.ifPresent(screen -> screen.update(mouseX, mouseY, partialTick));
                super.update(mouseX, mouseY, partialTick);
            }

            @Override
            public void updateGui() {
                parent.ifPresent(MalisisGui::updateGui);
                super.updateGui();
            }
        };
        screen.display();
    }
}
