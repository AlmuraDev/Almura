package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Stitcher.class)
public abstract class MixinStitcher {

    @Shadow
    private final Set setStitchHolders = new HashSet(256);
    @Shadow
    private final List stitchSlots = new ArrayList(256);
    @Shadow
    public boolean forcePowerOf2;
    @Shadow
    private int mipmapLevelStitcher;
    @Shadow
    private int currentWidth;
    @Shadow
    private int currentHeight;
    @Shadow
    private int maxWidth;
    @Shadow
    private int maxHeight;

    @SuppressWarnings("unchecked")
    @Overwrite
    private boolean expandAndAllocateSlot(Stitcher.Holder p_94311_1_) {
        int i = Math.min(p_94311_1_.getWidth(), p_94311_1_.getHeight());
        boolean flag = this.currentWidth == 0 && this.currentHeight == 0;
        boolean flag1;
        int j;

        if (this.forcePowerOf2) {
            j = MathHelper.roundUpToPowerOfTwo(this.currentWidth);
            int k = MathHelper.roundUpToPowerOfTwo(this.currentHeight);
            int l = MathHelper.roundUpToPowerOfTwo(this.currentWidth + i);
            int i1 = MathHelper.roundUpToPowerOfTwo(this.currentHeight + i);
            boolean flag2 = l <= this.maxWidth;
            boolean flag3 = i1 <= this.maxHeight;

            if (!flag2 && !flag3) {
                System.out.println(
                        "Current width [" + currentWidth + ", Current height [" + currentHeight + "]. Texture width [" + i + "], Texture height [" + i
                        + "].");
                return false;
            }

            boolean flag4 = j != l;
            boolean flag5 = k != i1;

            if (flag4 ^ flag5) {
                flag1 = flag5 && flag3; //Forge: Bug fix: Attempt to fill all downward space before expanding width
            } else {
                flag1 = flag2 && j <= k;
            }
        } else {
            boolean flag6 = this.currentWidth + i <= this.maxWidth;
            boolean flag7 = this.currentHeight + i <= this.maxHeight;

            if (!flag6 && !flag7) {
                return false;
            }

            flag1 = flag6 && (flag || this.currentWidth <= this.currentHeight);
        }

        j = Math.max(p_94311_1_.getWidth(), p_94311_1_.getHeight());

        if (MathHelper.roundUpToPowerOfTwo((!flag1 ? this.currentHeight : this.currentWidth) + j) > (!flag1 ? this.maxHeight : this.maxWidth)) {
            System.out.println("Stitcher Variable flag1 [" + flag1 + "]");
            System.out.println(
                    "Current width [" + currentWidth + "], Current height [" + currentHeight + "]. Texture width [" + i + "], Texture height [" + i
                    + "].");
            return false;
        } else {
            Stitcher.Slot slot;

            if (flag1) {
                if (p_94311_1_.getWidth() > p_94311_1_.getHeight()) {
                    p_94311_1_.rotate();
                }

                if (this.currentHeight == 0) {
                    this.currentHeight = p_94311_1_.getHeight();
                }

                slot = new Stitcher.Slot(this.currentWidth, 0, p_94311_1_.getWidth(), this.currentHeight);
                this.currentWidth += p_94311_1_.getWidth();
            } else {
                slot = new Stitcher.Slot(0, this.currentHeight, this.currentWidth, p_94311_1_.getHeight());
                this.currentHeight += p_94311_1_.getHeight();
            }

            slot.addSlot(p_94311_1_);
            this.stitchSlots.add(slot);
            return true;
        }
    }
}
