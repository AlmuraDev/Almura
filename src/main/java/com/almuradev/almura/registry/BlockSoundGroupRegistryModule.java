/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.block.sound.BlockSoundGroups;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class BlockSoundGroupRegistryModule implements AdditionalCatalogRegistryModule<BlockSoundGroup> {

    @RegisterCatalog(BlockSoundGroups.class)
    public final Map<String, BlockSoundGroup> map = new HashMap<>(52);

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.register("wood", net.minecraft.block.SoundType.WOOD);
        this.register("ground", net.minecraft.block.SoundType.GROUND);
        this.register("plant", net.minecraft.block.SoundType.PLANT);
        this.register("stone", net.minecraft.block.SoundType.STONE);
        this.register("metal", net.minecraft.block.SoundType.METAL);
        this.register("glass", net.minecraft.block.SoundType.GLASS);
        this.register("cloth", net.minecraft.block.SoundType.CLOTH);
        this.register("sand", net.minecraft.block.SoundType.SAND);
        this.register("snow", net.minecraft.block.SoundType.SNOW);
        this.register("ladder", net.minecraft.block.SoundType.LADDER);
        this.register("anvil", net.minecraft.block.SoundType.ANVIL);
        this.register("slime", net.minecraft.block.SoundType.SLIME);
    }

    @Override
    public void registerAdditionalCatalog(BlockSoundGroup group) {
        this.map.put(group.getId(), group);
    }

    private void register(String id, final net.minecraft.block.SoundType group) {
        final String name = id;
        id = SpongeImplHooks.getModIdFromClass(group.getClass()) + ':' + id;
        this.map.put(id, (BlockSoundGroup) group);
        ((IMixinSetCatalogTypeId) group).setId(id, name);
    }

    @Override
    public Optional<BlockSoundGroup> getById(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<BlockSoundGroup> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }
}
