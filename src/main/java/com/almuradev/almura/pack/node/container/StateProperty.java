package com.almuradev.almura.pack.node.container;

import com.almuradev.almura.pack.node.property.IProperty;

import java.util.List;

public class StateProperty implements IProperty<Boolean> {
    private final boolean enabled;
    private final String identifier;
    private final String textureName;
    private final List<Integer> textureCoordinates;

    public StateProperty(boolean enabled, String identifier, String textureName, List<Integer> textureCoordinates) {
        this.enabled = enabled;
        this.identifier = identifier;
        this.textureName = textureName;
        this.textureCoordinates = textureCoordinates;
    }

    @Override
    public Boolean getSource() {
        return enabled;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTextureName() {
        return textureName;
    }

    public List<Integer> getTextureCoordinates() {
        return textureCoordinates;
    }
}
