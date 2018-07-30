/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.util.ResourceLocation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public final class DatabaseUtils {

  public static byte[] toBytes(final UUID uuid) {
    final byte[] bytes = new byte[16];
    ByteBuffer.wrap(bytes)
      .order(ByteOrder.BIG_ENDIAN)
      .putLong(uuid.getMostSignificantBits())
      .putLong(uuid.getLeastSignificantBits());
    return bytes;
  }

  public static UUID fromBytes(final byte[] bytes) {
    final ByteBuffer buf = ByteBuffer.wrap(bytes)
      .order(ByteOrder.BIG_ENDIAN);
    return new UUID(buf.getLong(), buf.getLong());
  }

  public static String toString(final ResourceLocation location) {
    checkNotNull(location);

    final String domain = location.getResourceDomain();
    final String path = location.getResourcePath();

    return domain + ":" + path;
  }

  public static ResourceLocation fromString(final String value) {
    checkNotNull(value);

    if (!value.contains(":")) {
      return null;
    }

    final int index = value.indexOf(":");

    return new ResourceLocation(value.substring(0, index), value.substring(index + 1, value.length()));
  }

  private DatabaseUtils() {}
}
