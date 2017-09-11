/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.Pack;
import com.almuradev.almura.registry.BuildableCatalogType;

import javax.annotation.Nullable;

public final class AssetContext<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    private final Pack pack;
    private final Asset asset;
    private final B builder;
    @Nullable private C catalog;
    
    AssetContext(Pack pack, Asset asset, B builder) {
        this.pack = pack;
        this.asset = asset;
        this.builder = builder;
    }

    public Pack getPack() {
        return this.pack;
    }

    public Asset getAsset() {
        return this.asset;
    }

    public B getBuilder() {
        return this.builder;
    }

    @Nullable
    public C getCatalog() {
        return this.catalog;
    }

    public void setCatalog(C catalog) {
        if (this.catalog != null) {
            throw new IllegalStateException("A catalog value has already been set for asset '" + this.asset.getPath() + '\'');
        }

        this.catalog = catalog;
    }
}
