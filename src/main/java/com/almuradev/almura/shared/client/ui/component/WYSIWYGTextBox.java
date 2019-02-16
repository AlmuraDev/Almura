/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import com.almuradev.almura.shared.util.TextUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent;
import net.malisis.core.util.FontColors;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.List;

public class WYSIWYGTextBox extends BasicContainer {

    private static final List<TextColor> colors = Arrays.asList(
            TextColors.DARK_BLUE, TextColors.BLUE,
            TextColors.DARK_AQUA, TextColors.AQUA,
            TextColors.DARK_RED, TextColors.RED,
            TextColors.YELLOW, TextColors.GOLD,
            TextColors.DARK_GREEN, TextColors.GREEN,
            TextColors.DARK_PURPLE, TextColors.LIGHT_PURPLE,
            TextColors.WHITE, TextColors.GRAY,
            TextColors.DARK_GRAY, TextColors.BLACK
    );

    private static final List<TextFormatting> styles = Arrays.asList(
            TextFormatting.BOLD, TextFormatting.UNDERLINE,
            TextFormatting.ITALIC, TextFormatting.STRIKETHROUGH,
            TextFormatting.RESET
    );

    private static final int colorContainerSize = 18;
    private static final int colorContainerPadding = 1;
    private final BasicContainer<?> colorStyleContainer, rawFormatContainer;
    private final UILabel collapsedLabel;
    private final BasicTextBox tbContent;
    private ToolbarState state = ToolbarState.EXPANDED;
    private boolean showRaw = false;

    @SuppressWarnings("unchecked")
    public WYSIWYGTextBox(final BasicScreen screen, final int height, final String text) {
        super(screen);

        this.collapsedLabel = new UILabel(screen, "...");
        this.collapsedLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
        this.collapsedLabel.setFontOptions(FontColors.GRAY_FO);

        this.setBackgroundAlpha(0);

        this.colorStyleContainer = new BasicContainer<>(screen, 0, colorContainerSize);
        this.colorStyleContainer.setColor(FontColors.GRAY);
        this.colorStyleContainer.add(this.collapsedLabel);

        // Generate 'buttons'
        int startX = 0;
        for (final TextColor color : colors) {
            final int currentWidth = this.colorStyleContainer.getWidth();
            final BasicContainer<?> colorContainer = this.generateColorContainer(screen, startX, color);
            colorContainer.attachData(color);
            startX += colorContainerSize + colorContainerPadding;
            this.colorStyleContainer.add(colorContainer);
            this.colorStyleContainer.setWidth(currentWidth + colorContainer.getWidth() + colorContainerPadding);
        }
        for (final TextFormatting style : styles) {
            final int currentWidth = this.colorStyleContainer.getWidth();
            final BasicContainer<?> styleContainer = this.generateStyleContainer(screen, startX, style);
            styleContainer.attachData(style);
            startX += colorContainerSize + colorContainerPadding;
            this.colorStyleContainer.add(styleContainer);
            this.colorStyleContainer.setWidth(currentWidth + styleContainer.getWidth() + colorContainerPadding);
        }
        this.rawFormatContainer = this.generateLabelContainer(screen, startX, "format", "#", "Toggle RAW");
        this.colorStyleContainer.add(this.rawFormatContainer);
        this.colorStyleContainer.setWidth(this.colorStyleContainer.getWidth() + this.rawFormatContainer.getWidth());

        // Content textbox
        this.tbContent = new BasicTextBox(screen, text, true);
        this.tbContent.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
        this.tbContent.getScrollbar().setAutoHide(true);

        this.width = this.colorStyleContainer.getWidth() + this.getLeftPadding() + this.getRightPadding();
        this.setHeight(height);

        this.add(this.colorStyleContainer, this.tbContent);

        this.updateControls();
    }

    @Override
    @Deprecated
    public UIComponent setSize(final int width, final int height) {
        throw new UnsupportedOperationException("This component does not support setting the 'width' property!");
    }

    @Override
    @Deprecated
    public BasicContainer setWidth(final int width) {
        throw new UnsupportedOperationException("This component does not support setting this property!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public WYSIWYGTextBox setHeight(final int height) {
        this.height = height;
        fireEvent(new SpaceChangeEvent.SizeChangeEvent<>(this, this.width, this.height));
        return this;
    }

    @Subscribe
    public void onResize(final SpaceChangeEvent.SizeChangeEvent event) {
        if (this.equals(event.getComponent())) {
            this.updateControls();
        }
    }

    @Override
    public boolean onClick(final int x, final int y) {

        final UIComponent<?> componentAt = this.getComponentAt(x, y);
        if (componentAt.getData() instanceof TextColor) {
            this.color((TextColor) componentAt.getData());
            return true;
        } else if (componentAt.getData() instanceof TextFormatting) {
            this.color((TextFormatting) componentAt.getData());
            return true;
        }

        if (this.rawFormatContainer.isInsideBounds(x, y)) {
            this.showRaw = !this.showRaw;
            this.format();
            return true;
        }

        if (this.colorStyleContainer.isInsideBounds(x, y)) {
            switch (this.state) {
                case COLLAPSED:
                    this.state = ToolbarState.EXPANDED;
                    break;
                case EXPANDED:
                    this.state = ToolbarState.COLLAPSED;
                    break;
            }

            this.updateControls();
            return true;
        }

        return super.onClick(x, y);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void draw(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        // Reset alpha values
        this.colorStyleContainer.getComponents()
                .stream()
                .filter(c -> c instanceof BasicContainer)
                .forEach(c -> ((BasicContainer) c).setBackgroundAlpha(255));
        // Reset colors
        this.colorStyleContainer.getComponents()
                .stream()
                .filter(c -> c instanceof BasicContainer && c.getData() instanceof TextFormatting
                        || (c.getName() != null && c.getName().startsWith("container.label")))
                .forEach(c -> ((BasicContainer) c).setColor(0));

        final UIComponent<?> componentAt = this.getComponentAt(mouseX, mouseY);
        if (componentAt != null) {
            if (componentAt.getData() instanceof TextColor) {
                ((BasicContainer) componentAt).setBackgroundAlpha(200);
            } else if (componentAt.getData() instanceof TextFormatting
                    || (componentAt.getName() != null && componentAt.getName().startsWith("container.label"))) {
                ((BasicContainer) componentAt).setColor(FontColors.GRAY);
                ((BasicContainer) componentAt).setBackgroundAlpha(200);
            }
        }
        super.draw(renderer, mouseX, mouseY, partialTick);
    }

    public BasicTextBox getTextBox() {
        return this.tbContent;
    }

    public void showToolbar() {
        this.state = ToolbarState.EXPANDED;
        this.updateControls();
    }

    public void collapseToolbar() {
        this.state = ToolbarState.COLLAPSED;
        this.updateControls();
    }

    public void hideToolbar() {
        this.state = ToolbarState.HIDDEN;
        this.updateControls();
    }

    private void updateControls() {
        final int y;
        int height = BasicScreen.getPaddedHeight(this);
        switch (state) {
            case COLLAPSED:
                final int collapsedContainerHeight = 4;
                this.collapsedLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
                this.colorStyleContainer.setVisible(true);
                this.colorStyleContainer.setHeight(collapsedContainerHeight);
                this.colorStyleContainer.setBackgroundAlpha(185);
                this.colorStyleContainer.getComponents().stream().filter(c -> c instanceof BasicContainer).forEach(c -> c.setVisible(false));
                y = BasicScreen.getPaddedY(this.colorStyleContainer, 0);
                height -= collapsedContainerHeight;
                break;
            case EXPANDED:
                final int expandedContainerHeight = colorContainerSize + 4;
                this.collapsedLabel.setPosition(0, 2, Anchor.BOTTOM | Anchor.CENTER);
                this.colorStyleContainer.setVisible(true);
                this.colorStyleContainer.setHeight(expandedContainerHeight);
                this.colorStyleContainer.setBackgroundAlpha(0);
                this.colorStyleContainer.getComponents().forEach(c -> c.setVisible(true));
                y = BasicScreen.getPaddedY(this.colorStyleContainer, colorContainerPadding);
                height -= expandedContainerHeight + colorContainerPadding;
                break;
            case HIDDEN:
                this.colorStyleContainer.setVisible(false);
                y = 0;
                break;
            default:
                y = 0;
        }
        this.tbContent.setPosition(0, y);
        this.tbContent.setSize(UIComponent.INHERITED, height);
    }

    private void format() {
        final float scrollPos = this.getTextBox().getScrollbar().getOffset();
        final BasicTextBox.CursorPosition cursorPos = this.getTextBox().getCursorPosition();

        final String currentContent = this.getTextBox().getText();
        if (this.showRaw) {
            // Need to convert the content from sectional -> ampersand
            this.getTextBox().setText(TextUtil.asUglyText(currentContent));
        } else {
            // Need to convert the content from ampersand -> sectional
            this.getTextBox().setText(TextUtil.asFriendlyText(currentContent));
        }
        this.getTextBox().getScrollbar().scrollTo(scrollPos);
        this.getTextBox().setCursorPosition(cursorPos.getXOffset(), cursorPos.getYOffset());
        this.getTextBox().focus();
    }

    private void color(final TextFormatting style) {
        this.color(style.toString());
    }

    @SuppressWarnings("deprecation")
    private void color(final TextColor color) {
        this.color(TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(color)));
    }

    private void color(final String value) {
        final float scrollPosition = this.tbContent.getScrollbar().getOffset();
        final BasicTextBox.CursorPosition cursorPosition = this.tbContent.getCursorPosition();

        if (this.tbContent.getSelectedText().isEmpty()) {
            // Add the value to the text
            this.tbContent.addText(value);
        } else {
            // Wrap the selected text with the value and a reset
            this.tbContent.wrap(value, TextFormatting.RESET.toString());
        }

        this.format();

        // Go back to the original position
        this.tbContent.getScrollbar().scrollTo(scrollPosition);
        this.tbContent.setCursorPosition(cursorPosition.getXOffset(), cursorPosition.getYOffset());

        // Focus the textbox
        this.tbContent.focus();
    }

    private BasicContainer<?> generateBaseContainer(final BasicScreen screen, final String name, final int x) {
        final BasicContainer<?> baseContainer = new BasicContainer<>(screen, colorContainerSize, colorContainerSize);
        baseContainer.setPosition(x, 0);
        baseContainer.setBorder(FontColors.WHITE, 1, 185);
        baseContainer.setName("container." + name);

        return baseContainer;
    }

    private BasicContainer<?> generateColorContainer(final BasicScreen screen, final int x, final TextColor color) {
        final BasicContainer<?> baseContainer = this.generateBaseContainer(screen, "color." + color.getId(), x);
        baseContainer.setBorder(FontColors.WHITE, 1, 185);
        baseContainer.setTooltip(color.getName());
        baseContainer.setColor(color.getColor().getRgb());

        return baseContainer;
    }

    private BasicContainer<?> generateStyleContainer(final BasicScreen screen, final int x, final TextFormatting style) {
        final BasicContainer<?> baseContainer = this.generateBaseContainer(screen, "style." + style.getFriendlyName().toLowerCase(), x);
        baseContainer.setTooltip(style.getFriendlyName());
        baseContainer.setColor(0);

        final UILabel formatLabel = new UILabel(screen, style + style.getFriendlyName().substring(0, 1));
        final int y = style == TextFormatting.BOLD || style == TextFormatting.ITALIC ? 1 : 0;
        formatLabel.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
        formatLabel.setPosition(0, y, Anchor.MIDDLE | Anchor.CENTER);
        baseContainer.add(formatLabel);

        return baseContainer;
    }

    private BasicContainer<?> generateLabelContainer(final BasicScreen screen, final int x, final String id, final String value, final String tooltip) {
        final BasicContainer<?> baseContainer = this.generateBaseContainer(screen, "label." + id, x);
        baseContainer.setTooltip(tooltip);
        baseContainer.setColor(0);

        final UILabel formatLabel = new UILabel(screen, value);
        formatLabel.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO);
        formatLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        baseContainer.add(formatLabel);

        return baseContainer;
    }

    private enum ToolbarState {
        COLLAPSED,
        EXPANDED,
        HIDDEN
    }
}
