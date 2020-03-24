/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Query {

  private InetSocketAddress address;
  private Map<String, String> values;
  private String[] onlineUsernames;
  private String maxPlayers = "0";
  private String players = "0";
  private boolean online;
  private String motd;

  public Query(final String host, final int port) {
    this(new InetSocketAddress(host, port));
  }

  public Query(final InetSocketAddress address) {
    this.address = address;
  }

  public Query(final ServerData template) {
    // Split on the colon, if a port is specified it'll follow: 127.0.0.1:25565
    final String[] splitValues = template.serverIP.split(":");

    // Determine if we're specifying a port or not, if not then default to the Minecraft standard
    final int port = splitValues.length == 1 ? 25565 : Integer.valueOf(splitValues[1]);
    this.address = new InetSocketAddress(splitValues[0], port);
  }

  private static void sendPacket(final DatagramSocket socket, final InetSocketAddress targetAddress, final byte... data) throws IOException {
    final DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetAddress.getAddress(), targetAddress.getPort());
    socket.send(sendPacket);
  }

  private static void sendPacket(final DatagramSocket socket, final InetSocketAddress targetAddress, final int... data) throws IOException {
    final byte[] d = new byte[data.length];
    int i = 0;
    for (final int j : data) {
      d[i++] = (byte) (j & 0xff);
    }
    sendPacket(socket, targetAddress, d);
  }

  private static DatagramPacket receivePacket(final DatagramSocket socket, final byte[] buffer) throws IOException {
    final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
    socket.receive(dp);
    return dp;
  }

  private static String readString(final byte[] array, final AtomicInteger cursor) {
    final int startPosition = cursor.incrementAndGet();
    for (; cursor.get() < array.length && array[cursor.get()] != 0; cursor.incrementAndGet()) {}
    return new String(Arrays.copyOfRange(array, startPosition, cursor.get()));
  }

  public void sendQuery() {
    sendQueryRequest();
  }

  public boolean pingServer() {
    try {
      final Socket socket = new Socket();
      socket.connect(this.address, 1000);
      socket.close();
      this.online = true;
    } catch (final IOException ignored) {
      this.online = false;
    }
    return this.online;
  }

  public Map<String, String> getValues() {
    if (this.values == null) {
      throw new IllegalStateException("Query has not been sent yet!");
    } else {
      return this.values;
    }
  }

  public String[] getOnlineUsernames() {
    if (this.onlineUsernames == null) {
      throw new IllegalStateException("Query has not been sent yet!");
    } else {
      return this.onlineUsernames;
    }
  }

  private void sendQueryRequest() {
    try (final DatagramSocket socket = new DatagramSocket()) {
      final byte[] receiveData = new byte[10240];
      socket.setSoTimeout(1000);
      sendPacket(socket, this.address, 0xFE, 0xFD, 0x09, 0x01, 0x01, 0x01, 0x01);
      final int challengeInteger;
      {
        receivePacket(socket, receiveData);
        byte byte1;
        int i = 0;
        final byte[] buffer = new byte[8];
        for (int count = 5; (byte1 = receiveData[count++]) != 0; ) {
          buffer[i++] = byte1;
        }
        challengeInteger = Integer.parseInt(new String(buffer).trim());
      }
      sendPacket(socket, this.address, 0xFE, 0xFD, 0x00, 0x01, 0x01, 0x01, 0x01, challengeInteger >> 24, challengeInteger >> 16,
          challengeInteger >> 8, challengeInteger, 0x00, 0x00, 0x00, 0x00);
      final int length = receivePacket(socket, receiveData).getLength();
      this.values = new HashMap<>();
      final AtomicInteger cursor = new AtomicInteger(5);
      while (cursor.get() < length) {
        final String s = readString(receiveData, cursor);
        if (s.length() == 0) {
          break;
        } else {
          final String v = readString(receiveData, cursor);
          if (s.equalsIgnoreCase("numplayers")) {
            this.players = v;
          }
          if (s.equalsIgnoreCase("maxplayers")) {
            this.maxPlayers = v;
          }
          if (s.equalsIgnoreCase("hostname")) {
            this.motd = v;
          }
          this.values.put(s, v);
        }
      }

      readString(receiveData, cursor);
      final Set<String> players = new HashSet<>();
      while (cursor.get() < length) {
        final String name = readString(receiveData, cursor);
        if (name.length() > 0) {
          players.add(name);
        }
      }
      this.onlineUsernames = players.toArray(new String[0]);
    } catch (final IOException ignored) {
      // Threw exception during query which means server is restarting...
      this.maxPlayers = "-1";
    }
  }

  public String getPlayers() {
    return this.players;
  }

  public String getMaxPlayers() {
    return this.maxPlayers;
  }

  public String getMotd() {
    return this.motd;
  }

  public boolean isOnline() {
    return this.online;
  }
}
