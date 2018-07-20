/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.block.coinexchange;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.block.ComplexBlock;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.math.BigDecimal;

public final class CoinExchange extends ComplexBlock {

    @Inject
    private static ServerNotificationManager serverNotificationManager;

    public CoinExchange(ResourceLocation registryName, float hardness, float resistance) {
        super(Material.GROUND);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(registryName.getResourcePath().replace('/', '.'));
        this.setHardness(hardness);
        this.setResistance(resistance);

        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":currency").ifPresent((itemGroup) -> setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockHorizontal.FACING);
    }

    @Override
    protected boolean hasInvalidNeighbor(World worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Deprecated
    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            return;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer mcPlayer, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {

        if (world.isRemote) {
            return true;
        }
        final Player player = (Player) mcPlayer;

        if (mcPlayer.getHeldItemMainhand().isEmpty()) {
            serverNotificationManager.sendPopupNotification(player, Text.of("Coin Exchange"), Text.of("Place coins in hand prior to clicking on Coin Exchange"),5);
            return true;
        }

        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (service != null) {

            final Account account = service.getOrCreateAccount(player.getUniqueId()).orElse(null);

            if (account != null) {
                final Currency currency = service.getDefaultCurrency();
                final double coinValue = getCoinValue(player.getItemInHand(HandTypes.MAIN_HAND).get());

                if (coinValue == 0 ) {
                    serverNotificationManager.sendPopupNotification(player, Text.of("Coin Exchange"), Text.of("This item is not a coin..."),5);
                    return true;  // Not Coins
                }

                final BigDecimal depositAmount = new BigDecimal((coinValue * player.getItemInHand(HandTypes.MAIN_HAND).get().getQuantity()));
                account.deposit(currency, depositAmount, Sponge.getCauseStackManager().getCurrentCause());

                ((Player) mcPlayer).setItemInHand(HandTypes.MAIN_HAND, null); // Clear ItemStack from Players hand.
            }
        }

       return true;
    }

    private double getCoinValue(org.spongepowered.api.item.inventory.ItemStack item) {

        String name = item.getType().getName();
        switch (name) {
            case "almura:normal/currency/coppercoin":
                return 100;

            case "almura:normal/currency/silvercoin":
                return 1000;

            case "almura:normal/currency/goldcoin":
                return 100000;

            case "almura:normal/currency/platinumcoin":
                return 1000000;

        }
        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        this.onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (!(world instanceof WorldServer) || ((WorldServer) world).isRemote) {
            return;
        }

        final ItemStack toDrop = new ItemStack(this, 1, 0);
        drops.add(toDrop);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        final ItemStack pickStack = new ItemStack(this, 1, 0);

        return pickStack;
    }
}
