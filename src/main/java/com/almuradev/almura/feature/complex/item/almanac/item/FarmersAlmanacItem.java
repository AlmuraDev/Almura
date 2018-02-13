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
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import org.spongepowered.api.text.Text;

public abstract class FarmersAlmanacItem extends ComplexItem {

    public FarmersAlmanacItem(ResourceLocation registryName, String unlocalizedName) {
        super(new ResourceLocation(Almura.ID, "normal/tool/farmers_almanac"), "farmers_almanac");
        this.maxStackSize = 1;
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
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if ((player instanceof EntityPlayerMP)) {
            if (!worldIn.isRemote) {
                final org.spongepowered.api.entity.living.player.Player spongePlayer = (org.spongepowered.api.entity.living.player.Player) player;
                if (spongePlayer.hasPermission("almura.wand.light_repair_wand")) {
                    return EnumActionResult.FAIL;
                }
                // Fire PlayerInteractEvent

                /*
                final PlayerInteractEvent event = new PlayerInteractEvent(player, player.getActiveHand(), pos, player.getHorizontalFacing()); //Todo: not finished, incorrect syntax.
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    return EnumActionResult.FAIL;
                }
                */


                final Block block = worldIn.getBlockState(pos).getBlock();
                final IBlockState state = worldIn.getBlockState(pos);
                final int metadata = block.getMetaFromState(state);
                boolean fertile = false;
                boolean farm = false;

                if (block instanceof BlockFarmland || block instanceof BlockCrops || block instanceof BlockCocoa) {
                    farm = true;
                }

                if (block instanceof BlockCrops) {
                    BlockPos underCropPos = new BlockPos(hitX, hitY-1, hitZ);
                    Block underCropBlock = worldIn.getBlockState(underCropPos).getBlock();
                    int underBlockMeta = underCropBlock.getMetaFromState(worldIn.getBlockState(underCropPos));

                    if (underBlockMeta > 0) {
                        fertile = true;
                    }
                } else { //Farmland or Cocoa
                    BlockPos cropPos = new BlockPos(hitX, hitY, hitZ);
                    Block cropBlock = worldIn.getBlockState(cropPos).getBlock();
                    int BlockMeta = cropBlock.getMetaFromState(worldIn.getBlockState(cropPos));

                    if (BlockMeta > 0) {
                        fertile = true;
                    }
                }

                int areaBlockLight;
                int sunlight;

                Biome biomegenbase = worldIn.getBiome(pos);
                float temp = biomegenbase.getTemperature(pos);
                float rain = biomegenbase.getRainfall();
                if (block instanceof BlockCrops) {
                    areaBlockLight = worldIn.getLightFor(EnumSkyBlock.BLOCK, pos);
                    sunlight = worldIn.getLightFor(EnumSkyBlock.SKY, pos) - worldIn.getSkylightSubtracted();
                } else {
                    BlockPos underCropPos = new BlockPos(hitX, hitY-1, hitZ);
                    areaBlockLight = worldIn.getLightFor(EnumSkyBlock.BLOCK, underCropPos);
                    sunlight = worldIn.getLightFor(EnumSkyBlock.SKY, underCropPos) - worldIn.getSkylightSubtracted();
                }

                if (block != null && farm) {
                    /* // Todo: Zidane can you finish this section?
                    ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S0BPacketAnimation(player, 0));
                    player.swingItem();
                    CommonProxy.NETWORK_FORGE.sendTo(new S04OpenFarmersAlmanacGui(block, metadata, fertile, temp, rain, areaBlockLight, sunlight), (EntityPlayerMP) player);
                    */
                }

                if (block != null && !farm) {
                    spongePlayer.sendMessage(Text.of(FontColors.WHITE_FO + "[Farmers Almanac] - " + FontColors.GRAY_FO + "Can only be used on Farmland or a Crop."));
                }
            }
        }
        return EnumActionResult.PASS;
    }
}
