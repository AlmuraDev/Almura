/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Bindings {
    public static final KeyBinding BINDING_OPEN_GUIDE =
            new AlmuraBinding("key.almura.openGuide", "Guide", Keyboard.KEY_F12, "key.categories.guide", "Guide");

    public static final KeyBinding BINDING_CONFIG_GUI =
            new AlmuraBinding("key.almura.config", "Config", Keyboard.KEY_F4, "key.categories.almura", "Almura");

    public static final KeyBinding BINDING_OPEN_BACKPACK =
            new AlmuraBinding("key.almura.backpack", "Backpack", Keyboard.KEY_B, "key.categories.almura", "Almura");


    public static void register() {
        ClientRegistry.registerKeyBinding(BINDING_CONFIG_GUI);
        ClientRegistry.registerKeyBinding(BINDING_OPEN_BACKPACK);
        ClientRegistry.registerKeyBinding(BINDING_OPEN_GUIDE);
    }

    final static class AlmuraBinding extends KeyBinding {

        public AlmuraBinding(String unlocalizedIdentifier, String name, int keycode, String unlocalizedCategory, String category) {
            super(unlocalizedIdentifier, keycode, unlocalizedCategory);

            LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedIdentifier, name);
            LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedCategory, category);
        }
    }
}
