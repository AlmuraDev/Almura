/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.model;

import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.element.Shape;

import java.util.List;

public class PackModelContainer {

    private final String identifier;
    private final PackPhysics physics;
    private Optional<IModel> model;

    public PackModelContainer(String identifier, PackPhysics physics) {
        this.identifier = identifier;
        this.physics = physics;
    }

    public String getIdentifier() {
        return identifier;
    }

    public PackPhysics getPhysics() {
        return physics;
    }

    @SideOnly(Side.CLIENT)
    public Optional<IModel> getModel() {
        return model;
    }

    @SideOnly(Side.CLIENT)
    public void setModel(IModel model) {
        this.model = Optional.fromNullable(model);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && identifier.equals(((PackModelContainer) o).identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "PackModel {identifier= " + identifier + "}";
    }

    @SideOnly(Side.CLIENT)
    public static final class PackShape extends Shape implements IModel {

        public PackShape(PackFace... faces) {
            super(faces);
        }

        public PackShape(List<PackFace> faces) {
            this(faces.toArray(new PackFace[faces.size()]));
        }

        public PackShape(PackShape s) {
            PackFace[] shapeFaces = (PackFace[]) s.getFaces();
            this.faces = new PackFace[shapeFaces.length];
            for (int i = 0; i < shapeFaces.length; i++) {
                faces[i] = new PackFace(shapeFaces[i]);
            }
            copyMatrix(s);
        }
    }


}
