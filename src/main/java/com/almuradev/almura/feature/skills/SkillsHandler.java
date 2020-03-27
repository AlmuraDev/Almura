/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills;

import com.almuradev.almura.feature.membership.network.ClientboundMembershipGuiOpenPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import org.inspirenxe.skills.api.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;

public final class SkillsHandler implements Witness {

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public SkillsHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
    }

    public double getExperienceForSkill(Player player, String skillName) {
        final SkillService service = Sponge.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return -1;
        }
        final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, "skills:"+ skillName).orElse(null);
        if (skillType == null) {
            return -2;
        }

        SkillHolderContainer container = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (container != null) {
            container = service.getParentContainer(container).orElse(container);
            SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);
            if (holder != null) {
                final Skill skill = holder.getSkill(skillType).orElse(null);
                if (skill != null) {
                    return skill.getCurrentExperience();
                }
            }
        }
        return -3;
    }

    public int getLevelForSkill(Player player, SkillType skillType) {
        final SkillService service = Sponge.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return -1;
        }
        SkillHolderContainer container = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (container != null) {
            container = service.getParentContainer(container).orElse(container);
            SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);
            if (holder != null) {
                final Skill skill = holder.getSkill(skillType).orElse(null);
                if (skill != null) {
                    return skill.getCurrentLevel();
                }
            }
        }
        return -1;
    }

    public int getTotalSkillLevel(Player player) {
        final SkillService service = Sponge.getServiceManager().provide(SkillService.class).orElse(null);
        if (service == null) {
            return -1;
        }
        final SkillType farmingSkillType = Sponge.getRegistry().getType(SkillType.class, "skills:farming").orElse(null);
        final SkillType miningSkillType = Sponge.getRegistry().getType(SkillType.class, "skills:mining").orElse(null);
        final SkillType diggerSkillType = Sponge.getRegistry().getType(SkillType.class, "skills:digger").orElse(null);
        final SkillType woodcuttingSkillType = Sponge.getRegistry().getType(SkillType.class, "skills:woodcutting").orElse(null);
        final SkillType craftingSkillType = Sponge.getRegistry().getType(SkillType.class, "skills:crafting").orElse(null);

        if (farmingSkillType == null || miningSkillType == null || diggerSkillType == null || woodcuttingSkillType == null || craftingSkillType == null) {
            return -2;
        }

        int farmingLevel = getLevelForSkill(player, farmingSkillType);
        int miningLevel = getLevelForSkill(player, miningSkillType);
        int diggerLevel = getLevelForSkill(player, diggerSkillType);
        int woodcuttingLevel = getLevelForSkill(player, woodcuttingSkillType);
        int craftingLevel = getLevelForSkill(player, craftingSkillType);

        if (farmingLevel <= -1 || miningLevel <= -1 || diggerLevel <= -1 || woodcuttingLevel <= -1 || craftingLevel <= -1) {
            return -1;
        }

        return farmingLevel + miningLevel + diggerLevel + woodcuttingLevel + craftingLevel;
    }
}
