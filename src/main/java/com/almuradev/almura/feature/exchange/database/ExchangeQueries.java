/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.database;

import static com.almuradev.generated.axs.Tables.AXS;
import static com.almuradev.generated.axs.Tables.AXS_ITEM;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.database.DatabaseUtils;
import com.almuradev.generated.axs.tables.records.AxsItemRecord;
import com.almuradev.generated.axs.tables.records.AxsRecord;
import net.minecraft.item.Item;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep5;
import org.jooq.InsertValuesStep8;
import org.jooq.SelectLimitPercentStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

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

        final byte[] creatorData = DatabaseUtils.toBytes(creator);

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
     * Item
     */

    public static DatabaseQuery<SelectLimitPercentStep<AxsItemRecord>> createFetchItemsFor(final String id, final int limit) {
        checkNotNull(id);

        return context -> context
            .selectFrom(AXS_ITEM)
            .where(AXS_ITEM.AXS.eq(id))
            .limit(limit);
    }

    public DatabaseQuery<InsertValuesStep8<AxsItemRecord, Timestamp, String, byte[], String, Integer, Integer, BigDecimal, Integer>> createInsertItem(final String id,
        final Instant created, final UUID seller, final Item item, final int quantity, final int metadata, final BigDecimal price, final int index) {
        checkNotNull(id);
        checkNotNull(created);
        checkNotNull(seller);
        checkNotNull(item);
        checkState(quantity > 0);
        checkState(metadata >= 0);
        checkNotNull(price);
        checkState(index >= 0);

        final String itemId = DatabaseUtils.toString(item.getRegistryName());
        final byte[] sellerData = DatabaseUtils.toBytes(seller);

        return context -> context
            .insertInto(AXS_ITEM, AXS_ITEM.CREATED, AXS_ITEM.AXS, AXS_ITEM.SELLER, AXS_ITEM.ITEM_TYPE, AXS_ITEM.QUANTITY, AXS_ITEM.METADATA,
                AXS_ITEM.PRICE, AXS_ITEM.INDEX)
            .values(Timestamp.from(created), id, sellerData, itemId, quantity, metadata, price, index);
    }
}
