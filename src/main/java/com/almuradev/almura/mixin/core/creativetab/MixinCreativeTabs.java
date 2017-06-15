/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.creativetab;

import com.almuradev.almura.creativetab.CreativeTab;
import com.almuradev.almura.mixin.interfaces.IMixinCreativeTabs;
import com.almuradev.almura.registry.CreativeTabRegistryModule;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;

@Mixin(CreativeTabs.class)
public abstract class MixinCreativeTabs implements CreativeTab, IMixinCreativeTabs {

    @Shadow @Final private String tabLabel;
    @Shadow @Final private int tabIndex;
    @Shadow private ItemStack iconItemStack;

    private String id;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    public void onConstruction(int index, String label, CallbackInfo ci) {
        final String modId = SpongeImplHooks.getModIdFromClass(getClass());
        this.id = modId + ":" + label;
        CreativeTabRegistryModule.getInstance().registerAdditionalCatalog(this);
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
    public String toString() {
        return MoreObjects.toStringHelper(CreativeTabs.class)
                .add("tabIndex", this.tabIndex)
                .add("tabLabel", this.tabLabel)
                .toString();
    }

    @Override
    public void setIconItemStack(ItemStack itemStack) {
        this.iconItemStack = itemStack;
    }

    @Override
    public org.spongepowered.api.item.inventory.ItemStack getTabIcon() {
        return (org.spongepowered.api.item.inventory.ItemStack) (Object) this.iconItemStack;
    }

    @Override
    public int getTabIndex() {
        return this.tabIndex;
    }
}
