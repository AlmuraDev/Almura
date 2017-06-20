/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.material;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.content.item.group.ItemGroup;

import java.util.Optional;

public abstract class AbstractMaterialTypeBuilder<MATERIAL extends MaterialType, BUILDER extends AbstractMaterialTypeBuilder<MATERIAL, BUILDER>>
        implements MaterialType.Builder<MATERIAL, BUILDER> {

    private ItemGroup itemGroup;

    public AbstractMaterialTypeBuilder() {
        this.reset();
    }

    @Override
    public final BUILDER itemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
        return (BUILDER) this;
    }

    @Override
    public final Optional<ItemGroup> itemGroup() {
        return Optional.ofNullable(this.itemGroup);
    }

    @Override
    public BUILDER from(MaterialType value) {
        checkNotNull(value);
        this.itemGroup = value.getItemGroup().orElse(null);
        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        this.itemGroup = null;
        return (BUILDER) this;
    }
}
