/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage;

import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.loader.stage.task.StageTask;

public interface LoaderStage {

    AssetType[] getValidAssetTypes();

    default boolean isAssetTypeValid(AssetType assetType) {
        for (AssetType at : this.getValidAssetTypes()) {
            if (at == assetType) {
                return true;
            }
        }

        return false;
    }

    StageTask<?, ?>[] getStageTasks();
}
