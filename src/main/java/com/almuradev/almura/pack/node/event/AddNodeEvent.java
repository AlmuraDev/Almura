/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.event;

import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.node.INode;

public class AddNodeEvent extends NodeEvent {
    public AddNodeEvent(INodeContainer container, INode<?> node) {
        super(container, node);
    }
}
