/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage;

import com.almuradev.almura.content.loader.AssetType;
import com.almuradev.almura.content.loader.stage.task.CreateItemGroupTask;
import com.almuradev.almura.content.loader.stage.task.StageTask;

public class LoadItemGroupsStage implements LoaderStage {

    public static final LoadItemGroupsStage instance = new LoadItemGroupsStage();

    @Override
    public AssetType[] getValidAssetTypes() {
        return new AssetType[]{
                AssetType.ITEMGROUP
        };
    }

    @Override
    public StageTask<?, ?>[] getStageTasks() {
        return new StageTask<?, ?>[]{
                CreateItemGroupTask.instance
        };
    }
}
