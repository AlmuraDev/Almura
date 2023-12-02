/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import com.google.common.collect.Sets;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(EntityChicken.class)
public abstract class MixinEntityChicken extends EntityAnimal {

    @Shadow public int timeUntilNextEgg;

    public MixinEntityChicken(World worldIn) {
        super(worldIn);
        this.setSize(0.4F, 0.7F);
        this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
        this.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    /**
     * @author Dockter
     * Purpose: overwrite the AT init method to customize the temptations list.
     */
    @Overwrite
    protected void initEntityAI() {

        final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);

        Item soyBeanItem = GameRegistry.makeItemStack("almura:food/food/soybean", 0, 1, null).getItem();
        Item cornItem = GameRegistry.makeItemStack("almura:food/food/corn", 0, 1, null).getItem();

        if (soyBeanItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(soyBeanItem);
        if (cornItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(cornItem);

        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.4D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    /**
     * @author Dockter
     * Purpose: overwrite the AT init method to customize the temptations list.
     */
    @Overwrite // Intended to bring compatibility to FarmingForBlockHeads.
    // Note: required to overwrite how EntityChicken normally implements this.
    public boolean isBreedingItem(ItemStack stack) {
        // Call the super which has our custom handler in it.
        return super.isBreedingItem(stack);
    }
}
