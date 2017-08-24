/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.content.block.component.sound.BlockSoundGroup;
import net.minecraft.block.SoundType;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;

public class BlockSoundGroupRegistryModule extends AbstractCatalogRegistryModule.Mapped<BlockSoundGroup, SoundType> implements AbstractCatalogRegistryModule.Additional<BlockSoundGroup> {

    public BlockSoundGroupRegistryModule() {
        super(52);
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
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
