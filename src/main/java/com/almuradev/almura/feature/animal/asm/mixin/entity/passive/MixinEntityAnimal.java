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
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

import javax.annotation.Nullable;


@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal extends EntityAgeable implements IAnimals {

    public MixinEntityAnimal(World worldIn) {
        super(worldIn);
    }

    @Shadow int inLove;
    @Shadow UUID playerInLove;

    /**
     * @author Dockter
     */
    @Overwrite
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (!itemstack.isEmpty()) {
            if (this.isCustomBreedingItem(itemstack) && this.getGrowingAge() == 0 && this.inLove <= 0) {
                this.consumeItemFromStack(player, itemstack);
                this.setInLove(player);
                return true;
            }

            if (this.isChild() && this.isCustomBreedingItem(itemstack)){
                this.consumeItemFromStack(player, itemstack);
                this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
                return true;
            }
        }

        return super.processInteract(player, hand);
    }

    private boolean isCustomBreedingItem(ItemStack stack) {
        final String itemName = stack.getTranslationKey();
        final Entity animal = this;

        if (animal instanceof EntityCow) {
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.WHEAT":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityPig) {
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.CARROT":
                    return true;

                default:
                    return false;
            }
        }

        if (animal instanceof EntityChicken) {
            switch (itemName.toUpperCase()) {
                case "ITEM.ALMURA.FOOD.FOOD.CORN":
                case "ITEM.ALMURA.FOOD.FOOD.SOYBEAN":
                case "ITEM.WHEAT_SEEDS":
                case "ITEM.MELON_SEEDS":
                case "ITEM.BEETROOT_SEEDS":
                case "ITEM.PUMPKIN_SEEDS":
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
