/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.treefeller;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Keeps a felled tree from removing blocks in a different claim than the log the player cut.
 */
interface ClaimGuard {

    /**
     * Used when no claim plugin is installed: every position is in the same "claim".
     */
    ClaimGuard NONE = (world, pos) -> Boolean.TRUE;

    /**
     * @return a key identifying the claim at the position. Two positions are in the same claim when their keys are equal.
     */
    Object claimAt(World world, BlockPos pos);
}
