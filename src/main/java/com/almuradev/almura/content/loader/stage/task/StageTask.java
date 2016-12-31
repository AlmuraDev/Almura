/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.BuildableCatalogType;
import com.almuradev.almura.content.loader.AssetContext;

public interface StageTask<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    void execute(AssetContext<C, B> context) throws TaskExecutionFailedException;
}
