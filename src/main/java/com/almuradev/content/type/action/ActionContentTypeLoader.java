/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.content.loader.MultiTypeContentLoader;
import com.almuradev.content.loader.MultiTypeExternalContentProcessor;

import javax.inject.Singleton;

@Singleton
public final class ActionContentTypeLoader extends MultiTypeContentLoader<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>, ActionContentProcessor<ActionContentType, ActionContentType.Builder<ActionContentType>>> implements MultiTypeExternalContentProcessor<ActionGenre, ActionContentType, ActionContentType.Builder<ActionContentType>> {
}
