/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The titles' messages, as 3.5's packets. A title is named by its id; an empty id is no title.
 */
public final class TitleMessages {

    public static final int MAX_ID = 64, MAX_PERMISSION = 255, MAX_CONTENT = 255;

    private TitleMessages() {
    }

    /**
     * A change to the titles: add, modify or delete one.
     */
    public enum ModifyType {
        ADD, MODIFY, DELETE
    }

    private static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(final E[] values) {
        return ByteBufCodecs.idMapper(index -> values[index], Enum::ordinal);
    }

    /**
     * The titles the player may see: every one, the hidden ones only for title administrators.
     */
    public record ClientboundRegistry(List<Title> titles) implements CustomPacketPayload {

        public static final Type<ClientboundRegistry> TYPE = AlmuraNetwork.type("title_registry");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRegistry> CODEC = StreamCodec.composite(
            Title.CODEC.apply(ByteBufCodecs.list()), ClientboundRegistry::titles, ClientboundRegistry::new);

        @Override
        public Type<ClientboundRegistry> type() {
            return TYPE;
        }
    }

    /**
     * The titles the player may pick.
     */
    public record ClientboundAvailable(List<String> ids) implements CustomPacketPayload {

        public static final Type<ClientboundAvailable> TYPE = AlmuraNetwork.type("title_available");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAvailable> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ClientboundAvailable::ids, ClientboundAvailable::new);

        @Override
        public Type<ClientboundAvailable> type() {
            return TYPE;
        }
    }

    /**
     * Each online player's title: all of them ({@code replace}), or changes.
     */
    public record ClientboundSelected(boolean replace, Map<UUID, String> selected) implements CustomPacketPayload {

        public static final Type<ClientboundSelected> TYPE = AlmuraNetwork.type("title_selected");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSelected> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundSelected::replace,
            ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), ClientboundSelected::selected,
            ClientboundSelected::new);

        @Override
        public Type<ClientboundSelected> type() {
            return TYPE;
        }
    }

    /**
     * Opens the Titles window: whether the player may pick a title, and manage them.
     */
    public record ClientboundOpen(boolean canChange, boolean admin) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("title_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundOpen::canChange, ByteBufCodecs.BOOL, ClientboundOpen::admin, ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player asks to open the Titles window (the Features menu).
     */
    public record ServerboundOpen() implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("title_open_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.unit(new ServerboundOpen());

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player picks a title, or none (an empty id).
     */
    public record ServerboundSelect(String id) implements CustomPacketPayload {

        public static final Type<ServerboundSelect> TYPE = AlmuraNetwork.type("title_select");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSelect> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundSelect::id, ServerboundSelect::new);

        @Override
        public Type<ServerboundSelect> type() {
            return TYPE;
        }
    }

    /**
     * An administrator adds, modifies or deletes a title.
     */
    public record ServerboundModify(ModifyType modify, String id, String permission, String content, boolean hidden)
        implements CustomPacketPayload {

        public static final Type<ServerboundModify> TYPE = AlmuraNetwork.type("title_modify");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundModify> CODEC = StreamCodec.composite(
            enumCodec(ModifyType.values()), ServerboundModify::modify,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModify::id,
            ByteBufCodecs.stringUtf8(MAX_PERMISSION), ServerboundModify::permission,
            ByteBufCodecs.stringUtf8(MAX_CONTENT * 2), ServerboundModify::content,
            ByteBufCodecs.BOOL, ServerboundModify::hidden,
            ServerboundModify::new);

        @Override
        public Type<ServerboundModify> type() {
            return TYPE;
        }
    }
}
