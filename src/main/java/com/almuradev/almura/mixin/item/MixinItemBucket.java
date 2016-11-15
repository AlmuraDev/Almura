package com.almuradev.almura.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemBucket.class, remap = false)
public abstract class MixinItemBucket extends Item {

    @Override
    public Item setMaxStackSize(int p_77625_1_) {
        this.maxStackSize = 64;
        return this;
    }
}