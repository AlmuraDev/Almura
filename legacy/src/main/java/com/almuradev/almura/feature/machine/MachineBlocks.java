/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.feature.machine.processing.MachineType;
import com.almuradev.almura.feature.machine.processing.ProcessingBlock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Holder so the blocks are only created when first used during block registration, once Almura's item groups (creative tabs) exist.
 */
public final class MachineBlocks {

    /** One block per machine type. */
    public static final Map<MachineType, ProcessingBlock> PROCESSING;

    static {
        final Map<MachineType, ProcessingBlock> blocks = new EnumMap<>(MachineType.class);
        for (final MachineType type : MachineType.values()) {
            // Machines whose required mod is missing (the wash plant without IndustrialCraft 2) don't exist
            if (!type.available()) {
                continue;
            }
            blocks.put(type, new ProcessingBlock(type));
        }
        PROCESSING = Collections.unmodifiableMap(blocks);
    }

    private MachineBlocks() {
    }
}
