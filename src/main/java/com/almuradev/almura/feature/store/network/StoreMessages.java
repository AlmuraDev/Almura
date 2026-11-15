/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network;

import com.almuradev.almura.feature.store.StoreSide;
import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * The stores' messages, as 3.5's packets. Prices go as text ({@code BigDecimal}'s), so nothing is rounded on the way; items go with their
 * components, one of each (a listing's quantity is its own).
 */
public final class StoreMessages {

    public static final int MAX_ID = 255, MAX_NAME = 255, MAX_PERMISSION = 255;

    private StoreMessages() {
    }

    private static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(final E[] values) {
        return ByteBufCodecs.idMapper(index -> values[index], Enum::ordinal);
    }

    /**
     * A store as the client knows it, for the management screen and to open it.
     */
    public record StoreInfo(String id, String name, String permission, boolean hidden, UUID creator, String creatorName, long created) {

        public static final StreamCodec<ByteBuf, StoreInfo> CODEC = StreamCodec.of((buffer, store) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, store.id);
            ByteBufCodecs.STRING_UTF8.encode(buffer, store.name);
            ByteBufCodecs.STRING_UTF8.encode(buffer, store.permission);
            buffer.writeBoolean(store.hidden);
            UUIDUtil.STREAM_CODEC.encode(buffer, store.creator);
            ByteBufCodecs.STRING_UTF8.encode(buffer, store.creatorName);
            ByteBufCodecs.VAR_LONG.encode(buffer, store.created);
        }, buffer -> new StoreInfo(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readBoolean(), UUIDUtil.STREAM_CODEC.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_LONG.decode(buffer)));
    }

    /**
     * A listing: the item (one of it), how many are left ({@code -1} for no end), the price of one and its place in the shop order.
     */
    public record ItemView(int recNo, long created, ItemStack stack, int quantity, String price, int index) {

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemView> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ItemView::recNo,
            ByteBufCodecs.VAR_LONG, ItemView::created,
            ItemStack.STREAM_CODEC, ItemView::stack,
            ByteBufCodecs.INT, ItemView::quantity,
            ByteBufCodecs.STRING_UTF8, ItemView::price,
            ByteBufCodecs.VAR_INT, ItemView::index,
            ItemView::new);
    }

    /**
     * The stores the player may see (hidden ones for store admins only).
     */
    public record ClientboundRegistry(List<StoreInfo> stores) implements CustomPacketPayload {

        public static final Type<ClientboundRegistry> TYPE = AlmuraNetwork.type("store_registry");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRegistry> CODEC = StreamCodec.composite(
            StoreInfo.CODEC.apply(ByteBufCodecs.list()), ClientboundRegistry::stores, ClientboundRegistry::new);

        @Override
        public Type<ClientboundRegistry> type() {
            return TYPE;
        }
    }

    /**
     * Opens the store management screen ({@code manage}), or a store (its id; {@code admin} for its admin pane).
     */
    public record ClientboundOpen(boolean manage, String id, boolean admin) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("store_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundOpen::manage,
            ByteBufCodecs.STRING_UTF8, ClientboundOpen::id,
            ByteBufCodecs.BOOL, ClientboundOpen::admin,
            ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * A side of a store's listings, in shop order.
     */
    public record ClientboundItems(String id, StoreSide side, List<ItemView> items) implements CustomPacketPayload {

        public static final Type<ClientboundItems> TYPE = AlmuraNetwork.type("store_items");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundItems> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundItems::id,
            enumCodec(StoreSide.values()), ClientboundItems::side,
            ItemView.CODEC.apply(ByteBufCodecs.list()), ClientboundItems::items,
            ClientboundItems::new);

        @Override
        public Type<ClientboundItems> type() {
            return TYPE;
        }
    }

    /**
     * The player asks to manage the stores (the Features menu's Manage Stores), or ({@code manage} false) to open a store by its id.
     */
    public record ServerboundOpen(boolean manage, String id) implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("store_open_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundOpen::manage,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundOpen::id,
            ServerboundOpen::new);

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * A change to a store, from the management screen.
     */
    public enum ModifyType {
        ADD, MODIFY, DELETE
    }

    public record ServerboundModifyStore(ModifyType change, String id, String name, String permission, boolean hidden)
        implements CustomPacketPayload {

        public static final Type<ServerboundModifyStore> TYPE = AlmuraNetwork.type("store_modify");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundModifyStore> CODEC = StreamCodec.composite(
            enumCodec(ModifyType.values()), ServerboundModifyStore::change,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModifyStore::id,
            ByteBufCodecs.stringUtf8(MAX_NAME), ServerboundModifyStore::name,
            ByteBufCodecs.stringUtf8(MAX_PERMISSION), ServerboundModifyStore::permission,
            ByteBufCodecs.BOOL, ServerboundModifyStore::hidden,
            ServerboundModifyStore::new);

        @Override
        public Type<ServerboundModifyStore> type() {
            return TYPE;
        }
    }

    /**
     * A store admin lists an item on a side: how many ({@code -1} for no end), at what place in the shop order and price.
     */
    public record ServerboundListItem(String id, StoreSide side, ItemStack stack, int quantity, int index, String price)
        implements CustomPacketPayload {

        public static final Type<ServerboundListItem> TYPE = AlmuraNetwork.type("store_list_item");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundListItem> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundListItem::id,
            enumCodec(StoreSide.values()), ServerboundListItem::side,
            ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC, ServerboundListItem::stack,
            ByteBufCodecs.INT, ServerboundListItem::quantity,
            ByteBufCodecs.VAR_INT, ServerboundListItem::index,
            ByteBufCodecs.stringUtf8(32), ServerboundListItem::price,
            ServerboundListItem::new);

        @Override
        public Type<ServerboundListItem> type() {
            return TYPE;
        }
    }

    /**
     * A listing's new quantity, place in the shop order and price.
     */
    public record ModifyCandidate(int recNo, int quantity, int index, String price) {

        public static final StreamCodec<ByteBuf, ModifyCandidate> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ModifyCandidate::recNo,
            ByteBufCodecs.INT, ModifyCandidate::quantity,
            ByteBufCodecs.VAR_INT, ModifyCandidate::index,
            ByteBufCodecs.stringUtf8(32), ModifyCandidate::price,
            ModifyCandidate::new);
    }

    /**
     * A store admin changes listings on a side (the list dialog, or moving them in the shop order).
     */
    public record ServerboundModifyItems(String id, StoreSide side, List<ModifyCandidate> items) implements CustomPacketPayload {

        public static final Type<ServerboundModifyItems> TYPE = AlmuraNetwork.type("store_modify_items");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundModifyItems> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModifyItems::id,
            enumCodec(StoreSide.values()), ServerboundModifyItems::side,
            ModifyCandidate.CODEC.apply(ByteBufCodecs.list(256)), ServerboundModifyItems::items,
            ServerboundModifyItems::new);

        @Override
        public Type<ServerboundModifyItems> type() {
            return TYPE;
        }
    }

    /**
     * A store admin unlists listings on a side.
     */
    public record ServerboundDelistItems(String id, StoreSide side, List<Integer> recNos) implements CustomPacketPayload {

        public static final Type<ServerboundDelistItems> TYPE = AlmuraNetwork.type("store_delist_items");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDelistItems> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundDelistItems::id,
            enumCodec(StoreSide.values()), ServerboundDelistItems::side,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(256)), ServerboundDelistItems::recNos,
            ServerboundDelistItems::new);

        @Override
        public Type<ServerboundDelistItems> type() {
            return TYPE;
        }
    }

    /**
     * The player buys ({@link StoreSide#BUY}) or sells ({@link StoreSide#SELL}) some of a listing.
     */
    public record ServerboundTransaction(String id, StoreSide side, int recNo, int quantity) implements CustomPacketPayload {

        public static final Type<ServerboundTransaction> TYPE = AlmuraNetwork.type("store_transaction");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTransaction> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundTransaction::id,
            enumCodec(StoreSide.values()), ServerboundTransaction::side,
            ByteBufCodecs.VAR_INT, ServerboundTransaction::recNo,
            ByteBufCodecs.VAR_INT, ServerboundTransaction::quantity,
            ServerboundTransaction::new);

        @Override
        public Type<ServerboundTransaction> type() {
            return TYPE;
        }
    }
}
