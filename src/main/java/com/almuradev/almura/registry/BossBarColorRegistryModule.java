/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.asm.mixin.interfaces.IMixinBossBarColor;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.util.Color;

public final class BossBarColorRegistryModule implements RegistryModule {

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.register(BossBarColors.BLUE, Color.ofRgb(0, 183, 236));
        this.register(BossBarColors.GREEN, Color.ofRgb(29, 236, 0));
        this.register(BossBarColors.PINK, Color.ofRgb(236, 0, 184));
        this.register(BossBarColors.PURPLE, Color.ofRgb(123, 0, 236));
        this.register(BossBarColors.RED, Color.ofRgb(236, 53, 0));
        this.register(BossBarColors.WHITE, Color.ofRgb(236, 236, 236));
        this.register(BossBarColors.YELLOW, Color.ofRgb(233, 236, 0));
    }

    private void register(final BossBarColor definition, final Color color) {
        ((IMixinBossBarColor) definition).setAlmuraColor(color);
    }
}
