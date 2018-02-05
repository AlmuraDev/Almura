/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.registry.AbstractCatalogRegistryModule;
import com.almuradev.content.registry.EagerCatalogRegistration;
import net.minecraft.item.Item;

@EagerCatalogRegistration
public final class ToolTierRegistryModule extends AbstractCatalogRegistryModule.Mapped<ToolItem.Tier, Item.ToolMaterial> {
    public ToolTierRegistryModule() {
        super(5);
    }

    @Override
    public void registerDefaults() {
       // this.register("wood", Item.ToolMaterial.WOOD);
       // this.register("stone", Item.ToolMaterial.STONE);
       // this.register("iron", Item.ToolMaterial.IRON);
       // this.register("diamond", Item.ToolMaterial.DIAMOND);
       // this.register("gold", Item.ToolMaterial.GOLD);
    }
}
