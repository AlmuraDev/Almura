/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.multiplayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Asks a server how it is through the standard server query (enable-query): whether it answers at all, its player count, its MOTD and
 * whether it is whitelisted. As 1.12's Query did; call it off the client thread, since each step can wait up to a second.
 */
final class ServerQuery {

    private static final int TIMEOUT_MILLIS = 1000;

    private final InetSocketAddress address;

    ServerQuery(final String host, final int port) {
        this.address = new InetSocketAddress(host, port);
    }

    /**
     * @return Whether the server takes a connection
     */
    boolean ping() {
        try (Socket socket = new Socket()) {
            socket.connect(this.address, TIMEOUT_MILLIS);
            return true;
        } catch (final IOException ignored) {
            return false;
        }
    }

    /**
     * @return The query's values ("numplayers", "maxplayers", "hostname", "whitelist" and so on), or null when the server didn't answer it,
     *     which Almura's servers do while they restart
     */
    Map<String, String> query() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MILLIS);
            final byte[] received = new byte[10240];

            // Handshake: the server answers with a challenge number, as text
            send(socket, 0xFE, 0xFD, 0x09, 0x01, 0x01, 0x01, 0x01);
            receive(socket, received);
            final StringBuilder challengeText = new StringBuilder();
            for (int i = 5; i < received.length && received[i] != 0; i++) {
                challengeText.append((char) received[i]);
            }
            final int challenge = Integer.parseInt(challengeText.toString().trim());

            // Full stat request
            send(socket, 0xFE, 0xFD, 0x00, 0x01, 0x01, 0x01, 0x01, challenge >> 24, challenge >> 16, challenge >> 8, challenge, 0x00, 0x00, 0x00,
                0x00);
            final int length = receive(socket, received);
            final Map<String, String> values = new HashMap<>();
            final AtomicInteger cursor = new AtomicInteger(5);
            while (cursor.get() < length) {
                final String key = readString(received, cursor);
                if (key.isEmpty()) {
                    break;
                }
                values.put(key.toLowerCase(java.util.Locale.ROOT), readString(received, cursor));
            }
            return values;
        } catch (final IOException | NumberFormatException ignored) {
            return null;
        }
    }

    private void send(final DatagramSocket socket, final int... data) throws IOException {
        final byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) (data[i] & 0xFF);
        }
        socket.send(new DatagramPacket(bytes, bytes.length, this.address.getAddress(), this.address.getPort()));
    }

    private static int receive(final DatagramSocket socket, final byte[] buffer) throws IOException {
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet.getLength();
    }

    // A zero-ended string after the cursor; leaves the cursor on its zero
    private static String readString(final byte[] array, final AtomicInteger cursor) {
        final int start = cursor.incrementAndGet();
        while (cursor.get() < array.length && array[cursor.get()] != 0) {
            cursor.incrementAndGet();
        }
        return new String(Arrays.copyOfRange(array, start, Math.min(cursor.get(), array.length)));
    }
}
