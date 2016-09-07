/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.event;

import cpw.mods.fml.common.eventhandler.Event;

public class PageDeleteEvent extends Event {

    public final String identifier;

    public PageDeleteEvent(String identifier) {
        this.identifier = identifier;
    }
}
