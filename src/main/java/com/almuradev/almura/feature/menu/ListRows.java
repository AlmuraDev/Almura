/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;

/**
 * The rows of Almura's lists, as the Guide's (3.5's page boxes): each row a black box with a grey border, the selected one grey, one against
 * the next with no gap. The rows draw themselves, so the list's own row backgrounds and selection are off.
 */
public final class ListRows {

    public static final Paint ROW = Paint.solid(0xFF000000);
    public static final Paint ROW_SELECTED = Paint.solid(0xFF4A4A4A);
    public static final Border BORDER = Border.of(0xFFA0A0A0, 1f);

    private ListRows() {
    }

    /**
     * Sets a list up for these rows: no backgrounds or gaps of its own, and its rows built again when the selection changes, so the
     * selected one is drawn grey.
     */
    public static <T> ListView<T> style(final ListView<T> list) {
        list.setRowBackgrounds(false);
        list.setRowSpacing(0f);
        list.onEvent(Selection.SELECTED, event -> list.refresh());
        return list;
    }

    /**
     * A row's box: black, or grey when selected, with the grey border.
     */
    public static AnchorPanel box(final AnchorPanel row, final boolean selected) {
        return box(row, selected, BORDER);
    }

    /**
     * A row's box with a border of its own (the player's own listings, outlined in aqua).
     */
    public static AnchorPanel box(final AnchorPanel row, final boolean selected, final Border border) {
        row.setBackground(selected ? ROW_SELECTED : ROW).setBorder(border);
        return row;
    }
}
