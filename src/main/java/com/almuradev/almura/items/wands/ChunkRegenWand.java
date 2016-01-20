/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items.wands;

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
                
                //final IChunkProvider chunkProvider = ((ChunkProviderServer) worldServer.getChunkProvider()).serverChunkGenerator;
                //final IChunkProvider myProvider = worldServer.getChunkProvider().currentChunkProvider;
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
                
                
                oldChunk.isTerrainPopulated = false;                
                System.out.println("X: " + oldChunk.xPosition + " Z: " + oldChunk.zPosition);
                chunkProviderServer.populate(chunkProviderServer, oldChunk.xPosition, oldChunk.zPosition);
                
                Chunk chunk2 = world.getChunkFromChunkCoords(oldChunk.xPosition+1, oldChunk.zPosition);
                chunk2.isTerrainPopulated = false;
                chunkProviderServer.populate(chunkProviderServer, chunk2.xPosition, chunk2.zPosition);
                
                Chunk chunk3 = world.getChunkFromChunkCoords(oldChunk.xPosition, oldChunk.zPosition+1);
                chunk3.isTerrainPopulated = false;
                chunkProviderServer.populate(chunkProviderServer, chunk3.xPosition, chunk3.zPosition);
                
                Chunk chunk4 = world.getChunkFromChunkCoords(oldChunk.xPosition+1, oldChunk.zPosition+1);
                chunk4.isTerrainPopulated = false;
                chunkProviderServer.populate(chunkProviderServer, chunk4.xPosition, chunk4.zPosition);
            }                        
        }
        //if (world.isRemote) {
        // Fire PlayerInteractEvent
        //  final PlayerInteractEvent event = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, (int) player.posX, (int) player.posY, (int) player.posZ, -1, world);
        //  MinecraftForge.EVENT_BUS.post(event);

        // Return if the event was cancelled
        // if (event.isCanceled()) {
        //    return itemStack;
        // }

        // player.swingItem();

        // B03ChunkRegenWand message = new B03ChunkRegenWand();
        // ClientProxy.NETWORK_BUKKIT.sendToServer(message);
        // }

        return itemStack;
    }
}