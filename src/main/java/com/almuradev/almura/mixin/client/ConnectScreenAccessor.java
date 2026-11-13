/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import io.netty.channel.ChannelFuture;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Vanilla's connect screen keeps the connection it is making to itself. Almura shows its own Server Status screen in its place and reads the
 * connection and its status from here, and cancels it the way the screen's Cancel button does.
 */
@Mixin(ConnectScreen.class)
public interface ConnectScreenAccessor {

    @Accessor("connection")
    @Nullable Connection almura$connection();

    @Accessor("channelFuture")
    @Nullable ChannelFuture almura$channelFuture();

    @Accessor("channelFuture")
    void almura$setChannelFuture(@Nullable ChannelFuture channelFuture);

    @Accessor("aborted")
    void almura$setAborted(boolean aborted);

    @Accessor("status")
    Component almura$status();

    @Accessor("parent")
    Screen almura$parent();
}
