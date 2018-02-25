package com.almuradev.almura.feature.special.irrigation.block;

import com.almuradev.almura.Almura;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public final class IrrigationPipeBlock extends BlockFence {

    public IrrigationPipeBlock() {
        super(Material.ROCK, MapColor.OBSIDIAN);
        this.setRegistryName(new ResourceLocation(Almura.ID, "horizontal/farming/irrigation_pipe"));
        this.setUnlocalizedName("horizontal.farming.irrigation_pipe");

        // TODO Dockter, change these to what you wish
        this.setHardness(1f);
        this.setResistance(50f);

        this.setTickRandomly(true);
    }

    // Who do we connect with?
    @Override
    public boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        final IBlockState state = worldIn.getBlockState(pos);
        final Block block = state.getBlock();
        final BlockFaceShape shape = state.getBlockFaceShape(worldIn, pos, facing);
        return shape == BlockFaceShape.MIDDLE_POLE && block instanceof IrrigationPipeBlock;
    }

    // Who can connect to us?
    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        final Block connector = world.getBlockState(pos.offset(facing)).getBlock();

        return connector instanceof IrrigationPipeBlock;
    }

    // TODO Dockter, this method is important as it is part of how things connect to the pipe. Currently uses Fence's code
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.CENTER;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        // Particles/etc
    }
}
