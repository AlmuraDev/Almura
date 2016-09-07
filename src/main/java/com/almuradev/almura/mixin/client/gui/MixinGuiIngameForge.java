/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.client.gui;

import com.almuradev.almura.client.DisplayNameManager;
import com.google.common.base.Optional;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PLAYER_LIST;

import com.almuradev.almura.Configuration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge extends GuiIngame {

    @Shadow private FontRenderer fontrenderer;

    @Shadow abstract boolean pre(RenderGameOverlayEvent.ElementType type);
    @Shadow abstract void post(RenderGameOverlayEvent.ElementType type);

    public MixinGuiIngameForge(Minecraft p_i1036_1_) {
        super(p_i1036_1_);
    }

    // TODO: Use modify args when Mixin is updated
    @Overwrite
    protected void renderBossHealth() {
        if (BossStatus.bossName != null && BossStatus.statusBarTime > 0)
        {
            --BossStatus.statusBarTime;
            FontRenderer fontrenderer = this.mc.fontRendererObj;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            short short1 = 182;
            int j = i / 2 - short1 / 2;
            int k = (int)(BossStatus.healthScale * (float)(short1 + 1));
            byte b0 = 12;
            // Almura start - Check for our HUD
            if (Configuration.HUD_TYPE.equalsIgnoreCase("almura")) {
                b0 = 45;
            } else if (Configuration.HUD_TYPE.equalsIgnoreCase("less")) {
                b0 = 30;
            }
            // Almura end
            this.drawTexturedModalRect(j, b0, 0, 74, short1, 5);
            this.drawTexturedModalRect(j, b0, 0, 74, short1, 5);

            if (k > 0)
            {
                this.drawTexturedModalRect(j, b0, 0, 79, k, 5);
            }

            String s = BossStatus.bossName;
            fontrenderer.drawStringWithShadow(s, i / 2 - fontrenderer.getStringWidth(s) / 2, b0 - 10, 16777215);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(icons);
        }
    }

    @SuppressWarnings("unchecked")
	protected void renderPlayerList(int width, int height)
    {
        ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = mc.thePlayer.sendQueue;

        if (mc.gameSettings.keyBindPlayerList.getIsKeyPressed() && (!mc.isIntegratedServerRunning() || handler.playerInfoList.size() > 1 || scoreobjective != null))
        {
            if (pre(PLAYER_LIST)) return;
            this.mc.mcProfiler.startSection("playerList");
            List<GuiPlayerInfo> players = (List<GuiPlayerInfo>)handler.playerInfoList;
            int maxPlayers = handler.currentServerMaxPlayers;
            int rows = maxPlayers;
            int columns = 1;

            for (columns = 1; rows > 20; rows = (maxPlayers + columns - 1) / columns) {
                columns++;
            }

            int columnWidth = 300 / columns;

            if (columnWidth > 150) {
                columnWidth = 150;
            }
            
            // Experimental to be tested in-game with server full.
            columns = 2;
            rows = 25;
            columnWidth = 200;
            // End Experimental

            int left = (width - columns * columnWidth) / 2;
            byte border = 10;
            // Almura start - Check for our HUD
            if (Configuration.HUD_TYPE.equalsIgnoreCase("almura")) {
                border = 40;
            } else if (Configuration.HUD_TYPE.equalsIgnoreCase("less")) {
                border = 25;
            }
            // Almura end
            drawRect(left - 1, border - 1, left + columnWidth * columns, border + 9 * rows, Integer.MIN_VALUE);

            for (int i = 0; i < maxPlayers; i++)
            {
                int xPos = left + i % columns * columnWidth;
                int yPos = border + i / columns * 9;
                drawRect(xPos, yPos, xPos + columnWidth - 1, yPos + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);

                if (i < players.size())
                {
                    GuiPlayerInfo player = (GuiPlayerInfo)players.get(i);
                    ScorePlayerTeam team = mc.theWorld.getScoreboard().getPlayersTeam(player.name);
                    final DisplayNameManager.Tuple<Optional<String>, Optional<String>> tuple = DisplayNameManager.getDisplayNameAndTitle(player.name);
                    String displayName = "";
                    if (tuple.left.isPresent() && tuple.right.isPresent() && !tuple.right.get().isEmpty()) {
                        if (!tuple.left.get().equalsIgnoreCase(player.name)) {
                            displayName = ScorePlayerTeam.formatPlayerName(team, tuple.left.get() + " | " + ScorePlayerTeam.formatPlayerName(team, player.name) + " | " + tuple.right.get());
                        } else if (tuple.left.get().equalsIgnoreCase(player.name)) {
                            displayName = ScorePlayerTeam.formatPlayerName(team, ScorePlayerTeam.formatPlayerName(team, player.name) + " | " + tuple.right.get());
                        } else {
                            displayName = ScorePlayerTeam.formatPlayerName(team, player.name);
                        }
                    }
                    fontrenderer.drawStringWithShadow(displayName, xPos, yPos, 16777215);

                    if (scoreobjective != null)
                    {
                        int endX = xPos + fontrenderer.getStringWidth(displayName) + 5;
                        int maxX = xPos + columnWidth - 12 - 5;

                        if (maxX - endX > 5)
                        {
                            Score score = scoreobjective.getScoreboard().getValueFromObjective(player.name, scoreobjective);
                            String scoreDisplay = EnumChatFormatting.YELLOW + "" + score.getScorePoints();
                            fontrenderer.drawStringWithShadow(scoreDisplay, maxX - fontrenderer.getStringWidth(scoreDisplay), yPos, 16777215);
                        }
                    }

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                    // This is totally worthless anyways because it isn't accurate.
                    /*
                    mc.getTextureManager().bindTexture(Gui.icons);
                    int pingIndex = 4;
                    int ping = player.responseTime;
                    if (ping < 0) pingIndex = 5;
                    else if (ping < 150) pingIndex = 0;
                    else if (ping < 300) pingIndex = 1;
                    else if (ping < 600) pingIndex = 2;
                    else if (ping < 1000) pingIndex = 3;

                    zLevel += 100.0F;
                    drawTexturedModalRect(xPos + columnWidth - 12, yPos, 0, 176 + pingIndex * 8, 10, 8);
                    zLevel -= 100.0F;
                    */
                }
            }
            post(PLAYER_LIST);
        }
    }
}
