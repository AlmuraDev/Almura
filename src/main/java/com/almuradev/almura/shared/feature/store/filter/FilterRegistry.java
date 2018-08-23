/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.store.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
public final class FilterRegistry {

    // TODO Fix this better when I care
    public static final FilterRegistry instance = new FilterRegistry();

    private static final String DELIMETER = ";";
    private static final String EQUALITY = "=";

    private final Map<String, Filter<?>> filters = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Filter<?>> Optional<T> getFilter(final String id) {
        checkNotNull(id);
        return Optional.ofNullable((T) this.filters.get(id));
    }

    public FilterRegistry register(final String id, final Filter<?> filter) {
        checkNotNull(id);
        checkNotNull(filter);

        this.filters.put(id, filter);

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> List<FilterElement<T>> getElements(final String query) {
        checkNotNull(query);

        final List<FilterElement<T>> elements = new ArrayList<>();

        final String[] elementsSplit = query.split(DELIMETER);

        for (final String rawElement : elementsSplit) {
            final int index = rawElement.indexOf(EQUALITY);
            if (index == -1) {
                continue;
            }

            final String filterId = rawElement.substring(0, index);
            final Filter<T> filter = (Filter<T>) this.getFilter(filterId).orElse(null);
            if (filter == null) {
                continue;
            }

            final String value = rawElement.substring(index);
            elements.add(new FilterElement(filter, value));
        }

        return elements;
    }

    public static class FilterElement<T> {
        private final Filter<T> filter;
        private final String value;

        public FilterElement(final Filter<T> filter, final String value) {
            this.filter = filter;
            this.value = value;
        }

        public Filter<T> getFilter() {
            return filter;
        }

        public String getValue() {
            return value;
        }
    }
}
