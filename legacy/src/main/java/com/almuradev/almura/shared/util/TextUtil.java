/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;

public final class TextUtil {

    public static String asFriendlyText(final String string) {
        return LegacyTexts.replace(string, '&', SpongeTexts.COLOR_CHAR);
    }

    public static String asUglyText(final String string) {
        return LegacyTexts.replace(string, SpongeTexts.COLOR_CHAR, '&');
    }


    private TextUtil() {}

}
