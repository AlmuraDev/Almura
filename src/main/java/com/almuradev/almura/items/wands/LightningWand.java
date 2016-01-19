/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.items.AlmuraItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class LightningWand extends AlmuraItem {

    public LightningWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
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

            final MovingObjectPosition objectMouseOver = getDistanceFromPlayer(world, player, true);
            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                world.addWeatherEffect(new EntityLightningBolt(world, objectMouseOver.blockX, objectMouseOver.blockY, objectMouseOver.blockZ));
            }
        }

        return itemStack;
    }

    private MovingObjectPosition getDistanceFromPlayer(World world, EntityPlayer player, boolean bool) {
        float f = 1.0F;
        float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
        float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
        // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) f + (double) (world.isRemote ? player.getEyeHeight() - player
                .getDefaultEyeHeight() : player.getEyeHeight());
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double reachDistance = 20.0D;

        Vec3 vec31 = vec3.addVector((double) f7 * reachDistance, (double) f6 * reachDistance, (double) f8 * reachDistance);
        return world.rayTraceBlocks(vec3, vec31, bool, !bool, false);
    }
}
