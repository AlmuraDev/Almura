/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.items.AlmuraItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class FireballWand extends AlmuraItem {

    public FireballWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Fire PlayerInteractEvent
            final PlayerInteractEvent
                    event =
                    new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, (int) player.posX, (int) player.posY,
                            (int) player.posZ, -1, world);
            MinecraftForge.EVENT_BUS.post(event);

            // Return if the event was cancelled
            if (event.isCanceled()) {
                return itemStack;
            }

            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
            player.swingItem();

            world.playAuxSFXAtEntity(null, 1008, (int) player.posX, (int) player.posY, (int) player.posZ, 0);
            final Vec3 look = player.getLookVec();
            final EntityLargeFireball fireball = new EntityLargeFireball(world, player, 1, 1, 1);
            fireball.setPosition(player.posX + look.xCoord,
                    player.posY + look.yCoord + 1,
                    player.posZ + look.zCoord);
            fireball.accelerationX = look.xCoord * 0.1;
            fireball.accelerationY = look.yCoord * 0.1;
            fireball.accelerationZ = look.zCoord * 0.1;

            world.spawnEntityInWorld(fireball);
        }

        return itemStack;
    }
}
