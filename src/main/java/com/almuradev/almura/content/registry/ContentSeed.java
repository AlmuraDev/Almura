/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * A seed from the content: it plants its crop where the crop may stand (on its soil), as vanilla's seeds do, and planting tires the
 * player a little, as on 3.5 ("this is how we make players use more food/drink").
 */
public final class ContentSeed extends BlockItem {

    ContentSeed(final Block crop, final Properties properties) {
        super(crop, properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        if (player != null) {
            player.causeFoodExhaustion(0.01F);
        }
        return super.useOn(context);
    }
}
