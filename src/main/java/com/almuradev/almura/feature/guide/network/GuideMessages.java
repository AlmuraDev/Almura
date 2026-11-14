/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * The Guide's messages, as 3.5's: the client asks to open the guide and to open a page; the server sends the pages the player may see (and
 * which to show) and a page's text.
 */
public final class GuideMessages {

    // A page's text: 3.5 split it in four strings of 8191; one string is enough
    private static final int MAX_CONTENT = 262_144;

    private GuideMessages() {
    }

    /**
     * Why the guide opens: the server's {@code /almura guide force}, the player (the Features menu), or joining the server.
     */
    public enum OpenType {
        COMMAND, PLAYER, LOGGED_IN, REFRESH
    }

    /**
     * The player asks to open the guide (the Features menu's Guide button).
     */
    public record ServerboundOpen() implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("guide_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.unit(new ServerboundOpen());

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player asks for a page.
     */
    public record ServerboundPage(String id) implements CustomPacketPayload {

        public static final Type<ServerboundPage> TYPE = AlmuraNetwork.type("guide_page_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPage> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundPage::id, ServerboundPage::new);

        @Override
        public Type<ServerboundPage> type() {
            return TYPE;
        }
    }

    /**
     * One page in the list: its id and name (with colour codes).
     */
    public record Entry(String id, String name) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Entry::id, ByteBufCodecs.STRING_UTF8, Entry::name, Entry::new);
    }

    /**
     * The pages the player may see, in order, and the page to show ({@code ""} for the one shown, or the first). Opens the guide unless it
     * is a {@link OpenType#REFRESH} (the pages changed), which only updates an open guide.
     */
    public record ClientboundListing(OpenType open, List<Entry> entries, String show) implements CustomPacketPayload {

        public static final Type<ClientboundListing> TYPE = AlmuraNetwork.type("guide_listing");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundListing> CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> OpenType.values()[index], OpenType::ordinal), ClientboundListing::open,
            Entry.CODEC.apply(ByteBufCodecs.list()), ClientboundListing::entries,
            ByteBufCodecs.STRING_UTF8, ClientboundListing::show,
            ClientboundListing::new);

        @Override
        public Type<ClientboundListing> type() {
            return TYPE;
        }
    }

    /**
     * A page: its name, its text (with § colour codes and [text](address) links), and who made and last changed it, when.
     */
    public record ClientboundPage(String id, String name, String content, String creator, long created, String modifier, long modified)
        implements CustomPacketPayload {

        public static final Type<ClientboundPage> TYPE = AlmuraNetwork.type("guide_page");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPage> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundPage::id,
            ByteBufCodecs.STRING_UTF8, ClientboundPage::name,
            ByteBufCodecs.stringUtf8(MAX_CONTENT), ClientboundPage::content,
            ByteBufCodecs.STRING_UTF8, ClientboundPage::creator,
            ByteBufCodecs.VAR_LONG, ClientboundPage::created,
            ByteBufCodecs.STRING_UTF8, ClientboundPage::modifier,
            ByteBufCodecs.VAR_LONG, ClientboundPage::modified,
            ClientboundPage::new);

        @Override
        public Type<ClientboundPage> type() {
            return TYPE;
        }
    }
}
