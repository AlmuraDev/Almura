/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import com.google.common.collect.Sets;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;

@Mixin(EntityCow.class)
public abstract class MixinEntityCow extends EntityAnimal {

    private EntityAIEatGrass entityAIEatGrass;
    private int cowTimer;
    public MixinEntityCow(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 1.4F);
    }

    /**
     * @author Dockter
     * @reason: overwrite the AT init method to customize the temptations list.
     */
    @Overwrite
    protected void initEntityAI() {
        final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT);

        Item soyBeanItem = GameRegistry.makeItemStack("almura:food/food/soybean", 0, 1, null).getItem();
        Item cornItem = GameRegistry.makeItemStack("almura:food/food/corn", 0, 1, null).getItem();
        Item alfalfaItem = GameRegistry.makeItemStack("almura:normal/crop/alfalfa_item", 0, 1, null).getItem();

        if (soyBeanItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(soyBeanItem);
        if (cornItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(cornItem);
        if (alfalfaItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(alfalfaItem);

        this.entityAIEatGrass = new EntityAIEatGrass(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, this.entityAIEatGrass);
        this.tasks.addTask(4, new EntityAITempt(this, 1.25D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
    }

    /**
     * @author Dockter
     * @reason: overwrite vanilla implementation to include glass cruet support!
     * Date: 4/1/2019
     */
    @Overwrite
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        ItemStack glass_cruet = GameRegistry.makeItemStack("almura:normal/ingredient/glass_cruet", 0, 1, null);
        ItemStack glass_milk_cruet = GameRegistry.makeItemStack("almura:normal/ingredient/glass_milk_cruet", 0, 1, null);

        if (itemstack.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !this.isChild()) {
            player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
            itemstack.shrink(1);

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
            } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
            }

            return true;
        } else if (itemstack.getItem() == glass_cruet.getItem() && !player.capabilities.isCreativeMode && !this.isChild()) {
            player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, new ItemStack(glass_milk_cruet.getItem()));
            } else if (!player.inventory.addItemStackToInventory(new ItemStack(glass_milk_cruet.getItem()))) {
                player.dropItem(new ItemStack(glass_milk_cruet.getItem()), false);
            }
            return true;
        } else {
            return super.processInteract(player, hand);
        }
    }
    @Override
    public void eatGrassBonus() {
        if (this.isChild()) {
            this.addGrowth(60);
        }
    }

    @Override
    protected void updateAITasks() {
        this.cowTimer = this.entityAIEatGrass.getEatingGrassTimer();
        super.updateAITasks();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            this.cowTimer = Math.max(0, this.cowTimer - 1);
        }

        super.onLivingUpdate();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 10) {
            this.cowTimer = 40;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * @author Dockter
     * @reason Note:  this doesn't work yet, need to be implemented in the Cows Model.
     */
    @SideOnly(Side.CLIENT)
    public float getHeadRotationPointY(float p_70894_1_) {
        if (this.cowTimer <= 0) {
            return 0.0F;
        } else if (this.cowTimer >= 4 && this.cowTimer <= 36) {
            return 1.0F;
        } else {
            return this.cowTimer < 4 ? ((float)this.cowTimer - p_70894_1_) / 4.0F : -((float)(this.cowTimer - 40) - p_70894_1_) / 4.0F;
        }
    }

    @SideOnly(Side.CLIENT)
    public float getHeadRotationAngleX(float p_70890_1_) {
        if (this.cowTimer > 4 && this.cowTimer <= 36) {
            float f = ((float)(this.cowTimer - 4) - p_70890_1_) / 32.0F;
            return ((float)Math.PI / 5F) + ((float)Math.PI * 7F / 100F) * MathHelper.sin(f * 28.7F);
        } else {
            return this.cowTimer > 0 ? ((float)Math.PI / 5F) : this.rotationPitch * 0.017453292F;
        }
    }
}
