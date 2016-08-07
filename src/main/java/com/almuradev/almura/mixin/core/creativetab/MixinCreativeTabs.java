/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.creativetab;

import com.almuradev.almura.api.CreativeTab;
import com.google.common.base.Objects;
import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativeTabs.class)
public abstract class MixinCreativeTabs implements CreativeTab {

    @Shadow @Final private String tabLabel;

    @Override
    public String getId() {
        return this.tabLabel;
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
