/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.entity.animal;

import com.almuradev.almura.Almura;
import com.almuradev.almura.extension.animal.IMixinEntityAnimal;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityCow.class)
public abstract class MixinEntityCow extends EntityAnimal {

    public MixinEntityCow(World world) {
        super(world);        
    }

    @Override
    public boolean isBreedingItem(ItemStack itemStack) {
        if (itemStack.getItem() == Items.wheat) {
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
        int k;

        for (k = 0; k < j; ++k) {
            this.dropItem(Items.leather, 1);
        }

        j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + p_70628_2_);

        for (k = 0; k < j; ++k) {  //2nd random to create more random... thanks mojang...
            if (this.isBurning()) {
                this.dropItem(Items.cooked_beef, 1);
            } else {
                this.dropItem(Items.beef, 1);
            }
        }
    }
}
