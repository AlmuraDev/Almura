/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.horizontal;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinition;
import com.almuradev.content.type.block.type.horizontal.state.HorizontalBlockStateDefinitionBuilder;

public interface HorizontalBlock extends ContentBlockType.InInventory {

    interface Builder extends ContentBlockType.Builder<HorizontalBlock, HorizontalBlockStateDefinition, HorizontalBlockStateDefinitionBuilder> {

    }
}
