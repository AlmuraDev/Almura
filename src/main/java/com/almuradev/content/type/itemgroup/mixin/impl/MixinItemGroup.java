/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup.mixin.impl;

import com.almuradev.content.mixin.iface.IMixinSetCatalogTypeId;
import com.almuradev.content.type.itemgroup.ItemGroup;
import com.almuradev.content.type.itemgroup.ItemGroupRegistryModule;
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
public abstract class MixinItemGroup implements ItemGroup, IMixinSetCatalogTypeId {

    @Shadow @Final private String tabLabel;
    @Shadow @Final private int tabIndex;
    @Shadow public ItemStack iconItemStack;
    private String id;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void onConstruction(final int index, final String label, final CallbackInfo ci) {
        this.id = label; // HACK: re-set using setId(String, String) when registerAdditionalCatalog is called
        ItemGroupRegistryModule.get().registerAdditionalCatalog(this);
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
    public void setId(final String id, final String name) {
        this.id = id;
        // name is `this.tabLabel`, we can ignore the parameter
    }

    @Override
    public ItemStack icon() {
        return this.iconItemStack;
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
