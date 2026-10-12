/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorBlock;

/**
 * Holder so the blocks are only created when first used during block registration, once Almura's item groups (creative tabs) exist.
 */
public final class MachineBlocks {

    public static final SeedProcessorBlock SEED_PROCESSOR = new SeedProcessorBlock();

    private MachineBlocks() {
    }
}
