/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.layout.WrapPanel;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A guide page's text, as 3.5 showed it: § colour and style codes, which carry on from line to line, and links written
 * {@code [text](address)}, shown as their text in blue, underlined, that open the address (after the game asks) when clicked. Client only.
 */
final class GuideText {

    // 3.5's link markup
    static final Pattern LINK = Pattern.compile("\\[([^\\[\\]\\n]+)]\\(([^()\\s]+)\\)");
    private static final int LINK_COLOR = 0xFF5555FF;
    // A space between the words of a line holding links, which are laid out word by word
    private static final float SPACE = 4f;

    private GuideText() {
    }

    /**
     * The page's text, a line a paragraph. A line without links wraps as one label; one with links is laid out word by word, so its links
     * can be clicked.
     */
    static StackPanel build(final ComponentFactory factory, final String content) {
        return build(factory, content, (label, index, address) -> {
        });
    }

    /**
     * A link in the page, to give a right click menu: its label, which link of the page it is (from 0) and its address.
     */
    @FunctionalInterface
    interface LinkMenus {

        void give(Label label, int index, String address);
    }

    /**
     * The page's text, each link given its right click menu.
     */
    static StackPanel build(final ComponentFactory factory, final String content, final LinkMenus menus) {
        final StackPanel lines = factory.stackPanel().setSpacing(0f);
        int link = 0;
        String format = "";
        for (final String line : content.replace("\r", "").split("\n", -1)) {
            if (line.isEmpty()) {
                lines.add(factory.label(" ").setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
                continue;
            }
            final Matcher links = LINK.matcher(line);
            if (!links.find()) {
                lines.add(factory.label(MinecraftText.of(format + line)).setWrap(true)
                    .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
                format = formatAfter(format, line);
                continue;
            }
            links.reset();
            final WrapPanel words = factory.wrapPanel().setSpacing(SPACE, 0f);
            int at = 0;
            while (links.find()) {
                format = addWords(factory, words, format, line.substring(at, links.start()));
                words.add(link(factory, links.group(1), links.group(2), link++, menus));
                at = links.end();
            }
            format = addWords(factory, words, format, line.substring(at));
            lines.add(words.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        }
        return lines;
    }

    /**
     * Adds the text's words, each in the formatting in force at it.
     *
     * @return the formatting after it
     */
    private static String addWords(final ComponentFactory factory, final WrapPanel words, final String format, final String text) {
        String current = format;
        for (final String word : text.split(" ")) {
            if (!stripped(word).isEmpty()) {
                words.add(factory.label(MinecraftText.of(current + word)));
            }
            current = formatAfter(current, word);
        }
        return current;
    }

    private static Component<?> link(final ComponentFactory factory, final String text, final String address, final int index,
        final LinkMenus menus) {
        final Label label = factory.label(Text.of(stripped(text)).color(LINK_COLOR).underline());
        label.setTooltip(Text.of(address));
        // A left click opens it; a right click is its menu's
        label.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT) {
                open(address);
            }
        });
        menus.give(label, index, address);
        return label;
    }

    /**
     * Opens a link's address in the browser, after the game asks, as 3.5 did; only web addresses (http is added when it names none).
     */
    static void open(final String address) {
        final URI uri = webAddress(address);
        if (uri != null) {
            final Minecraft minecraft = Minecraft.getInstance();
            ConfirmLinkScreen.confirmLinkNow(minecraft.gui.screen(), uri);
        }
    }

    static @Nullable URI webAddress(final String address) {
        final String full = address.contains("://") ? address : "http://" + address;
        try {
            final URI uri = new URI(full);
            final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            return (scheme.equals("http") || scheme.equals("https")) && uri.getHost() != null ? uri : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * The colour and styles in force after the text: a colour code or reset clears the styles, a style code adds to them.
     */
    static String formatAfter(final String format, final String text) {
        String colour = "";
        final StringBuilder styles = new StringBuilder();
        final String all = format + text;
        for (int i = 0; i < all.length() - 1; i++) {
            if (all.charAt(i) != '§') {
                continue;
            }
            final char code = Character.toLowerCase(all.charAt(i + 1));
            if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
                colour = "§" + code;
                styles.setLength(0);
            } else if (code == 'r') {
                colour = "";
                styles.setLength(0);
            } else if (code >= 'k' && code <= 'o') {
                styles.append('§').append(code);
            }
            i++;
        }
        return colour + styles;
    }

    static String stripped(final String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
