/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

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

  private DatabaseUtils() {}
}
