package com.almuradev.almura.pack.node;

import com.almuradev.almura.pack.node.container.StateProperty;

import java.util.Set;

public class ContainerNode implements INode<Set<StateProperty>> {
    private final String title;
    private final int size;
    private final boolean useDisplayNameOfContainerAsTitle;
    private final int maxStackSize;
    private final Set<StateProperty> value;

    public ContainerNode(String title, int size, boolean useDisplayNameOfContainerAsTitle, int maxStackSize, Set<StateProperty> value) {
        this.title = title;
        this.size = size;
        this.useDisplayNameOfContainerAsTitle = useDisplayNameOfContainerAsTitle;
        this.maxStackSize = maxStackSize;
        this.value = value;
    }

    @Override
    public Set<StateProperty> getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public boolean useDisplayNameOfContainerAsTitle() {
        return useDisplayNameOfContainerAsTitle;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }
}
