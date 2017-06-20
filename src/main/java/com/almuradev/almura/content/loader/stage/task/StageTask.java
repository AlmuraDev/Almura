/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.registry.BuildableCatalogType;

public interface StageTask<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

    void execute(AssetContext<C, B> context) throws TaskExecutionFailedException;
}
