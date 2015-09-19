/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.permissions;

import java.util.HashSet;
import java.util.Set;

public class PermissibleAllMods implements Permissible {

    private Set<Permissible> permissibles = new HashSet<>();

    public void addPermissible(Permissible permissible) {
        this.permissibles.add(permissible);
    }

    @Override
    public String getPermissibleModName() {
        return "all";
    }

    @Override
    public float getPermissibleModVersion() {
        return 0.0F;
    }

    @Override
    public void registerPermissions(PermissionsManager permissionsManager) {
    }

    @Override
    public void onPermissionsCleared(PermissionsManager manager) {
        for (Permissible permissible : this.permissibles) {
            permissible.onPermissionsCleared(manager);
        }
    }

    @Override
    public void onPermissionsChanged(PermissionsManager manager) {
        for (Permissible permissible : this.permissibles) {
            permissible.onPermissionsChanged(manager);
        }
    }
}

