/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.material;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.shared.registry.catalog.CatalogDelegate;

public abstract class AbstractMaterialTypeBuilder<MATERIAL extends MaterialType, BUILDER extends AbstractMaterialTypeBuilder<MATERIAL, BUILDER>>
        implements MaterialType.Builder<MATERIAL, BUILDER> {

    private CatalogDelegate<ItemGroup> itemGroupDelegate;

    public AbstractMaterialTypeBuilder() {
        this.reset();
    }

    @Override
    public final BUILDER itemGroup(ItemGroup itemGroup) {
        this.itemGroupDelegate = CatalogDelegate.of(itemGroup);
        return (BUILDER) this;
    }

    @Override
    public final CatalogDelegate<ItemGroup> itemGroup() {
        return this.itemGroupDelegate;
    }

    @Override
    public final BUILDER itemGroup(CatalogDelegate<ItemGroup> itemGroupDelegate) {
        this.itemGroupDelegate = itemGroupDelegate;
        return (BUILDER) this;
    }

    @Override
    public BUILDER from(MATERIAL value) {
        checkNotNull(value);
        value.getItemGroup().ifPresent(itemGroup -> this.itemGroupDelegate = CatalogDelegate.of(itemGroup));
        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        this.itemGroupDelegate = null;
        return (BUILDER) this;
    }
}
