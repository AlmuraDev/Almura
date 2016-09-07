/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.items.AlmuraItem;
import com.almuradev.almura.pack.crop.PackCrops;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class MetadataWand extends AlmuraItem {

    public MetadataWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float pointX, float pointY,
            float pointZ) {
        if (!world.isRemote) {
            if (world.getBlock(x, y, z) == Blocks.air) {
                return false;
            }

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
            final int maxMetadataValue;
            if (block instanceof PackCrops) {
                maxMetadataValue = ((PackCrops) block).getStages().size() - 1;
            } else if (block instanceof BlockCrops || block instanceof BlockStem) {
                maxMetadataValue = 7;
            } else {
                // TODO Without going through each type of Block and incrementing the metadata value if another is present, this is
                // TODO impossible to do on Minecraft 1.7.x as its not known to Block itself. This was fixed with BlockState in Minecraft
                // TODO 1.8.x and we'll have to wait till then.
                // Why Mojang?
                // final List list = new ArrayList();
                // world.getBlock(x, y, z).getSubBlocks(null, null, list);
                // maxMetadataValue = list.size() - 1;
                maxMetadataValue = 0;
            }

            // If the max data is 0 then we have no variants of this block
            if (maxMetadataValue == 0) {
                return false;
            }

            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
            player.swingItem();

            final int currentDataValue = world.getBlockMetadata(x, y, z);

            // If we're at the max metadata value, rollover to 0
            world.setBlockMetadataWithNotify(x, y, z, currentDataValue == maxMetadataValue ? 0 : currentDataValue + 1, 1);

            // Send a message to the player
            player.addChatComponentMessage(new ChatComponentText(
                    String.format("Metadata was changed from [%1$s] to [%2$s].", currentDataValue, world.getBlockMetadata(x, y, z))));
            return true;
        }
        return false;
    }
}
