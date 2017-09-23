/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.type.block.component.action.breaks.BlockBreak;

import java.util.List;

public interface IMixinAlmuraBlock {

    List<BlockBreak> getBreaks();

    void setBreaks(List<BlockBreak> breaks);
}
