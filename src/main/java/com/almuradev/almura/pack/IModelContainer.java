package com.almuradev.almura.pack;

import net.malisis.core.renderer.model.MalisisModel;

public interface IModelContainer extends IPackObject {
    MalisisModel getModel();
    void setModelFromPack();
}
