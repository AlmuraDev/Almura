/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.entity.animal;

import com.almuradev.almura.Almura;
import com.almuradev.almura.extension.animal.IMixinEntityAnimal;
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

    public MixinEntityPig(World world) {
        super(world);        
    }

    @Overwrite
    public boolean isBreedingItem(ItemStack itemStack) {
        if (itemStack.getItem() == Items.carrot) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(false);
            return true;
        }
        if (itemStack.getItem() == GameRegistry.findItem(Almura.MOD_ID, "Food\\corn") || itemStack.getItem() == GameRegistry.findItem(Almura.MOD_ID, "Food\\soybean")) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(true);
            return true;
        }
        return false;        
    }   
}
