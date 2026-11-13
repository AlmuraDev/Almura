/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor;

import com.almuradev.core.event.Witness;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Registers Almura's hand coded armor: the lighted helmet.
 */
public final class ArmorFeature implements Witness {

    @SubscribeEvent
    public void onRegisterItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ArmorItems.LIGHTED_HELMET);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(ArmorItems.LIGHTED_HELMET);
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(final Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(LightedHelmetItem.ID, "inventory"));
    }
}
