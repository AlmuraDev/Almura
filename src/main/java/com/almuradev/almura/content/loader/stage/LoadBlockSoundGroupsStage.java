/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage;

import com.almuradev.almura.content.loader.AssetType;
import com.almuradev.almura.content.loader.stage.task.CreateBlockSoundGroupTask;
import com.almuradev.almura.content.loader.stage.task.StageTask;

public enum LoadBlockSoundGroupsStage implements LoaderStage {
    INSTANCE;

    @Override
    public AssetType[] getValidAssetTypes() {
        return new AssetType[] {
                AssetType.SOUNDGROUP
        };
    }

    @Override
    public StageTask<?, ?>[] getStageTasks() {
        return new StageTask<?, ?>[] {
                CreateBlockSoundGroupTask.INSTANCE
        };
    }
}
