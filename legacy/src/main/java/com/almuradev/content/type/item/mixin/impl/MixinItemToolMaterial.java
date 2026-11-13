/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.mixin.impl;

import com.almuradev.content.type.item.type.tool.ToolItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.ToolMaterial.class)
public abstract class MixinItemToolMaterial implements ToolItem.Tier {
}
