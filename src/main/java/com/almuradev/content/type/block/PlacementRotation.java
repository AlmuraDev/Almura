/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

/**
 * How a horizontal block placed against the side of another block (such as the end of a pipe) is turned, instead of facing the
 * player. Set with {@code rotate_placement}: {@code true} or {@code "across"}, {@code "toward"} or {@code "corner"}. On a floor or
 * ceiling the block always faces the player.
 */
public enum PlacementRotation {
    /** A quarter turn from the clicked side, so a model running across its facing continues out from that block (straight pipes). */
    ACROSS,
    /** Facing the clicked block, so the model's front points into it (the branch of a T pipe). */
    TOWARD,
    /**
     * For a model with arms to the west and south when facing north (corner pipes): one arm into the clicked block, the other
     * turned toward the side the player stands on (or looks toward, standing in line).
     */
    CORNER
}
