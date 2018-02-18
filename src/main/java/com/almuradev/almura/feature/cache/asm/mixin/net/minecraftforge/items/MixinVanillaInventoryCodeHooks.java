/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.asm.mixin.net.minecraftforge.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = VanillaInventoryCodeHooks.class, remap = false)
public abstract class MixinVanillaInventoryCodeHooks {

    /**
     * @author Zidane - Chris Sanders
     * @reason Fix potential Forge bug where they don't respect the {@link IItemHandler}'s slot limit and instead use what the stack's slot limit is.
     * @param itemHandler The item handler
     * @return True if full, false if not
     */
    @Overwrite
    private static boolean isFull(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            // TODO PR Forge if Lex says I'm right here
            // Almura start: stackInSlot.getCount() != stackInSlot.getMaxStackSize()
            if (stackInSlot.isEmpty() || stackInSlot.getCount() != itemHandler.getSlotLimit(slot))
            {
                return false;
            }
            // Almura End
        }
        return true;
    }

}
