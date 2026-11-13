/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.feature.menu.SimpleOptionsMenu;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * This mixin's objective is to add an 'Almura' button to the GuiOptions screen that opens our custom options screen
 * for Almura-specific settings and configuration options
 */
@Mixin(GuiOptions.class)
public abstract class MixinGuiOptions extends GuiScreen {

    // Add our button to the list and move the Done button lower
    @Inject(method = "initGui", at = @At("RETURN"))
    public void onInitGui(CallbackInfo ci) {
        final GuiButton almuraButton = new GuiButton(301, this.width / 2 - 155, this.height / 6 + 144 - 6, 150, 20, "Almura");
        this.buttonList.add(almuraButton);

        /*
         * Button IDs                     Buttons
         * 108                          = Difficulty
         * 109                          = Lock
         * REALMS_NOTIFICATIONS ordinal = Realms notification
         * 200                          = Done
         */
        // Move every button that isn't any of the above listed up by 12
        this.buttonList.stream()
                .filter(btn -> !(btn instanceof GuiOptionSlider) && btn.id != 200 && btn.id != 108 && btn.id != 109 && btn.id != GameSettings
                        .Options.REALMS_NOTIFICATIONS.getOrdinal())
                .forEach(btn -> btn.y = btn.y - 12);
    }

    // Open our screen when our button is pressed
    @Inject(method = "actionPerformed", at = @At("RETURN"))
    protected void actionPerformed(GuiButton button, CallbackInfo ci) throws IOException {
        if (button.enabled && button.id == 301) { // Almura button
            new SimpleOptionsMenu((GuiOptions) (Object) this).display();
        }
    }
}
