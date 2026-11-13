/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The screen vanilla's server list goes back to.
 */
@Mixin(JoinMultiplayerScreen.class)
public interface JoinMultiplayerScreenAccessor {

    @Accessor("lastScreen")
    Screen almura$lastScreen();
}
