/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import org.jspecify.annotations.Nullable;

/**
 * The memberships, as 3.5's: each its level (the place on the LuckPerms {@code members} track after survivor), its in-game price and the
 * skills total that also earns it.
 */
public enum Membership {
    CITIZEN(1, "Citizen", "citizen", 2_500_000L, "$2,500,000", "$2.5 million in-game", 250),
    EXPLORER(2, "Explorer", "explorer", 5_000_000L, "$5,000,000", "$5 million in-game", 375),
    PIONEER(3, "Pioneer", "pioneer", 10_000_000L, "$10,000,000", "$10 million in-game", 400);

    /** A player who isn't on the members track (default, staff): they can't buy one. */
    public static final int NONE = -1;
    /** A survivor: the track's first group, before any membership. */
    public static final int SURVIVOR = 0;

    private final int level;
    private final String displayName, group, price, priceButton;
    private final long fee;
    private final int skillsTotal;

    Membership(final int level, final String displayName, final String group, final long fee, final String price, final String priceButton,
        final int skillsTotal) {
        this.level = level;
        this.displayName = displayName;
        this.group = group;
        this.fee = fee;
        this.price = price;
        this.priceButton = priceButton;
        this.skillsTotal = skillsTotal;
    }

    public int level() {
        return this.level;
    }

    public String displayName() {
        return this.displayName;
    }

    /**
     * @return the LuckPerms group it puts the player in
     */
    public String group() {
        return this.group;
    }

    public long fee() {
        return this.fee;
    }

    /**
     * @return the price as 3.5 showed it in its confirmation ({@code $2,500,000})
     */
    public String price() {
        return this.price;
    }

    /**
     * @return the price as 3.5's buy button read
     */
    public String priceButton() {
        return this.priceButton;
    }

    public int skillsTotal() {
        return this.skillsTotal;
    }

    /**
     * Whether a player at {@code current} (see {@link #levelOf}) may take this one: they are on the track, and below it.
     */
    public boolean availableFrom(final int current) {
        return current >= SURVIVOR && current < this.level;
    }

    public static @Nullable Membership byLevel(final int level) {
        for (final Membership membership : values()) {
            if (membership.level == level) {
                return membership;
            }
        }
        return null;
    }

    /**
     * @return a LuckPerms primary group's place on the members track, as 3.5 read it: survivor 0, citizen 1, explorer 2, pioneer 3, and
     *     {@link #NONE} for any other group
     */
    public static int levelOf(final String group) {
        if (group.equalsIgnoreCase("survivor")) {
            return SURVIVOR;
        }
        for (final Membership membership : values()) {
            if (membership.group.equalsIgnoreCase(group)) {
                return membership.level;
            }
        }
        return NONE;
    }
}
