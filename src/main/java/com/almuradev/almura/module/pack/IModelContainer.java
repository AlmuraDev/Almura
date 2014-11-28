/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.module.pack;

import net.malisis.core.renderer.model.MalisisModel;

public interface IModelContainer extends IPackObject {

    MalisisModel getModel();

    void setModelFromPack();
}
