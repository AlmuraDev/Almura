/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature.filter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.shared.feature.FeatureConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
public final class FilterRegistry {

    // TODO Fix this better when I care
    public static final FilterRegistry instance = new FilterRegistry();

    private final Map<String, Filter<?>> filters = new HashMap<>();
    private final Map<String, Comparator<?>> comparators = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Filter<?>> Optional<T> getFilter(final String id) {
        checkNotNull(id);

        return Optional.ofNullable((T) this.filters.get(id));
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparator<?>> Optional<T> getComparator(final String id) {
        checkNotNull(id);

        return Optional.ofNullable((T) this.comparators.get(id));
    }

    public <T> FilterRegistry registerFilter(final String id, final Filter<T> filter) {
        checkNotNull(id);
        checkNotNull(filter);

        this.filters.put(id, filter);

        return this;
    }

    public <T> FilterRegistry registerComparator(final String id, final Comparator<T> comparator) {
        checkNotNull(id);
        checkNotNull(comparator);

        this.comparators.put(id, comparator);

        return this;
    }

    // display_name=Hi;seller_name=Bye
    @SuppressWarnings("unchecked")
    public <T> List<FilterElement<T>> getFilterElements(final String filter) {
        checkNotNull(filter);

        final List<FilterElement<T>> elements = new ArrayList<>();

        final String[] elementsSplit = filter.split(FeatureConstants.DELIMITER);

        for (final String rawElement : elementsSplit) {
            final int index = rawElement.indexOf(FeatureConstants.EQUALITY);
            if (index == -1) {
                continue;
            }

            final String id = rawElement.substring(0, index);
            final Filter<T> found = (Filter<T>) this.getFilter(id).orElse(null);
            if (found == null) {
                continue;
            }

            final String value = rawElement.substring(index + 1);
            elements.add(new FilterElement(found, value));
        }

        return elements;
    }

    // display_name=asc;seller_name=desc
    public <T> List<SorterElement<T>> getSortingElements(final String sorter) {
        checkNotNull(sorter);

        final List<SorterElement<T>> elements = new ArrayList<>();

        final String[] elementsSplit = sorter.split(FeatureConstants.DELIMITER);

        for (final String rawElement : elementsSplit) {
            final int index = rawElement.indexOf(FeatureConstants.EQUALITY);
            if (index == -1) {
                continue;
            }

            final String id = rawElement.substring(0, index);
            final Comparator<T> found = (Comparator<T>) this.getComparator(id).orElse(null);
            if (found == null) {
                continue;
            }

            final String value = rawElement.substring(index + 1);
            final Direction direction = Direction.getDirection(value).orElse(null);
            if (direction == null) {
                continue;
            }

            elements.add(new SorterElement<>(found, direction));
        }

        return elements;
    }

    public <T> Optional<Comparator<T>> buildSortingComparator(final List<SorterElement<T>> elements) {
        checkNotNull(elements);
        checkState(!elements.isEmpty());

        Comparator<T> comparator = null;
        for (final SorterElement<T> element : elements) {
            if (comparator == null) {
                if (element.getDirection() == Direction.ASCENDING) {
                    comparator = element.getComparator();
                } else {
                    comparator = element.getComparator().reversed();
                }
            } else if (element.getDirection() == Direction.ASCENDING) {
                comparator = comparator.thenComparing(element.getComparator());
            } else if (element.getDirection() == Direction.DESCENDING) {
                comparator = comparator.thenComparing(element.getComparator().reversed());
            }
        }

        return Optional.ofNullable(comparator);
    }

    public static class FilterElement<T> {
        private final Filter<T> filter;
        private final String value;

        FilterElement(final Filter<T> filter, final String value) {
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

    public static class SorterElement<T> {
        private final Comparator<T> comparator;
        private final Direction direction;

        public SorterElement(final Comparator<T> comparator, final Direction direction) {
            this.comparator = comparator;
            this.direction = direction;
        }

        public Comparator<T> getComparator() {
            return comparator;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}
