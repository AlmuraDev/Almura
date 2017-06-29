/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.asm.mixin.interfaces.IMixinGuiBossOverlay;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiBossOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

/**
 * This Mixin's objective is to have the Mojang BossBar's respect a Y offset provided by the HUD in-use
 */
@Mixin(GuiBossOverlay.class)
public abstract class MixinGuiBossOverlay extends Gui implements IMixinGuiBossOverlay {

    @Shadow @Final private Map<UUID, BossInfoClient> mapBossInfos;

    @Override
    public Map<UUID, BossInfoClient> getBossInfo() {
        return this.mapBossInfos;
    }
}
