/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.screen.SimpleContainerScreen;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.components.UISimpleList;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.util.builders.FontRenderOptionsBuilder;
import com.almuradev.almura.client.gui.util.builders.UIButtonBuilder;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SimpleAboutMenu extends SimpleContainerScreen {

    private static final String SKIN_URL_BASE = "https://mc-heads.net/avatar/%s/32.png";

    private UISimpleList<AboutListElement> list;
    private UITextField textField;

    public SimpleAboutMenu(@Nullable SimpleScreen parent) {
        super(parent, Text.of("About"));
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    @Override
    public void construct() {
        super.construct();

        this.list = new UISimpleList(this, 125, UIComponent.INHERITED);
        this.list.setPosition(4, 0);
        this.list.setElementSpacing(4);
        this.list.setUnselect(false);
        final List<AboutListElement> elementList = Lists.newArrayList();
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiTexture(GuiConstants.ALMURA_MAN_LOCATION), null), 23, 32, 5, 0, 8,
                Text.of(TextColors.WHITE, "Almura"),
                Text.of(TextColors.WHITE, "Almura was created and is maintained by the AlmuraDev Team (https://www.github.com/AlmuraDev/)",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "The Beginning...", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "Almura 1.0 was conceived on June 1st, 2011. It was built around Spoutcraft (1.7.3 beta). As the technology changed we were",
                        " forced to abandon the Spoutcraft ecosystem in favor of the Forge one. ", TextColors.AQUA, "Zidane", TextColors.RESET,
                        " our lead developer for AlmuraDev finally put his foot down and said enough is enough which began our six ",
                        "month journey to migrate the features we had relied on in Spoutcraft to our new client.", Text.NEW_LINE,
                        Text.NEW_LINE,
                        "Almura Rediscovered was initially conceived on September 4th, 2014.", Text.NEW_LINE,
                        Text.NEW_LINE,
                        "Almura has a number of changes from a typical Minecraft setup that gives it an extremely unique and customizable "
                                + "experience.",
                        Text.NEW_LINE,
                        "  • ASM through Mixin.", Text.NEW_LINE,
                        "  • YAML/JSON content loading system for items, blocks, crops, trees and more.", Text.NEW_LINE,
                        "  • Client/Server security system.", Text.NEW_LINE,
                        "  • Customized GUI for a more unique experience.", Text.NEW_LINE,
                        "  • Information guide system displayed in-game.", Text.NEW_LINE,
                        "  • Player accessory system for hats, wings, capes, earrings and more.")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_ZIDANE + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_ZIDANE.toString()),
                        32, 32), null),
                Text.of(TextColors.BLUE, "Zidane"),
                Text.of(TextColors.WHITE, "Zidane is the biggest driving force behind Almura. Without him this project would simply "
                                + "not be where it stands today.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Lead developer and co-founder of SpongePowered", Text.NEW_LINE,
                        "  • Lead developer of AlmuraDev", Text.NEW_LINE,
                        "  • Former Spoutcraft/Spout developer")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_DOCKTER + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_DOCKTER.toString()),
                        32, 32), null),
                Text.of(TextColors.GOLD, "Dockter"),
                Text.of(TextColors.WHITE, "Dockter is the owner of Almura and AlmuraDev.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Developer, Lead Tester, and Owner for/of Almura", Text.NEW_LINE,
                        "  • Chief Financial Officer for Sponge Foundation", Text.NEW_LINE,
                        "  • Former Spoutcraft developer")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_GRINCH + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_GRINCH.toString()),
                        32, 32), null),
                Text.of(TextColors.DARK_GREEN, "Grinch"),
                Text.of(TextColors.WHITE, "Grinch is a developer for Almura and the designer behind a majority of GUI elements.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Moderator and contributor to Sponge", Text.NEW_LINE,
                        "  • Solder administrator for AlmuraDev", Text.NEW_LINE,
                        "  • Developer for Almura")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_WIFEE + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_WIFEE.toString()),
                        32, 32), null),
                Text.of(TextColors.LIGHT_PURPLE, "Wifee"),
                Text.of(TextColors.WHITE, "Wifee is the designer behind a majority of graphics and models seen in Almura.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Lead Graphics and Model artist for Almura")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_WOLFEYE + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_WOLFEYE.toString()),
                        32, 32), null),
                Text.of(TextColors.RED, "Wolfeye"),
                Text.of(TextColors.WHITE, "Wolfeye has stuck with Almura through both good and bad times. She is by far the most loyal person to "
                                + "Almura.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Lead Moderator", Text.NEW_LINE,
                        "  • Destroyer of Worlds")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_MUMFREY + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_MUMFREY.toString()),
                        32, 32), null),
                Text.of(TextColors.GRAY, "Mumfrey"),
                Text.of(TextColors.WHITE, "Mumfrey is the brains behind the Mixin technology used in Almura.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Developer for Sponge", Text.NEW_LINE,
                        "  • Creator and maintainer of Mixin ASM for Java")));
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiRemoteTexture(
                        GuiConstants.AVATAR_GENERIC_LOCATION,
                        new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + GuiConstants.UNIQUE_ID_BLOOD + ".png"),
                        String.format(SKIN_URL_BASE, GuiConstants.UNIQUE_ID_BLOOD.toString()),
                        32, 32), null),
                Text.of(TextColors.DARK_RED, "Blood"),
                Text.of(TextColors.WHITE, "Acknowledgement for past contributions to Almura such as Cauldron.",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "Titles", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "  • Developer and co-founder of SpongePowered", Text.NEW_LINE,
                        "  • Former owner/developer of Cauldron")));

        final UIButton doneButton = new UIButtonBuilder(this)
                .text(Text.of("Done"))
                .size(98, 20)
                .position(0, -15)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .listener(this)
                .build("button.done");

        this.textField = new UITextField(this, "", true);
        this.textField.setPosition(SimpleScreen.getPaddedX(list, 4), 0);
        this.textField.setEditable(false);
        this.textField.setFontRenderOptions(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_WHITE).shadow(false).build());

        this.list.setElements(elementList);
        this.list.register(this);
        this.list.select(elementList.get(0));

        this.getContainer().add(list, textField);

        final UILabel spongeForgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "SpongeForge: " + ((Optional<String>) Sponge.getPlatform()
                .asMap().get("ImplementationVersion")).orElse("dev"));
        spongeForgeVersionLabel.setPosition(4, -24, Anchor.LEFT | Anchor.BOTTOM);

        final Optional<PluginContainer> optForge = Sponge.getPluginManager().getPlugin("Forge");
        if (optForge.isPresent()) {
            final UILabel forgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "Forge: " + optForge.get().getVersion().orElse("dev"));
            forgeVersionLabel.setPosition(4, -14, Anchor.LEFT | Anchor.BOTTOM);
            addToScreen(forgeVersionLabel);
        }
        final Optional<PluginContainer> optAlmura = Sponge.getPluginManager().getPlugin("almura");
        if (optForge.isPresent()) {
            final UILabel almuraVersionLabel = new UILabel(this, TextFormatting.WHITE + "Almura: " + optAlmura.get().getVersion().orElse("dev"));
            almuraVersionLabel.setPosition(4, -4, Anchor.LEFT | Anchor.BOTTOM);
            addToScreen(almuraVersionLabel);
        }

        addToScreen(doneButton);
        addToScreen(spongeForgeVersionLabel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.setPosition(4, 0);
        this.list.setSize(125, this.getContainer().getHeight());
        this.textField.setSize(this.width - this.list.getWidth() - 12, this.getContainer().getHeight());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.done":
                this.close();
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onElementSelect(UIListContainer.SelectEvent event) {
        if (event.getSelected() instanceof AboutListElement) {
            final AboutListElement element = (AboutListElement) event.getSelected();

            textField.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(element.contentText));
        }
    }

    protected static final class AboutListElement extends UIContainer<AboutListElement> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();

        public final Text contentText;
        private final UIImage image;
        private final UILabel label;

        private AboutListElement(MalisisGui gui, UIComponent parent, UIImage image, Text text) {
            this(gui, parent, image, text, Text.EMPTY);
        }

        private AboutListElement(MalisisGui gui, UIComponent parent, UIImage image, Text text, Text contentText) {
            this(gui, parent, image, 32, 32, 2, 0, 4, text, contentText);
        }

        @SuppressWarnings({"deprecation", "unchecked"})
        private AboutListElement(MalisisGui gui, UIComponent parent, UIImage image, int imageWidth, int imageHeight, int imageX, int imageY, int
                padding, Text text, Text contentText) {
            super(gui);
            this.parent = parent;
            this.image = image;
            this.image.setSize(imageWidth, imageHeight);
            this.image.setPosition(imageX, imageY, Anchor.MIDDLE | Anchor.LEFT);
            this.label = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
            this.label.setPosition(SimpleScreen.getPaddedX(image, padding), 3);
            this.contentText = contentText;
            this.add(this.image, this.label);

            this.setSize(((UIListContainer) this.getParent()).getContentWidth() - 3, image.getHeight() + 6);
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            super.drawBackground(renderer, mouseX, mouseY, partialTick);

            if (this != ((UIListContainer) this.parent).getSelected()) {
                return;
            }

            renderer.enableBlending();
            renderer.disableTextures();
            this.rp.usePerVertexAlpha.set(true);
            this.rp.usePerVertexColor.set(true);

            // Border
            this.shape.resetState();
            this.shape.setSize(((UIListContainer)this.getParent()).getContentWidth() , this.image.getHeight() + 6);
            this.shape.setPosition(0, 0);
            this.shape.getVertexes("TopLeft")
                    .get(0)
                    .setColor(BORDER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("TopRight")
                    .get(0)
                    .setColor(BORDER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("BottomLeft")
                    .get(0)
                    .setColor(BORDER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("BottomRight")
                    .get(0)
                    .setColor(BORDER_COLOR)
                    .setAlpha(255);
            renderer.drawShape(shape, rp);

            // Inner
            this.shape.resetState();
            this.shape.setSize(((UIListContainer)this.getParent()).getContentWidth() - 2, this.image.getHeight() + 4);
            this.shape.setPosition(1, 1);
            this.shape.getVertexes("TopLeft")
                    .get(0)
                    .setColor(INNER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("TopRight")
                    .get(0)
                    .setColor(INNER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("BottomLeft")
                    .get(0)
                    .setColor(INNER_COLOR)
                    .setAlpha(255);
            this.shape.getVertexes("BottomRight")
                    .get(0)
                    .setColor(INNER_COLOR)
                    .setAlpha(255);
            renderer.drawShape(shape, rp);
            renderer.next();

            renderer.enableTextures();
        }
    }
}
