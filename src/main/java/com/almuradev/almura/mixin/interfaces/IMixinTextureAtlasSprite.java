/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.interfaces;

/**
 * Could I have just done an AT? Sure...but AT's fucking suck.
 */
public interface IMixinTextureAtlasSprite {

    void setMinU(float minU);

    void setMinV(float minV);

    void setMaxU(float maxU);

    void setMaxV(float maxV);
}
