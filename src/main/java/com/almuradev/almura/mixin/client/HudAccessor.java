/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Which bar vanilla shows above the hotbar (experience, the locator or a mount's jump): Almura's HUD shows experience itself, and hides
 * vanilla's bar only while it is the experience one.
 */
@Mixin(Hud.class)
public interface HudAccessor {

    /**
     * @return what the bar shows (vanilla's private ContextualInfo, read by name) and the bar
     */
    @Accessor("contextualInfoBar")
    Pair<?, ?> almura$contextualInfoBar();
}
