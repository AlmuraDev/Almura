/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class Colors {

    public static final Color BLACK = new Color("black", EnumChatFormatting.BLACK.getFormattingCode(), 0, 0);
    public static final Color DARK_BLUE = new Color("dark_blue", EnumChatFormatting.DARK_BLUE.getFormattingCode(), 1, 170);
    public static final Color DARK_GREEN = new Color("dark_green", EnumChatFormatting.DARK_GREEN.getFormattingCode(), 2, 43520);
    public static final Color DARK_AQUA = new Color("dark_aqua", EnumChatFormatting.DARK_AQUA.getFormattingCode(), 3, 43690);
    public static final Color DARK_RED = new Color("dark_red", EnumChatFormatting.DARK_RED.getFormattingCode(), 4, 11141120);
    public static final Color DARK_PURPLE = new Color("dark_purple", EnumChatFormatting.DARK_PURPLE.getFormattingCode(), 5, 11141290);
    public static final Color GOLD = new Color("gold", EnumChatFormatting.GOLD.getFormattingCode(), 6, 16755200);
    public static final Color GRAY = new Color("gray", EnumChatFormatting.GRAY.getFormattingCode(), 7, 11184810);
    public static final Color DARK_GRAY = new Color("dark_gray", EnumChatFormatting.DARK_GRAY.getFormattingCode(), 8, 5592405);
    public static final Color BLUE = new Color("blue", EnumChatFormatting.BLUE.getFormattingCode(), 9, 5592575);
    public static final Color GREEN = new Color("green", EnumChatFormatting.GREEN.getFormattingCode(), 10, 5635925);
    public static final Color AQUA = new Color("aqua", EnumChatFormatting.AQUA.getFormattingCode(), 11, 5636095);
    public static final Color RED = new Color("red", EnumChatFormatting.RED.getFormattingCode(), 12, 16733525);
    public static final Color LIGHT_PURPLE = new Color("light_purple", EnumChatFormatting.LIGHT_PURPLE.getFormattingCode(), 13, 16733695);
    public static final Color YELLOW = new Color("yellow", EnumChatFormatting.YELLOW.getFormattingCode(), 14, 16777045);
    public static final Color WHITE = new Color("white", EnumChatFormatting.WHITE.getFormattingCode(), 15, 16777215);
    public static final Color OBFUSCATED = new Color("obfuscated", EnumChatFormatting.OBFUSCATED.getFormattingCode(), 16, true);
    public static final Color BOLD = new Color("bold", EnumChatFormatting.BOLD.getFormattingCode(), 17, true);
    public static final Color STRIKETHROUGH = new Color("strikethrough", EnumChatFormatting.STRIKETHROUGH.getFormattingCode(), 18, true);
    public static final Color UNDERLINE = new Color("underline", EnumChatFormatting.UNDERLINE.getFormattingCode(), 19, true);
    public static final Color ITALIC = new Color("italic", EnumChatFormatting.ITALIC.getFormattingCode(), 20, true);
    public static final Color RESET = new Color("reset", EnumChatFormatting.RESET.getFormattingCode(), 21);
    private static final List<Color> COLORS;
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(Color.CHAR_COLOR_BEGIN) + "[0-9A-FK-OR]");


    static {
        COLORS = Lists.newArrayList();

        final Field[] declaredFields = Colors.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    final Object obj = field.get(null);
                    if (obj instanceof Color) {
                        COLORS.add((Color) obj);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Color> getBuiltinColors() {
        return Collections.unmodifiableList(COLORS);
    }

    public static Optional<Color> getByCode(char code) {
        return getByCode(code);
    }

    public static Optional<Color> getByCode(int code) {
        for (Color c : COLORS) {
            if (c.getChatCode() == code) {
                return Optional.of(c);
            }
        }

        return Optional.absent();
    }

    public static String stripColors(String colorized) {
        if (colorized == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(colorized).replaceAll("");
    }
}
