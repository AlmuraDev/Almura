/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.client;

import com.almuradev.almura.content.registry.ContentContainers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

/**
 * The storage barrel's screen: a chest's, named for what the barrel holds first ("Basil Seed Storage Barrel") as it changes, as 3.5's was.
 * A barrel given a name of its own keeps it.
 */
public final class StorageBarrelScreen extends ContainerScreen {

    private final Component barrelName;
    private final boolean named;

    public StorageBarrelScreen(final ChestMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title);
        this.barrelName = BuiltInRegistries.BLOCK.getValue(ContentContainers.STORAGE_BARREL).getName();
        // The server named it for its contents, or it has a name of its own
        final String opened = title.getString(), barrel = this.barrelName.getString();
        this.named = !opened.equals(barrel) && !opened.endsWith(" " + barrel);
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int xm, final int ym) {
        final ItemStack first = this.menu.getSlot(0).getItem();
        final Component title = this.named || first.isEmpty() ? (this.named ? this.title : this.barrelName)
            : Component.empty().append(first.getHoverName()).append(" ").append(this.barrelName);
        graphics.text(this.font, title, this.titleLabelX, this.titleLabelY, -12566464, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }
}
