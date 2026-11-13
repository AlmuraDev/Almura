/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * What vanilla's disconnected screen would show, for Almura's in its place.
 */
@Mixin(DisconnectedScreen.class)
public interface DisconnectedScreenAccessor {

    @Accessor("parent")
    Screen almura$parent();

    @Accessor("details")
    DisconnectionDetails almura$details();

    @Accessor("buttonText")
    Component almura$buttonText();
}
