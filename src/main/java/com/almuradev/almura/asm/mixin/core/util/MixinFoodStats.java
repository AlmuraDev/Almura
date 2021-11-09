/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FoodStats.class)
public class MixinFoodStats {

    @Shadow private int foodLevel;
    @Shadow private float foodSaturationLevel;
    @Shadow private float foodExhaustionLevel;
    @Shadow private int foodTimer;
    @Shadow private int prevFoodLevel;
    private float prevSaturationLevel;
    @Shadow public void addExhaustion(float exhaustion) { };

    /**
     * @author Dockter
     * @reason Make food consumption faster and ignore world difficulty.
     */

    @Overwrite
    public void onUpdate(EntityPlayer player) {
        EnumDifficulty enumdifficulty = player.world.getDifficulty(); // Not used, kept for posterity sake.
        this.prevFoodLevel = this.foodLevel;
        this.prevSaturationLevel = this.foodSaturationLevel;

        if (this.foodExhaustionLevel > 2.0F) {
            this.foodExhaustionLevel -= 2.0F;

            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else  {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean flag = player.world.getGameRules().getBoolean("naturalRegeneration");
        if (flag && this.foodSaturationLevel > 0.0F && player.shouldHeal() && this.foodLevel >= 20) {
            ++this.foodTimer;

            if (this.foodTimer >= 10) {
                float f = Math.min(this.foodSaturationLevel, 6.0F);
                player.heal(f / 6.0F);
                this.addExhaustion(f);
                this.foodTimer = 0;
            }
        } else if (flag && this.foodLevel >= 18 && player.shouldHeal()) {
            ++this.foodTimer;

            if (this.foodTimer >= 80) {
                player.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTimer;

            if (this.foodTimer >= 80) {
                // Be aware:  We at AlmuraDev strive to make your life difficult.
                if ((player.getHealth() > 2 && enumdifficulty == EnumDifficulty.NORMAL) || (player.getHealth() > 1 && enumdifficulty == EnumDifficulty.HARD) || (player.getHealth() > 5 && enumdifficulty == EnumDifficulty.EASY))
                    player.attackEntityFrom(DamageSource.STARVE, 1.0F);

                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }

        if (prevSaturationLevel != this.foodSaturationLevel) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketUpdateHealth(player.getHealth(), this.foodLevel, this.foodSaturationLevel));
            }
        }
    }
}
