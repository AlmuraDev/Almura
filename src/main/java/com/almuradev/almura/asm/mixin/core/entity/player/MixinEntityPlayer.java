/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.entity.player;

import com.almuradev.almura.asm.mixin.interfaces.IMixinEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IMixinEntityPlayer {

    private Text prefix = Text.EMPTY;
    private Text suffix = Text.EMPTY;

    @Override
    public Text getPrefix() {
        return this.prefix;
    }

    @Override
    public void setPrefix(Text prefix) {
        this.prefix = prefix;
    }

    @Override
    public Text getSuffix() {
        return this.suffix;
    }

    @Override
    public void setSuffix(Text suffix) {
        this.suffix = suffix;
    }
}
