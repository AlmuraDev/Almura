/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.flicker;

import net.minecraft.tileentity.TileEntity;

/**
 * Marks a flickering light so the client can track it and draw its fast flicker. Holds no data and doesn't tick.
 */
public final class FlickerLightTileEntity extends TileEntity {

    @Override
    public void onLoad() {
        FlickerLights.onLoad(this.world, this.pos);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        FlickerLights.onUnload(this.world, this.pos);
    }

    @Override
    public void onChunkUnload() {
        FlickerLights.onUnload(this.world, this.pos);
    }

    @Override
    public boolean shouldRenderInPass(final int pass) {
        // The dimming overlay is translucent
        return pass == 1;
    }
}
