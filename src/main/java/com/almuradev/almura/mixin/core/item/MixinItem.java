/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.item;

import com.almuradev.almura.pack.item.PackItemObject;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class MixinItem extends net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl<Item> implements PackItemObject {

}
