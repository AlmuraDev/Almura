/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage;

import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.loader.stage.task.StageTask;
import com.almuradev.almura.content.loader.stage.task.CreateCreativeTabTask;

public class LoadCreativeTabsStage implements LoaderStage {

    public static final LoadCreativeTabsStage instance = new LoadCreativeTabsStage();

    @Override
    public AssetType[] getValidAssetTypes() {
        return new AssetType[]{
                AssetType.TAB
        };
    }

    @Override
    public StageTask<?, ?>[] getStageTasks() {
        return new StageTask<?, ?>[]{
                CreateCreativeTabTask.instance
        };
    }
}
