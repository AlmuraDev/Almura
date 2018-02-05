/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.type.item.ItemModule;
import com.almuradev.content.type.item.type.tool.type.hoe.HoeToolItemModule;
import com.almuradev.content.type.item.type.tool.type.pickaxe.PickaxeToolItemModule;
import com.almuradev.content.type.item.type.tool.type.shovel.ShovelToolItemModule;

public final class ToolItemModule extends ItemModule.Module {
    @Override
    protected void configure() {
        this.install(new HoeToolItemModule());
        this.install(new PickaxeToolItemModule());
        this.install(new ShovelToolItemModule());
    }
}
