/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.BuildableCatalogType;

import javax.annotation.Nullable;

public final class AssetContext<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    private final Asset asset;
    private final B builder;
    @Nullable
    private C catalog;
    
    public AssetContext(Asset asset, B builder) {
        this.asset = asset;
        this.builder = builder;
    }

    public Asset getAsset() {
        return asset;
    }

    public B getBuilder() {
        return builder;
    }

    @Nullable
    public C getCatalog() {
        return this.catalog;
    }

    public void setCatalog(C catalog) {
        if (this.catalog != null) {
            // TODO
            throw new IllegalStateException();
        }

        this.catalog = catalog;
    }
}
