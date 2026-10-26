/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

/**
 * How a crop grows in water ({@code "aquatic"}).
 */
public enum AquaticMode {
    /** A land crop. */
    NONE,
    /** {@code true} / {@code "submerged"}: grows fully underwater (seaweed), planted with water above it. */
    SUBMERGED,
    /**
     * {@code "surface"}: grows in a single block of water with air above, sticking out of it (rice in a paddy). The crop draws its own
     * water surface. Crops of this kind that were planted on farmland before (when the crop wasn't aquatic) keep growing there as land crops.
     */
    SURFACE
}
