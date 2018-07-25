/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.database;

import static com.almuradev.generated.title.Tables.TITLE;
import static com.almuradev.generated.title.Tables.TITLE_SELECT;
import static com.almuradev.generated.title.Tables.TITLE_SELECT_HISTORY;
import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.database.DatabaseQuery;
import com.almuradev.almura.shared.database.DatabaseUtils;
import com.almuradev.generated.title.tables.records.TitleRecord;
import com.almuradev.generated.title.tables.records.TitleSelectHistoryRecord;
import com.almuradev.generated.title.tables.records.TitleSelectRecord;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep3;
import org.jooq.InsertValuesStep5;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;

import java.util.UUID;

public final class TitleQueries {

    /**
     * Title
     */

    public static DatabaseQuery<SelectWhereStep<TitleRecord>> createFetchAllTitles() {
        return context -> context.selectFrom(TITLE);
    }

    public static DatabaseQuery<InsertValuesStep5<TitleRecord, byte[], String, String, String, String>> createInsertTitle(final UUID creator,
        final String id, final String name, final String permission, final String content) {
        checkNotNull(creator);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);
        checkNotNull(content);

        final byte[] creatorData = DatabaseUtils.toBytes(creator);

        return context -> context
            .insertInto(TITLE, TITLE.CREATOR, TITLE.ID, TITLE.NAME, TITLE.PERMISSION, TITLE.CONTENT)
            .values(creatorData, id, name, permission, content);
    }

    public static DatabaseQuery<UpdateConditionStep<TitleRecord>> createSetTitleHidden(final String id, final boolean isHidden) {
        checkNotNull(id);

        return context -> context
            .update(TITLE)
            .set(TITLE.IS_HIDDEN, isHidden)
            .where(TITLE.ID.eq(id));
    }

    public static DatabaseQuery<UpdateConditionStep<TitleRecord>> createUpdateTitle(final String id, final String name, final String permission,
        final String content) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);
        checkNotNull(content);

        return context -> context
            .update(TITLE)
            .set(TITLE.NAME, name)
            .set(TITLE.PERMISSION, permission)
            .set(TITLE.CONTENT, content)
            .where(TITLE.ID.eq(id));
    }

    /**
     * Title Select
     */

    public static DatabaseQuery<SelectConditionStep<TitleSelectRecord>> createFetchSelectedTitleFor(final UUID holder) {
        checkNotNull(holder);

        final byte[] holderData = DatabaseUtils.toBytes(holder);

        return context -> context
            .selectFrom(TITLE_SELECT)
            .where(TITLE_SELECT.HOLDER.eq(holderData));
    }

    public static DatabaseQuery<InsertValuesStep2<TitleSelectRecord, String, byte[]>> createInsertSelectedTitleFor(final UUID holder,
        final String id) {
        checkNotNull(holder);
        checkNotNull(id);

        final byte[] holderData = DatabaseUtils.toBytes(holder);

        return context -> context
            .insertInto(TITLE_SELECT, TITLE_SELECT.TITLE, TITLE_SELECT.HOLDER)
            .values(id, holderData);
    }

    public static DatabaseQuery<DeleteConditionStep<TitleSelectRecord>> createDeleteSelectedTitleFor(final UUID holder) {
        checkNotNull(holder);

        final byte[] holderData = DatabaseUtils.toBytes(holder);

        return context -> context.deleteFrom(TITLE_SELECT).where(TITLE_SELECT.HOLDER.eq(holderData));
    }

    /**
     * Title History
     */

    public static DatabaseQuery<InsertValuesStep2<TitleSelectHistoryRecord, String, byte[]>> createInsertSelectedTitleHistoryFor(final UUID holder,
        final String oldId) {
        checkNotNull(holder);
        checkNotNull(oldId);

        final byte[] holderData = DatabaseUtils.toBytes(holder);

        return context -> context
            .insertInto(TITLE_SELECT_HISTORY, TITLE_SELECT_HISTORY.OLD_TITLE, TITLE_SELECT_HISTORY.HOLDER)
            .values(oldId, holderData);
    }
}
