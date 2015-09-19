/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

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

/**
 * TODO When AlmuraSDK is a Sponge plugin, use the Query event and remove this
 */
public class Query {

    private InetSocketAddress address;
    private Map<String, String> values;
    private String[] onlineUsernames;
    private String maxPlayers, onlinePlayers = "0";
    private boolean online;


    public Query(String host, int port) {
        this(new InetSocketAddress(host, port));
    }

    public Query(InetSocketAddress address) {
        this.address = address;
    }

    public Query(ServerData template, int port) {
        this(template.serverIP, port);
    }

    private static void sendPacket(DatagramSocket socket, InetSocketAddress targetAddress, byte... data) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, targetAddress.getAddress(), targetAddress.getPort());
        socket.send(sendPacket);
    }

    private static void sendPacket(DatagramSocket socket, InetSocketAddress targetAddress, int... data) throws IOException {
        final byte[] d = new byte[data.length];
        int i = 0;
        for (int j : data) {
            d[i++] = (byte) (j & 0xff);
        }
        sendPacket(socket, targetAddress, d);
    }

    private static DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws IOException {
        final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        socket.receive(dp);
        return dp;
    }

    private static String readString(byte[] array, AtomicInteger cursor) {
        final int startPosition = cursor.incrementAndGet();
        for (; cursor.get() < array.length && array[cursor.get()] != 0; cursor.incrementAndGet()) {
            ;
        }
        return new String(Arrays.copyOfRange(array, startPosition, cursor.get()));
    }

    public void sendQuery() throws IOException {
        sendQueryRequest();
    }

    public boolean pingServer() {
        try {
            final Socket socket = new Socket();
            socket.connect(address, 1000);
            socket.close();
            online = true;
            return true;
        } catch (IOException ignored) {
        }
        online = false;
        return false;
    }

    public Map<String, String> getValues() {
        if (values == null) {
            throw new IllegalStateException("Query has not been sent yet!");
        } else {
            return values;
        }
    }

    public String[] getOnlineUsernames() {
        if (onlineUsernames == null) {
            throw new IllegalStateException("Query has not been sent yet!");
        } else {
            return onlineUsernames;
        }
    }

    private void sendQueryRequest() throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            final byte[] receiveData = new byte[10240];
            socket.setSoTimeout(1000);
            sendPacket(socket, address, 0xFE, 0xFD, 0x09, 0x01, 0x01, 0x01, 0x01);
            final int challengeInteger;
            {
                receivePacket(socket, receiveData);
                byte byte1;
                int i = 0;
                byte[] buffer = new byte[8];
                for (int count = 5; (byte1 = receiveData[count++]) != 0; ) {
                    buffer[i++] = byte1;
                }
                challengeInteger = Integer.parseInt(new String(buffer).trim());
            }
            sendPacket(socket, address, 0xFE, 0xFD, 0x00, 0x01, 0x01, 0x01, 0x01, challengeInteger >> 24, challengeInteger >> 16,
                    challengeInteger >> 8, challengeInteger, 0x00, 0x00, 0x00, 0x00);
            final int length = receivePacket(socket, receiveData).getLength();
            values = new HashMap<>();
            final AtomicInteger cursor = new AtomicInteger(5);
            while (cursor.get() < length) {
                final String s = readString(receiveData, cursor);
                if (s.length() == 0) {
                    break;
                } else {
                    final String v = readString(receiveData, cursor);
                    if (s.equalsIgnoreCase("numplayers")) {
                        onlinePlayers = v;
                    }
                    if (s.equalsIgnoreCase("maxplayers")) {
                        maxPlayers = v;
                    }
                    values.put(s, v);
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
            onlineUsernames = players.toArray(new String[players.size()]);
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }
    }

    public String getPlayers() {
        return onlinePlayers;
    }

    public String getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isOnline() {
        return online;
    }
}
