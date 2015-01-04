/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import com.almuradev.almura.client.gui.AlmuraGui;

public class GuideGUI extends AlmuraGui {

    /**
     * Creates an gui with a parent screen and calls {@link AlmuraGui#setup}, if the parent is null then no background will be added

     * @param parent the {@link AlmuraGui} that we came from
     */
    public GuideGUI(AlmuraGui parent) {
        super(parent);
        setup();
    }

    @Override
    protected void setup() {
    }
}
