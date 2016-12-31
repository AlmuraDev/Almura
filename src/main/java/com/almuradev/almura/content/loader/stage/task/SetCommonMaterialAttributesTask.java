/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.MaterialType;
import com.almuradev.almura.content.loader.AssetContext;

public class SetCommonMaterialAttributesTask implements StageTask<MaterialType, MaterialType.Builder> {

    public static final SetCommonMaterialAttributesTask instance = new SetCommonMaterialAttributesTask();

    @Override
    public void execute(AssetContext<MaterialType, MaterialType.Builder> context) throws TaskExecutionFailedException {

    }
}
