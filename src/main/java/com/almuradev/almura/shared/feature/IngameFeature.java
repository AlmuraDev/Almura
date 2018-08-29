/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface IngameFeature {

    UUID UNKNOWN_OWNER = new UUID(0, 0);

    String getId();

    String getName();

    Instant getCreated();

    UUID getCreator();

    /**
     * Returns the creator name, if known. A server shop who is created by default would not have one.
     *
     * No assumption should ever be made that the name is a direct correlation to the UUID.
     *
     * @return The name, if known
     */
    Optional<String> getCreatorName();

    void syncCreatorNameToUniqueId();

    String getPermission();

    boolean isHidden();

    boolean isLoaded();

    void setLoaded(final boolean loaded);

    /**
     * INTERNAL USE ONLY
     */
    void setCreatorName(@Nullable final String creatorName);
}
