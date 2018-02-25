/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.special;

import com.almuradev.almura.feature.special.irrigation.block.IrrigationPipeBlock;
import com.almuradev.almura.feature.special.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.special.wand.item.LightRepairWand;
import com.almuradev.almura.shared.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

public final class SpecialContentFeature implements Witness {

    @SubscribeEvent
    public void onRegisterBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new IrrigationPipeBlock());
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new LightRepairWand());
        event.getRegistry().register(new FarmersAlmanacItem());
        event.getRegistry().register(new ItemBlock(SpecialBlocks.IRRIGATION_PIPE).setRegistryName(SpecialBlocks.IRRIGATION_PIPE.getRegistryName()));

        if (Sponge.getPlatform().getType().isClient()) {
            this.registerItemModels();
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerItemModels() {
        ModelLoader.setCustomModelResourceLocation(SpecialItems.WAND_LIGHT_REPAIR, 0, new ModelResourceLocation(SpecialItems.WAND_LIGHT_REPAIR
                .getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(SpecialItems.FARMERS_ALMANAC, 0, new ModelResourceLocation(SpecialItems.FARMERS_ALMANAC
                .getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(SpecialItems.IRRIGATION_PIPE, 0, new ModelResourceLocation(SpecialItems.IRRIGATION_PIPE
                .getRegistryName(), "inventory"));
    }
}
