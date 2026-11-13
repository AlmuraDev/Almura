/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.network;

import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.shared.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

import java.math.BigDecimal;

/**
 * Asks the bank to do something. Which fields count depends on the action: the account, a name (account name, member or player to look up),
 * an amount, a member and a flag (whether a member may withdraw).
 */
public final class ServerboundBankActionPacket implements Message {

    public BankAction action;
    public int account = -1;
    public String text = "";
    public BigDecimal amount = BigDecimal.ZERO;
    public String member = "";
    public boolean flag;

    public ServerboundBankActionPacket() {
    }

    public ServerboundBankActionPacket(final BankAction action, final int account, final String text, final BigDecimal amount, final String member,
        final boolean flag) {
        this.action = action;
        this.account = account;
        this.text = text;
        this.amount = amount;
        this.member = member;
        this.flag = flag;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.action = BankAction.values()[buf.readByte()];
        this.account = buf.readInteger();
        this.text = buf.readString();
        this.amount = ByteBufUtil.readBigDecimal((ByteBuf) buf);
        this.member = buf.readString();
        this.flag = buf.readBoolean();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeByte((byte) this.action.ordinal());
        buf.writeInteger(this.account);
        buf.writeString(this.text);
        ByteBufUtil.writeBigDecimal((ByteBuf) buf, this.amount);
        buf.writeString(this.member);
        buf.writeBoolean(this.flag);
    }
}
