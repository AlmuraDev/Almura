/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

public class Color {

    public static final char CHAR_COLOR_BEGIN = '§';
    public static final char CHAR_COLOR_BLACK = '0';
    public static final int INTEGER_COLOR_BLACK = 0;
    private final String name;
    private final char chatCode;
    private final int chatIntCode, guiColorCode;
    private final boolean isFormattingColor;

    public Color(String name, char chatCode, int chatIntCode) {
        this.name = name;
        this.chatCode = chatCode;
        this.chatIntCode = chatIntCode;
        this.isFormattingColor = false;
        this.guiColorCode = INTEGER_COLOR_BLACK;
    }

    public Color(String name, char chatCode, int chatIntCode, int guiColorCode) {
        this.name = name;
        this.chatCode = chatCode;
        this.chatIntCode = chatIntCode;
        this.guiColorCode = guiColorCode;
        this.isFormattingColor = false;
    }

    public Color(String name, char chatCode, int chatIntCode, boolean isFormattingColor) {
        this.name = name;
        this.chatCode = chatCode;
        this.chatIntCode = chatIntCode;
        this.isFormattingColor = isFormattingColor;
        this.guiColorCode = INTEGER_COLOR_BLACK;
    }

    public Color(String name, int guiColorCode) {
        this.name = name;
        this.chatCode = CHAR_COLOR_BLACK;
        this.chatIntCode = INTEGER_COLOR_BLACK;
        this.isFormattingColor = false;
        this.guiColorCode = guiColorCode;
    }

    public String getName() {
        return name;
    }

    public char getChatCode() {
        return chatCode;
    }

    public int getChatIntCode() {
        return chatIntCode;
    }

    public int getGuiColorCode() {
        return guiColorCode;
    }

    public boolean isFormattingColor() {
        return isFormattingColor;
    }

    public boolean isColor() {
        return !this.isFormattingColor;
    }

    @Override
    public String toString() {
        return new String(new char[]{CHAR_COLOR_BEGIN, chatCode});
    }
}
