package com.almuradev.almura.content.item.builder;

import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.impl.GenericItem;
import com.almuradev.almura.content.material.AbstractMaterialTypeBuilder;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractItemTypeBuilder<ITEM extends BuildableItemType, BUILDER extends AbstractItemTypeBuilder<ITEM, BUILDER>>
        extends AbstractMaterialTypeBuilder<ITEM, BUILDER> implements BuildableItemType.Builder<ITEM, BUILDER> {

    @Override
    public ITEM build(String id) {
        checkNotNull(id);
        checkState(!id.isEmpty(), "Id cannot be empty!");

        final Item item = this.createItem((BUILDER) this).setRegistryName(id);
        item.setUnlocalizedName(id.replace(":", "."));
        item.setCreativeTab((CreativeTabs) this.itemGroup().orElse(null));

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
