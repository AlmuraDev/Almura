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
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(EntitySheep.class)
public abstract class MixinEntitySheep extends EntityAnimal {

    @Shadow
    private EntityAIEatGrass entityAIEatGrass;

    public MixinEntitySheep(World worldIn) {
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

        Item oatItem = GameRegistry.makeItemStack("almura:normal/ingredient/oat", 0, 1, null).getItem();
        Item alfalfaItem = GameRegistry.makeItemStack("almura:normal/crop/alfalfa_item", 0, 1, null).getItem();

        if (oatItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(oatItem);
        if (alfalfaItem != ItemStack.EMPTY.getItem())
            TEMPTATION_ITEMS.add(alfalfaItem);

        this.entityAIEatGrass = new EntityAIEatGrass(this);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, false, TEMPTATION_ITEMS));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, this.entityAIEatGrass);
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));

    }
}
