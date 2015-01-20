/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySign.class)
public abstract class MixinTileEntitySign extends TileEntity implements IExtendedTileEntitySign {
    @Shadow String[] signText;

    private int columnBeingEdited;
    private CacheFlag flag;

    @Override
    public int getColumnBeingEdited() {
        return columnBeingEdited;
    }

    @Override
    public void setColumnBeingEdited(int column) {
        this.columnBeingEdited = column;
    }

    @Override
    public void recalculateText() {
        flag = CacheFlag.INVALID;
    }

    @Override
    public boolean hasText() {
        if (flag == CacheFlag.TRUE) {
            return true;
        }

        flag = CacheFlag.FALSE;

        for (String aSignText : signText) {
            if (aSignText != null && !aSignText.isEmpty()) {
                flag = CacheFlag.TRUE;
            }
        }

        return flag != CacheFlag.FALSE;
    }

    @Inject(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntitySign;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound)V", shift = At.Shift.AFTER), cancellable = true)
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        for (int l = 0; l < 4; ++l) {
            signText[l] = compound.getString("Text" + (l + 1));

            final String line = signText[l];
            if (line.length() > 30) {
                signText[l] = line.substring(0, 30);
            }
        }

        recalculateText();

        ci.cancel();
    }

    public static enum CacheFlag {
        INVALID,
        FALSE,
        TRUE
    }
}
