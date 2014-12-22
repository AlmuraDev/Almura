/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.event;

import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.node.INode;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class NodeEvent extends Event {

    private final INodeContainer container;
    private final INode<?> node;

    public NodeEvent(INodeContainer container, INode<?> node) {
        this.container = container;
        this.node = node;
    }

    public INodeContainer getContainer() {
        return container;
    }

    public final INode<?> getNode() {
        return node;
    }
}
