/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.text.StyleRun;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextHighlighter;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The guide's page editor, in place of 3.5's: the page's text with its § codes, coloured as they colour it (the codes themselves grey), and
 * 3.5's formatter bar of the 16 colours, bold, italic, underline, strikethrough, reset and link. A colour or style applies to the selected
 * text (the formatting before it carries on after it), or is put in at the cursor. Client only.
 */
final class GuideEditor {

    // Minecraft's 16 colours, by code
    static final String CODES = "0123456789abcdef";
    static final int[] COLOURS = {0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA, 0xFFAA0000, 0xFFAA00AA, 0xFFFFAA00, 0xFFAAAAAA,
        0xFF555555, 0xFF5555FF, 0xFF55FF55, 0xFF55FFFF, 0xFFFF5555, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF};
    private static final String[] NAMES = {"Black", "Dark Blue", "Dark Green", "Dark Aqua", "Dark Red", "Dark Purple", "Gold", "Gray",
        "Dark Gray", "Blue", "Green", "Aqua", "Red", "Light Purple", "Yellow", "White"};
    private static final int CODE_COLOUR = 0xFF777777;
    private static final float SWATCH = 10f, STYLE_BUTTON = 16f;

    private GuideEditor() {
    }

    /**
     * The text as the editor shows it: its § codes as &amp; ones, as 3.5 kept them. The game's font takes a § for formatting, hiding the
     * code and leaving the cursor a character off from what is drawn; an &amp; is drawn like any other character.
     */
    static String toEditor(final String text) {
        return text.replaceAll("§([0-9a-fk-orA-FK-OR])", "&$1");
    }

    /**
     * The editor's text as the page keeps it, with § codes.
     */
    static String fromEditor(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    private static boolean isCode(final char code) {
        return "0123456789abcdefklmnor".indexOf(Character.toLowerCase(code)) >= 0;
    }

    /**
     * Colours the editor's text as its &amp; codes do, the codes grey.
     */
    static final TextHighlighter HIGHLIGHTER = (text, base) -> {
        final List<StyleRun> runs = new ArrayList<>();
        TextStyle style = base;
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '&' || i + 1 >= text.length() || !isCode(text.charAt(i + 1))) {
                continue;
            }
            if (i > start) {
                runs.add(new StyleRun(start, i, style));
            }
            runs.add(new StyleRun(i, i + 2, base.withColor(CODE_COLOUR)));
            style = apply(style, base, Character.toLowerCase(text.charAt(i + 1)));
            i++;
            start = i + 1;
        }
        if (start < text.length()) {
            runs.add(new StyleRun(start, text.length(), style));
        }
        return runs;
    };

    private static TextStyle apply(final TextStyle style, final TextStyle base, final char code) {
        final int colour = CODES.indexOf(code);
        if (colour >= 0) {
            return base.withColor(COLOURS[colour]);
        }
        return switch (code) {
            case 'l' -> style.withBold(true);
            case 'o' -> style.withItalic(true);
            case 'n' -> style.withUnderline(true);
            case 'm' -> style.withStrikethrough(true);
            case 'r' -> base;
            default -> style;
        };
    }

    /**
     * The formatter bar for the area; {@code edit} runs first (using the bar starts editing, as on 3.5), and {@code link} asks for a
     * link's address for the selected text.
     */
    static StackPanel bar(final ComponentFactory factory, final TextArea area, final Runnable edit, final Consumer<TextArea> link) {
        final StackPanel bar = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f);
        for (int i = 0; i < CODES.length(); i++) {
            final char code = CODES.charAt(i);
            final AnchorPanel swatch = factory.anchorPanel().setBackground(Paint.solid(COLOURS[i])).setBorder(Border.of(0xFFFFFFFF, 1f));
            swatch.setSize(SWATCH, SWATCH).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
            swatch.setTooltip(Text.of(NAMES[i]).color(COLOURS[i]));
            swatch.onEvent(InputEvents.CLICK, event -> {
                edit.run();
                format(area, code);
            });
            bar.add(swatch);
        }
        bar.add(style(factory, area, edit, 'l', Text.of("B").bold(), "Bold"));
        bar.add(style(factory, area, edit, 'o', Text.of("I").italic(), "Italic"));
        bar.add(style(factory, area, edit, 'n', Text.of("U").underline(), "Underline"));
        bar.add(style(factory, area, edit, 'm', Text.of("S").strikethrough(), "Strikethrough"));
        bar.add(style(factory, area, edit, 'r', Text.of("R"), "Reset"));
        bar.add(factory.button(Text.of("Link")).setSize(30f, STYLE_BUTTON).setTooltip(Text.of("Make the selected text a link"))
            .onEvent(Button.PRESSED, event -> {
                edit.run();
                link.accept(area);
            }).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        return bar;
    }

    private static Button style(final ComponentFactory factory, final TextArea area, final Runnable edit, final char code, final Text label,
        final String name) {
        return factory.button(label).setSize(STYLE_BUTTON, STYLE_BUTTON).setTooltip(Text.of(name))
            .onEvent(Button.PRESSED, event -> {
                edit.run();
                format(area, code);
            }).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
    }

    /**
     * Puts a code in: around the selection, the formatting before it carrying on after it, or at the cursor.
     */
    static void format(final TextArea area, final char code) {
        final String text = area.text();
        final int start = Math.min(area.selectionStart(), area.selectionEnd());
        final int end = Math.max(area.selectionStart(), area.selectionEnd());
        if (start == end) {
            final int at = area.caret();
            area.setText(text.substring(0, at) + "&" + code + text.substring(at));
            area.select(at + 2, at + 2);
            return;
        }
        final String before = toEditor(GuideText.formatAfter("", fromEditor(text.substring(0, start))));
        final String inserted = "&" + code + text.substring(start, end) + "&r" + before;
        area.setText(text.substring(0, start) + inserted + text.substring(end));
        area.select(start, start + inserted.length());
    }

    /**
     * Whether the selection can become a link: some text, without brackets or line breaks.
     */
    static boolean canLink(final TextArea area) {
        final String selected = area.selectedText();
        return !selected.isEmpty() && selected.indexOf('[') < 0 && selected.indexOf(']') < 0 && selected.indexOf('\n') < 0;
    }

    /**
     * Makes the selection a link to the address.
     */
    static void link(final TextArea area, final String address) {
        final String text = area.text();
        final int start = Math.min(area.selectionStart(), area.selectionEnd());
        final int end = Math.max(area.selectionStart(), area.selectionEnd());
        final String link = "[" + text.substring(start, end) + "](" + address + ")";
        area.setText(text.substring(0, start) + link + text.substring(end));
        area.select(start + link.length(), start + link.length());
    }
}
