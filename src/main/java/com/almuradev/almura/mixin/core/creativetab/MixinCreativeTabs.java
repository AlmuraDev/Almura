/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.creativetab;

import com.almuradev.almura.api.creativetab.CreativeTab;
import com.almuradev.almura.registry.CreativeTabRegistryModule;
import com.google.common.base.Objects;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;

@Mixin(CreativeTabs.class)
public abstract class MixinCreativeTabs implements CreativeTab {

    @Shadow @Final private String tabLabel;
    private String id, modId;

    private CreativeTabs this$ = (CreativeTabs) (Object) this;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(String label, CallbackInfo ci) {
        final String modId = SpongeImplHooks.getModIdFromClass(getClass());
        this.id = modId + ":" + label;
        CreativeTabRegistryModule.getInstance().registerAdditionalCatalog(this);
    }

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
        return Objects.toStringHelper(CreativeTabs.class)
                .add("tabLabel", this.tabLabel)
                .toString();
    }
}
