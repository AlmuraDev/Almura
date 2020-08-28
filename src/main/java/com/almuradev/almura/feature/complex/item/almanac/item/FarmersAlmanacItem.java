/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.item;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.item.ComplexItem;
import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundWorldPositionInformationPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

import javax.inject.Inject;

public final class FarmersAlmanacItem extends ComplexItem {

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;

    public FarmersAlmanacItem() {
        super(new ResourceLocation(Almura.ID, "normal/tool/farmers_almanac"), "farmers_almanac");
        this.setMaxStackSize(1);
        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":tool").ifPresent((itemGroup) -> this.setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            boolean irrigationPipe = false;
            boolean irrigationPipeNear = false;
            final Player spongePlayer = (Player) player;
            if (!spongePlayer.hasPermission("almura.item.farmers_almanac")) {
                spongePlayer.sendMessage(Text.of("Access denied, missing permission: ", TextColors.AQUA, "almura.item.farmers_"
                        + "almanac", TextColors.WHITE, "."));
                return EnumActionResult.FAIL;
            }

            final Block block = world.getBlockState(pos).getBlock();
            if (block.getRegistryName().toString().length() > 30 && block.getRegistryName().toString().substring(0,30).equalsIgnoreCase("almura:horizontal/agriculture/")) {
                irrigationPipe = true;
            }

            Block pipe = null;
            pipe = Block.getBlockFromName("almura:horizontal/agriculture/pipe");

            if (pipe != null) {
                for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-6, 1, -6), pos.add(6, 1, 6))) {
                    if (((WorldServer) world).getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4) && !irrigationPipeNear) {
                        Block posBlock = world.getBlockState(blockpos$mutableblockpos).getBlock();
                        if (posBlock.getRegistryName().toString().length() > 30 && posBlock.getRegistryName().toString().substring(0,30).equalsIgnoreCase("almura:horizontal/agriculture/")) {
                            irrigationPipeNear = true;
                        }
                    }
                }
            }

            if (block instanceof BlockFarmland | block instanceof BlockCrops | irrigationPipe) {

                final Biome biome = world.getBiome(pos);
                final float biomeTemperature = biome.getTemperature(pos);
                final float biomeRainfall = biome.getRainfall();
                final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
                final int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted();
                final int combinedLight = world.getLightFromNeighbors(pos);
                final boolean isDaytime = world.isDaytime();
                final boolean canSeeSky = world.canSeeSky(pos);
                final boolean hasAdditionalLightHeatSource = CropBlockImpl.hasAdditionalLightSource(world, pos, 1);

                player.swingArm(hand);

                network.sendTo(spongePlayer, new ClientboundWorldPositionInformationPacket(pos.getX(), pos.getY(), pos.getZ(), hitX, hitY, hitZ,
                        biome.getRegistryName().toString(), biomeTemperature, biomeRainfall, blockLight, skyLight, combinedLight, isDaytime,
                        canSeeSky, hasAdditionalLightHeatSource, irrigationPipe, irrigationPipeNear));
            } else {
                spongePlayer.sendMessage(Text.of("The ", TextColors.AQUA,"Farmer's Almanac", TextColors.WHITE, " can only be "
                        + "used on crops, farmland or irrigation pipes."));
            }
        }

        return EnumActionResult.PASS;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add("Displays useful information on farming");
        super.addInformation(stack, player, tooltip, advanced);
    }
}
