/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

/**
 * The checkboxes in the claim manager's Flags section, in display order. Checked means the claim is protected from it. The server maps each one
 * to GriefDefender flags; the client only needs the labels. Packets carry them as a bitmask of {@link #bit()}, so new ones go at the end.
 */
public enum ClaimFlag {
    PREVENT_MONSTERS("Prevent Monsters", "Stops monsters spawning in this claim or hurting players in it"),
    PREVENT_PVP("Prevent PvP", "Stops players fighting each other in this claim"),
    PREVENT_EXPLOSIONS("Prevent Explosions", "Stops explosions breaking blocks in this claim"),
    PREVENT_FIRE_SPREAD("Prevent Fire Spread", "Stops fire spreading, and lava starting fires, in this claim"),
    PREVENT_ENDERMAN_GRIEF("Prevent Enderman Griefing", "Stops endermen picking up or placing blocks in this claim");

    public final String label;
    public final String tooltip;

    ClaimFlag(final String label, final String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

    public int bit() {
        return 1 << this.ordinal();
    }

    public boolean in(final int flags) {
        return (flags & this.bit()) != 0;
    }
}
