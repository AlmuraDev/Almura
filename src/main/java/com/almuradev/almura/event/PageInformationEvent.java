/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.event;

import com.almuradev.almura.content.Page;
import cpw.mods.fml.common.eventhandler.Event;

public final class PageInformationEvent extends Event {

    public final Page page;

    public PageInformationEvent(Page page) {
        this.page = page;
    }
}
