/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor;

/**
 * Holder so the items are only created when first used during item registration, once Almura's item groups (creative tabs) exist.
 */
public final class ArmorItems {

    public static final LightedHelmetItem LIGHTED_HELMET = new LightedHelmetItem();

    private ArmorItems() {
    }
}
