/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.asm.mixin.client.renderer.entity;

import com.almuradev.almura.feature.title.ClientTitleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.inject.Inject;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {

    @Inject private static ClientTitleManager manager;

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * @author Zidane
     * @reason To have titles render under the nameplate
     */
    @Overwrite
    protected void renderEntityName(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq) {
        if (distanceSq < 100.0D) {

            // Draw bottom-up

            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null) {
                Score score = scoreboard.getOrCreateScore(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (double) ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F);
            }

            // Nickname
            // Title
            // Scoreboard
            final String title = manager.getTitle(entityIn.getUniqueID());
            if (title != null) {
                // TODO Could make this configurable
                this.renderLivingLabel(entityIn, title, x, y, z, 128);
                y += (double) ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F);
            }
        }

        super.renderEntityName(entityIn, x, y, z, name, distanceSq);
    }

    /**
     * @author Dockter
     * @reason Helps display name and title when in 3rd person view.
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
