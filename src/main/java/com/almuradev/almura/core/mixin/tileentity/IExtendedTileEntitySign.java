/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.tileentity;

public interface IExtendedTileEntitySign {
    int getColumnBeingEdited();
    void setColumnBeingEdited(int column);
    void recalculateText();
    boolean hasText();
}
