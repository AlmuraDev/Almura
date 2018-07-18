/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.keyboard.binder;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class KeyBindingBinder {

    private final Multibinder<KeyBindingEntry> keybindings;

    public static KeyBindingBinder create(final Binder binder) {
        return new KeyBindingBinder(binder);
    }

    private KeyBindingBinder(final Binder binder) {
        this.keybindings = Multibinder.newSetBinder(binder, new TypeLiteral<KeyBindingEntry>() {});
    }

    /**
     * Installs a new {@link KeyBinding}.
     *
     * @param key The key {@link Keyboard}
     * @param name The name, as a language entry
     * @param category The category, as a language entry
     * @return This binder, for chaining
     */
    public KeyBindingBinder key(int key, String name, String category) {
        this.keybindings.addBinding().toInstance(new KeyBindingEntry(new KeyBinding(name, key, category)));
        return this;
    }

    /**
     * Installs a new {@link KeyBinding}.
     *
     * @param key The key {@link Keyboard}
     * @param name The name, as a language entry
     * @param category The category, as a language entry
     * @param conflictContext A conflict context, used for resolving how to handle conflicts with other alike bindings
     * @return This binder, for chaining
     */
    public KeyBindingBinder key(int key, String name, String category, IKeyConflictContext conflictContext) {
        this.keybindings.addBinding().toInstance(new KeyBindingEntry(new KeyBinding(name, conflictContext, key, category)));
        return this;
    }

    /**
     * Installs a new {@link KeyBinding}.
     *
     * @param key The key {@link Keyboard}
     * @param modifier The key modifier {@link KeyModifier} (i.e. SHIFT or ALT)
     * @param name The name, as a language entry
     * @param category The category, as a language entry
     * @return This binder, for chaining
     */
    public KeyBindingBinder key(int key, KeyModifier modifier, String name, String category) {
        this.keybindings.addBinding().toInstance(new KeyBindingEntry(new KeyBinding(name, KeyConflictContext.UNIVERSAL, modifier, key, category)));
        return this;
    }

    /**
     * Installs a new {@link KeyBinding}.
     *
     * @param key The key {@link Keyboard}
     * @param modifier The key modifier {@link KeyModifier} (i.e. SHIFT or ALT)
     * @param name The name, as a language entry
     * @param category The category, as a language entry
     * @param conflictContext A conflict context, used for resolving how to handle conflicts with other alike bindings
     * @return This binder, for chaining
     */
    public KeyBindingBinder key(int key, KeyModifier modifier, String name, String category, IKeyConflictContext conflictContext) {
        this.keybindings.addBinding().toInstance(new KeyBindingEntry(new KeyBinding(name, conflictContext, modifier, key, category)));
        return this;
    }
}
