/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateBlockAttributes;
import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.impl.GenericBlock;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.CatalogDelegate;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(value = {GenericBlock.class})
public abstract class MixinDelegateBlockAttributes extends MixinBlock implements BuildableBlockType, IMixinDelegateMaterialAttributes,
        IMixinDelegateBlockAttributes {

    @Nullable
    private CatalogDelegate<ItemGroup> itemGroupDelegate;

    @Nullable
    private CatalogDelegate<BlockSoundGroup> blockSoundGroupDelegate;

    @Override
    public Optional<ItemGroup> getItemGroup() {
        if (this.displayOnCreativeTab != null) {
            return Optional.of((ItemGroup) this.displayOnCreativeTab);
        }

        // Having no delegate instance means it was truly null
        if (this.itemGroupDelegate == null) {
            return Optional.empty();
        }

        final ItemGroup cached = this.itemGroupDelegate.getCatalog();
        this.displayOnCreativeTab = (CreativeTabs) (Object) cached;

        return Optional.of((ItemGroup) this.displayOnCreativeTab);
    }

    @Override
    public void setItemGroupDelegate(CatalogDelegate<ItemGroup> itemGroupDelegate) {
        this.itemGroupDelegate = itemGroupDelegate;
    }

    @Overwrite
    @SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTabToDisplayOn() {
        return (CreativeTabs) (Object) this.getItemGroup().orElse(null);
    }

    @Override
    public void setBlockSoundGroupDelegate(CatalogDelegate<BlockSoundGroup> blockSoundGroupDelegate) {
        this.blockSoundGroupDelegate = blockSoundGroupDelegate;
    }
}
