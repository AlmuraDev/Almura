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

import com.almuradev.almura.feature.exchange.InventoryAction;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VirtualStack;
import com.almuradev.almura.shared.util.SerializationUtil;
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
    public void readFrom(final ChannelBuf buf) {
        this.id = buf.readString();

        final int count = buf.readInteger();

        checkState(count > 0);

        this.actions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final InventoryAction.Direction direction = InventoryAction.Direction.valueOf(buf.readString());
            final ResourceLocation location = new ResourceLocation(buf.readString(), buf.readString());

            final Item item = ForgeRegistries.ITEMS.getValue(location);

            if (item == null) {
                new IOException("Unknown item id '" + location.toString() + "' when receiving list item! . Skipping...").printStackTrace();
                continue;
            }

            final int quantity = buf.readInteger();
            final int metadata = buf.readInteger();
            final int compoundDataLength = buf.readInteger();
            NBTTagCompound compound = null;

            if (compoundDataLength > 0) {
                try {
                    compound = SerializationUtil.compoundFromBytes(buf.readBytes(compoundDataLength));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            final BasicVanillaStack stack = new BasicVanillaStack(item, quantity, metadata, compound);
            this.actions.add(new InventoryAction(direction, stack));
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        checkNotNull(this.id);
        checkNotNull(this.actions);
        checkState(!this.actions.isEmpty());

        buf.writeString(this.id);
        buf.writeInteger(this.actions.size());

        for (final InventoryAction action : this.actions) {
            final VirtualStack stack = action.getStack();

            final ResourceLocation location = stack.getItem().getRegistryName();
            if (location == null) {
                new IOException("Malformed resource location for Item '" + stack + "' when sending list item!").printStackTrace();
                continue;
            }

            final NBTTagCompound compound = stack.getCompound();
            byte[] compoundData = null;

            if (compound != null) {
                try {
                    compoundData = SerializationUtil.toBytes(compound);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            buf.writeString(action.getDirection().name().toUpperCase());

            buf.writeString(location.getResourceDomain());
            buf.writeString(location.getResourcePath());

            buf.writeInteger(stack.getQuantity());

            buf.writeInteger(stack.getMetadata());

            if (compoundData == null) {
                buf.writeInteger(0);
            } else {
                buf.writeInteger(compoundData.length);
                buf.writeBytes(compoundData);
            }
        }
    }
}
