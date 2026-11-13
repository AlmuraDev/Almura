/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry;

import org.spongepowered.api.CatalogType;

public interface CatalogedContent extends CatalogType {
    abstract class Impl implements CatalogedContent {
        private final String id;
        private final String name;

        protected Impl(final String id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
