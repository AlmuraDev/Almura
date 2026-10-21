/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.shared.client.ui.component.UIContextMenu;
import net.malisis.core.client.gui.component.UIComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Right click menus ({@link UIContextMenu}) for any Malisis component: a right click on a component with a menu attached opens it. Right
 * clicks pass from a component to its parents, so a menu attached to a container also opens for anything inside it.
 */
@Mixin(value = UIComponent.class, remap = false)
public abstract class MixinUIComponent implements UIContextMenu.Holder {

    @Nullable private UIContextMenu.Provider almura$contextMenu;

    @Nullable
    @Override
    public UIContextMenu.Provider almura$getContextMenu() {
        return this.almura$contextMenu;
    }

    @Override
    public void almura$setContextMenu(@Nullable final UIContextMenu.Provider provider) {
        this.almura$contextMenu = provider;
    }

    @Inject(method = "onRightClick", at = @At("HEAD"), cancellable = true)
    private void almura$openContextMenu(final int x, final int y, final CallbackInfoReturnable<Boolean> cir) {
        final UIComponent<?> component = (UIComponent<?>) (Object) this;
        if (component.isEnabled() && UIContextMenu.openFor(component, x, y)) {
            cir.setReturnValue(true);
        }
    }
}
