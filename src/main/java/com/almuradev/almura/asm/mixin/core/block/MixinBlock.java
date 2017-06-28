/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import javax.annotation.Nullable;

// Makes all blocks BuildableBlockTypes (so they can be used in Almura's framework)
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock extends net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl<Block> implements BuildableBlockType {

    @Nullable
    @Shadow public CreativeTabs displayOnCreativeTab;

    @Override
    public Optional<ItemGroup> getItemGroup() {
        return Optional.ofNullable((ItemGroup) (Object) this.displayOnCreativeTab);
    }
}
