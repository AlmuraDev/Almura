/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import com.almuradev.almura.content.block.component.action.blockbreak.BlockBreak;

import java.util.List;

public interface IMixinAlmuraBlock {

    List<BlockBreak> getBreaks();

    void setBreaks(List<BlockBreak> breaks);
}
