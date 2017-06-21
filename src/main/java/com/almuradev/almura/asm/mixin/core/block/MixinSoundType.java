/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.block;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import net.minecraft.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;

// remaining BlockSoundGroup methods are implemented in SpongeCommon
@Mixin(SoundType.class)
public abstract class MixinSoundType implements BlockSoundGroup, IMixinSetCatalogTypeId {

    private String id;
    private String name;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setId(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
