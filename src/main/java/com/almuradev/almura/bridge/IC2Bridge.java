/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.bridge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ic2.api.event.LaserEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;

public class IC2Bridge {
    private static IC2Bridge bridgeInstance;

    public static void init() {
        if (bridgeInstance == null) {
            bridgeInstance = new IC2Bridge();
            MinecraftForge.EVENT_BUS.register(bridgeInstance);
        }
    }

    /**
     * @Author Zidane
     * @Purpose IC2's MiningLaser is actually a TileEntity that fires its' own Entity
     *          when shot. We want the Player though, not it so we can tell Cauldron
     *          about this event. We'll fire an AttackEntityEvent and cancel appropriately.
     * @param event IC2 event
     */
    @SubscribeEvent
    public void onLaserHitsEntity(LaserEvent.LaserHitsEntityEvent event) {
        if (event.owner instanceof EntityPlayerMP && !((EntityPlayerMP) event.owner).worldObj.isRemote) {
            final AttackEntityEvent event1 = new AttackEntityEvent((EntityPlayer) event.owner, event.hitentity);
            MinecraftForge.EVENT_BUS.post(event1);
            event.setCanceled(event1.isCanceled());
        }
    }

    /**
     * @Author Zidane
     * @Purpose IC2's MiningLaser is actually a TileEntity that fires its' own Entity
     *          when shot. We want the Player though, not it so we can tell Cauldron
     *          about this event. We'll fire a BlockEvent.BreakEvent and cancel
     *          appropriately.
     * @param event IC2 event
     */
    @SubscribeEvent
    public void onLaserHitsBlock(LaserEvent.LaserHitsBlockEvent event) {
        if (event.owner instanceof EntityPlayerMP && !event.world.isRemote) {
            final Block hit = event.world.getBlock(event.x, event.y, event.z);
            final int blockMetadata = event.world.getBlockMetadata(event.x, event.y, event.z);
            final BlockEvent.BreakEvent event1 = new BlockEvent.BreakEvent(event.x, event.y, event.z, event.world, hit, blockMetadata,
                    (EntityPlayer) event.owner);
            MinecraftForge.EVENT_BUS.post(event1);
            event.setCanceled(event1.isCanceled());
        }
    }
}
