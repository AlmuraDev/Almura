/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network;

import com.almuradev.almura.feature.bank.BankAccountView;
import com.almuradev.almura.shared.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A player's wallet and the bank accounts they own or share, opening the bank window or refreshing it if it is open.
 */
public final class ClientboundBankStatePacket implements Message {

    // Opens the window (otherwise only refreshes an open one)
    public boolean open;
    public boolean admin;
    // How many accounts the player may own
    public int limit;
    public BigDecimal wallet = BigDecimal.ZERO;
    public List<BankAccountView> accounts = new ArrayList<>();

    public ClientboundBankStatePacket() {
    }

    public ClientboundBankStatePacket(final boolean open, final boolean admin, final int limit, final BigDecimal wallet,
        final List<BankAccountView> accounts) {
        this.open = open;
        this.admin = admin;
        this.limit = limit;
        this.wallet = wallet;
        this.accounts = accounts;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.open = buf.readBoolean();
        this.admin = buf.readBoolean();
        this.limit = buf.readInteger();
        this.wallet = ByteBufUtil.readBigDecimal((ByteBuf) buf);
        final int count = buf.readInteger();
        for (int i = 0; i < count; i++) {
            final int recNo = buf.readInteger();
            final String name = buf.readString();
            final String ownerName = buf.readString();
            final boolean mine = buf.readBoolean();
            final boolean canWithdraw = buf.readBoolean();
            final BigDecimal balance = ByteBufUtil.readBigDecimal((ByteBuf) buf);
            final int memberCount = buf.readInteger();
            final List<BankAccountView.Member> members = new ArrayList<>();
            for (int j = 0; j < memberCount; j++) {
                members.add(new BankAccountView.Member(UUID.fromString(buf.readString()), buf.readString(), buf.readBoolean()));
            }
            this.accounts.add(new BankAccountView(recNo, name, ownerName, mine, canWithdraw, balance, members));
        }
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.open);
        buf.writeBoolean(this.admin);
        buf.writeInteger(this.limit);
        ByteBufUtil.writeBigDecimal((ByteBuf) buf, this.wallet);
        buf.writeInteger(this.accounts.size());
        for (final BankAccountView account : this.accounts) {
            buf.writeInteger(account.getRecNo());
            buf.writeString(account.getName());
            buf.writeString(account.getOwnerName());
            buf.writeBoolean(account.isMine());
            buf.writeBoolean(account.canWithdraw());
            ByteBufUtil.writeBigDecimal((ByteBuf) buf, account.getBalance());
            buf.writeInteger(account.getMembers().size());
            for (final BankAccountView.Member member : account.getMembers()) {
                buf.writeString(member.getUniqueId().toString());
                buf.writeString(member.getName());
                buf.writeBoolean(member.canWithdraw());
            }
        }
    }
}
