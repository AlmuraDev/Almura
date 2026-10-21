/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.shared.client.ui.component.UIContextMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * List rows pick their hover colour from the mouse position alone, so they light up under an open {@link UIContextMenu}: move the mouse
 * out of their reach while it is over the menu.
 */
@Mixin(targets = "net.malisis.core.client.gui.component.container.BasicList$ItemComponent", remap = false)
public abstract class MixinBasicListItemComponent {

    @ModifyVariable(method = "drawBackground", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int almura$noHoverUnderContextMenu(final int mouseX) {
        return UIContextMenu.isMouseOver() ? Integer.MIN_VALUE / 2 : mouseX;
    }
}
