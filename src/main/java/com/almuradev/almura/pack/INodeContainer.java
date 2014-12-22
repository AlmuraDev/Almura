/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.pack.node.INode;

public interface INodeContainer {
    <T extends INode<?>> T addNode(T node);

    void addNodes(INode... nodes);

    <T extends INode<?>> T getNode(Class<T> clazz);

    <T extends INode<?>> boolean hasNode(Class<T> clazz);
}
