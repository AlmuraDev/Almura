/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply;

import com.almuradev.content.component.ComponentModule;

public final class ApplyModule extends ComponentModule.Module {
    @Override
    protected void configure() {
        this.bind(ApplyParser.class).to(ApplyParserImpl.class);
    }
}
