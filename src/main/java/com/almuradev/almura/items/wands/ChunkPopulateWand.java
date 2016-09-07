/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import com.almuradev.almura.items.AlmuraItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ChunkPopulateWand extends AlmuraItem {

    public ChunkPopulateWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            Chunk oldChunk = world.getChunkFromBlockCoords((int)player.posX, (int)player.posZ);
            if (world instanceof WorldServer) {
                WorldServer worldServer = (WorldServer) world;                
                ChunkProviderServer chunkProviderServer = worldServer.theChunkProviderServer;                           
                
                int chunkX = oldChunk.xPosition;
                int chunkZ = oldChunk.zPosition;
                
                for (int rx = -1; rx < 1; rx++) {
                    for (int rz = -2; rz < 2; rz++) {
                        int cx = chunkX + rx;
                        int cz = chunkZ + rz;
                        Chunk radiusChunk = world.getChunkFromChunkCoords(cx, cz);
                        radiusChunk.isTerrainPopulated = false;
                        chunkProviderServer.populate(chunkProviderServer, cx, cz);                        
                    }
                }
                oldChunk.isTerrainPopulated = false;
                chunkProviderServer.populate(chunkProviderServer, oldChunk.xPosition, oldChunk.zPosition);
                player.addChatComponentMessage(new ChatComponentText("[Chunk Population Wand] - Population Complete."));
            }                        
        }
        return itemStack;
    }
}