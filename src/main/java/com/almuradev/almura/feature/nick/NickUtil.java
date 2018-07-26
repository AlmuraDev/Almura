/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import java.util.regex.Pattern;

public final class NickUtil {

    public static final String regexPattern = "[a-zA-Z0-9_§]+";
    public static final Pattern nickNameRegex = Pattern.compile(regexPattern);

    private NickUtil() {}
}
