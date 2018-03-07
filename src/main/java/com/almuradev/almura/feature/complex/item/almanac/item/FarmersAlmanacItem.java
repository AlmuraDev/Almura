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
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.biome.BiomeType;

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

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            final Player spongePlayer = (Player) player;
            if (!spongePlayer.hasPermission("almura.item.farmers_almanac")) {
                spongePlayer.sendMessage(Text.of("Access denied, missing permission: ", TextColors.AQUA, "almura.item.farmers_"
                        + "almanac", TextColors.WHITE, "."));
                return EnumActionResult.FAIL;
            }

            final Block block = world.getBlockState(pos).getBlock();
            if (block instanceof BlockFarmland | block instanceof BlockCrops) {

                final Biome biome = world.getBiome(pos);
                final float biomeTemperature = biome.getTemperature(pos);
                final float biomeRainfall = biome.getRainfall();
                final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
                final int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted();

                player.swingArm(hand);

                network.sendTo(spongePlayer, new ClientboundWorldPositionInformationPacket(pos.getX(), pos.getY(), pos.getZ(), hitX, hitY, hitZ, (
                        (BiomeType) biome).getId(), biomeTemperature, biomeRainfall, blockLight, skyLight));
            } else {
                spongePlayer.sendMessage(Text.of("The ", TextColors.AQUA,"Farmer's Almanac", TextColors.WHITE, " can only be "
                        + "used on crops or farmland."));
            }
        }

        return EnumActionResult.PASS;
    }
}
