/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.normal;

import com.almuradev.content.type.block.ContentBlockType;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinition;
import com.almuradev.content.type.block.type.normal.state.NormalBlockStateDefinitionBuilder;

public interface NormalBlock extends ContentBlockType.InInventory {

    interface Builder extends ContentBlockType.Builder.Single<NormalBlock, NormalBlockStateDefinition, NormalBlockStateDefinitionBuilder> {

    }
}
