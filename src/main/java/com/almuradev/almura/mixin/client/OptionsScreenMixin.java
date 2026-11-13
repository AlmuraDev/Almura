/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.feature.menu.options.AlmuraOptions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Adds an "Almura..." button under vanilla's option buttons, across both columns, which opens Almura's options, as on 1.12.
 */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin {

    // Below vanilla's ten buttons, two to a row
    private static final int ALMURA_ROW = 5;

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private LayoutElement almura$addAlmuraButton(final LayoutElement contents) {
        if (contents instanceof final GridLayout grid) {
            final Screen options = (Screen) (Object) this;
            grid.addChild(Button.builder(Component.translatable("almura.menu_button.options.open"), button -> AlmuraOptions.open(options)).build(),
                ALMURA_ROW, 0, 1, 2);
        }
        return contents;
    }
}
