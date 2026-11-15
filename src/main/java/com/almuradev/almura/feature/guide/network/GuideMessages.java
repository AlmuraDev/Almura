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
 * The Guide's messages, as 3.5's: the client asks to open the guide, to open a page, to add, change or remove one and to move one up or
 * down; the server sends the pages the player may see (and which to show, and what they may do), a page's text, and how a change went.
 */
public final class GuideMessages {

    // A page's text: 3.5 split it in four strings of 8191 (32764 in all); one string is enough
    public static final int MAX_CONTENT = 32_764;
    public static final int MAX_NAME = 50;

    private GuideMessages() {
    }

    /**
     * Why the guide opens: the server's {@code /almura guide force}, the player (the Features menu), or joining the server; or only an
     * update of the open guide (the pages changed).
     */
    public enum OpenType {
        COMMAND, PLAYER, LOGGED_IN, REFRESH
    }

    /**
     * A change to a page.
     */
    public enum ChangeType {
        ADD, MODIFY, REMOVE
    }

    private static <E extends Enum<E>> StreamCodec<io.netty.buffer.ByteBuf, E> enumCodec(final E[] values) {
        return ByteBufCodecs.idMapper(index -> values[index], Enum::ordinal);
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
     * The player adds, changes or removes a page. For an added player guide the server picks the id from the name.
     */
    public record ServerboundChange(ChangeType change, String id, int index, String name, String content, boolean playerGuide)
        implements CustomPacketPayload {

        public static final Type<ServerboundChange> TYPE = AlmuraNetwork.type("guide_change");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChange> CODEC = StreamCodec.composite(
            enumCodec(ChangeType.values()), ServerboundChange::change,
            ByteBufCodecs.STRING_UTF8, ServerboundChange::id,
            ByteBufCodecs.VAR_INT, ServerboundChange::index,
            ByteBufCodecs.stringUtf8(MAX_NAME * 4), ServerboundChange::name,
            ByteBufCodecs.stringUtf8(MAX_CONTENT), ServerboundChange::content,
            ByteBufCodecs.BOOL, ServerboundChange::playerGuide,
            ServerboundChange::new);

        @Override
        public Type<ServerboundChange> type() {
            return TYPE;
        }
    }

    /**
     * The player moves a page up or down its group.
     */
    public record ServerboundMove(String id, boolean up) implements CustomPacketPayload {

        public static final Type<ServerboundMove> TYPE = AlmuraNetwork.type("guide_move");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMove> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundMove::id, ByteBufCodecs.BOOL, ServerboundMove::up, ServerboundMove::new);

        @Override
        public Type<ServerboundMove> type() {
            return TYPE;
        }
    }

    /**
     * One page in the list: its id and name (with colour codes), whether it is a player's guide (the player's own, or whose), and what the
     * player may do with it.
     */
    public record Entry(String id, String name, boolean playerGuide, boolean mine, String owner, boolean editable, boolean removable,
        boolean movable) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.of((buffer, entry) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, entry.id);
            ByteBufCodecs.STRING_UTF8.encode(buffer, entry.name);
            buffer.writeBoolean(entry.playerGuide);
            buffer.writeBoolean(entry.mine);
            ByteBufCodecs.STRING_UTF8.encode(buffer, entry.owner);
            buffer.writeBoolean(entry.editable);
            buffer.writeBoolean(entry.removable);
            buffer.writeBoolean(entry.movable);
        }, buffer -> new Entry(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readBoolean(),
            buffer.readBoolean(), ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean()));
    }

    /**
     * What the player may do: add server pages, add their own guides, administer the guide (everyone's guides, moving server pages), and
     * remove pages.
     */
    public record Abilities(boolean add, boolean addPlayer, boolean admin, boolean remove) {

        public static final Abilities NONE = new Abilities(false, false, false, false);
        public static final StreamCodec<RegistryFriendlyByteBuf, Abilities> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Abilities::add, ByteBufCodecs.BOOL, Abilities::addPlayer, ByteBufCodecs.BOOL, Abilities::admin,
            ByteBufCodecs.BOOL, Abilities::remove, Abilities::new);

        public boolean edits() {
            return this.admin || this.addPlayer || this.add;
        }
    }

    /**
     * The pages the player may see, in order, what the player may do, and the page to show ({@code ""} for the one shown, or the first).
     * Opens the guide unless it is a {@link OpenType#REFRESH}, which only updates an open guide.
     */
    public record ClientboundListing(OpenType open, List<Entry> entries, Abilities abilities, String show) implements CustomPacketPayload {

        public static final Type<ClientboundListing> TYPE = AlmuraNetwork.type("guide_listing");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundListing> CODEC = StreamCodec.composite(
            enumCodec(OpenType.values()), ClientboundListing::open,
            Entry.CODEC.apply(ByteBufCodecs.list()), ClientboundListing::entries,
            Abilities.CODEC, ClientboundListing::abilities,
            ByteBufCodecs.STRING_UTF8, ClientboundListing::show,
            ClientboundListing::new);

        @Override
        public Type<ClientboundListing> type() {
            return TYPE;
        }
    }

    /**
     * A page: its index and name, its text (with § colour codes and [text](address) links), who made and last changed it and when, and
     * whether the player may change it.
     */
    public record ClientboundPage(String id, int index, String name, String content, String creator, long created, String modifier,
        long modified, boolean playerGuide, boolean editable) implements CustomPacketPayload {

        public static final Type<ClientboundPage> TYPE = AlmuraNetwork.type("guide_page");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPage> CODEC = StreamCodec.of((buffer, page) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, page.id);
            buffer.writeVarInt(page.index);
            ByteBufCodecs.STRING_UTF8.encode(buffer, page.name);
            ByteBufCodecs.stringUtf8(MAX_CONTENT * 2).encode(buffer, page.content);
            ByteBufCodecs.STRING_UTF8.encode(buffer, page.creator);
            buffer.writeVarLong(page.created);
            ByteBufCodecs.STRING_UTF8.encode(buffer, page.modifier);
            buffer.writeVarLong(page.modified);
            buffer.writeBoolean(page.playerGuide);
            buffer.writeBoolean(page.editable);
        }, buffer -> new ClientboundPage(ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readVarInt(), ByteBufCodecs.STRING_UTF8.decode(buffer),
            ByteBufCodecs.stringUtf8(MAX_CONTENT * 2).decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readVarLong(),
            ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readVarLong(), buffer.readBoolean(), buffer.readBoolean()));

        @Override
        public Type<ClientboundPage> type() {
            return TYPE;
        }
    }

    /**
     * How a change went: when it failed, why.
     */
    public record ClientboundChangeResult(ChangeType change, boolean success, String id, String message) implements CustomPacketPayload {

        public static final Type<ClientboundChangeResult> TYPE = AlmuraNetwork.type("guide_change_result");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChangeResult> CODEC = StreamCodec.composite(
            enumCodec(ChangeType.values()), ClientboundChangeResult::change,
            ByteBufCodecs.BOOL, ClientboundChangeResult::success,
            ByteBufCodecs.STRING_UTF8, ClientboundChangeResult::id,
            ByteBufCodecs.STRING_UTF8, ClientboundChangeResult::message,
            ClientboundChangeResult::new);

        @Override
        public Type<ClientboundChangeResult> type() {
            return TYPE;
        }
    }
}
