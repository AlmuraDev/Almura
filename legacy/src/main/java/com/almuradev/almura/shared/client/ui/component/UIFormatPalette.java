/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.util.FontColors;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.List;

/**
 * A small square version of the {@link WYSIWYGTextBox} formatter bar for a text box elsewhere on the screen: the 16 colors in a 4 x 4
 * grid, bold/underline/italic/strikethrough beside them and reset along the bottom. Clicking one wraps the selected text in it, or puts it
 * at the cursor.
 */
@SideOnly(Side.CLIENT)
public class UIFormatPalette extends BasicContainer<UIFormatPalette> {

    private static final List<TextColor> colors = Arrays.asList(
            TextColors.DARK_BLUE, TextColors.BLUE, TextColors.DARK_AQUA, TextColors.AQUA,
            TextColors.DARK_RED, TextColors.RED, TextColors.YELLOW, TextColors.GOLD,
            TextColors.DARK_GREEN, TextColors.GREEN, TextColors.DARK_PURPLE, TextColors.LIGHT_PURPLE,
            TextColors.WHITE, TextColors.GRAY, TextColors.DARK_GRAY, TextColors.BLACK
    );

    private static final List<TextFormatting> styles = Arrays.asList(
            TextFormatting.BOLD, TextFormatting.UNDERLINE, TextFormatting.ITALIC, TextFormatting.STRIKETHROUGH
    );

    private static final int cellSize = 10;
    private static final int cellPadding = 1;
    private static final int columns = 5;
    // 5 cells and the gaps between them, both ways
    public static final int SIZE = columns * cellSize + (columns - 1) * cellPadding;

    private final BasicTextBox target;

    public UIFormatPalette(final BasicScreen screen, final BasicTextBox target) {
        super(SIZE, SIZE);
        this.target = target;
        this.setPadding(0, 0);
        this.setBackgroundAlpha(0);

        for (int i = 0; i < colors.size(); i++) {
            final TextColor color = colors.get(i);
            final BasicContainer<?> cell = this.cell(screen, "color." + color.getId(), i % 4, i / 4, cellSize, color.getName());
            cell.setColor(color.getColor().getRgb());
            cell.attachData(color);
            this.add(cell);
        }

        for (int i = 0; i < styles.size(); i++) {
            final TextFormatting style = styles.get(i);
            final BasicContainer<?> cell = this.cell(screen, "style." + style.getFriendlyName().toLowerCase(), 4, i, cellSize,
                    style.getFriendlyName());
            cell.add(this.label(screen, style + style.getFriendlyName().substring(0, 1)));
            cell.attachData(style);
            this.add(cell);
        }

        final BasicContainer<?> reset = this.cell(screen, "style.reset", 0, 4, SIZE, TextFormatting.RESET.getFriendlyName());
        reset.add(this.label(screen, "Reset"));
        reset.attachData(TextFormatting.RESET);
        this.add(reset);
    }

    private BasicContainer<?> cell(final BasicScreen screen, final String name, final int column, final int row, final int width,
            final String tooltip) {
        final BasicContainer<?> cell = new BasicContainer<>(width, cellSize);
        cell.setPosition(column * (cellSize + cellPadding), row * (cellSize + cellPadding));
        cell.setPadding(0, 0);
        cell.setBorder(FontColors.WHITE, 1, 185);
        cell.setColor(0);
        cell.setName("container." + name);
        cell.setTooltip(tooltip);
        return cell;
    }

    private UILabel label(final BasicScreen screen, final String text) {
        final UILabel label = new UILabel(text);
        label.setFontOptions(UIConstants.DEFAULT_TEXTBOX_FO.toBuilder().scale(0.8f).build());
        label.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        return label;
    }

    @Override
    public boolean onClick() {
        final UIComponent componentAt = this.getComponentAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
        final Object data = componentAt == null ? null : componentAt.getData() != null ? componentAt.getData()
                : componentAt.getParent() == null ? null : componentAt.getParent().getData();
        if (data instanceof TextColor) {
            this.apply(TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of((TextColor) data)));
            return true;
        } else if (data instanceof TextFormatting) {
            this.apply(data.toString());
            return true;
        }
        return super.onClick();
    }

    private void apply(final String code) {
        if (!this.target.isEditable()) {
            return;
        }
        if (this.target.getSelectedText().isEmpty()) {
            this.target.addText(code);
        } else {
            this.target.wrap(code, TextFormatting.RESET.toString());
        }
        this.target.focus();
    }

    @Override
    public void render(final GuiRenderer renderer) {
        // Highlight the cell under the mouse, like the formatter bar
        final UIComponent hovered = this.getComponentAt(MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y());
        for (final UIComponent component : this.getComponents()) {
            if (!(component instanceof BasicContainer)) {
                continue;
            }
            final BasicContainer<?> cell = (BasicContainer<?>) component;
            final boolean isHovered = hovered == cell || hovered != null && hovered.getParent() == cell;
            if (cell.getData() instanceof TextColor) {
                cell.setBackgroundAlpha(isHovered ? 200 : 255);
            } else {
                cell.setColor(isHovered ? FontColors.GRAY : 0);
                cell.setBackgroundAlpha(isHovered ? 200 : 255);
            }
        }
        super.render(renderer);
    }
}
