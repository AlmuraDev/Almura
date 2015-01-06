package com.almuradev.almura.pack.node.container;

import com.almuradev.almura.pack.node.property.IProperty;

import java.util.List;
import java.util.Map;

public class StateProperty implements IProperty<Boolean> {
    private final boolean enabled;
    private final String identifier;
    private final String textureName;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String shapeName;

    public StateProperty(boolean enabled, String identifier, String textureName, Map<Integer, List<Integer>> textureCoordinates, String shapeName) {
        this.enabled = enabled;
        this.identifier = identifier;
        this.textureName = textureName;
        this.textureCoordinates = textureCoordinates;
        this.shapeName = shapeName;
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

    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinates;
    }

    public String getShapeName() {
        return shapeName;
    }
}
