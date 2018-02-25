/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.special.wand.item;

import com.almuradev.almura.Almura;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class LightRepairWand extends WandItem {

    public LightRepairWand() {
        super(new ResourceLocation(Almura.ID, "normal/tool/light_repair_wand"), "normal.tool.light_repair_wand");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        if (!worldIn.isRemote) {
            final Player spongePlayer = (org.spongepowered.api.entity.living.player.Player) player;

            if (!spongePlayer.hasPermission("almura.item.light_repair_wand")) {
                spongePlayer.sendMessage(Text.of(TextColors.WHITE + "Access denied, missing permission: ", TextColors.AQUA, "almura.item.light_repair_wand", TextColors.WHITE, "."));
                return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn));
            } else {
                final BlockPos pos = player.getPosition();

                for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-100, -20, -100), pos.add(100, 20, 100))) {
                    worldIn.setLightFor(EnumSkyBlock.BLOCK, blockpos$mutableblockpos, 0);
                    if (worldIn.getBlockState(blockpos$mutableblockpos).getBlock() == Blocks.TORCH) {
                        worldIn.setBlockToAir(blockpos$mutableblockpos);
                    }

                    if (worldIn.getBlockState(blockpos$mutableblockpos).getBlock() == Blocks.GLOWSTONE) {
                        worldIn.setBlockToAir(blockpos$mutableblockpos);
                    }
                }

                spongePlayer.sendMessage(Text.of("Light values within a -100/[40]+100 range have been fixed.  Unload and reload chunks to get client ot update."));
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));  //Both Server & Client expect a returned value.
    }
}
