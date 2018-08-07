/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public final class DatabaseQueue implements Runnable {

    private final Set<DatabaseAction> fetchQueue = new LinkedHashSet<>();
    private final Queue<DatabaseAction> updateQueue = new ArrayDeque<>();
    private final Queue<DatabaseAction> deleteQueue = new ArrayDeque<>();

    @Override
    public void run() {

        // TODO Right now we flush but there is an argument for distributes actions across ticks
        this.flush();
    }

    public void queue(final ActionType type, final String id, final Runnable runnable) {
        checkNotNull(type);
        checkNotNull(id);
        checkNotNull(runnable);

        switch (type) {
            case FETCH_IGNORE_DUPLICATES:
                this.fetchQueue.add(new FetchAndWaitAction(id, runnable));
                break;
            case UPDATE:
                this.updateQueue.add(new BasicDatabaseAction(id, runnable));
                break;
            case DELETE:
                this.deleteQueue.add(new BasicDatabaseAction(id, runnable));
                break;
        }
    }

    public void flush() {
        final Iterator<DatabaseAction> iter = this.fetchQueue.iterator();

        DatabaseAction action;

        while (iter.hasNext()) {
            action = iter.next();
            action.run();
            iter.remove();
        }

        while ((action = this.updateQueue.poll()) != null) {
            action.run();
        }

        while ((action = this.deleteQueue.poll()) != null) {
            action.run();
        }
    }

    private interface DatabaseAction extends Runnable {
    }

    private static class BasicDatabaseAction implements DatabaseAction {

        private final String id;
        private final Runnable action;

        private BasicDatabaseAction(final String id, final Runnable action) {
            checkNotNull(id);
            checkNotNull(action);

            this.id = id;
            this.action = action;
        }

        @Override
        public void run() {
            this.action.run();
        }
    }

    private static class FetchAndWaitAction implements DatabaseAction {
        private final String id;
        private final Runnable action;

        private FetchAndWaitAction(final String id, final Runnable action) {
            checkNotNull(id);
            checkNotNull(action);

            this.id = id;
            this.action = action;
        }

        @Override
        public void run() {
            this.action.run();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final FetchAndWaitAction that = (FetchAndWaitAction) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id);
        }
    }

    public enum ActionType {
        /**
         * Instructs the pipeline to queue the action in such a way that any additional calls will be disregarded
         */
        FETCH_IGNORE_DUPLICATES,
        UPDATE,
        DELETE
    }
}
