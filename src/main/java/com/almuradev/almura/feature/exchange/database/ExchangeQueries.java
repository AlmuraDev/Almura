/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.database;

import static com.almuradev.generated.axs.Tables.AXS;
import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.database.DatabaseUtils;
import com.almuradev.generated.axs.tables.records.AxsRecord;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep4;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

import java.util.UUID;

public final class ExchangeQueries {

    /**
     * Exchange
     */

    public static DatabaseQuery<SelectWhereStep<AxsRecord>> createFetchAllExchanges() {
        return context -> context.selectFrom(AXS);
    }

    public static DatabaseQuery<InsertValuesStep4<AxsRecord, byte[], String, String, Boolean>> createInsertExchange(final UUID creator,
        final String id, final String permission, final boolean isHidden) {
        checkNotNull(creator);
        checkNotNull(id);
        checkNotNull(permission);

        final byte[] creatorData = DatabaseUtils.toBytes(creator);

        return context -> context
            .insertInto(AXS, AXS.CREATOR, AXS.ID, AXS.PERMISSION, AXS.IS_HIDDEN)
            .values(creatorData, id, permission, isHidden);
    }

    public static DatabaseQuery<UpdateConditionStep<AxsRecord>> createUpdateExchange(final String id, final String permission,
        final String content, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(permission);
        checkNotNull(content);

        return context -> context
            .update(AXS)
            .set(AXS.PERMISSION, permission)
            .set(AXS.IS_HIDDEN, isHidden)
            .where(AXS.ID.eq(id));
    }

    public static DatabaseQuery<DeleteConditionStep<AxsRecord>> createDeleteExchange(final String id) {
        checkNotNull(id);

        return context -> context.deleteFrom(AXS).where(AXS.ID.eq(id));
    }

    private ExchangeQueries() {}
}
