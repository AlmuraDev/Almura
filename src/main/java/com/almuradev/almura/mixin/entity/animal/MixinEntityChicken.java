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
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityChicken.class)
public abstract class MixinEntityChicken extends EntityAnimal {

    public MixinEntityChicken(World world) {
        super(world);        
    }

    @Overwrite
    public boolean isBreedingItem(ItemStack itemStack) {
        if (itemStack.getItem() == Items.wheat_seeds) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(false);
            return true;
        }
        if (itemStack.getItem() == GameRegistry.findItem(Almura.MOD_ID, "Food\\corn") || itemStack.getItem() == GameRegistry.findItem(Almura.MOD_ID, "Food\\soybean")) {
            ((IMixinEntityAnimal) this).setCanSpawnTwin(true);
            return true;
        }
        return false;        
    } 

    @Overwrite
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
        int j = this.rand.nextInt(3) + this.rand.nextInt(1 + p_70628_2_);

        for (int k = 0; k < j; ++k) {
            this.dropItem(Items.feather, 1);
        }

        j = this.rand.nextInt(3) + this.rand.nextInt(1 + p_70628_2_);

        for (int k = 0; k < j; ++k) { //Fix enchantment check for increased loot drops.... 
            if (this.isBurning()) {
                this.dropItem(Items.cooked_chicken, 1);
            } else {
                this.dropItem(Items.chicken, 1);
            }
        }
    }
}
