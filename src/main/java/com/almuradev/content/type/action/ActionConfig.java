/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.content.type.action.type.blockdecay.BlockDecayActionConfig;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyActionConfig;

public interface ActionConfig {
    /**
     * @see BlockDestroyActionConfig
     */
    String DESTROY = "destroy";

    /**
     * @see BlockDecayActionConfig
     */
    String DECAY = "decay";
}
