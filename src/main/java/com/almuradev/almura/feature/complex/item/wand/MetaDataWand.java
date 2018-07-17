/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.wand;

import com.almuradev.almura.Almura;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;

public final class MetaDataWand extends WandItem {

    public MetaDataWand() {
        super(new ResourceLocation(Almura.ID, "normal/tool/meta_data_wand"), "meta_data_wand");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            final Player spongePlayer = (Player) player;

            player.swingArm(hand);

        }

        return EnumActionResult.PASS;
    }
}
