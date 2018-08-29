/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.database;

import static com.almuradev.generated.store.Tables.STORE;
import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.generated.store.tables.records.StoreRecord;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep6;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

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
}
