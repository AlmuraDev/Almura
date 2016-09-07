/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.permissions;

public interface Permissible {

    /**
     * Returns the node name of the mod, replicated net.eq2online.permissions will be of the form mod.{modname}.permission.node so this
     * method must return a valid name for use in permission nodes. This method must also return the same value every
     * time it is called since permissible names are not necessarily cached.
     *
     * @return Permissible name
     */
    String getPermissibleModName();

    /**
     * The mod version to replicate to the server
     *
     * @return Mod version as a float
     */
    float getPermissibleModVersion();

    /**
     * Called by the net.eq2online.permissions manager at initialisation to instruct the mod to populate the list of net.eq2online.permissions it
     * supports. This method should call back against the supplied net.eq2online.permissions manager to register the net.eq2online.permissions to
     * be sent to the server when connecting.
     *
     * @param permissionsManager net.eq2online.permissions manager
     */
    void registerPermissions(PermissionsManager permissionsManager);

    /**
     * Called when the net.eq2online.permissions set is cleared
     *
     * @param manager
     */
    void onPermissionsCleared(PermissionsManager manager);

    /**
     * Called when the net.eq2online.permissions are changed (eg. when new net.eq2online.permissions are received from the server)
     *
     * @param manager
     */
    void onPermissionsChanged(PermissionsManager manager);
}
