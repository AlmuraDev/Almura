/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.items.AlmuraItem;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.server.network.play.S01OpenBlockInformationGui;
import com.almuradev.almura.server.network.play.S04OpenFarmInformationGui;
import com.almuradev.almura.util.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class FarmWand extends AlmuraItem {

    public FarmWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float pointX, float pointY,
            float pointZ) {
        if (!world.isRemote) {
            // Fire PlayerInteractEvent
            final PlayerInteractEvent
                    event =
                    new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, (int) player.posX, (int) player.posY,
                            (int) player.posZ, -1, world);
            MinecraftForge.EVENT_BUS.post(event);

            // Return if the event was cancelled
            if (event.isCanceled()) {
                return false;
            }

            final Block block = world.getBlock(x, y, z);
            final int metadata = world.getBlockMetadata(x, y, z);            
            boolean fertile = false;   
            boolean farm = false;
            
            if (block instanceof BlockFarmland || block instanceof PackCrops) {
                farm = true;
            }
            
            if (block instanceof PackCrops) {
                if (world.getBlockMetadata(x, y-1, z) > 0) {
                    fertile = true;
                }   
            } else {
                if (world.getBlockMetadata(x, y, z) > 0) {
                    fertile = true;
                }    
            }

            BiomeGenBase biomegenbase = world.getBiomeGenForCoords(x, z);
            float temp = biomegenbase.getFloatTemperature(x, y, z);
            float rain = biomegenbase.rainfall;
                        
            if (block != null && farm) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
                player.swingItem();
                CommonProxy.NETWORK_FORGE.sendTo(new S04OpenFarmInformationGui(block, metadata, fertile, temp, rain), (EntityPlayerMP) player);
            }
            
            if (block != null && !farm) {
                player.addChatComponentMessage(new ChatComponentText(Colors.DARK_GRAY + "[Farm Information Wand] - " + Colors.BLUE + "Can only be used on Farmland or a Crop."));
            }
        }
        return false;
    }
}
