/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.inject;

import com.almuradev.shared.client.model.ModelBinder;
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
}
