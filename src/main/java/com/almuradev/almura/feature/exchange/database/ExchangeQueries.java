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
import static org.jooq.impl.DSL.sum;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.generated.axs.tables.AxsForSaleItem;
import com.almuradev.generated.axs.tables.AxsListItem;
import com.almuradev.generated.axs.tables.records.AxsForSaleItemRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemDataRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemRecord;
import com.almuradev.generated.axs.tables.records.AxsRecord;
import com.almuradev.generated.axs.tables.records.AxsTransactionRecord;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertResultStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep3;
import org.jooq.InsertValuesStep4;
import org.jooq.InsertValuesStep5;
import org.jooq.InsertValuesStep7;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;

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

    public static DatabaseQuery<SelectConditionStep<Record>> createFetchListItemsAndDataFor(final String id, final boolean isHidden) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(AXS_LIST_ITEM)
                .leftJoin(AXS_LIST_ITEM_DATA)
                .on(AXS_LIST_ITEM_DATA.LIST_ITEM.eq(AXS_LIST_ITEM.REC_NO))
            .where(AXS_LIST_ITEM.AXS.eq(id).and(AXS_LIST_ITEM.IS_HIDDEN.eq(isHidden)));
    }

    public static DatabaseQuery<InsertResultStep<AxsListItemRecord>> createInsertItem(final String id, final Instant created, final UUID seller,
        final Item item, final int quantity, final int metadata, final int index) {
        checkNotNull(id);
        checkNotNull(created);
        checkNotNull(seller);
        checkNotNull(item);
        checkState(quantity >= -1);
        checkState(metadata >= 0);
        checkState(index >= 0);

        final Timestamp sqlCreated = Timestamp.from(created);
        final String itemId = SerializationUtil.toString(item.getRegistryName());
        final byte[] sellerData = SerializationUtil.toBytes(seller);

        return context -> {
            final InsertValuesStep7<AxsListItemRecord, String, Timestamp, byte[], String, Integer, Integer, Integer> insertionStep = context
                .insertInto(AxsListItem.AXS_LIST_ITEM)
                .columns(AxsListItem.AXS_LIST_ITEM.AXS, AxsListItem.AXS_LIST_ITEM.CREATED, AxsListItem.AXS_LIST_ITEM.SELLER,
                    AxsListItem.AXS_LIST_ITEM.ITEM_TYPE, AxsListItem.AXS_LIST_ITEM.QUANTITY, AxsListItem.AXS_LIST_ITEM.METADATA,
                    AxsListItem.AXS_LIST_ITEM.INDEX);

            insertionStep.values(id, sqlCreated, sellerData, itemId, quantity, metadata, index);

            return insertionStep.returning();
        };
    }

    public static DatabaseQuery<UpdateConditionStep<AxsListItemRecord>> createUpdateListItem(final int listItemRecNo, final int quantity,
        final int index) {
        checkState(quantity > 0);
        checkState(index >= 0);

        return context -> context
            .update(AXS_LIST_ITEM)
            .set(AXS_LIST_ITEM.QUANTITY, quantity)
            .set(AXS_LIST_ITEM.INDEX, index)
            .where(AXS_LIST_ITEM.REC_NO.eq(listItemRecNo));
    }

    public static DatabaseQuery<UpdateConditionStep<AxsListItemRecord>> createUpdateListItemIsHidden(final int listItemRecNo,
        final boolean isHidden) {
        checkState(listItemRecNo >= 0);

        return context -> context
            .update(AXS_LIST_ITEM)
            .set(AXS_LIST_ITEM.IS_HIDDEN, isHidden)
            .where(AXS_LIST_ITEM.REC_NO.eq(listItemRecNo));
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

    public static DatabaseQuery<SelectConditionStep<Record>> createFetchForSaleItemsFor(final String id, final boolean isHidden) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(AXS_FOR_SALE_ITEM
                .join(AXS_LIST_ITEM)
                .onKey()
                .join(AXS)
                .on(AXS.ID.eq(id)))
            .where(AXS_FOR_SALE_ITEM.IS_HIDDEN.eq(isHidden));
    }

    public static DatabaseQuery<InsertResultStep<AxsForSaleItemRecord>> createInsertForSaleItem(
        final Instant created, final int listItemRecNo, final int quantityRemaining, final BigDecimal price) {
        checkNotNull(created);
        checkState(listItemRecNo >= 0);
        checkState(quantityRemaining > 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> {
            final InsertValuesStep4<AxsForSaleItemRecord, Timestamp, Integer, Integer, BigDecimal> insertionStep = context
                .insertInto(AxsForSaleItem.AXS_FOR_SALE_ITEM)
                .columns(AxsForSaleItem.AXS_FOR_SALE_ITEM.CREATED, AxsForSaleItem.AXS_FOR_SALE_ITEM.LIST_ITEM,
                    AxsForSaleItem.AXS_FOR_SALE_ITEM.QUANTITY_REMAINING, AxsForSaleItem.AXS_FOR_SALE_ITEM.PRICE);

            insertionStep.values(Timestamp.from(created), listItemRecNo, quantityRemaining, price);

            return insertionStep.returning();
        };
    }

    public static DatabaseQuery<UpdateConditionStep<AxsForSaleItemRecord>> createUpdateForSaleItemIsHidden(final int forSaleItemRecNo,
        final boolean isHidden) {
        checkState(forSaleItemRecNo >= 0);

        return context -> context
            .update(AXS_FOR_SALE_ITEM)
            .set(AXS_FOR_SALE_ITEM.IS_HIDDEN, isHidden)
            .where(AXS_FOR_SALE_ITEM.REC_NO.eq(forSaleItemRecNo));
    }

    public static DatabaseQuery<UpdateConditionStep<AxsForSaleItemRecord>> createUpdateForSaleItemQuantityRemaining(final int forSaleItemRecNo,
        final int quantityRemaining) {
        checkState(forSaleItemRecNo >= 0);
        checkState(quantityRemaining >= 0);

        return context -> context
            .update(AXS_FOR_SALE_ITEM)
            .set(AXS_FOR_SALE_ITEM.QUANTITY_REMAINING, quantityRemaining)
            .where(AXS_FOR_SALE_ITEM.REC_NO.eq(forSaleItemRecNo));
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
