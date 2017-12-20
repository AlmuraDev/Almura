package com.almuradev.almura.feature.title.asm.mixin.client.renderer.entity;

import com.almuradev.almura.feature.title.TitleManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.inject.Inject;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends Render {

    @Inject private static TitleManager manager;

    protected MixinRenderPlayer(RenderManager renderManager) {
        super(renderManager);
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
            final String title = manager.getTitleForRender(entityIn.getUniqueID());
            if (title != null) {
                // TODO Could make this configurable
                this.renderLivingLabel(entityIn, title, x, y, z, 96);
                y += (double) ((float) this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * 0.025F);
            }
        }

        super.renderEntityName(entityIn, x, y, z, name, distanceSq);
    }
}
