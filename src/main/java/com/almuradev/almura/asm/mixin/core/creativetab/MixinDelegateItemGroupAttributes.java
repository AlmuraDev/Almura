package com.almuradev.almura.asm.mixin.core.creativetab;

import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.item.group.impl.GenericCreativeTabs;
import com.almuradev.almura.content.loader.CatalogDelegate;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(GenericCreativeTabs.class)
public abstract class MixinDelegateItemGroupAttributes extends MixinCreativeTabs implements ItemGroup {

    @Nullable
    private CatalogDelegate<ItemType> itemTypeDelegate;

    @Override
    public ItemStack getTabIcon() {
        if (!this.iconItemStack.isEmpty() || this.itemTypeDelegate == null) {
            return (ItemStack) (Object) this.iconItemStack;
        }

        final ItemType itemType = this.itemTypeDelegate.getCatalog();
        this.iconItemStack = new net.minecraft.item.ItemStack((Item) itemType);

        return (ItemStack) (Object) this.iconItemStack;
    }
}
