/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.permissions;

public class LocalPermissions implements Permissions {

    @Override
    public boolean hasPermissionSet(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return defaultValue;
    }
}
