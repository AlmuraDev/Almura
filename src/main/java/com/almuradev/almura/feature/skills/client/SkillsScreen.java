/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.client;

import com.almuradev.almura.feature.menu.AlmuraScreens;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.skills.SkillEntry;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

/**
 * The Features menu's Skills, as 3.5's {@code SkillsScreen}: each skill's level, experience and progress to the next level, with a HUD box
 * to show it in the bottom right of the HUD, and the total level. Built again when new skills arrive while it is open.
 */
public final class SkillsScreen implements Screen {

    private static final float MIN_WIDTH = 260f, BAR_HEIGHT = 5f, BUTTON_HEIGHT = 16f, CLOSE_WIDTH = 40f, INSET = 6f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, YELLOW = 0xFFFFFF55;
    private static final Paint BAR_BACK = Paint.solid(0xFF202020);
    private static final DecimalFormat XP = new DecimalFormat("#,##0.##");

    private @Nullable ComponentFactory factory;
    private @Nullable StackPanel column;
    private @Nullable Window window;
    private int builtRevision = -1;

    @Override
    public Text title() {
        final String player = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getGameProfile().name();
        return Text.of("My Skills (" + player + ")");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final StackPanel column = factory.stackPanel().setSpacing(2f);
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        this.column = column;
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(column)
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        this.window = window;
        // Built again when new skills arrive
        window.onEvent(Component.PAINT, event -> {
            if (this.builtRevision != ClientSkills.revision()) {
                this.fill();
            }
        });
        this.fill();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private void fill() {
        final ComponentFactory factory = this.factory;
        final StackPanel column = this.column;
        if (factory == null || column == null || this.window == null) {
            return;
        }
        this.builtRevision = ClientSkills.revision();
        column.clear();
        final List<SkillEntry> skills = ClientSkills.skills();
        // Wide enough for the longest experience line, never narrower than 3.5's
        float width = MIN_WIDTH;
        for (final SkillEntry skill : skills) {
            width = Math.max(width, Minecraft.getInstance().font.width(experienceText(skill).plain()) + INSET * 2 + 8f);
        }
        this.window.setWidth(width);
        if (skills.isEmpty()) {
            column.add(centred(factory.label(Text.of("Your skills haven't arrived from the server yet.").color(GRAY))));
        }
        for (final SkillEntry skill : skills) {
            final CheckBox hud = factory.checkBox(Text.of("HUD").color(WHITE)).setBoxSize(AlmuraScreens.CHECKBOX_SIZE)
                .setState(ClientSkills.isShownOnHud(skill.id()) ? CheckState.CHECKED : CheckState.UNCHECKED);
            hud.setTooltip(Text.of("Show " + MinecraftText.of(skill.name()).plain() + " in the bottom-right corner of your HUD"));
            hud.onEvent(CheckBox.CHANGED, event -> ClientSkills.setShownOnHud(skill.id(), event.value() == CheckState.CHECKED));
            column.add(factory.anchorPanel()
                .add(factory.label(MinecraftText.of(skill.name()).append(Text.of(" :: ").color(GRAY))
                    .append(Text.of("Lv. " + skill.level() + " / " + skill.maxLevel()).color(WHITE))).setAnchor(Anchor.MIDDLE_LEFT))
                .add(hud.setAnchor(Anchor.MIDDLE_RIGHT))
                .setHeight(11f).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
            column.add(indented(factory.label(experienceText(skill))));
            final String next = skill.isMaxLevel() ? "Max level" : XP.format(Math.max(0, skill.nextLevelExperience() - skill.experience()));
            column.add(indented(factory.label(Text.of("Next Level: ").color(WHITE).append(Text.of(next).color(GRAY)))));
            column.add(indented(bar(factory, skill)).setMargin(INSET, 1f, INSET, 6f));
        }
        if (!skills.isEmpty()) {
            column.add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
            column.add(factory.label(Text.of("Total Level: ").color(YELLOW)
                .append(Text.of(ClientSkills.totalLevel() + " / " + ClientSkills.totalMaxLevel()).color(WHITE))));
        }
        final Button close = factory.button(Text.translatable("almura.button.close")).setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        final AnchorPanel bottom = factory.anchorPanel().add(close.setAnchor(Anchor.MIDDLE_RIGHT)).setHeight(BUTTON_HEIGHT);
        if (!skills.isEmpty()) {
            bottom.add(factory.label(Text.of("Checked skills appear on your HUD.").color(GRAY)).setAnchor(Anchor.MIDDLE_LEFT));
        }
        column.add(bottom.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(0f, 4f, 0f, 0f));
    }

    private static Text experienceText(final SkillEntry skill) {
        return Text.of("Current XP: ").color(WHITE).append(Text.of(XP.format(skill.experience()) + " / " + XP.format(skill.maxExperience())).color(GRAY));
    }

    /**
     * The skill's progress through its level, in its colour.
     */
    private static AnchorPanel bar(final ComponentFactory factory, final SkillEntry skill) {
        final AnchorPanel fill = factory.anchorPanel().setBackground(Paint.solid(0xFF000000 | skill.color()));
        final AnchorPanel bar = factory.anchorPanel().setBackground(BAR_BACK).setHeight(BAR_HEIGHT);
        bar.add(fill.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH));
        // As wide as the progress, once the bar knows its width
        bar.onEvent(Component.PAINT, event -> {
            final float width = bar.bounds().width() * skill.levelProgress();
            if (Math.abs(fill.bounds().width() - width) > 0.5f) {
                fill.setWidth(width);
            }
        });
        return bar;
    }

    private static <C extends Component<C>> C indented(final C component) {
        return component.setMargin(INSET, 0f, INSET, 0f).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    private static <C extends Component<C>> C centred(final C component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
