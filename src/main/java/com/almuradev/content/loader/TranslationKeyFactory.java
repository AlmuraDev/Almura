/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

@FunctionalInterface
public interface TranslationKeyFactory {

    String buildTranslationKey(final String key);
}
