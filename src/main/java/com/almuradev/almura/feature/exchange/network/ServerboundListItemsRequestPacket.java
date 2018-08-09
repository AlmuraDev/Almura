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

import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.almura.shared.item.BasicVirtualStack;
import com.almuradev.almura.shared.item.VirtualStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public final class ServerboundListItemsRequestPacket implements Message {

    @Nullable public String id;
    @Nullable public List<InventoryAction> actions;

    public ServerboundListItemsRequestPacket() {
    }

    public ServerboundListItemsRequestPacket(final String id, final List<InventoryAction> actions) {
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
                final ResourceLocation location = SerializationUtil.fromString(buf.readString());

                if (location == null) {
                    // TODO Malformed ResourceLocation
                    continue;
                }

                final Item item = ForgeRegistries.ITEMS.getValue(location);

                if (item == null) {
                    // TODO Unknown item
                    continue;
                }

                final int quantity = buf.readVarInt();
                final int metadata = buf.readVarInt();
                final int compoundDataLength = buf.readVarInt();
                NBTTagCompound compound = null;

                if (compoundDataLength > 0) {
                    try {
                        compound = SerializationUtil.compoundFromBytes(buf.readBytes(compoundDataLength));
                    } catch (IOException e) {
                        // TODO Malformed tag compound
                        e.printStackTrace();
                        continue;
                    }
                }
                final BasicVirtualStack stack = new BasicVirtualStack(item, quantity, metadata, compound);
                this.actions.add(new InventoryAction(direction, stack));
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

        for (final InventoryAction action : this.actions) {
            final VirtualStack stack = action.getStack();

            final ResourceLocation location = stack.getItem().getRegistryName();
            if (location == null) {
                // TODO Bad item, no location
                continue;
            }

            final NBTTagCompound compound = stack.getCompound();
            byte[] compoundData = null;

            if (compound != null) {
                try {
                    compoundData = SerializationUtil.toBytes(compound);
                } catch (IOException e) {
                    // TODO Malformed tag compound
                    e.printStackTrace();
                    continue;
                }
            }

            buf.writeString(action.getDirection().name().toUpperCase());

            buf.writeString(SerializationUtil.toString(location));
            buf.writeVarInt(stack.getQuantity());
            buf.writeVarInt(stack.getMetadata());

            if (compoundData == null) {
                buf.writeVarInt(0);
            } else {
                buf.writeVarInt(compoundData.length);
                buf.writeBytes(compoundData);
            }
        }
    }
}
