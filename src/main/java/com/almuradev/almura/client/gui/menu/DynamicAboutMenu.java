/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Color;

@SideOnly(Side.CLIENT)
public class DynamicAboutMenu extends SimpleGui {

    private static final Text aboutText = Text.of(
            "Almura was created and is maintained by the AlmuraDev Team. https://www.github.com/AlmuraDev/", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, TextStyles.UNDERLINE, "The Beginning...", TextStyles.RESET, Text.NEW_LINE,
            "Almura 1.0 was conceived on June 1st, 2011. It was built around Spoutcraft (1.7.3 beta). As the technology changed we wereforced to ",
            "abandon the Spoutcraft ecosystem in favor of the Forge one. ", TextColors.BLUE, "Zidane", TextColors.RESET,
            " our lead developer for AlmuraDev finally put his foot down and said enough is enough which began our six ",
            "month journey to migrate the features we had relied on in Spoutcraft to our new client.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura Rediscovered was initially conceived on September 4th, 2014.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura has a number of changes from a typical Minecraft setup that gives it an extremely unique and customizable experience.", Text
                    .NEW_LINE,
            "  - ASM through Mixin.", Text.NEW_LINE,
            "  - YAML/JSON content loading system for items, blocks, crops, trees and more.", Text.NEW_LINE,
            "  - Client/Server security system.", Text.NEW_LINE,
            "  - Customized GUI for a more unique experience.", Text.NEW_LINE,
            "  - Information guide system displayed in-game.", Text.NEW_LINE,
            "  - Player accessory system for hats, wings, capes, earrings and more.", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, TextStyles.UNDERLINE, "Credits", TextStyles.RESET, Text.NEW_LINE,
            TextColors.BLUE, "Zidane", TextColors.RESET, Text.NEW_LINE,
            "  - Lead developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Lead developer of AlmuraDev", Text.NEW_LINE,
            "  - Former Spoutcraft/Spout developer", Text.NEW_LINE,
            TextColors.GREEN, "Grinch", TextColors.RESET, Text.NEW_LINE,
            "  - Moderator and contributor to Sponge", Text.NEW_LINE,
            "  - Solder administrator for AlmuraDev", Text.NEW_LINE,
            TextColors.GOLD, "Dockter", TextColors.RESET, Text.NEW_LINE,
            "  - Developer, Lead Tester and Owner for/of Almura public servers", Text.NEW_LINE,
            "  - CFO for Sponge Foundation", Text.NEW_LINE,
            "  - Former Spoutcraft developer", Text.NEW_LINE,
            TextColors.DARK_RED, "Blood", TextColors.RESET, Text.NEW_LINE,
            "  - Developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Former Cauldron developer", Text.NEW_LINE,
            TextColors.DARK_PURPLE, "Mumfrey", TextColors.RESET, Text.NEW_LINE,
            "  - Developer for Sponge", Text.NEW_LINE,
            "  - Developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Creator and maintainer of Mixin ASM for Java", Text.NEW_LINE,
            TextColors.GOLD, "Wifee", TextColors.RESET, Text.NEW_LINE,
            "  - Lead model and graphics artist for Almura's custom content", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, "Special thanks to ", TextColors.BLUE, "Zidane", TextColors.RESET, " for all his time, patience and effort over the ",
            "years. Without this individual.. Almura and AlmuraDev simply would not exist."
    );

    private UIBackgroundContainer contentContainer;
    private UIBackgroundContainer bottomBorderContainer;

    public DynamicAboutMenu(SimpleGui parent) {
        super(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        // Create the form
        final UIBackgroundContainer container = new UIBackgroundContainer(this);
        container.setBackgroundAlpha(0);

        final UILabel titleLabel = new UILabel(this, "About");
        titleLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

        final UIBackgroundContainer topBorderContainer = new UIBackgroundContainer(this, UIComponent.INHERITED, 4);
        topBorderContainer.setPosition(0, SimpleGui.getPaddedY(titleLabel, 3));
        topBorderContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        topBorderContainer.setTopAlpha(255);
        topBorderContainer.setBottomAlpha(120);

        contentContainer = new UIBackgroundContainer(this, UIComponent.INHERITED, container.getHeight() - 104);
        contentContainer.setPosition(0, SimpleGui.getPaddedY(topBorderContainer, 0));
        contentContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        contentContainer.setBackgroundAlpha(120);
        contentContainer.setPadding(4, 10);
        contentContainer.setClipContent(true);

        bottomBorderContainer = new UIBackgroundContainer(this, UIComponent.INHERITED, 4);
        bottomBorderContainer.setPosition(0, container.getHeight() - 86);
        bottomBorderContainer.setColor(Color.ofRgb(0, 0, 0).getRgb());
        bottomBorderContainer.setTopAlpha(120);
        bottomBorderContainer.setBottomAlpha(255);

        final AboutList list = new AboutList(this, 100, container.getHeight());
        final AboutListElement element = new AboutListElement(this, new GuiTexture(SimpleGui.ZIDANE_HEAD_LOCATION),
                new UILabel(this, TextFormatting.WHITE + "Zidane"), 100, 0);

        list.add(new UISlimScrollbar(this, list, UISlimScrollbar.Type.VERTICAL).setAutoHide(true).setSize(list.getHeight(), 2));
        list.add(element);
        contentContainer.add(list);
        container.add(titleLabel, topBorderContainer, contentContainer, bottomBorderContainer);

        addToScreen(container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.contentContainer != null && this.bottomBorderContainer != null) {
            bottomBorderContainer.setPosition(0, SimpleGui.getPaddedY(this.contentContainer, 0));
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
    }

    protected static final class AboutList extends UIBackgroundContainer {

        public AboutList(MalisisGui gui) {
            super(gui);
        }

        public AboutList(MalisisGui gui, String title) {
            super(gui, title);
            this.construct();
        }

        public AboutList(MalisisGui gui, int width, int height) {
            super(gui, width, height);
            this.construct();
        }

        public AboutList(MalisisGui gui, String title, int width, int height) {
            super(gui, title, width, height);
            this.construct();
        }

        private void construct() {
            this.setColor(org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb());
            this.setBackgroundAlpha(155);
        }
    }

    protected static final class AboutListElement extends UIBackgroundContainer {
        private final UIImage image;
        private final UILabel label;
        private final int padding = 4;

        protected AboutListElement(MalisisGui gui, GuiIcon icon, UILabel label, int width, int height) {
            this(gui, null, icon, label, width, height);
        }

        protected AboutListElement(MalisisGui gui, GuiTexture texture, UILabel label, int width, int height) {
            this(gui, texture, null, label, width, height);
        }

        private AboutListElement(MalisisGui gui, GuiTexture texture, GuiIcon icon, UILabel label, int width, int height) {
            super(gui, width, height);

            this.setColor(org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb());

            this.image = new UIImage(gui, texture, icon);
            this.label = label;

            final int labelWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(label.getText());
            if (width <= this.image.getWidth() + labelWidth + (padding * 3)) {
                this.setSize(this.image.getWidth() + labelWidth + (padding * 3), this.getHeight());
            }
            if (height <= this.image.getHeight() + (padding * 2)) {
                this.setSize(this.getWidth(), this.image.getHeight() + (padding * 2));
            }

            this.image.setPosition(padding, (this.getHeight() / 2) - (image.getHeight() / 2));
            this.label.setPosition(SimpleGui.getPaddedX(this.image, padding), this.image.getY());

            this.add(image, label);
        }

        @Override
        public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.isHovered()) {
                this.setBackgroundAlpha(125);
            } else {
                this.setBackgroundAlpha(0);
            }
            this.components.stream().filter(UIComponent::isHovered).forEach(component -> this.setBackgroundAlpha(125));

            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }
}
