/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.permissions;

/**
 * Represents a set of net.eq2online.permissions assigned by a remote authority such as a server
 *
 * @author Adam Mummery-Smith
 */
public interface ReplicatedPermissions extends Permissions {

    /**
     * Return true if this net.eq2online.permissions object is valid (within cache period)
     */
    boolean isValid();

    /**
     * Forcibly invalidate this permission container, forces update at the next opportunity
     */
    void invalidate();

    /**
     * Temporarily forces the net.eq2online.permissions object to be valid to prevent repeated re-validation
     */
    void notifyRefreshPending();
}

