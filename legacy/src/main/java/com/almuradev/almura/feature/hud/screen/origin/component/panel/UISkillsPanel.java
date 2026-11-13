/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.skills.SkillEntry;
import com.almuradev.almura.feature.skills.client.ClientSkillsManager;
import com.almuradev.almura.shared.client.ui.Fonts;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.util.FontColors;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Shows the skills the player picked in the Skills window: name, level and a bar for progress to the next level.
 */
@SideOnly(Side.CLIENT)
public class UISkillsPanel extends AbstractPanel {

    private static final int PADDING = 3;
    private static final int TEXT_HEIGHT = 9;
    private static final int BAR_HEIGHT = 4;
    private static final int ROW_GAP = 3;
    private static final int ROW_HEIGHT = TEXT_HEIGHT + 1 + BAR_HEIGHT + ROW_GAP;

    @Inject private static ClientSkillsManager skillsManager;

    // The text's distance in from the padding (the background's border is wider than it), and the least gap between a skill's
    // name and its level
    private static final int TEXT_INSET = 2;
    private static final int NAME_GAP = 6;

    private List<SkillEntry> skills = Collections.emptyList();
    // Its width with short names: it grows for longer ones (see refresh)
    private final int minWidth;

    public UISkillsPanel(final MalisisGui gui, final int width) {
        super(gui, width, 0);
        this.minWidth = width;
    }

    /**
     * Picks up the current selection and resizes to fit: tall enough for every skill, wide enough for the longest name and level
     * side by side. Call once per frame before drawing.
     */
    public void refresh() {
        this.skills = skillsManager.getHudSkills();
        this.setVisible(!this.skills.isEmpty());
        if (!this.skills.isEmpty()) {
            int widest = 0;
            for (final SkillEntry skill : this.skills) {
                final int name = (int) Math.ceil(Fonts.MINECRAFT.getStringWidth(skill.name, FontColors.WHITE_FO));
                final int level = (int) Math.ceil(Fonts.MINECRAFT.getStringWidth(level(skill), FontColors.WHITE_FO));
                widest = Math.max(widest, name + NAME_GAP + level);
            }
            this.setSize(Math.max(this.minWidth, (PADDING + TEXT_INSET) * 2 + widest),
                    PADDING * 2 + this.skills.size() * ROW_HEIGHT - ROW_GAP);
        }
    }

    private static String level(final SkillEntry skill) {
        return TextFormatting.WHITE + "Lv. " + skill.level + TextFormatting.GRAY + " / " + skill.maxLevel;
    }

    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final int barWidth = this.width - PADDING * 2;
        final int backgroundAlpha = MathUtil.squashi(this.getAlpha() - 75, 0, 255);
        int y = PADDING + TEXT_HEIGHT + 1;
        for (final SkillEntry skill : this.skills) {
            renderer.drawRectangle(PADDING, y, this.getZIndex(), barWidth, BAR_HEIGHT, 0, backgroundAlpha);
            renderer.drawRectangle(PADDING + 1, y + 1, this.getZIndex(), (int) ((barWidth - 2) * skill.getLevelProgress()), BAR_HEIGHT - 2,
                    skill.color, this.getAlpha());
            y += ROW_HEIGHT;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        super.drawForeground(renderer, mouseX, mouseY, partialTick);

        int y = PADDING;
        for (final SkillEntry skill : this.skills) {
            renderer.drawText(Fonts.MINECRAFT, skill.name, PADDING + TEXT_INSET, y, this.getZIndex(), FontColors.WHITE_FO);

            final String level = level(skill);
            final int levelWidth = (int) Math.ceil(Fonts.MINECRAFT.getStringWidth(level, FontColors.WHITE_FO));
            renderer.drawText(Fonts.MINECRAFT, level, this.width - PADDING - TEXT_INSET - levelWidth, y, this.getZIndex(), FontColors.WHITE_FO);
            y += ROW_HEIGHT;
        }
    }
}
