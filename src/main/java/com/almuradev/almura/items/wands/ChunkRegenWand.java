/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
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

public class ChunkRegenWand extends AlmuraItem {

    public ChunkRegenWand(String unlocalizedName, String displayName, String textureName, CreativeTabs creativeTab) {
        super(unlocalizedName, displayName, textureName, creativeTab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            Chunk oldChunk = world.getChunkFromBlockCoords((int)player.posX, (int)player.posZ);
            if (world instanceof WorldServer) {
                WorldServer worldServer = (WorldServer) world;
                ChunkProviderServer chunkProviderServer = worldServer.theChunkProviderServer;
                IChunkProvider chunkProviderGenerate = ((IChunkProvider) ObfuscationReflectionHelper.getPrivateValue(ChunkProviderServer.class, chunkProviderServer, "currentChunkProvider", "e", "field_73246_d"));
                
                Chunk newChunk = chunkProviderGenerate.provideChunk(oldChunk.xPosition, oldChunk.zPosition);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < world.getHeight(); y++) {
                            Block block = newChunk.getBlock(x, y, z);                                
                            int metadata = newChunk.getBlockMetadata(x, y, z);
                            worldServer.setBlock(x + oldChunk.xPosition * 16, y, z + oldChunk.zPosition * 16, block, metadata, 2);
                            TileEntity tileEntity = newChunk.getBlockTileEntityInChunk(x, y, z);

                            if (tileEntity != null) {
                                worldServer.setTileEntity(x + oldChunk.xPosition * 16, y, z + oldChunk.zPosition * 16, tileEntity);
                            }
                        }
                    }
                }
            }
            player.addChatComponentMessage(new ChatComponentText("[Chunk Regen Wand] - Regen Complete."));
        }
        return itemStack;
    }
}