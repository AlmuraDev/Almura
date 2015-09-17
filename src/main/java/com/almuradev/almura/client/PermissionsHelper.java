/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.permissions.Permissible;
import com.almuradev.almura.permissions.PermissionsManager;
import com.almuradev.almura.permissions.PermissionsManagerClient;

public class PermissionsHelper {
    public static final Permissible PERMISSIBLE_GUIDE = new GuideClientPermissible();

    public static void register() {
        PermissionsManagerClient.getInstance().registerPermissible(PERMISSIBLE_GUIDE);
    }

    public static boolean hasPermission(Permissible source, String perm) {
        return PermissionsManagerClient.getInstance().getPermissions(source).hasPermission(perm);
    }

    private static class GuideClientPermissible implements Permissible {

        @Override
        public String getPermissibleModName() {
            return "guide";
        }

        @Override
        public float getPermissibleModVersion() {
            return 1;
        }

        @Override
        public void registerPermissions(PermissionsManager permissionsManager) {
            permissionsManager.registerModPermission(this, "open");
            permissionsManager.registerModPermission(this, "create");
            permissionsManager.registerModPermission(this, "auto");
        }

        @Override
        public void onPermissionsCleared(PermissionsManager manager) {

        }

        @Override
        public void onPermissionsChanged(PermissionsManager manager) {
            // TODO Re-populate Guide list if it is currently open
        }
    }
}
