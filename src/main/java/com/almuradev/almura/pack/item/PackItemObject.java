/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.item;

import com.almuradev.almura.pack.PackObject;

public interface PackItemObject extends PackObject {

    interface Builder<P extends PackItemObject, B extends Builder<P, B>> extends PackObject.Builder<P, B> {

    }
}
