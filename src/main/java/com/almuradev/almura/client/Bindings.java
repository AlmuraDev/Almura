/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Bindings {
    public static final KeyBinding almuraDebugGUI = new KeyBinding("key.almura.almuraDebug", Keyboard.KEY_F3, "key.categories.almura");
    public static final KeyBinding almuraHud = new KeyBinding("key.almura.almuraHud", Keyboard.KEY_F6, "key.categories.almura");
}
