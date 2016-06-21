/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import com.almuradev.almura.Almura;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.scheduler.Task;

public final class ThreadUtil {

    private ThreadUtil() {}

    /**
     * Executes the runnable on either the dedicated or integrated server thread. This will not have -any- client class
     * available if integrated server (Minecraft, LWJGL, etc).
     *
     * @param taskName The name of the task (for pretty reporting to Sponge)
     * @param runnable The runnable to invoke
     */
    public static void executeOnServerThread(String taskName, Runnable runnable) {
        Task.builder().name(taskName).execute(runnable).submit(Almura.instance.container);
    }

    /**
     * Executes the {@link Runnable} on the client thread. While all the client classes will be available, you'll have
     * little to none of the server-side logic objects (additional players, world information, etc).
     *
     * TODO Once I draft the initial Sponge client-side API plan, this will go back to Sponge's scheduler on the client.
     *
     * @param runnable The runnable to invoke
     */
    @SideOnly(Side.CLIENT)
    public static void executeOnClientThread(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }

}
