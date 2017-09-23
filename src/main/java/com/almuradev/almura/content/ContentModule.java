/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
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
