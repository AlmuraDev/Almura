/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type;

import com.almuradev.content.type.action.ActionModule;
import com.almuradev.content.type.block.BlockModule;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupModule;
import com.almuradev.content.type.cactus.CactusModule;
import com.almuradev.content.type.deadbush.DeadBushModule;
import com.almuradev.content.type.flower.FlowerModule;
import com.almuradev.content.type.generation.GenerationModule;
import com.almuradev.content.type.grass.GrassModule;
import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.itemgroup.ItemGroupModule;
import com.almuradev.content.type.mapcolor.MapColorModule;
import com.almuradev.content.type.material.MaterialModule;
import com.almuradev.content.type.tree.TreeModule;
import net.kyori.violet.AbstractModule;

public final class TypeModule extends AbstractModule {
    @Override
    protected void configure() {
        this.install(new ActionModule());
        this.install(new BlockModule());
        this.install(new BlockSoundGroupModule());
        this.install(new GenerationModule());
        this.install(new ItemModule());
        this.install(new ItemGroupModule());
        this.install(new MapColorModule());
        this.install(new MaterialModule());
        this.install(new TreeModule());
        this.install(new GrassModule());
        this.install(new DeadBushModule());
        this.install(new FlowerModule());
        this.install(new CactusModule());
    }
}
