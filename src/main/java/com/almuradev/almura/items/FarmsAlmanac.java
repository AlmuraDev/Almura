/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.server.network.play.S01OpenBlockInformationGui;
import com.almuradev.almura.server.network.play.S04OpenFarmersAlmanacGui;
import com.almuradev.almura.util.Colors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class FarmsAlmanac extends AlmuraItem {

    public FarmsAlmanac(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
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
            
            if (block instanceof BlockFarmland || block instanceof PackCrops || block instanceof BlockCrops || block instanceof BlockCocoa) {
                farm = true;
            }
            
            if (block instanceof PackCrops || block instanceof BlockCrops) {
                if (world.getBlockMetadata(x, y-1, z) > 0) {
                    fertile = true;
                }   
            } else { //Farmland or Cocoa
                if (world.getBlockMetadata(x, y, z) > 0) {
                    fertile = true;
                }    
            }
            
            int areaBlockLight;
            int sunlight;

            BiomeGenBase biomegenbase = world.getBiomeGenForCoords(x, z);
            float temp = biomegenbase.getFloatTemperature(x, y, z);
            float rain = biomegenbase.rainfall;
            if (block instanceof PackCrops) {
                areaBlockLight = world.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
                sunlight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) - world.skylightSubtracted;
            } else {
                areaBlockLight = world.getSavedLightValue(EnumSkyBlock.Block, x, y+1, z);
                sunlight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y+1, z) - world.skylightSubtracted;   
            }
                        
            if (block != null && farm) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
                player.swingItem();
                CommonProxy.NETWORK_FORGE.sendTo(new S04OpenFarmersAlmanacGui(block, metadata, fertile, temp, rain, areaBlockLight, sunlight), (EntityPlayerMP) player);
            }
            
            if (block != null && !farm) {
                player.addChatComponentMessage(new ChatComponentText(Colors.WHITE + "[Farmers Almanac] - " + Colors.GRAY + "Can only be used on Farmland or a Crop."));
            }
        }
        return false;
    }
}
