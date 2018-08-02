/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class ServerboundModifyListItemsPacket implements Message {

    @Nullable public String id;
    @Nullable public List<InventoryAction> actions;

    public ServerboundModifyListItemsPacket() {
    }

    public ServerboundModifyListItemsPacket(final String id, final List<InventoryAction> actions) {
        checkNotNull(id);
        checkNotNull(actions);
        checkState(!actions.isEmpty());

        this.id = id;
        this.actions = actions;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.id = buf.readString();

        final int count = buf.readVarInt();

        if (count > 0) {
            this.actions = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final InventoryAction.Direction direction = InventoryAction.Direction.valueOf(buf.readString());
                try {
                    final ItemStack itemStack = ((PacketBuffer) buf).readItemStack();
                    actions.add(new InventoryAction(direction, itemStack));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.actions);
        checkState(!this.actions.isEmpty());

        buf.writeString(this.id);
        buf.writeVarInt(this.actions.size());

        this.actions.forEach(action -> {
            buf.writeString(action.getDirection().name().toUpperCase());
            // TODO Add writeItemStack to API ChannelBuf
            ((PacketBuffer) buf).writeItemStack(action.getStack());
        });
    }
}
