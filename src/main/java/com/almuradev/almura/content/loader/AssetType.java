/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.rotatable.HorizontalType;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.stage.task.CreateItemGroupTask;
import com.almuradev.almura.content.loader.stage.task.SetCommonBlockAttributesTask;
import com.almuradev.almura.content.loader.stage.task.SetCommonMaterialAttributesTask;
import com.almuradev.almura.content.loader.stage.task.StageTask;
import com.almuradev.almura.registry.BuildableCatalogType;

import java.util.Locale;

public enum AssetType {
    ITEMGROUP(ItemGroup.Builder.class, CreateItemGroupTask.instance),
    BLOCK(BuildableBlockType.Builder.class, SetCommonMaterialAttributesTask.instance, SetCommonBlockAttributesTask.instance),
    HORIZONTAL(HorizontalType.Builder.class, SetCommonMaterialAttributesTask.instance, SetCommonBlockAttributesTask.instance),
    ITEM(BuildableItemType.Builder.class, SetCommonMaterialAttributesTask.instance);

    private final String loggerName;
    private final Class<? extends BuildableCatalogType.Builder> builderClass;
    private final StageTask<?, ?>[] deserializationTasks;

    AssetType(Class<? extends BuildableCatalogType.Builder> builderClass, StageTask<?, ?>... deserializationTasks) {
        this.loggerName = this.name().toLowerCase(Locale.ENGLISH);
        this.builderClass = builderClass;
        this.deserializationTasks = deserializationTasks;
    }

    public String getLoggerName() {
        return this.loggerName;
    }

    public Class<? extends BuildableCatalogType.Builder> getBuilderClass() {
        return this.builderClass;
    }

    public StageTask<?, ?>[] getDeserializationTasks() {
        return this.deserializationTasks;
    }

    public boolean hasDeserializationTask(StageTask<?, ?> task) {
        for (StageTask<?, ?> st : this.deserializationTasks) {
            if (st == task) {
                return true;
            }
        }

        return false;
    }
}
