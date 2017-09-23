/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import net.minecraft.client.gui.BossInfoClient;

import java.util.Map;
import java.util.UUID;

public interface IMixinGuiBossOverlay {

    Map<UUID, BossInfoClient> getBossInfo();
}
