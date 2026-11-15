/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

/**
 * A change to whether one of the player's items is for sale, and at what price, as 3.5's. The admin ones work on anyone's listing.
 */
public enum ListStatusType {
    LIST,
    DE_LIST,
    ADJUST_PRICE,
    ADMIN_DE_LIST,
    ADMIN_ADJUST_PRICE
}
