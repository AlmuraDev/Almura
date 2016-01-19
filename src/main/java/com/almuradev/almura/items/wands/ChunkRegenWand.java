/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.network.play.B03ChunkRegenWand;
import com.almuradev.almura.items.AlmuraItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ChunkRegenWand extends AlmuraItem {

    public ChunkRegenWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) {
            // Fire PlayerInteractEvent
            final PlayerInteractEvent event = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, (int) player.posX, (int) player.posY, (int) player.posZ, -1, world);
            MinecraftForge.EVENT_BUS.post(event);

            // Return if the event was cancelled
            if (event.isCanceled()) {
                return itemStack;
            }
                        
            player.swingItem();
            
            B03ChunkRegenWand message = new B03ChunkRegenWand();
            ClientProxy.NETWORK_BUKKIT.sendToServer(message);
            }

        return itemStack;
    }    
}