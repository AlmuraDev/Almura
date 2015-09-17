/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.items.AlmuraItem;
import com.almuradev.almura.server.network.play.S02OpenBlockWireframeGui;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class WireframeWand extends AlmuraItem {

    public WireframeWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
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
            final ItemStack stack = new ItemStack(block, 1, metadata);

            if (block != null) {
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
                player.swingItem();
                CommonProxy.NETWORK_FORGE.sendTo(new S02OpenBlockWireframeGui(stack, x, y, z, AxisAlignedBB
                                .getBoundingBox((double) x + block.minX, (double) y + block.minY, (double) z + block.minZ, (double) x + block.maxX,
                                        (double) y + block.maxY, (double) z + block.maxZ)),
                        (EntityPlayerMP) player);
            }
        }
        return false;
    }
}
