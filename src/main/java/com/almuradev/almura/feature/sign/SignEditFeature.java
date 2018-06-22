/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.sign;

import com.almuradev.core.event.Witness;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.tileentity.TileEntitySign;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.util.VecHelper;

public final class SignEditFeature implements Witness {

    @Listener
    public void interact(final InteractBlockEvent.Secondary.MainHand event, @Root final Player player) {
        if (!(player instanceof EntityPlayerMP) || !player.require(Keys.IS_SNEAKING)) {
            return;
        }
        final BlockSnapshot snapshot = event.getTargetBlock();
        final BlockType type = snapshot.getState().getType();
        if (type == BlockTypes.STANDING_SIGN || type == BlockTypes.WALL_SIGN) {
            snapshot.getLocation().flatMap(Location::getTileEntity).ifPresent((be) -> {
                if (be instanceof TileEntitySign) {
                    ((TileEntitySign) be).setEditable(true);
                    ((TileEntitySign) be).setPlayer((EntityPlayer)player);
                    ((EntityPlayerMP) player).connection.sendPacket(new SPacketSignEditorOpen(VecHelper.toBlockPos(snapshot.getPosition())));
                }
            });
        }
    }
}
