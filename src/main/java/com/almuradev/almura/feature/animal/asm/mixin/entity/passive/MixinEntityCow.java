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
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Set;

@Mixin(EntityCow.class)
public abstract class MixinEntityCow extends EntityAnimal {

    public MixinEntityCow(World worldIn) {
        super(worldIn);
        this.setSize(0.9F, 1.4F);
    }

    /**
     * @author Dockter
     * Purpose: overwrite the AT init method to customize the temptations list.
     */
    @Overwrite
    protected void initEntityAI() {
        final Set<Item> TEMPTATION_ITEMS = Sets.newHashSet(Items.WHEAT);

        Item soyBeanItem = GameRegistry.makeItemStack("almura:food/food/soybean", 0, 1, null).getItem();
        Item cornItem = GameRegistry.makeItemStack("almura:food/food/corn", 0, 1, null).getItem();

        if (soyBeanItem != null && cornItem != null) {
            TEMPTATION_ITEMS.add(soyBeanItem);
            TEMPTATION_ITEMS.add(cornItem);
        }

        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.25D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }
}
