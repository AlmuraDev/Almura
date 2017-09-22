/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action;

import com.almuradev.almura.content.type.block.component.action.fertilize.Fertilizer;
import com.almuradev.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class BlockActionModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.facet().add(Fertilizer.class);
    }
}
