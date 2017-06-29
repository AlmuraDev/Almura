/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import net.minecraft.client.gui.BossInfoClient;

import java.util.Map;
import java.util.UUID;

public interface IMixinGuiBossOverlay {

    Map<UUID, BossInfoClient> getBossInfo();
}
