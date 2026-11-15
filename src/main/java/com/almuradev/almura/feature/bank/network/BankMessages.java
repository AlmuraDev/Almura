/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network;

import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

/**
 * The bank's messages, as 3.5's packets. Money goes as text ({@code BigDecimal}'s), so nothing is rounded on the way.
 */
public final class BankMessages {

    public static final int MAX_NAME = 32;

    private BankMessages() {
    }

    private static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(final E[] values) {
        return ByteBufCodecs.idMapper(index -> values[index], Enum::ordinal);
    }

    /**
     * The player asks the bank to do something: on an account (-1 for none, or the wallet), with a name, an amount, a member (their UUID)
     * and a flag (may withdraw) as the action needs.
     */
    public record ServerboundAction(BankAction action, int account, String text, String amount, String member, boolean flag)
        implements CustomPacketPayload {

        public static final Type<ServerboundAction> TYPE = AlmuraNetwork.type("bank_action");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAction> CODEC = StreamCodec.composite(
            enumCodec(BankAction.values()), ServerboundAction::action,
            ByteBufCodecs.VAR_INT, ServerboundAction::account,
            ByteBufCodecs.stringUtf8(64), ServerboundAction::text,
            ByteBufCodecs.stringUtf8(32), ServerboundAction::amount,
            ByteBufCodecs.stringUtf8(36), ServerboundAction::member,
            ByteBufCodecs.BOOL, ServerboundAction::flag,
            ServerboundAction::new);

        public static ServerboundAction of(final BankAction action, final int account) {
            return new ServerboundAction(action, account, "", "0", "", false);
        }

        @Override
        public Type<ServerboundAction> type() {
            return TYPE;
        }
    }

    /**
     * Someone on an account.
     */
    public record Member(UUID uniqueId, String name, boolean canWithdraw) {

        public static final StreamCodec<ByteBuf, Member> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, Member::uniqueId,
            ByteBufCodecs.STRING_UTF8, Member::name,
            ByteBufCodecs.BOOL, Member::canWithdraw,
            Member::new);
    }

    /**
     * An account as the player sees it: whether it's theirs (rename, delete, members) and whether they may withdraw (owners always).
     */
    public record AccountView(int recNo, String name, String ownerName, boolean mine, boolean canWithdraw, String balance,
        List<Member> members) {

        public static final StreamCodec<ByteBuf, AccountView> CODEC = StreamCodec.of((buffer, account) -> {
            ByteBufCodecs.VAR_INT.encode(buffer, account.recNo);
            ByteBufCodecs.STRING_UTF8.encode(buffer, account.name);
            ByteBufCodecs.STRING_UTF8.encode(buffer, account.ownerName);
            buffer.writeBoolean(account.mine);
            buffer.writeBoolean(account.canWithdraw);
            ByteBufCodecs.STRING_UTF8.encode(buffer, account.balance);
            Member.CODEC.apply(ByteBufCodecs.list()).encode(buffer, account.members);
        }, buffer -> new AccountView(ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.STRING_UTF8.decode(buffer),
            ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readBoolean(), buffer.readBoolean(), ByteBufCodecs.STRING_UTF8.decode(buffer),
            Member.CODEC.apply(ByteBufCodecs.list()).decode(buffer)));

        public boolean shared() {
            return !this.members.isEmpty();
        }
    }

    /**
     * The player's accounts and wallet: opens the bank window, or refreshes an open one.
     */
    public record ClientboundState(boolean open, boolean admin, int limit, String wallet, List<AccountView> accounts)
        implements CustomPacketPayload {

        public static final Type<ClientboundState> TYPE = AlmuraNetwork.type("bank_state");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundState> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundState::open,
            ByteBufCodecs.BOOL, ClientboundState::admin,
            ByteBufCodecs.VAR_INT, ClientboundState::limit,
            ByteBufCodecs.STRING_UTF8, ClientboundState::wallet,
            AccountView.CODEC.apply(ByteBufCodecs.list()), ClientboundState::accounts,
            ClientboundState::new);

        @Override
        public Type<ClientboundState> type() {
            return TYPE;
        }
    }

    /**
     * An entry in a history: when, on which account (its name then), by whom, what and how much.
     */
    public record HistoryEntry(long created, String accountName, String actorName, BankTransactionType kind, String amount) {

        public static final StreamCodec<ByteBuf, HistoryEntry> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, HistoryEntry::created,
            ByteBufCodecs.STRING_UTF8, HistoryEntry::accountName,
            ByteBufCodecs.STRING_UTF8, HistoryEntry::actorName,
            enumCodec(BankTransactionType.values()), HistoryEntry::kind,
            ByteBufCodecs.STRING_UTF8, HistoryEntry::amount,
            HistoryEntry::new);
    }

    /**
     * A history: an account's (its record number and name), or (account -1) a player's for an admin (their name), newest first.
     */
    public record ClientboundHistory(int account, String title, List<HistoryEntry> entries) implements CustomPacketPayload {

        public static final Type<ClientboundHistory> TYPE = AlmuraNetwork.type("bank_history");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHistory> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundHistory::account,
            ByteBufCodecs.STRING_UTF8, ClientboundHistory::title,
            HistoryEntry.CODEC.apply(ByteBufCodecs.list()), ClientboundHistory::entries,
            ClientboundHistory::new);

        @Override
        public Type<ClientboundHistory> type() {
            return TYPE;
        }
    }
}
