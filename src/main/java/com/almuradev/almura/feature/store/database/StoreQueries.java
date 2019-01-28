/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.database;

import static com.almuradev.generated.store.Tables.STORE;
import static com.almuradev.generated.store.Tables.STORE_BUYING_ITEM;
import static com.almuradev.generated.store.Tables.STORE_BUYING_ITEM_DATA;
import static com.almuradev.generated.store.Tables.STORE_BUYING_TRANSACTION;
import static com.almuradev.generated.store.Tables.STORE_SELLING_ITEM;
import static com.almuradev.generated.store.Tables.STORE_SELLING_ITEM_DATA;
import static com.almuradev.generated.store.Tables.STORE_SELLING_TRANSACTION;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.generated.store.tables.StoreSellingItem;
import com.almuradev.generated.store.tables.StoreSellingItemData;
import com.almuradev.generated.store.tables.records.StoreBuyingItemDataRecord;
import com.almuradev.generated.store.tables.records.StoreBuyingItemRecord;
import com.almuradev.generated.store.tables.records.StoreBuyingTransactionRecord;
import com.almuradev.generated.store.tables.records.StoreRecord;
import com.almuradev.generated.store.tables.records.StoreSellingItemDataRecord;
import com.almuradev.generated.store.tables.records.StoreSellingItemRecord;
import com.almuradev.generated.store.tables.records.StoreSellingTransactionRecord;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertResultStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep5;
import org.jooq.InsertValuesStep6;
import org.jooq.InsertValuesStep8;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public final class StoreQueries {
    private StoreQueries() {
    }

    /**
     * Store
     */

    public static DatabaseQuery<SelectWhereStep<StoreRecord>> createFetchAllStores() {
        return context -> context.selectFrom(STORE);
    }

    public static DatabaseQuery<InsertValuesStep6<StoreRecord, Timestamp, byte[], String, String, String, Boolean>> createInsertStore(
        final Instant created, final UUID creator, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(created);
        checkNotNull(creator);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        final byte[] creatorData = SerializationUtil.toBytes(creator);

        return context -> context
            .insertInto(STORE, STORE.CREATED, STORE.CREATOR, STORE.ID, STORE.NAME, STORE.PERMISSION, STORE.IS_HIDDEN)
            .values(Timestamp.from(created), creatorData, id, name, permission, isHidden);
    }

    public static DatabaseQuery<UpdateConditionStep<StoreRecord>> createUpdateStore(final String id, final String name, final String permission,
        final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        return context -> context
            .update(STORE)
            .set(STORE.NAME, name)
            .set(STORE.PERMISSION, permission)
            .set(STORE.IS_HIDDEN, isHidden)
            .where(STORE.ID.eq(id));
    }

    public static DatabaseQuery<DeleteConditionStep<StoreRecord>> createDeleteStore(final String id) {
        checkNotNull(id);

        return context -> context.deleteFrom(STORE).where(STORE.ID.eq(id));
    }

    /**
     * SellingItem
     */

    public static DatabaseQuery<SelectConditionStep<Record>> createFetchSellingItemsAndDataFor(final String id, final boolean isHidden) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(STORE_SELLING_ITEM)
            .leftJoin(STORE_SELLING_ITEM_DATA)
            .on(STORE_SELLING_ITEM_DATA.SELLING_ITEM.eq(STORE_SELLING_ITEM.REC_NO))
            .where(STORE_SELLING_ITEM.STORE.eq(id).and(STORE_SELLING_ITEM.IS_HIDDEN.eq(isHidden)));
    }

    public static DatabaseQuery<InsertResultStep<StoreSellingItemRecord>> createInsertSellingItem(final String id, final Instant created,
        final Item item, final int quantity, final int metadata, final int index, final BigDecimal price) {
        checkNotNull(id);
        checkNotNull(created);
        checkNotNull(item);
        checkNotNull(item.getRegistryName());
        checkState(quantity >= FeatureConstants.UNLIMITED);
        checkState(metadata >= 0);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> {
            final InsertValuesStep8<StoreSellingItemRecord, String, Timestamp, String, String, Integer, Integer, Integer, BigDecimal> insertionStep =
                context
                .insertInto(STORE_SELLING_ITEM)
                .columns(STORE_SELLING_ITEM.STORE, STORE_SELLING_ITEM.CREATED, STORE_SELLING_ITEM.DOMAIN, STORE_SELLING_ITEM.PATH, STORE_SELLING_ITEM
                    .QUANTITY, STORE_SELLING_ITEM.METADATA, STORE_SELLING_ITEM.INDEX, STORE_SELLING_ITEM.PRICE);

            insertionStep.values(id, Timestamp.from(created), item.getRegistryName().getResourceDomain(), item.getRegistryName()
                .getResourcePath(), quantity, metadata, index, price);

            return insertionStep.returning();
        };
    }

    public static DatabaseQuery<UpdateConditionStep<StoreSellingItemRecord>> createUpdateSellingItem(final int sellingItemRecNo, final int quantity,
        final int index, final BigDecimal price) {
        checkState(sellingItemRecNo >= 0);
        checkState(quantity >= FeatureConstants.UNLIMITED);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> context
            .update(STORE_SELLING_ITEM)
            .set(STORE_SELLING_ITEM.QUANTITY, quantity)
            .set(STORE_SELLING_ITEM.INDEX, index)
            .set(STORE_SELLING_ITEM.PRICE, price)
            .where(STORE_SELLING_ITEM.REC_NO.eq(sellingItemRecNo));
    }

    public static DatabaseQuery<UpdateConditionStep<StoreSellingItemRecord>> createUpdateSellingItemIsHidden(final int sellingItemRecNo,
        final boolean isHidden) {
        checkState(sellingItemRecNo >= 0);

        return context -> context
            .update(STORE_SELLING_ITEM)
            .set(STORE_SELLING_ITEM.IS_HIDDEN, isHidden)
            .where(STORE_SELLING_ITEM.REC_NO.eq(sellingItemRecNo));
    }

    public static DatabaseQuery<DeleteConditionStep<StoreSellingItemRecord>> createDeleteSellingItem(final int sellingItemRecNo) {
        checkState(sellingItemRecNo >= 0);

        return context -> context
            .deleteFrom(STORE_SELLING_ITEM)
            .where(STORE_SELLING_ITEM.REC_NO.eq(sellingItemRecNo));
    }

    /**
     * SellingItemData
     */

    public static DatabaseQuery<InsertResultStep<StoreSellingItemDataRecord>> createInsertSellingItemData(final int sellingItemRecNo,
        final NBTTagCompound compound) throws IOException {
        checkState(sellingItemRecNo >= 0);
        checkNotNull(compound);

        final byte[] compoundData = SerializationUtil.toBytes(compound);

        return context -> {
            final InsertValuesStep2<StoreSellingItemDataRecord, Integer, byte[]> insertionStep = context
                .insertInto(STORE_SELLING_ITEM_DATA)
                .columns(STORE_SELLING_ITEM_DATA.SELLING_ITEM, STORE_SELLING_ITEM_DATA.DATA);

            insertionStep.values(sellingItemRecNo, compoundData);

            return insertionStep.returning();
        };
    }

    /**
     * BuyingItem
     */
    public static DatabaseQuery<SelectConditionStep<Record>> createFetchBuyingItemsAndDataFor(final String id, final boolean isHidden) {
        checkNotNull(id);

        return context -> context
            .select()
            .from(STORE_BUYING_ITEM)
            .leftJoin(STORE_BUYING_ITEM_DATA)
            .on(STORE_BUYING_ITEM_DATA.BUYING_ITEM.eq(STORE_BUYING_ITEM.REC_NO))
            .where(STORE_BUYING_ITEM.STORE.eq(id).and(STORE_BUYING_ITEM.IS_HIDDEN.eq(isHidden)));
    }

    public static DatabaseQuery<InsertResultStep<StoreBuyingItemRecord>> createInsertBuyingItem(final String id, final Instant created,
        final Item item, final int quantity, final int metadata, final int index, final BigDecimal price) {
        checkNotNull(id);
        checkNotNull(created);
        checkNotNull(item);
        checkNotNull(item.getRegistryName());
        checkState(quantity >= FeatureConstants.UNLIMITED);
        checkState(metadata >= 0);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> {
            final InsertValuesStep8<StoreBuyingItemRecord, String, Timestamp, String, String, Integer, Integer, Integer, BigDecimal> insertionStep =
                context
                    .insertInto(STORE_BUYING_ITEM)
                    .columns(STORE_BUYING_ITEM.STORE, STORE_BUYING_ITEM.CREATED, STORE_BUYING_ITEM.DOMAIN, STORE_BUYING_ITEM.PATH, STORE_BUYING_ITEM
                        .QUANTITY, STORE_BUYING_ITEM.METADATA, STORE_BUYING_ITEM.INDEX, STORE_BUYING_ITEM.PRICE);

            insertionStep.values(id, Timestamp.from(created), item.getRegistryName().getResourceDomain(), item.getRegistryName()
                .getResourcePath(), quantity, metadata, index, price);

            return insertionStep.returning();
        };
    }

    public static DatabaseQuery<UpdateConditionStep<StoreBuyingItemRecord>> createUpdateBuyingItem(final int buyingItemRecNo, final int quantity,
        final int index, final BigDecimal price) {
        checkState(buyingItemRecNo >= 0);
        checkState(quantity >= FeatureConstants.UNLIMITED);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        return context -> context
            .update(STORE_BUYING_ITEM)
            .set(STORE_BUYING_ITEM.QUANTITY, quantity)
            .set(STORE_BUYING_ITEM.INDEX, index)
            .set(STORE_BUYING_ITEM.PRICE, price)
            .where(STORE_BUYING_ITEM.REC_NO.eq(buyingItemRecNo));
    }

    public static DatabaseQuery<UpdateConditionStep<StoreBuyingItemRecord>> createUpdateBuyingItemIsHidden(final int buyingItemRecNo, final boolean
        isHidden) {
        checkState(buyingItemRecNo >= 0);

        return context -> context
            .update(STORE_BUYING_ITEM)
            .set(STORE_BUYING_ITEM.IS_HIDDEN, isHidden)
            .where(STORE_BUYING_ITEM.REC_NO.eq(buyingItemRecNo));
    }

    public static DatabaseQuery<DeleteConditionStep<StoreBuyingItemRecord>> createDeleteBuyingItem(final int buyingItemRecNo) {
        checkState(buyingItemRecNo >= 0);

        return context -> context
            .deleteFrom(STORE_BUYING_ITEM)
            .where(STORE_BUYING_ITEM.REC_NO.eq(buyingItemRecNo));
    }

    /**
     * BuyingItemData
     */

    public static DatabaseQuery<InsertResultStep<StoreBuyingItemDataRecord>> createInsertBuyingItemData(final int buyingItemRecNo,
        final NBTTagCompound compound) throws IOException {
        checkState(buyingItemRecNo >= 0);
        checkNotNull(compound);

        final byte[] compoundData = SerializationUtil.toBytes(compound);

        return context -> {
            final InsertValuesStep2<StoreBuyingItemDataRecord, Integer, byte[]> insertionStep = context
                .insertInto(STORE_BUYING_ITEM_DATA)
                .columns(STORE_BUYING_ITEM_DATA.BUYING_ITEM, STORE_BUYING_ITEM_DATA.DATA);

            insertionStep.values(buyingItemRecNo, compoundData);

            return insertionStep.returning();
        };
    }

    /**
     * SellingTransaction
     */

    public static DatabaseQuery<InsertValuesStep5<StoreSellingTransactionRecord, Timestamp, Integer, byte[], BigDecimal, Integer>> createInsertSellingTransaction(
        final Instant created, final int sellingItemRecNo, final UUID buyer, final BigDecimal price, final int quantity) {
        checkNotNull(created);
        checkState(sellingItemRecNo >= 0);
        checkNotNull(buyer);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);
        checkState(price.doubleValue() <= FeatureConstants.ONE_TRILLION);
        checkState(quantity > 0);

        final byte[] buyerData = SerializationUtil.toBytes(buyer);

        return context -> context
            .insertInto(STORE_SELLING_TRANSACTION, STORE_SELLING_TRANSACTION.CREATED, STORE_SELLING_TRANSACTION.SELLING_ITEM, STORE_SELLING_TRANSACTION.BUYER,
                STORE_SELLING_TRANSACTION.PRICE, STORE_SELLING_TRANSACTION.QUANTITY)
            .values(Timestamp.from(created), sellingItemRecNo, buyerData, price, quantity);
    }

    /**
     * BuyingTransaction
     */

    public static DatabaseQuery<InsertValuesStep5<StoreBuyingTransactionRecord, Timestamp, Integer, byte[], BigDecimal, Integer>> createInsertBuyingTransaction(
        final Instant created, final int buyingItemRecNo, final UUID buyer, final BigDecimal price, final int quantity) {
        checkNotNull(created);
        checkState(buyingItemRecNo >= 0);
        checkNotNull(buyer);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);
        checkState(price.doubleValue() <= FeatureConstants.ONE_TRILLION);
        checkState(quantity > 0);

        final byte[] buyerData = SerializationUtil.toBytes(buyer);

        return context -> context
            .insertInto(STORE_BUYING_TRANSACTION, STORE_BUYING_TRANSACTION.CREATED, STORE_BUYING_TRANSACTION.BUYING_ITEM, STORE_BUYING_TRANSACTION.BUYER,
                STORE_BUYING_TRANSACTION.PRICE, STORE_BUYING_TRANSACTION.QUANTITY)
            .values(Timestamp.from(created), buyingItemRecNo, buyerData, price, quantity);
    }
}
