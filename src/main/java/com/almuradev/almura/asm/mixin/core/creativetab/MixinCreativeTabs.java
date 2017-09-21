/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.creativetab;

import com.almuradev.almura.asm.mixin.interfaces.IMixinCreativeTabs;
import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.registry.ItemGroupRegistryModule;
import com.google.common.base.MoreObjects;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeTabs.class, priority = 999)
public abstract class MixinCreativeTabs implements ItemGroup, IMixinCreativeTabs, IMixinSetCatalogTypeId {

    @Shadow @Final private String tabLabel;
    @Shadow @Final private int tabIndex;
    @Shadow public ItemStack iconItemStack;
    private String id;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    public void onConstruction(int index, String label, CallbackInfo ci) {
        this.id = label; // HACK: re-set using setId(String, String) when registerAdditionalCatalog is called
        ItemGroupRegistryModule.getInstance().registerAdditionalCatalog(this);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.tabLabel;
    }

    @Override
    public void setId(String id, String name) {
        this.id = id;
        // name is `this.tabLabel`, we can ignore the parameter
    }

    @Override
    public org.spongepowered.api.item.inventory.ItemStack getIcon() {
        return (org.spongepowered.api.item.inventory.ItemStack) (Object) this.iconItemStack;
    }

    @Override
    public int getIndex() {
        return this.tabIndex;
    }

    @Override
    public void setIconItemStack(ItemStack stack) {
        this.iconItemStack = stack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CreativeTabs.class)
                .add("index", this.tabIndex)
                .add("id", this.id)
                .add("label", this.tabLabel)
                .add("icon", this.iconItemStack)
                .toString();
    }
}
