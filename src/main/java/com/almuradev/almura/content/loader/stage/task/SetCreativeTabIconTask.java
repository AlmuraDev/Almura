/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.creativetab.CreativeTab;

public class SetCreativeTabIconTask implements StageTask<CreativeTab, CreativeTab.Builder> {

    public static final SetCreativeTabIconTask instance = new SetCreativeTabIconTask();

    @Override
    public void execute(AssetContext<CreativeTab, CreativeTab.Builder> context) {

    }
}
