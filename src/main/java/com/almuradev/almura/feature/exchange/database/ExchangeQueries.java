/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.database;

import static com.almuradev.generated.axs.Tables.AXS;
import static com.almuradev.generated.axs.Tables.AXS_FOR_SALE_ITEM;
import static com.almuradev.generated.axs.Tables.AXS_LIST_ITEM;
import static com.almuradev.generated.axs.Tables.AXS_LIST_ITEM_DATA;
import static com.almuradev.generated.axs.Tables.AXS_TRANSACTION;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.generated.axs.tables.records.AxsForSaleItemRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemDataRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemRecord;
import com.almuradev.generated.axs.tables.records.AxsRecord;
import com.almuradev.generated.axs.tables.records.AxsTransactionRecord;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep3;
import org.jooq.InsertValuesStep4;
import org.jooq.InsertValuesStep5;
import org.jooq.InsertValuesStep7;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public final class ExchangeQueries {

    private ExchangeQueries() {
    }

    /**
     * Exchange
     */

    public static DatabaseQuery<SelectWhereStep<AxsRecord>> createFetchAllExchanges() {
        return context -> context.selectFrom(AXS);
    }

    public static DatabaseQuery<InsertValuesStep5<AxsRecord, byte[], String, String, String, Boolean>> createInsertExchange(final UUID creator,
        final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(creator);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        final byte[] creatorData = SerializationUtil.toBytes(creator);

        return context -> context
            .insertInto(AXS, AXS.CREATOR, AXS.ID, AXS.NAME, AXS.PERMISSION, AXS.IS_HIDDEN)
            .values(creatorData, id, name, permission, isHidden);
    }

    public static DatabaseQuery<UpdateConditionStep<AxsRecord>> createUpdateExchange(final String id, final String name, final String permission,
        final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        return context -> context
            .update(AXS)
            .set(AXS.NAME, name)
            .set(AXS.PERMISSION, permission)
            .set(AXS.IS_HIDDEN, isHidden)
            .where(AXS.ID.eq(id));
    }

    public static DatabaseQuery<DeleteConditionStep<AxsRecord>> createDeleteExchange(final String id) {
        checkNotNull(id);

        return context -> context.deleteFrom(AXS).where(AXS.ID.eq(id));
    }

    /**
     * ListItem
     */

    public static DatabaseQuery<SelectConditionStep<Record>> createFetchListItemsAndDataFor(final String id) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(AXS_LIST_ITEM)
                .join(AXS_LIST_ITEM_DATA)
                .on(AXS_LIST_ITEM_DATA.LIST_ITEM.eq(AXS_LIST_ITEM.REC_NO))
            .where(AXS_LIST_ITEM.AXS.eq(id));
    }

    public static DatabaseQuery<InsertValuesStep7<AxsListItemRecord, Timestamp, String, byte[], String, Integer, Integer, Integer>> createInsertItem(
        final String id, final Instant created, final UUID seller, final Item item, final int quantity, final int metadata, final int index) {
        checkNotNull(id);
        checkNotNull(created);
        checkNotNull(seller);
        checkNotNull(item);
        checkState(quantity > 0);
        checkState(metadata >= 0);
        checkState(index >= 0);

        final String itemId = SerializationUtil.toString(item.getRegistryName());
        final byte[] sellerData = SerializationUtil.toBytes(seller);

        return context -> context
            .insertInto(AXS_LIST_ITEM, AXS_LIST_ITEM.CREATED, AXS_LIST_ITEM.AXS, AXS_LIST_ITEM.SELLER, AXS_LIST_ITEM.ITEM_TYPE,
                AXS_LIST_ITEM.QUANTITY, AXS_LIST_ITEM.METADATA, AXS_LIST_ITEM.INDEX)
            .values(Timestamp.from(created), id, sellerData, itemId, quantity, metadata, index);
    }

    /**
     * ItemData
     */

    public static DatabaseQuery<InsertValuesStep2<AxsListItemDataRecord, Integer, byte[]>> createInsertItemData(final int listItemRecNo,
        final NBTTagCompound compound) throws IOException {
        checkState(listItemRecNo >= 0);
        checkNotNull(compound);

        final byte[] compoundData = SerializationUtil.toBytes(compound);
        return context -> context
            .insertInto(AXS_LIST_ITEM_DATA, AXS_LIST_ITEM_DATA.LIST_ITEM, AXS_LIST_ITEM_DATA.DATA)
            .values(listItemRecNo, compoundData);
    }

    /**
     * ForSaleItem
     */

    public static DatabaseQuery<SelectJoinStep<Record>> createFetchForSaleItemsFor(final String id) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(AXS_FOR_SALE_ITEM
                .join(AXS_LIST_ITEM
                    .join(AXS)
                    .on(AXS.ID.eq(id)))
                .onKey());
    }

    public static DatabaseQuery<SelectConditionStep<AxsForSaleItemRecord>> createFetchForSaleItemsFor(final int listItemRecNo) {
        checkState(listItemRecNo >= 0);

        return context -> context
            .selectFrom(AXS_FOR_SALE_ITEM)
            .where(AXS_FOR_SALE_ITEM.LIST_ITEM.eq(listItemRecNo));
    }

    public static DatabaseQuery<InsertValuesStep3<AxsForSaleItemRecord, Timestamp, Integer, BigDecimal>> createInsertForSaleItem(final Instant created,
        final int listItemRecNo, final BigDecimal price) {
        checkNotNull(created);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> context
            .insertInto(AXS_FOR_SALE_ITEM, AXS_FOR_SALE_ITEM.CREATED, AXS_FOR_SALE_ITEM.LIST_ITEM, AXS_FOR_SALE_ITEM.PRICE)
            .values(Timestamp.from(created), listItemRecNo, price);
    }

    /**
     * Transaction
     */

    public DatabaseQuery<InsertValuesStep4<AxsTransactionRecord, Timestamp, Integer, byte[], Integer>> createInsertTransaction(final Instant created,
        final int forSaleItemRecNo, final UUID buyer, final int quantity) {
        checkNotNull(created);
        checkState(forSaleItemRecNo >= 0);
        checkNotNull(buyer);
        checkState(quantity > 0);

        final byte[] buyerData = SerializationUtil.toBytes(buyer);

        return context -> context
            .insertInto(AXS_TRANSACTION, AXS_TRANSACTION.CREATED, AXS_TRANSACTION.FOR_SALE_ITEM, AXS_TRANSACTION.BUYER, AXS_TRANSACTION.QUANTITY)
            .values(Timestamp.from(created), forSaleItemRecNo, buyerData, quantity);
    }
}
