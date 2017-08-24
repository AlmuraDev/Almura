/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.item.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateMaterialAttributes;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.impl.GenericItem;
import com.almuradev.almura.content.material.AbstractMaterialTypeBuilder;
import net.minecraft.item.Item;

public abstract class AbstractItemTypeBuilder<ITEM extends BuildableItemType, BUILDER extends AbstractItemTypeBuilder<ITEM, BUILDER>>
        extends AbstractMaterialTypeBuilder<ITEM, BUILDER> implements BuildableItemType.Builder<ITEM, BUILDER> {

    @Override
    public ITEM build(String id) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final String registryName = id.split(":")[1];

        final Item item = this.createItem((BUILDER) this).setRegistryName(registryName);
        item.setUnlocalizedName(id.replace(":", ".").replace("/", "."));

        ((IMixinDelegateMaterialAttributes) item).setItemGroupDelegate(this.itemGroup());

        return (ITEM) (Object) item;
    }

    protected abstract Item createItem(BUILDER builder);

    public static final class BuilderImpl extends AbstractItemTypeBuilder<BuildableItemType, BuilderImpl> {

        @Override
        protected Item createItem(BuilderImpl builder) {
            return new GenericItem();
        }
    }
}
