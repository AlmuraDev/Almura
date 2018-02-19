/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.tree.processor;

import com.almuradev.content.type.tree.Tree;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.almuradev.toolbox.config.processor.TaggedConfigProcessor;
import com.almuradev.toolbox.config.tag.ConfigTag;

interface AbstractTreeProcessor extends ConfigProcessor<Tree.Builder>, TaggedConfigProcessor<Tree.Builder, ConfigTag> {
}
