/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Names in notifications the server sends, which the server can't translate: a dedicated server only has vanilla's English, so an item's
 * name ({@code block.almura.horizontal.building.juko_plank}) is marked as {@code {tr:<key>}} and translated by the client, in its player's
 * language, as the notification arrives.
 */
public final class NotificationText {

    private static final Pattern MARK = Pattern.compile("\\{tr:([A-Za-z0-9_.\\-/:]+)}");

    private NotificationText() {
    }

    /**
     * An item's name for a notification: its own name when it was given one (an anvil, a command), else its translation key, marked.
     */
    public static String item(final ItemStack stack) {
        final Component name = stack.getHoverName();
        if (name.getContents() instanceof final TranslatableContents translatable && translatable.getArgs().length == 0
            && name.getSiblings().isEmpty()) {
            return "{tr:" + translatable.getKey() + "}";
        }
        return name.getString();
    }

    /**
     * The text with its marked names translated, by the client's language (or the key, if it has none for it).
     */
    public static String translate(final String text) {
        if (text.indexOf("{tr:") < 0) {
            return text;
        }
        final Matcher matcher = MARK.matcher(text);
        final StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement(Language.getInstance().getOrDefault(matcher.group(1))));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
