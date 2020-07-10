/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.util.text.translation;

import net.minecraft.util.text.translation.LanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.regex.Pattern;

@Mixin(LanguageMap.class)
public interface LanguageMapAccessor {
    //public net.minecraft.util.text.translation.LanguageMap field_74816_c # languageList
    @Accessor("languageList") Map<String, String> accessor$getLanguageList();

    //public net.minecraft.util.text.translation.LanguageMap field_111053_a # NUMERIC_VARIABLE_PATTERN
    @Accessor("NUMERIC_VARIABLE_PATTERN") Pattern accessor$getNumericVariablePattern();
}
