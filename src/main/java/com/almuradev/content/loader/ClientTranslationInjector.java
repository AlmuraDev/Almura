/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientTranslationInjector extends TranslationInjector implements IResourceManagerReloadListener {

    private final LanguageManager lm;

    @Inject
    private ClientTranslationInjector(final LanguageManager lm) {
        this.lm = lm;
    }

    @Override
    public void onResourceManagerReload(final IResourceManager manager) {
        this.inject();
    }

    @Override
    @SuppressWarnings("ConstantConditions") // Liar.
    void inject() {
        @Nullable final Language language = this.lm.getCurrentLanguage();
        if (language != null) {
            final String id = language.getLanguageCode();
            this.inject(this.manager.get(id));
        }
    }

    @Override
    void inject(final Map<String, String> translations) {
        super.inject(translations);
        LanguageManager.CURRENT_LOCALE.properties.putAll(translations);
    }
}
