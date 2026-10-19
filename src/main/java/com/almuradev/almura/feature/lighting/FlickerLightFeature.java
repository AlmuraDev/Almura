/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting;

import com.almuradev.almura.Almura;
import com.almuradev.content.type.block.flicker.FlickerLightTileEntity;
import com.almuradev.core.event.Witness;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

/**
 * Registers the tile entity flickering content lights use, on both sides.
 */
public final class FlickerLightFeature extends Witness.Impl implements Witness.Lifecycle {

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        GameRegistry.registerTileEntity(FlickerLightTileEntity.class, new ResourceLocation(Almura.ID, "flicker_light"));
    }
}
