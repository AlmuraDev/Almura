/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network;

import com.almuradev.almura.feature.bank.BankHistoryEntry;
import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.shared.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * History entries for the bank window: an account's (account set) or, for admins, a player's (account -1, title is their name).
 */
public final class ClientboundBankHistoryPacket implements Message {

    public int account = -1;
    public String title = "";
    public List<BankHistoryEntry> entries = new ArrayList<>();

    public ClientboundBankHistoryPacket() {
    }

    public ClientboundBankHistoryPacket(final int account, final String title, final List<BankHistoryEntry> entries) {
        this.account = account;
        this.title = title;
        this.entries = entries;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.account = buf.readInteger();
        this.title = buf.readString();
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            this.entries.add(new BankHistoryEntry(Instant.ofEpochMilli(buf.readLong()), buf.readString(), buf.readString(),
                BankTransactionType.values()[buf.readByte()], ByteBufUtil.readBigDecimal((ByteBuf) buf)));
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeInteger(this.account);
        buf.writeString(this.title);
        buf.writeInteger(this.entries.size());
        for (final BankHistoryEntry entry : this.entries) {
            buf.writeLong(entry.getCreated().toEpochMilli());
            buf.writeString(entry.getAccountName());
            buf.writeString(entry.getActorName());
            buf.writeByte((byte) entry.getType().ordinal());
            ByteBufUtil.writeBigDecimal((ByteBuf) buf, entry.getAmount());
        }
    }
}
