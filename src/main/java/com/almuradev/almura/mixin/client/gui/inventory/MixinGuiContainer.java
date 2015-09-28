/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiContainer;

import com.almuradev.almura.client.Bindings;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen { 

    @Inject(method = "keyTyped", at = @At("HEAD"))
    public void keyTyped(char p_73869_1_, int p_73869_2_) {
        if (p_73869_2_ == 1 || p_73869_2_ == Bindings.BINDING_OPEN_BACKPACK.getKeyCode())
        {
            this.mc.thePlayer.closeScreen();
        }       
    }   
}