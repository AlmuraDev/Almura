/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.content.type.block.BlockConfig;

/**
 * @see BlockConfig
 */
public interface CropBlockConfig {

    String SEED = "seed";

    interface State {
        String CAN_ROLLBACK = "can_rollback";
        String FERTILIZER = "fertilizer";
        String GROWTH = "growth";
        String HYDRATION = "hydration";
    }
}
