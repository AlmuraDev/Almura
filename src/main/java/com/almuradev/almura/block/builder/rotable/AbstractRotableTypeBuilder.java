/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotable;

import com.almuradev.almura.api.block.rotable.RotableBlockType;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;

public abstract class AbstractRotableTypeBuilder<ROTABLE extends RotableBlockType, BUILDER extends AbstractRotableTypeBuilder<ROTABLE, BUILDER>>
        extends AbstractBlockTypeBuilder<ROTABLE, BUILDER> implements RotableBlockType.Builder<ROTABLE, BUILDER>  {
}
