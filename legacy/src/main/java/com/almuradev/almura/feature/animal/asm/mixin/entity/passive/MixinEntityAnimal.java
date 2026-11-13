/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.UUID;


@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal extends EntityAgeable implements IAnimals {

    public MixinEntityAnimal(World worldIn) {
        super(worldIn);
    }

    @Shadow int inLove;
    @Shadow UUID playerInLove;

    @Shadow public abstract boolean isInLove();

    /**
     * @author Dockter
     */
    @Overwrite
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        //System.out.println("Info - Age: " + this.getGrowingAge() + " Love: " + this.isInLove() + " Child: " + this.isChild());
        if (!itemstack.isEmpty()) {
            if (this.isCustomBreedingItem(itemstack) && this.getGrowingAge() == 0 && this.inLove <= 0) {
                this.consumeItemFromStack(player, itemstack);
                this.setInLove(player);
                return true;
            }

            if (this.isChild() && this.isCustomBreedingItem(itemstack)){
                this.consumeItemFromStack(player, itemstack);
                this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0D, 0.0D, 0.0D);
                int age = (-this.getGrowingAge()) / 2;
                int bumpAge = age / 20;
                this.addGrowth(bumpAge);
                return true;
            }
        }

        return super.processInteract(player, hand);
    }

    /**
     * @author Dockter
     */
    @Overwrite
    // Notes:
    // This method is used by EntityCow and EntityAnimal only.
    // This method is typically only used when the "processInteract" method is called.
    // This method is not called within other child classes that extend EntityAnimal and override this method itself.
    // EntityPig and EntityCow have their own Mixin Overwrite for this functionality.

    public boolean isBreedingItem(ItemStack stack) {
        return isCustomBreedingItem(stack);
    }

    private boolean isCustomBreedingItem(ItemStack stack) {
        final String itemName = stack.getTranslationKey();
        final Entity animal = this;

        if (animal instanceof EntityCow) {
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.ALMURA.NORMAL.CROP.ALFALFA_ITEM":
                case "ITEM.WHEAT":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityPig) {
            // Note:  must update MixinEntityPig if you change this check.
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.CARROTS":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntitySheep) {
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.NORMAL.INGREDIENT.OAT":
                case "ITEM.ALMURA.NORMAL.CROP.ALFALFA_ITEM":
                case "ITEM.WHEAT":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityChicken) {
            // Note: must update MixinEntityChicken if you change this check.
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.SEEDS":
                case "ITEM.SEEDS_MELON":
                case "ITEM.BEETROOT_SEEDS":
                case "ITEM.SEEDS_PUMPKIN":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityOcelot) {
            switch (itemName.toUpperCase()) {
                case "ITEM.FISH":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityWolf) {
            return stack.getItem() instanceof ItemFood && ((ItemFood)stack.getItem()).isWolfsFavoriteMeat();
        }

        if (animal instanceof EntityRabbit) {
            return this.isRabbitBreedingItem(stack.getItem());
        }

        return false;
    }

    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
    }

    public void setInLove(@Nullable EntityPlayer player) {
        this.inLove = 600;

        if (player != null) {
            this.playerInLove = player.getUniqueID();
        }

        this.world.setEntityState(this, (byte)18);
    }

    private boolean isRabbitBreedingItem(Item itemIn) { // from EntityRabbit
        return itemIn == Items.CARROT || itemIn == Items.GOLDEN_CARROT || itemIn == Item.getItemFromBlock(Blocks.YELLOW_FLOWER);
    }
}
