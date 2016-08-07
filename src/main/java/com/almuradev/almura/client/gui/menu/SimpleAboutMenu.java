/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIVanillaContainer;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.RenderParameters;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

@SideOnly(Side.CLIENT)
public class SimpleAboutMenu extends SimpleGui {

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

    private UIVanillaContainer container;
    private AboutList list;

    public SimpleAboutMenu(SimpleGui parent) {
        super(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        this.container = new UIVanillaContainer(this, Text.of("About"));

        this.list = new AboutList(this, 150, UIComponent.INHERITED);
        final AboutListElement element = new AboutListElement(SimpleGui.ZIDANE_HEAD_LOCATION, Text.of(TextColors.LIGHT_PURPLE, "Zidane"));
        this.list.setElementSpacing(4);
        this.list.addElement(element);
        this.list.addElement(element);
        this.list.addElement(element);
        this.list.addElement(element);

        this.container.add(list);

        addToScreen(this.container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.setSize(150, UIComponent.INHERITED);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
    }

    protected static final class AboutList extends UIListContainer<AboutList, AboutListElement> {

        private static final int padding = 4;
        private static final int textureHeight = 32;
        private static final int textureWidth = 32;
        private static final int elementHeight = textureHeight + (padding * 2);

        public AboutList(MalisisGui gui, int width, int height) {
            super(gui, width, height);

            shape = new SimpleGuiShape();
            shape.setSize(textureWidth, textureHeight);
            shape.setPosition(padding, padding);
            shape.storeState();
        }

        public void addElement(AboutListElement element) {
            if (elements == null) {
                elements = Lists.newArrayList();
            }
            this.elements.add(element);
        }

        @Override
        public int getElementHeight(AboutListElement element) {
            return elementHeight;
        }

        @Override
        public void drawElementBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, AboutListElement element,
                boolean isHovered) {
        }

        @Override
        public void drawElementForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, AboutListElement element,
                boolean isHovered) {
            renderer.bindTexture(element.texture);
            this.shape.resetState();
            final RenderParameters params = new RenderParameters();
            renderer.drawShape(this.shape, params);
            renderer.drawText(element.text + " X: " + this.x + " Y: " + this.y, element.texture.getWidth() + padding * 2, padding, 0);
        }
    }

    protected static final class AboutListElement {

        public final GuiTexture texture;
        public final String text;

        private AboutListElement(ResourceLocation location, Text text) {
            this(new GuiTexture(location, 32, 32), text);
        }

        @SuppressWarnings("deprecation")
        private AboutListElement(GuiTexture texture, Text text) {
            this.texture = texture;
            this.text = TextSerializers.LEGACY_FORMATTING_CODE.serialize(text);
        }
    }
}
