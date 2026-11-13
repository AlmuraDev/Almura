/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.blocksoundgroup;

import com.almuradev.content.registry.AbstractCatalogRegistryModule;
import com.almuradev.content.registry.EagerCatalogRegistration;
import net.minecraft.block.SoundType;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

@EagerCatalogRegistration
public final class BlockSoundGroupRegistryModule extends AbstractCatalogRegistryModule.Mapped<BlockSoundGroup, SoundType> implements AdditionalCatalogRegistryModule<BlockSoundGroup> {
    public BlockSoundGroupRegistryModule() {
        super(12);
    }

    @Override
    public void registerDefaults() {
        this.register("wood", SoundType.WOOD);
        this.register("ground", SoundType.GROUND);
        this.register("plant", SoundType.PLANT);
        this.register("stone", SoundType.STONE);
        this.register("metal", SoundType.METAL);
        this.register("glass", SoundType.GLASS);
        this.register("cloth", SoundType.CLOTH);
        this.register("sand", SoundType.SAND);
        this.register("snow", SoundType.SNOW);
        this.register("ladder", SoundType.LADDER);
        this.register("anvil", SoundType.ANVIL);
        this.register("slime", SoundType.SLIME);
    }
}
