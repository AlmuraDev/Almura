/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.loader.AssetContext;

public class SetItemGroupIconTask implements StageTask<ItemGroup, ItemGroup.Builder> {

    public static final SetItemGroupIconTask instance = new SetItemGroupIconTask();

    @Override
    public void execute(AssetContext<ItemGroup, ItemGroup.Builder> context) {

    }
}
