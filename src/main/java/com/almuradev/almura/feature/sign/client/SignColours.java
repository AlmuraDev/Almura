/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.sign.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Optional;

/**
 * A sign line as text with its colours and styles as {@code §} codes, as 1.12's {@code getFormattedText}: what the sign editor edits and
 * sends back, so a coloured sign keeps its colours when it is edited again (see {@code AbstractSignEditScreenMixin}). Colours that aren't one
 * of the sixteen named ones have no code and are left out.
 */
public final class SignColours {

    private SignColours() {
    }

    public static String formatted(final Component line) {
        final StringBuilder text = new StringBuilder();
        final boolean[] styled = {false};
        line.visit((style, part) -> {
            if (!part.isEmpty()) {
                final String codes = codes(style);
                // Each styled part from a clean start, as 1.12 wrote them; a plain part after a styled one is reset to plain
                if (!codes.isEmpty()) {
                    text.append(ChatFormatting.RESET).append(codes);
                    styled[0] = true;
                } else if (styled[0]) {
                    text.append(ChatFormatting.RESET);
                }
                text.append(part);
            }
            return Optional.empty();
        }, Style.EMPTY);
        // Plain text stays plain
        return styled[0] ? text.toString() : line.getString();
    }

    private static String codes(final Style style) {
        final StringBuilder codes = new StringBuilder();
        final TextColor colour = style.getColor();
        if (colour != null) {
            for (final ChatFormatting formatting : ChatFormatting.values()) {
                final TextColor named = TextColor.fromLegacyFormat(formatting);
                if (named != null && named.equals(colour)) {
                    codes.append(formatting);
                    break;
                }
            }
        }
        if (style.isObfuscated()) {
            codes.append(ChatFormatting.OBFUSCATED);
        }
        if (style.isBold()) {
            codes.append(ChatFormatting.BOLD);
        }
        if (style.isStrikethrough()) {
            codes.append(ChatFormatting.STRIKETHROUGH);
        }
        if (style.isUnderlined()) {
            codes.append(ChatFormatting.UNDERLINE);
        }
        if (style.isItalic()) {
            codes.append(ChatFormatting.ITALIC);
        }
        return codes.toString();
    }
}
