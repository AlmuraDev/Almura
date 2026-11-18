/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * The CUI channel when WorldEdit isn't installed on this client (it doesn't need to be: WorldEdit runs on the server). Almura registers
 * {@code worldedit:cui} itself, as WorldEdit's protocol does: optional, version {@value ClientWorldEditSelection#PROTOCOL}, and the same
 * wire format, the message's {@code type|arg|arg} text as UTF-8 filling the payload. Optional, so servers without WorldEdit don't need it.
 * <p>
 * Only registered on the client, and only without the WorldEditCUI protocol mod, which registers the same payload.
 */
final class AlmuraCuiChannel implements CuiChannel {

    private AlmuraCuiChannel() {
    }

    static CuiChannel register(final RegisterPayloadHandlersEvent event, final ClientWorldEditSelection selection) {
        event.registrar(ClientWorldEditSelection.PROTOCOL).optional().playBidirectional(Message.TYPE, Message.CODEC,
            // Only ever reached in single player without WorldEdit, where nothing listens for the handshake
            (message, context) -> {
            },
            (message, context) -> {
                // As WorldEdit's protocol reads it: "+" before the type marks a message about another plugin's regions
                final String[] parts = message.text().split("\\|", -1);
                final boolean multi = parts[0].startsWith("+");
                final String type = multi ? parts[0].substring(1) : parts[0];
                final List<String> args = List.of(Arrays.copyOfRange(parts, 1, parts.length));
                selection.receive(multi, type, args);
            });
        return new AlmuraCuiChannel();
    }

    @Override
    public CustomPacketPayload.Type<?> type() {
        return Message.TYPE;
    }

    @Override
    public CustomPacketPayload handshake() {
        return new Message(ClientWorldEditSelection.HANDSHAKE + "|" + ClientWorldEditSelection.PROTOCOL);
    }

    /**
     * A CUI message, as its text.
     */
    record Message(String text) implements CustomPacketPayload {

        static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("worldedit", "cui"));
        static final StreamCodec<FriendlyByteBuf, Message> CODEC = CustomPacketPayload.codec(Message::write, Message::read);

        private static Message read(final FriendlyByteBuf buffer) {
            final String text = buffer.toString(StandardCharsets.UTF_8);
            buffer.skipBytes(buffer.readableBytes());
            return new Message(text);
        }

        private void write(final FriendlyByteBuf buffer) {
            buffer.writeBytes(this.text.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public CustomPacketPayload.Type<Message> type() {
            return TYPE;
        }
    }
}
