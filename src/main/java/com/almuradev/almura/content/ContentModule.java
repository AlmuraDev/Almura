/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import com.almuradev.almura.content.type.block.BlockModule;
import com.almuradev.almura.content.type.item.ItemModule;
import com.almuradev.almura.content.type.material.MaterialModule;
import net.kyori.violet.AbstractModule;

public class ContentModule extends AbstractModule {

    @Override
    protected void configure() {
        this.install(new BlockModule());
        this.install(new ItemModule());
        this.install(new MaterialModule());
    }
}
