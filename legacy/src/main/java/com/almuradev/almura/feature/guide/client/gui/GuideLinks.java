/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Links in guide pages, written as {@code [text](url)}: found in the page text, and shown as link styled text when the page is viewed.
 */
@SideOnly(Side.CLIENT)
final class GuideLinks {

    // The text may not hold brackets or line breaks, the url no spaces or parentheses
    private static final Pattern MARKUP = Pattern.compile("\\[([^\\[\\]\\n]+)]\\(([^()\\s]+)\\)");
    private static final String LINK_STYLE = TextFormatting.BLUE.toString() + TextFormatting.UNDERLINE;
    private static final char SECTION = TextFormatting.RESET.toString().charAt(0);

    private GuideLinks() {
    }

    /**
     * A link and where it sits in the text it was found in: from {@code start} (inclusive) to {@code end} (exclusive).
     */
    static final class Link {

        final int start;
        final int end;
        final String text;
        final String url;

        Link(final int start, final int end, final String text, final String url) {
            this.start = start;
            this.end = end;
            this.text = text;
            this.url = url;
        }
    }

    /**
     * A page as it is shown: the link markup replaced by link styled text, and where each link ended up.
     */
    static final class Rendered {

        final String text;
        final List<Link> links;
        // For each link: its markup in the page text, and where what replaced it ends in the shown text (after the restored formatting)
        private final List<Link> markups;
        private final List<Integer> shownEnds;

        Rendered(final String text, final List<Link> links, final List<Link> markups, final List<Integer> shownEnds) {
            this.text = text;
            this.links = links;
            this.markups = markups;
            this.shownEnds = shownEnds;
        }

        /**
         * @return The index in the page text of the place at {@code index} in the shown text (inside a link: the same spot in its text)
         */
        int toPageIndex(final int index) {
            int offset = 0;
            for (int i = 0; i < this.links.size(); i++) {
                final Link link = this.links.get(i);
                final Link markup = this.markups.get(i);
                if (index < link.start) {
                    break;
                }
                if (index <= this.shownEnds.get(i)) {
                    // After the '[' of the markup, as far into the text as into the shown link text
                    final int into = Math.max(0, Math.min(index - link.start - LINK_STYLE.length(), markup.text.length()));
                    return markup.start + 1 + into;
                }
                // Past this link the two texts line up again, this far apart
                offset = markup.end - this.shownEnds.get(i);
            }
            return index + offset;
        }
    }

    /**
     * @return The links written in {@code text}, where their markup sits
     */
    static List<Link> find(final String text) {
        final List<Link> links = new ArrayList<>();
        final Matcher matcher = MARKUP.matcher(text);
        while (matcher.find()) {
            links.add(new Link(matcher.start(), matcher.end(), matcher.group(1), matcher.group(2)));
        }
        return links;
    }

    /**
     * @return The link a cursor at {@code index} is on (either edge counts), or null
     */
    @Nullable
    static Link at(final List<Link> links, final int index) {
        for (final Link link : links) {
            if (index >= link.start && index <= link.end) {
                return link;
            }
        }
        return null;
    }

    /**
     * Replaces the link markup in {@code text} with the link's text, styled as a link, then the formatting that was in effect before it.
     */
    static Rendered render(final String text) {
        final StringBuilder out = new StringBuilder(text.length());
        final List<Link> links = new ArrayList<>();
        final List<Link> markups = new ArrayList<>();
        final List<Integer> shownEnds = new ArrayList<>();
        final Matcher matcher = MARKUP.matcher(text);
        String format = "";
        int last = 0;
        while (matcher.find()) {
            format = format(format, text, last, matcher.start());
            out.append(text, last, matcher.start());

            final int start = out.length();
            final String linkText = TextFormatting.getTextWithoutFormattingCodes(matcher.group(1));
            out.append(LINK_STYLE).append(linkText);
            links.add(new Link(start, out.length(), linkText, matcher.group(2)));
            out.append(TextFormatting.RESET).append(format);
            markups.add(new Link(matcher.start(), matcher.end(), matcher.group(1), matcher.group(2)));
            shownEnds.add(out.length());

            last = matcher.end();
        }
        out.append(text, last, text.length());
        return new Rendered(out.toString(), links, markups, shownEnds);
    }

    /**
     * @return The formatting in effect after {@code text} from {@code from} to {@code to}, starting from {@code format}
     */
    private static String format(String format, final String text, final int from, final int to) {
        for (int i = from; i < to - 1; i++) {
            if (text.charAt(i) != SECTION) {
                continue;
            }
            final char code = Character.toLowerCase(text.charAt(++i));
            if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                // A colour ends the styles before it
                format = SECTION + String.valueOf(code);
            } else if (code >= 'k' && code <= 'o') {
                format += SECTION + String.valueOf(code);
            } else if (code == 'r') {
                format = "";
            }
        }
        return format;
    }

    /**
     * @return The url as a web address to open (http:// added when it has no scheme), or null when it isn't an http(s) address
     */
    @Nullable
    static String toWebAddress(final String url) {
        String address = url.trim();
        if (address.isEmpty() || address.indexOf(' ') >= 0) {
            return null;
        }
        if (!address.contains("://")) {
            address = "http://" + address;
        }
        try {
            final URI uri = new URI(address);
            final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ENGLISH);
            if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
                return null;
            }
            return address;
        } catch (final URISyntaxException e) {
            return null;
        }
    }
}
