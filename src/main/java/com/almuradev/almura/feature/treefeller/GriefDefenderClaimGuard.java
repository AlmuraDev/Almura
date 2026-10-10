/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.treefeller;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Only loaded when GriefDefender is installed. Positions are in the same claim when GriefDefender returns the same claim (wilderness
 * included) for both.
 */
final class GriefDefenderClaimGuard implements ClaimGuard {

    @Override
    public Object claimAt(final World world, final BlockPos pos) {
        final Claim claim = GriefDefender.getCore()
                .getClaimManager(((org.spongepowered.api.world.World) world).getUniqueId())
                .getClaimAt(pos.getX(), pos.getY(), pos.getZ());
        return claim == null ? Boolean.TRUE : claim.getUniqueId();
    }
}
