/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.asm.mixin.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * @author Dockter
     *
     * Helps display name and title when in 3rd person view.
     */
    @Override
    protected boolean canRenderName(AbstractClientPlayer entity) {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
        boolean flag = !entity.isInvisibleToPlayer(entityplayersp);

        Team team = entity.getTeam();
        Team team1 = entityplayersp.getTeam();

        if (team != null) {
            Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

            switch (team$enumvisible) {
                case ALWAYS:
                    return flag;
                case NEVER:
                    return false;
                case HIDE_FOR_OTHER_TEAMS:
                    return team1 == null ? flag : team.isSameTeam(team1) && (team.getSeeFriendlyInvisiblesEnabled() || flag);
                case HIDE_FOR_OWN_TEAM:
                    return team1 == null ? flag : !team.isSameTeam(team1) && flag;
                default:
                    return true;
            }
        }

        return Minecraft.isGuiEnabled() && flag && !entity.isBeingRidden();
    }
}
