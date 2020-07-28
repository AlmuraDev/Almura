/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.resources;

import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LanguageManager.class)
public interface LanguageManagerAccessor {
    // public net.minecraft.client.resources.LanguageManager field_135049_a # CURRENT_LOCALE
    @Accessor("CURRENT_LOCALE") static Locale accessor$getCurrentLocale() {
        throw new IllegalStateException("Untransformed Accessor!");
    }
}
