/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import com.almuradev.almura.items.AlmuraItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.text.DecimalFormat;

public class ExplosionWand extends AlmuraItem {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final float explosionStrength;

    public ExplosionWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab, float explosionStrength) {
        super(unlocalizedName, displayName, textureName, creativeTab);
        this.explosionStrength = explosionStrength;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Fire PlayerInteractEvent
            final PlayerInteractEvent event = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, (int) player.posX,
                    (int) player.posY, (int) player.posZ, -1, world);
            MinecraftForge.EVENT_BUS.post(event);

            // Return if the event was cancelled
            if (event.isCanceled()) {
                return itemStack;
            }

            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
            player.swingItem();

            if (itemStack.stackTagCompound == null) {
                itemStack.stackTagCompound = new NBTTagCompound();
            }

            if (!itemStack.stackTagCompound.getBoolean("primed")) {
                itemStack.stackTagCompound.setDouble("posX", player.posX);
                itemStack.stackTagCompound.setDouble("posY", player.posY);
                itemStack.stackTagCompound.setDouble("posZ", player.posZ);
                itemStack.stackTagCompound.setBoolean("primed", true);
                player.addChatComponentMessage(new ChatComponentText(String.format("Primed detonation at [%1$s] [%2$s] [%3$s]",
                        DECIMAL_FORMAT.format(player.posX),
                        DECIMAL_FORMAT.format(player.posY),
                        DECIMAL_FORMAT.format(player.posZ))));
            } else {
                world.createExplosion(player, itemStack.getTagCompound().getDouble("posX"),
                        itemStack.getTagCompound().getDouble("posY"),
                        itemStack.getTagCompound().getDouble("posZ"), explosionStrength, true);
                itemStack.stackTagCompound.setBoolean("primed", false);
                player.addChatComponentMessage(new ChatComponentText("Detonation! You can now set a new prime location."));
            }
        }
        return itemStack;
    }
}
