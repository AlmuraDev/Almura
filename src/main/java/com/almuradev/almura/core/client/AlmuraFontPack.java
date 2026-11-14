/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.Almura;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.JarContentsPackResources;

import java.util.List;
import java.util.Optional;

/**
 * Almura's lettering, as 3.x's font pack gave it: a resource pack in the mod jar ({@code packs/almura_font}: Minecraft's
 * {@code font/ascii.png}) that is always on and held at the top of the list, so a player's resource pack can't replace the font. Accented
 * and other characters still come from Minecraft or the player's packs.
 */
public final class AlmuraFontPack {

    private static final String PATH = "packs/almura_font";

    private AlmuraFontPack() {
    }

    public static void register(final IEventBus modBus) {
        modBus.addListener((final AddPackFindersEvent event) -> {
            if (event.getPackType() != PackType.CLIENT_RESOURCES) {
                return;
            }
            final var modInfo = ModList.get().getModContainerById(Almura.ID).orElseThrow().getModInfo();
            final String version = modInfo.getVersion().toString();
            final PackLocationInfo location = new PackLocationInfo("mod/" + Almura.ID + ":" + PATH, Component.literal("Almura Font"),
                PackSource.BUILT_IN, Optional.of(new KnownPack("neoforge", "mod/" + Almura.ID + ":" + PATH, version)));
            final Pack pack = new Pack(location, new JarContentsPackResources.JarContentsResourcesSupplier(modInfo.getOwningFile().getFile()
                .getContents(), PATH), new Pack.Metadata(Component.literal("Almura's lettering"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(),
                // Shown in the Resource Packs screen, locked at the top
                List.of(), false),
                // Always on, at the top, and held there: above every pack the player picks
                new PackSelectionConfig(true, Pack.Position.TOP, true));
            event.addRepositorySource(consumer -> consumer.accept(pack));
        });
    }
}
