/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.feature.FeatureSortType;
import com.almuradev.almura.feature.SortType;
import com.almuradev.almura.shared.feature.filter.Direction;
import net.minecraft.client.resources.I18n;

public class FeatureSortTypes {
    public static final SortType CREATED_ASC =
      new FeatureSortType("created", I18n.format("almura.feature.common.text.sort.created_asc"), Direction.ASCENDING);
    public static final SortType CREATED_DESC =
      new FeatureSortType("created", I18n.format("almura.feature.common.text.sort.created_desc"), Direction.DESCENDING);
    public static final SortType PRICE_ASC =
      new FeatureSortType("price", I18n.format("almura.feature.common.text.sort.price_asc"), Direction.ASCENDING);
    public static final SortType PRICE_DESC =
      new FeatureSortType("price", I18n.format("almura.feature.common.text.sort.price_desc"), Direction.DESCENDING);
    public static final SortType ITEM_DISPLAY_NAME_ASC =
      new FeatureSortType("item_display_name", I18n.format("almura.feature.common.text.sort.item_display_name_asc"), Direction.ASCENDING);
    public static final SortType ITEM_DISPLAY_NAME_DESC =
      new FeatureSortType("item_display_name", I18n.format("almura.feature.common.text.sort.item_display_name_desc"), Direction.DESCENDING);
}
