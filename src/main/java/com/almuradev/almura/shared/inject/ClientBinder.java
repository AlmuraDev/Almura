/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.inject;

import com.almuradev.almura.shared.client.keyboard.binder.KeyBindingBinder;
import com.almuradev.content.model.ModelBinder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-specific binders.
 */
@SideOnly(Side.CLIENT)
public interface ClientBinder extends CommonBinder {

    /**
     * Creates a model binder.
     *
     * @return a model binder
     */
    default ModelBinder model() {
        return ModelBinder.create(this.binder());
    }

    /**
     * Creates a keybinding binder.
     *
     * @return a keybinding binder
     */
    default KeyBindingBinder keybinding() {
        return KeyBindingBinder.create(this.binder());
    }
}
