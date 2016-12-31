/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage;

import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.loader.stage.task.SetCommonBlockAttributesTask;
import com.almuradev.almura.content.loader.stage.task.StageTask;
import com.almuradev.almura.content.loader.stage.task.SetCommonMaterialAttributesTask;

public class MaterialsStage implements LoaderStage {

    public static final MaterialsStage instance = new MaterialsStage();

    @Override
    public AssetType[] getValidAssetTypes() {
        return new AssetType[]{
                AssetType.BLOCK,
                AssetType.HORIZONTAL,
                AssetType.ITEM
        };
    }

    @Override
    public StageTask<?, ?>[] getStageTasks() {
        return new StageTask<?, ?>[]{
                SetCommonMaterialAttributesTask.instance,
                SetCommonBlockAttributesTask.instance
        };
    }
}
