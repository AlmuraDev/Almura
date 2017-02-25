/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.extension.sign;

public interface IExtendedTileEntitySign {

    int getColumnBeingEdited();

    void setColumnBeingEdited(int column);

    void recalculateText();

    boolean hasText();
}
