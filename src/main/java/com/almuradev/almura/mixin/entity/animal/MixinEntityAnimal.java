package com.almuradev.almura.mixin.entity.animal;

import com.almuradev.almura.interfaces.entity.animal.IMixinEntityAnimal;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal extends EntityAgeable implements IAnimals, IMixinEntityAnimal {
    private boolean canSpawnTwin = false;

    public MixinEntityAnimal(World p_i1681_1_) {
        super(p_i1681_1_);
    }

    @Override
    public boolean canSpawnTwin() {
        return canSpawnTwin;
    }

    @Override
    public void setCanSpawnTwin(boolean setValue) {        
        canSpawnTwin = setValue;
    }
}