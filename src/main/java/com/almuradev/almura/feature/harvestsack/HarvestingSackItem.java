/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.item.ComplexItem;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import com.almuradev.content.type.block.type.crop.CropBlockImpl;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;

import java.util.List;

/**
 * Right click a ripe crop configured with {@code sack_harvest} to pick it: the hand swings, the crop's drops go into the sack and the
 * crop is set back to its reset age instead of being destroyed. Right click anything else to open the sack.
 */
public final class HarvestingSackItem extends ComplexItem {

    public HarvestingSackItem() {
        // Translation key matches the content loader's (content/item/normal/tool/_translations: "harvesting_sack.name")
        super(new ResourceLocation(Almura.ID, "normal/tool/harvesting_sack"), "almura.normal.tool.harvesting_sack");
        this.setMaxStackSize(1);
        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":agriculture").ifPresent(group -> this.setCreativeTab((CreativeTabs) group));
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlockImpl) || !((CropBlockImpl) state.getBlock()).isSackHarvestable()) {
            return EnumActionResult.PASS; // Not a pickable crop: falls through to opening the sack
        }

        // A pickable crop that isn't ripe (e.g. the one just picked, hit again while right click is held) only swings, it never
        // opens the sack
        if (((CropBlockImpl) state.getBlock()).canSackHarvest(state) && !world.isRemote && player instanceof EntityPlayerMP) {
            HarvestSackFeature.harvest((EntityPlayerMP) player, hand, pos, (CropBlockImpl) state.getBlock());
        }
        // SUCCESS swings the hand on the client, like breaking
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        // Not right after a pick (holding right click repeats the use, which would open the sack on whatever is aimed at next), and not
        // while aiming at the ground or plants of a field
        if (!world.isRemote && !HarvestSackFeature.pickedRecently(player) && !this.isAimingAtField(world, player)) {
            player.openGui(Almura.ID, AlmuraGuiHandler.HARVEST_SACK, world, hand.ordinal(), 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    /**
     * @return True if the player is aiming at dirt, grass, farmland or a plant (crops, stems, flowers, ...)
     */
    private boolean isAimingAtField(final World world, final EntityPlayer player) {
        final RayTraceResult hit = this.rayTrace(world, player, false);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }
        final Block block = world.getBlockState(hit.getBlockPos()).getBlock();
        return block instanceof BlockDirt || block instanceof BlockGrass || block instanceof BlockFarmland || block instanceof BlockBush;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag flag) {
        tooltip.add("Right click ripe berries and other pickable crops to harvest them");
        super.addInformation(stack, world, tooltip, flag);
    }
}
