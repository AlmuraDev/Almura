/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.toolbox.config.processor.ConfigProcessor;

public interface ActionContentProcessor<C extends ActionContentType, B extends ActionContentType.Builder<C>> extends ConfigProcessor<B> {

}
