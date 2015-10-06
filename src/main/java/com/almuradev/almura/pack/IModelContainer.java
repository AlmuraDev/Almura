/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.pack.model.PackModelContainer;
import com.google.common.base.Optional;

public interface IModelContainer {

    Optional<PackModelContainer> getModelContainer();

    void setModelContainer(PackModelContainer modelContainer);

    String getModelName();

    String getIdentifier();
}
