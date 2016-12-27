/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.entity.animal;

import com.almuradev.almura.Almura;
import com.almuradev.almura.interfaces.entity.animal.IMixinEntityAnimal;

import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityPig.class)
public abstract class MixinEntityPig extends EntityAnimal {

    public MixinEntityPig(World p_i1681_1_) {
        super(p_i1681_1_);        
    }

    @Overwrite
    public boolean isBreedingItem(ItemStack p_70877_1_) {
        if (p_70877_1_.getItem() == Items.carrot) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(false);
            return true;
        }
        if (p_70877_1_.getItem() == GameRegistry.findItem(Almura.MOD_ID, "Food\\corn")) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(true);
            return true;
        }
        return false;        
    }   
}
