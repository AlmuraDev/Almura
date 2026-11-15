/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.network;

import com.almuradev.almura.feature.exchange.ExchangeModifyType;
import com.almuradev.almura.feature.exchange.ExchangeSort;
import com.almuradev.almura.feature.exchange.ListStatusType;
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
 * The exchanges' messages, as 3.5's packets (merged where 3.5 sent two in a row). Money goes as text ({@code BigDecimal}'s); an item goes
 * as one of it, with how many there are next to it.
 */
public final class ExchangeMessages {

    public static final int MAX_ID = 255;

    private ExchangeMessages() {
    }

    private static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(final E[] values) {
        return ByteBufCodecs.idMapper(index -> values[index], Enum::ordinal);
    }

    // ---- To the client

    /**
     * An exchange the player may see: hidden ones only for administrators.
     */
    public record ExchangeInfo(String id, String name, String permission, boolean hidden, UUID creator, String creatorName, long created) {

        public static final StreamCodec<ByteBuf, ExchangeInfo> CODEC = StreamCodec.of((buffer, info) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, info.id);
            ByteBufCodecs.STRING_UTF8.encode(buffer, info.name);
            ByteBufCodecs.STRING_UTF8.encode(buffer, info.permission);
            buffer.writeBoolean(info.hidden);
            UUIDUtil.STREAM_CODEC.encode(buffer, info.creator);
            ByteBufCodecs.STRING_UTF8.encode(buffer, info.creatorName);
            ByteBufCodecs.VAR_LONG.encode(buffer, info.created);
        }, buffer -> new ExchangeInfo(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readBoolean(), UUIDUtil.STREAM_CODEC.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_LONG.decode(buffer)));
    }

    /**
     * The exchanges the player may see (3.5's registry packet), sent on joining and whenever they change.
     */
    public record ClientboundRegistry(List<ExchangeInfo> exchanges) implements CustomPacketPayload {

        public static final Type<ClientboundRegistry> TYPE = AlmuraNetwork.type("exchange_registry");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRegistry> CODEC = StreamCodec.composite(
            ExchangeInfo.CODEC.apply(ByteBufCodecs.list()), ClientboundRegistry::exchanges, ClientboundRegistry::new);

        @Override
        public Type<ClientboundRegistry> type() {
            return TYPE;
        }
    }

    /**
     * Which screen to open, as 3.5's: the management screen, an exchange (with how many listings the player may have and whether they are
     * its administrator), or the exchange's offer screen.
     */
    public enum Screen {
        MANAGE, SPECIFIC, OFFER
    }

    public record ClientboundOpen(Screen screen, String id, int limit, boolean admin) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("exchange_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            enumCodec(Screen.values()), ClientboundOpen::screen,
            ByteBufCodecs.STRING_UTF8, ClientboundOpen::id,
            ByteBufCodecs.VAR_INT, ClientboundOpen::limit,
            ByteBufCodecs.BOOL, ClientboundOpen::admin,
            ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * One of the player's items on an exchange: its record, the item and how many, its last price, and (record -1 when not) its listing's
     * record and price.
     */
    public record ListItemView(int recNo, ItemStack item, int quantity, String lastKnownPrice, int forSaleRecNo, String price) {

        public static final StreamCodec<RegistryFriendlyByteBuf, ListItemView> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ListItemView::recNo,
            ItemStack.STREAM_CODEC, ListItemView::item,
            ByteBufCodecs.VAR_INT, ListItemView::quantity,
            ByteBufCodecs.STRING_UTF8, ListItemView::lastKnownPrice,
            ByteBufCodecs.VAR_INT, ListItemView::forSaleRecNo,
            ByteBufCodecs.STRING_UTF8, ListItemView::price,
            ListItemView::new);

        public boolean listed() {
            return this.forSaleRecNo >= 0;
        }
    }

    /**
     * The player's items on an exchange and which are for sale (3.5's list items and sale status packets).
     */
    public record ClientboundListItems(String id, List<ListItemView> items) implements CustomPacketPayload {

        public static final Type<ClientboundListItems> TYPE = AlmuraNetwork.type("exchange_list_items");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundListItems> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundListItems::id,
            ListItemView.CODEC.apply(ByteBufCodecs.list()), ClientboundListItems::items,
            ClientboundListItems::new);

        @Override
        public Type<ClientboundListItems> type() {
            return TYPE;
        }
    }

    /**
     * An item for sale: its list item's record (what a purchase names), the item and how many, the price each, who sells it and when it was
     * listed.
     */
    public record ForSaleView(int listItemRecNo, ItemStack item, int quantity, String price, UUID seller, String sellerName, long created) {

        public static final StreamCodec<RegistryFriendlyByteBuf, ForSaleView> CODEC = StreamCodec.of((buffer, view) -> {
            ByteBufCodecs.VAR_INT.encode(buffer, view.listItemRecNo);
            ItemStack.STREAM_CODEC.encode(buffer, view.item);
            ByteBufCodecs.VAR_INT.encode(buffer, view.quantity);
            ByteBufCodecs.STRING_UTF8.encode(buffer, view.price);
            UUIDUtil.STREAM_CODEC.encode(buffer, view.seller);
            ByteBufCodecs.STRING_UTF8.encode(buffer, view.sellerName);
            ByteBufCodecs.VAR_LONG.encode(buffer, view.created);
        }, buffer -> new ForSaleView(ByteBufCodecs.VAR_INT.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer),
            ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer), UUIDUtil.STREAM_CODEC.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_LONG.decode(buffer)));
    }

    /**
     * A page of the items for sale that match the player's search, and how many matched in all.
     */
    public record ClientboundForSale(String id, List<ForSaleView> items, int total) implements CustomPacketPayload {

        public static final Type<ClientboundForSale> TYPE = AlmuraNetwork.type("exchange_for_sale");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundForSale> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundForSale::id,
            ForSaleView.CODEC.apply(ByteBufCodecs.list()), ClientboundForSale::items,
            ByteBufCodecs.VAR_INT, ClientboundForSale::total,
            ClientboundForSale::new);

        @Override
        public Type<ClientboundForSale> type() {
            return TYPE;
        }
    }

    /**
     * What is for sale on an exchange changed: a player looking at it searches again (3.5's filter request packet).
     */
    public record ClientboundRefresh(String id) implements CustomPacketPayload {

        public static final Type<ClientboundRefresh> TYPE = AlmuraNetwork.type("exchange_refresh");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRefresh> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundRefresh::id, ClientboundRefresh::new);

        @Override
        public Type<ClientboundRefresh> type() {
            return TYPE;
        }
    }

    /**
     * The player's purchase went through.
     */
    public record ClientboundTransactionComplete() implements CustomPacketPayload {

        public static final Type<ClientboundTransactionComplete> TYPE = AlmuraNetwork.type("exchange_transaction_complete");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTransactionComplete> CODEC =
            StreamCodec.unit(new ClientboundTransactionComplete());

        @Override
        public Type<ClientboundTransactionComplete> type() {
            return TYPE;
        }
    }

    // ---- To the server

    /**
     * The player asks to open an exchange, or (an empty id) the management screen; 3.5's client typed {@code /almura exchange open|manage}.
     */
    public record ServerboundOpen(String id) implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("exchange_open_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundOpen::id, ServerboundOpen::new);

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player asks for an exchange's offer screen (the +/- button).
     */
    public record ServerboundOfferRequest(String id) implements CustomPacketPayload {

        public static final Type<ServerboundOfferRequest> TYPE = AlmuraNetwork.type("exchange_offer_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOfferRequest> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundOfferRequest::id, ServerboundOfferRequest::new);

        @Override
        public Type<ServerboundOfferRequest> type() {
            return TYPE;
        }
    }

    /**
     * An administrator adds, changes or deletes an exchange.
     */
    public record ServerboundModify(ExchangeModifyType change, String id, String name, String permission, boolean hidden)
        implements CustomPacketPayload {

        public static final Type<ServerboundModify> TYPE = AlmuraNetwork.type("exchange_modify");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundModify> CODEC = StreamCodec.composite(
            enumCodec(ExchangeModifyType.values()), ServerboundModify::change,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModify::id,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModify::name,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundModify::permission,
            ByteBufCodecs.BOOL, ServerboundModify::hidden,
            ServerboundModify::new);

        @Override
        public Type<ServerboundModify> type() {
            return TYPE;
        }
    }

    /**
     * The player's search: item name and seller name (parts, any case), whether to leave out their own listings, the order and the page.
     */
    public record ServerboundQuery(String id, String itemName, String sellerName, boolean hideOwn, ExchangeSort sort, int skip, int limit)
        implements CustomPacketPayload {

        public static final Type<ServerboundQuery> TYPE = AlmuraNetwork.type("exchange_query");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundQuery> CODEC = StreamCodec.of((buffer, query) -> {
            ByteBufCodecs.stringUtf8(MAX_ID).encode(buffer, query.id);
            ByteBufCodecs.stringUtf8(128).encode(buffer, query.itemName);
            ByteBufCodecs.stringUtf8(64).encode(buffer, query.sellerName);
            buffer.writeBoolean(query.hideOwn);
            ExchangeMessages.<ExchangeSort>enumCodec(ExchangeSort.values()).encode(buffer, query.sort);
            ByteBufCodecs.VAR_INT.encode(buffer, query.skip);
            ByteBufCodecs.VAR_INT.encode(buffer, query.limit);
        }, buffer -> new ServerboundQuery(ByteBufCodecs.stringUtf8(MAX_ID).decode(buffer), ByteBufCodecs.stringUtf8(128).decode(buffer),
            ByteBufCodecs.stringUtf8(64).decode(buffer), buffer.readBoolean(), ExchangeMessages.<ExchangeSort>enumCodec(ExchangeSort.values())
            .decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer)));

        @Override
        public Type<ServerboundQuery> type() {
            return TYPE;
        }
    }

    /**
     * Where an offer moves items: from the inventory onto the exchange, or back.
     */
    public enum Direction {
        TO_LISTING, TO_INVENTORY
    }

    /**
     * Items the offer screen moves: one of the item and how many.
     */
    public record Move(Direction direction, ItemStack item, int quantity) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Move> CODEC = StreamCodec.composite(
            enumCodec(Direction.values()), Move::direction,
            ItemStack.STREAM_CODEC, Move::item,
            ByteBufCodecs.VAR_INT, Move::quantity,
            Move::new);
    }

    /**
     * The offer screen's moves, sent with OK (3.5's list items request packet).
     */
    public record ServerboundMoveItems(String id, List<Move> moves) implements CustomPacketPayload {

        public static final Type<ServerboundMoveItems> TYPE = AlmuraNetwork.type("exchange_move_items");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMoveItems> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundMoveItems::id,
            Move.CODEC.apply(ByteBufCodecs.list(256)), ServerboundMoveItems::moves,
            ServerboundMoveItems::new);

        @Override
        public Type<ServerboundMoveItems> type() {
            return TYPE;
        }
    }

    /**
     * The player lists, unlists or reprices an item (an administrator anyone's), by its list item's record.
     */
    public record ServerboundListStatus(ListStatusType change, String id, int listItemRecNo, String price) implements CustomPacketPayload {

        public static final Type<ServerboundListStatus> TYPE = AlmuraNetwork.type("exchange_list_status");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundListStatus> CODEC = StreamCodec.composite(
            enumCodec(ListStatusType.values()), ServerboundListStatus::change,
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundListStatus::id,
            ByteBufCodecs.VAR_INT, ServerboundListStatus::listItemRecNo,
            ByteBufCodecs.stringUtf8(32), ServerboundListStatus::price,
            ServerboundListStatus::new);

        @Override
        public Type<ServerboundListStatus> type() {
            return TYPE;
        }
    }

    /**
     * The player buys some of an item for sale.
     */
    public record ServerboundBuy(String id, int listItemRecNo, int quantity) implements CustomPacketPayload {

        public static final Type<ServerboundBuy> TYPE = AlmuraNetwork.type("exchange_buy");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBuy> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_ID), ServerboundBuy::id,
            ByteBufCodecs.VAR_INT, ServerboundBuy::listItemRecNo,
            ByteBufCodecs.VAR_INT, ServerboundBuy::quantity,
            ServerboundBuy::new);

        @Override
        public Type<ServerboundBuy> type() {
            return TYPE;
        }
    }
}
