/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.normal;

import com.almuradev.almura.content.type.block.type.BuildableBlockType;

public interface NormalBlockType extends BuildableBlockType {

    interface Builder<T extends NormalBlockType, B extends Builder<T, B>> extends BuildableBlockType.Builder<T, B> {

    }
}
