package com.almuradev.almura.feature.complex.item.almanac.network;

import com.almuradev.almura.feature.complex.item.almanac.gui.IngameFarmersAlmanac;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public class ClientboundWorldPositionInformationPacketHandler implements MessageHandler<ClientboundWorldPositionInformationPacket> {

    @Override
    public void handleMessage(ClientboundWorldPositionInformationPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            final Minecraft client = Minecraft.getMinecraft();
            final EntityPlayerSP player = client.player;
            final WorldClient world = client.world;


            if (world != null) {
                final BlockPos pos = new BlockPos(message.x, message.y, message.z);
                final IBlockState state = world.getBlockState(pos);
                final Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(message.biomeId));

                new IngameFarmersAlmanac(message, world, pos, state).display();
            }
        }
    }
}
