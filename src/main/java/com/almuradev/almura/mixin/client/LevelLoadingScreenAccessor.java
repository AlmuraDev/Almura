/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Vanilla's world loading screen keeps what it is waiting on to itself. Almura shows its own screen in its place and reads how far loading
 * has got and why it is loading from here.
 */
@Mixin(LevelLoadingScreen.class)
public interface LevelLoadingScreenAccessor {

    @Accessor("loadTracker")
    LevelLoadTracker almura$loadTracker();

    @Accessor("reason")
    LevelLoadingScreen.Reason almura$reason();
}
