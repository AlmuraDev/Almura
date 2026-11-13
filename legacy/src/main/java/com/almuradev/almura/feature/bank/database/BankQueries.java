/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.database;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.shared.util.SerializationUtil;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * The bank tables (db/bank/database.sql), queried by name rather than through generated classes.
 */
public final class BankQueries {

    public static final Table<Record> ACCOUNT = table(name("bank_account"));
    public static final Field<Integer> ACCOUNT_REC_NO = field(name("rec_no"), Integer.class);
    public static final Field<Timestamp> ACCOUNT_CREATED = field(name("created"), Timestamp.class);
    public static final Field<byte[]> ACCOUNT_OWNER = field(name("owner"), byte[].class);
    public static final Field<String> ACCOUNT_NAME = field(name("name"), String.class);
    public static final Field<String> ACCOUNT_ECONOMY_ID = field(name("economy_id"), String.class);

    public static final Table<Record> MEMBER = table(name("bank_account_member"));
    public static final Field<Integer> MEMBER_ACCOUNT = field(name("account"), Integer.class);
    public static final Field<byte[]> MEMBER_MEMBER = field(name("member"), byte[].class);
    public static final Field<Boolean> MEMBER_CAN_WITHDRAW = field(name("can_withdraw"), Boolean.class);

    public static final Table<Record> TRANSACTION = table(name("bank_transaction"));
    public static final Field<Timestamp> TRANSACTION_CREATED = field(name("created"), Timestamp.class);
    public static final Field<Integer> TRANSACTION_ACCOUNT = field(name("account"), Integer.class);
    public static final Field<String> TRANSACTION_ACCOUNT_NAME = field(name("account_name"), String.class);
    public static final Field<byte[]> TRANSACTION_OWNER = field(name("owner"), byte[].class);
    public static final Field<byte[]> TRANSACTION_ACTOR = field(name("actor"), byte[].class);
    public static final Field<String> TRANSACTION_TYPE = field(name("type"), String.class);
    public static final Field<BigDecimal> TRANSACTION_AMOUNT = field(name("amount"), BigDecimal.class);
    private static final Field<Integer> TRANSACTION_REC_NO = field(name("rec_no"), Integer.class);

    private BankQueries() {
    }

    public static Result<Record> fetchAccounts(final DSLContext context) {
        return context.select().from(ACCOUNT).orderBy(ACCOUNT_REC_NO).fetch();
    }

    public static Result<Record> fetchMembers(final DSLContext context) {
        return context.select().from(MEMBER).fetch();
    }

    public static void insertAccount(final DSLContext context, final UUID owner, final String name, final String economyId) {
        context.insertInto(ACCOUNT, ACCOUNT_CREATED, ACCOUNT_OWNER, ACCOUNT_NAME, ACCOUNT_ECONOMY_ID)
            .values(Timestamp.from(Instant.now()), SerializationUtil.toBytes(owner), name, economyId)
            .execute();
    }

    public static void renameAccount(final DSLContext context, final int account, final String name) {
        context.update(ACCOUNT).set(ACCOUNT_NAME, name).where(ACCOUNT_REC_NO.eq(account)).execute();
    }

    public static void deleteAccount(final DSLContext context, final int account) {
        // Members go with it (on delete cascade); its history is kept
        context.deleteFrom(MEMBER).where(MEMBER_ACCOUNT.eq(account)).execute();
        context.deleteFrom(ACCOUNT).where(ACCOUNT_REC_NO.eq(account)).execute();
    }

    public static void insertMember(final DSLContext context, final int account, final UUID member, final boolean canWithdraw) {
        context.insertInto(MEMBER, MEMBER_ACCOUNT, MEMBER_MEMBER, MEMBER_CAN_WITHDRAW)
            .values(account, SerializationUtil.toBytes(member), canWithdraw)
            .execute();
    }

    public static void deleteMember(final DSLContext context, final int account, final UUID member) {
        context.deleteFrom(MEMBER).where(MEMBER_ACCOUNT.eq(account)).and(MEMBER_MEMBER.eq(SerializationUtil.toBytes(member))).execute();
    }

    public static void updateMember(final DSLContext context, final int account, final UUID member, final boolean canWithdraw) {
        context.update(MEMBER).set(MEMBER_CAN_WITHDRAW, canWithdraw)
            .where(MEMBER_ACCOUNT.eq(account)).and(MEMBER_MEMBER.eq(SerializationUtil.toBytes(member)))
            .execute();
    }

    public static void insertTransaction(final DSLContext context, final int account, final String accountName, final UUID owner,
        final UUID actor, final BankTransactionType type, final BigDecimal amount) {
        context.insertInto(TRANSACTION, TRANSACTION_CREATED, TRANSACTION_ACCOUNT, TRANSACTION_ACCOUNT_NAME, TRANSACTION_OWNER, TRANSACTION_ACTOR,
            TRANSACTION_TYPE, TRANSACTION_AMOUNT)
            .values(Timestamp.from(Instant.now()), account, accountName, SerializationUtil.toBytes(owner), SerializationUtil.toBytes(actor),
                type.name(), amount)
            .execute();
    }

    /**
     * @return An account's latest history entries, newest first
     */
    public static Result<Record> fetchAccountHistory(final DSLContext context, final int account, final int limit) {
        return context.select().from(TRANSACTION)
            .where(TRANSACTION_ACCOUNT.eq(account))
            .orderBy(TRANSACTION_REC_NO.desc())
            .limit(limit)
            .fetch();
    }

    /**
     * @return A player's latest history entries, newest first: what they did, and what was done on accounts they own
     */
    public static Result<Record> fetchPlayerHistory(final DSLContext context, final UUID player, final int limit) {
        final byte[] playerData = SerializationUtil.toBytes(player);
        return context.select().from(TRANSACTION)
            .where(TRANSACTION_ACTOR.eq(playerData)).or(TRANSACTION_OWNER.eq(playerData))
            .orderBy(TRANSACTION_REC_NO.desc())
            .limit(limit)
            .fetch();
    }
}
