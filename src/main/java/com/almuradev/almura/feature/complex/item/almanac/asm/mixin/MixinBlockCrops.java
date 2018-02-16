package com.almuradev.almura.feature.complex.item.almanac.asm.mixin;

import com.almuradev.almura.feature.complex.item.almanac.asm.interfaces.IMixinBlockCrops;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockCrops.class, priority = 999)
public abstract class MixinBlockCrops extends BlockBush implements IMixinBlockCrops {

    @Shadow protected abstract PropertyInteger getAgeProperty();

    @Override
    public PropertyInteger getAgePropertyDirect() {
        return this.getAgeProperty();
    }
}
