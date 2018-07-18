/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import javax.annotation.Nullable;

abstract class SearchEntry {
    @Nullable String description;

    SearchEntry(final String description) {
        this.description = description;
    }
}
