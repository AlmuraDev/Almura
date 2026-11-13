/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Tells the client which claim outlines to draw. An empty box list clears them.
 */
public final class ClientboundClaimVisualPacket implements Message {

    public static final byte BASIC = 0;
    public static final byte ADMIN = 1;
    public static final byte TOWN = 2;
    public static final byte SUBDIVISION = 3;

    public final List<Box> boxes = new ArrayList<>();

    public ClientboundClaimVisualPacket() {
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.boxes.add(new Box(buf.readByte(), buf.readBoolean(), buf.readInteger(), buf.readInteger(), buf.readInteger(), buf.readInteger(),
                buf.readInteger(), buf.readInteger()));
        }
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.boxes.size());
        for (final Box box : this.boxes) {
            buf.writeByte(box.type);
            buf.writeBoolean(box.cuboid);
            buf.writeInteger(box.minX);
            buf.writeInteger(box.minY);
            buf.writeInteger(box.minZ);
            buf.writeInteger(box.maxX);
            buf.writeInteger(box.maxY);
            buf.writeInteger(box.maxZ);
        }
    }

    /**
     * A claim's bounds in block coordinates, both corners inclusive.
     */
    public static final class Box {

        public final byte type;
        public final boolean cuboid;
        public final int minX, minY, minZ, maxX, maxY, maxZ;

        public Box(final byte type, final boolean cuboid, final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
            this.type = type;
            this.cuboid = cuboid;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }
}
