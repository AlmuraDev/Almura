/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.resources;

import net.minecraft.client.resources.ResourcePackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ResourcePackRepository.class)
public interface ResourcePackRepositoryAccessor {
    //public-f net.minecraft.client.resources.ResourcePackRepository field_110619_e # repositoryEntriesAll
    @Accessor("repositoryEntriesAll") List<ResourcePackRepository.Entry> accessor$getRepositoryEntriesAll();
    //public-f net.minecraft.client.resources.ResourcePackRepository field_110617_f # repositoryEntries
    @Accessor("repositoryEntries") List<ResourcePackRepository.Entry> accessor$getRepositoryEntries();
}
