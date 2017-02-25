/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.entity.player;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements ICommandSender {

    @Shadow public InventoryPlayer inventory;
    private EntityPlayer this$ = (EntityPlayer) (Object) this; 
    
    public MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);        
    }
    
    public float getBreakSpeed(Block p_146096_1_, boolean p_146096_2_, int meta, int x, int y, int z) {        
        ItemStack stack = inventory.getCurrentItem();
        float f = (stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, p_146096_1_, meta));

        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = this.inventory.getCurrentItem();

            if (i > 0 && itemstack != null) {
                float f1 = (float)(i * i + 1);

                boolean canHarvest = ForgeHooks.canToolHarvestBlock(p_146096_1_, meta, itemstack);

                if (!canHarvest && f <= 1.0F) {
                    f += f1 * 0.08F;
                } else {
                    f += f1;
                }
            }
        }

        if (this.isPotionActive(Potion.digSpeed)) {
            f *= 1.0F + (float)(this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (this.isPotionActive(Potion.digSlowdown)) {
            f *= 1.0F - (float)(this.getActivePotionEffect(Potion.digSlowdown).getAmplifier() + 1) * 0.2F;
        }

        if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
            f /= 5.0F;
        }

        if (!this.onGround) {
            f /= 5.0F;
        }

        f = ForgeEventFactory.getBreakSpeed(this$, p_146096_1_, meta, f, x, y, z);
        return (f < 0 ? 0 : f);
    }
}
