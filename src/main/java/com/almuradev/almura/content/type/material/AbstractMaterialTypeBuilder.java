/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.material;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.registry.CatalogDelegate;

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
