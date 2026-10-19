/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.client.gui;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPropertyBar;
import com.almuradev.almura.feature.skills.SkillEntry;
import com.almuradev.almura.feature.skills.client.ClientSkillsManager;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Lists the player's skills, with a checkbox per skill to show it on the HUD.
 */
@SideOnly(Side.CLIENT)
public final class SkillsScreen extends BasicScreen {

    private static final String CHECKBOX_PREFIX = "checkbox.skill.";
    private static final int FORM_WIDTH = 260;
    private static final int LINE_HEIGHT = 10;
    private static final int SKILL_HEIGHT = LINE_HEIGHT * 3 + 9;

    @Inject private static ClientSkillsManager skillsManager;

    private final DecimalFormat xpFormat = new DecimalFormat("#,##0.##");
    private int builtRevision;

    public SkillsScreen(@Nullable final GuiScreen parent) {
        super(parent);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        this.builtRevision = skillsManager.getRevision();

        final List<SkillEntry> skills = skillsManager.getSkills();
        final String playerName = Minecraft.getMinecraft().player == null ? "" : Minecraft.getMinecraft().player.getName();
        final int contentHeight = skills.isEmpty() ? 30 : skills.size() * SKILL_HEIGHT + 40;
        final BasicForm form = new BasicForm(this, FORM_WIDTH, contentHeight + 30, "My Skills (" + playerName + ")");

        int y = 4;

        if (skills.isEmpty()) {
            final UILabel waitingLabel = new UILabel(this, TextFormatting.GRAY + "Your skills haven't arrived from the server yet.");
            waitingLabel.setFontOptions(FontColors.WHITE_FO);
            waitingLabel.setPosition(0, y, Anchor.TOP | Anchor.CENTER);
            form.add(waitingLabel);
        }

        for (final SkillEntry skill : skills) {
            final UILabel levelLabel = new UILabel(this, skill.name + TextFormatting.GRAY + " :: " + TextFormatting.WHITE + "Lv. " + skill.level
                    + " / " + skill.maxLevel);
            levelLabel.setFontOptions(FontColors.WHITE_FO);
            levelLabel.setPosition(4, y);

            final UICheckBox hudCheckBox = new UICheckBox(this, TextFormatting.WHITE + "HUD");
            hudCheckBox.setChecked(skillsManager.isShownOnHud(skill.id));
            hudCheckBox.setName(CHECKBOX_PREFIX + skill.id);
            hudCheckBox.setTooltip("Show " + TextFormatting.getTextWithoutFormattingCodes(skill.name) + " in the bottom-right corner of your HUD");
            hudCheckBox.setPosition(-4, y - 1, Anchor.TOP | Anchor.RIGHT);
            hudCheckBox.register(this);

            final UILabel experienceLabel = new UILabel(this, TextFormatting.WHITE + "Current XP: " + TextFormatting.GRAY
                    + this.xpFormat.format(skill.experience) + " / " + this.xpFormat.format(skill.maxExperience));
            experienceLabel.setFontOptions(FontColors.WHITE_FO);
            experienceLabel.setPosition(10, y + LINE_HEIGHT);

            final String nextLevel = skill.isMaxLevel() ? "Max level" : this.xpFormat.format(skill.getExperienceToNextLevel());
            final UILabel nextLevelLabel = new UILabel(this, TextFormatting.WHITE + "Next Level: " + TextFormatting.GRAY + nextLevel);
            nextLevelLabel.setFontOptions(FontColors.WHITE_FO);
            nextLevelLabel.setPosition(10, y + LINE_HEIGHT * 2);

            final UIPropertyBar progressBar = new UIPropertyBar(this, FORM_WIDTH - 20, 5)
                    .setColor(skill.color)
                    .setAmount(skill.getLevelProgress())
                    .setPosition(10, y + LINE_HEIGHT * 3);

            form.add(levelLabel, hudCheckBox, experienceLabel, nextLevelLabel, progressBar);
            y += SKILL_HEIGHT;
        }

        if (!skills.isEmpty()) {
            final UISeparator separator = new UISeparator(this);
            separator.setSize(form.getWidth() - 10, 1);
            separator.setPosition(0, y, Anchor.TOP | Anchor.CENTER);

            final UILabel totalLabel = new UILabel(this, TextFormatting.YELLOW + "Total Level: " + TextFormatting.WHITE + skillsManager.getTotalLevel()
                    + " / " + skillsManager.getTotalMaxLevel());
            totalLabel.setFontOptions(FontColors.WHITE_FO);
            totalLabel.setPosition(4, y + 5);

            final UILabel hintLabel = new UILabel(this, TextFormatting.GRAY + "Checked skills appear on your HUD.");
            hintLabel.setFontOptions(FontColors.WHITE_FO);
            hintLabel.setPosition(4, y + 5 + LINE_HEIGHT + 2);

            form.add(separator, totalLabel, hintLabel);
        }

        final UIButton closeButton = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(I18n.format("almura.button.close"))
                .onClick(this::close)
                .build("button.close");

        form.add(closeButton);
        addToScreen(form);
    }

    @Override
    public void update(final int mouseX, final int mouseY, final float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        // Rebuild when new skill data arrives while the screen is open
        if (skillsManager.getRevision() != this.builtRevision) {
            this.clearScreen();
            this.construct();
        }
    }

    @Subscribe
    public void onCheck(final UICheckBox.CheckEvent event) {
        final String name = event.getComponent().getName();
        if (name != null && name.startsWith(CHECKBOX_PREFIX)) {
            skillsManager.setShownOnHud(name.substring(CHECKBOX_PREFIX.length()), event.getNewValue());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
