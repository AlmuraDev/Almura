/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.asm.mixin.item;

import com.almuradev.almura.feature.cache.asm.interfaces.IMixinItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements IMixinItemStack {

    @Shadow private net.minecraftforge.common.capabilities.CapabilityDispatcher capabilities;

    @Override
    public CapabilityDispatcher getCapabilities() {
        return this.capabilities;
    }
}
