/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.Constants;
import com.almuradev.almura.block.BuildableBlockType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.AssetContext;

import java.util.Locale;

public class SetCommonBlockAttributesTask implements StageTask<BuildableBlockType, BuildableBlockType.Builder> {

    public static final SetCommonBlockAttributesTask instance = new SetCommonBlockAttributesTask();

    @Override
    public void execute(AssetContext<BuildableBlockType, BuildableBlockType.Builder> context) throws TaskExecutionFailedException {

        // TODO Read basic attributes

        final Pack pack = context.getPack();
        final BuildableBlockType.Builder builder = context.getBuilder();
        builder.build(Constants.Plugin.ID + ":" + pack.getName().toLowerCase(Locale.ENGLISH) + "/" + context.getAsset().getName());
    }
}
