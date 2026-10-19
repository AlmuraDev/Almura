/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.client;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.almuradev.almura.feature.skills.SkillEntry;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Holds the skills the server last sent, and which of them the player shows on the HUD.
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientSkillsManager implements Witness {

    private final MappedConfiguration<ClientConfiguration> configAdapter;
    private volatile List<SkillEntry> skills = ImmutableList.of();
    private int revision;

    @Inject
    private ClientSkillsManager(final MappedConfiguration<ClientConfiguration> configAdapter) {
        this.configAdapter = configAdapter;
    }

    @SubscribeEvent
    public void onClientConnectedToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.setSkills(ImmutableList.of());
    }

    public List<SkillEntry> getSkills() {
        return this.skills;
    }

    public void setSkills(final List<SkillEntry> skills) {
        this.skills = ImmutableList.copyOf(skills);
        this.revision++;
    }

    /**
     * @return a number that changes every time new skills arrive, so screens know when to refresh
     */
    public int getRevision() {
        return this.revision;
    }

    public int getTotalLevel() {
        return this.skills.stream().mapToInt(skill -> skill.level).sum();
    }

    public int getTotalMaxLevel() {
        return this.skills.stream().mapToInt(skill -> skill.maxLevel).sum();
    }

    public boolean isShownOnHud(final String id) {
        return this.configAdapter.get().general.hudSkills.contains(id);
    }

    public void setShownOnHud(final String id, final boolean shown) {
        final GeneralCategory general = this.configAdapter.get().general;
        if (shown == general.hudSkills.contains(id)) {
            return;
        }
        // Copy, in case the loaded list is immutable
        final List<String> hudSkills = new ArrayList<>(general.hudSkills);
        if (shown) {
            hudSkills.add(id);
        } else {
            hudSkills.remove(id);
        }
        general.hudSkills = hudSkills;
        this.configAdapter.save();
        this.configAdapter.load();
    }

    /**
     * @return the skills the player chose to show on the HUD, in display order
     */
    public List<SkillEntry> getHudSkills() {
        final List<String> hudSkills = this.configAdapter.get().general.hudSkills;
        return this.skills.stream().filter(skill -> hudSkills.contains(skill.id)).collect(Collectors.toList());
    }
}
