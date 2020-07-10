/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.almura.asm.mixin.accessors.util.text.translation.LanguageMapAccessor;
import net.minecraft.util.text.translation.LanguageMap;

import java.util.Map;

import javax.inject.Inject;

public abstract class TranslationInjector {
    @Inject protected TranslationManager manager;

    abstract void inject();

    void inject(final Map<String, String> translations) {
        ((LanguageMapAccessor)LanguageMap.getInstance()).accessor$getLanguageList().putAll(translations);
    }
}
