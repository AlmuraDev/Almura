/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Keybindings {
    public static final KeyBinding REFRESH_SHAPES = register("Refresh shapes", Keyboard.KEY_K, "Refresh the shapes for rendering");

    public static KeyBinding register(String title, int key, String message) {
        final KeyBinding binding = new KeyBinding(title, key, message);
        ClientRegistry.registerKeyBinding(binding);
        return binding;
    }

    public static void fakeStaticLoad() {}
}
