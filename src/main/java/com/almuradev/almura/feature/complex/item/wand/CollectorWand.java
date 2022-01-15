/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.wand;

import com.almuradev.almura.Almura;
import net.malisis.core.MalisisCore;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public final class CollectorWand extends WandItem {

    public CollectorWand() {
        super(new ResourceLocation(Almura.ID, "normal/tool/collector_wand"), "collector_wand");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        if (!worldIn.isRemote) {
            final Player spongePlayer = (org.spongepowered.api.entity.living.player.Player) player;

            if (!spongePlayer.hasPermission("almura.item.collector_wand") || spongePlayer.hasPermission("almura.singleplayer") && MalisisCore.isObfEnv) {
                spongePlayer.sendMessage(Text.of(TextColors.WHITE + "Access denied, missing permission: ", TextColors.AQUA, "almura.item.light_repair_wand", TextColors.WHITE, "."));
                return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn));
            } else {
                double radius = 10;
                List<EntityItem> items = worldIn.getEntitiesWithinAABB(EntityItem.class, player.getEntityBoundingBox().grow(radius, radius, radius));
                for (EntityItem it : items) {
                    double distX = player.posX - it.posX;
                    double distZ = player.posZ - it.posZ;
                    double distY = it.posY + 1.5D - player.posY;
                    double dir = Math.atan2(distZ, distX);
                    double speed = 1F / it.getDistance(player) * 2;

                    if (distY < 0) {
                        it.motionY += speed;
                    }

                    it.motionX = Math.cos(dir) * speed;
                    it.motionZ = Math.sin(dir) * speed;
                }
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(handIn));
    }
}
