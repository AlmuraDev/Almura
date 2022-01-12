/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.texture.GuiRemoteTexture;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicContainerScreen;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;
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
public class SimpleAboutMenu extends BasicContainerScreen {

    private BasicList<AboutItemData> list;
    private UITextField textField;

    public SimpleAboutMenu(@Nullable final BasicScreen parent) {
        super(parent, I18n.format("almura.menu_button.about"));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        super.construct();

        this.list = new BasicList<>(this, 125, UIComponent.INHERITED);
        this.list.setPosition(4, 0);
        this.list.setItemComponentSpacing(4);
        this.list.setCanDeselect(false);

        final List<AboutItemData> elementDataList = Lists.newArrayList();

        // Static entry
        elementDataList.add(new AboutItemData(this.list,
                new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_MAN), null), 5, 0, 23, 32, 8, AboutConfig.TITLE, AboutConfig.STORY));

        // Dynamic entry
        AboutConfig.ENTRIES.forEach(entry -> {
            Text titles = Text.EMPTY;
            for (final String title : entry.titles) {
                titles = titles.toBuilder().append(Text.of("  • ", I18n.format(title)), Text.NEW_LINE).build();
            }
            elementDataList.add(new AboutItemData(this.list,
                    new UIImage(this, new GuiRemoteTexture(
                            GuiConfig.Location.GENERIC_AVATAR,
                            new ResourceLocation(Almura.ID, "textures/gui/skins/avatars/" + entry.uniqueId + ".png"),
                            String.format(GuiConfig.Url.SKINS, entry.uniqueId, 32),
                            32,
                            32), null), 2, 0, 32, 32, 4,
                    Text.of(entry.color, I18n.format(entry.name)),
                    Text.of(TextColors.WHITE, I18n.format(entry.description), Text.NEW_LINE, Text.NEW_LINE, TextStyles.BOLD,
                            I18n.format("almura.menu.about.titles"), TextStyles.RESET, TextColors.RESET, Text.NEW_LINE, titles))
            );
        });

        final UIButton doneButton = new UIButtonBuilder(this)
                .text(I18n.format("gui.done"))
                .size(98, 20)
                .position(0, -15, 1)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .listener(this)
                .build("button.done");

        this.textField = new UITextField(this, "", true);
        this.textField.setPosition(BasicScreen.getPaddedX(this.list, 4), 0);
        this.textField.setEditable(false);
        this.textField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        this.list.setItemComponentFactory(AboutItemComponent::new);
        this.list.setItems(elementDataList);
        this.list.setSelectConsumer(i -> this.textField.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(i.getContent())));
        this.list.setSelectedItem(elementDataList.get(0));

        this.getContainer().add(this.list, this.textField);

        // SpongeForge
        final UILabel spongeForgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "SpongeForge: " + ((Optional<String>) Sponge.getPlatform()
                .asMap().get("ImplementationVersion")).orElse("dev"));
        spongeForgeVersionLabel.setPosition(4, -24, Anchor.LEFT | Anchor.BOTTOM);

        // Forge
        final PluginContainer forgeContainer = Sponge.getPluginManager().getPlugin("Forge").orElseThrow(NullPointerException::new);
        final UILabel forgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "Forge: " + forgeContainer.getVersion().orElse("dev"));
        forgeVersionLabel.setPosition(4, -14, Anchor.LEFT | Anchor.BOTTOM);
        addToScreen(forgeVersionLabel);

        // Almura
        final PluginContainer almuraContainer = Sponge.getPluginManager().getPlugin("almura").orElseThrow(NullPointerException::new);
        final UILabel almuraVersionLabel = new UILabel(this, TextFormatting.WHITE + "Almura 3.1 - " + Almura.buildNumber);
        almuraVersionLabel.setPosition(4, -4, Anchor.LEFT | Anchor.BOTTOM);
        addToScreen(almuraVersionLabel);

        addToScreen(doneButton);
        addToScreen(spongeForgeVersionLabel);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.list.setPosition(4, 0);
        this.list.setSize(125, this.getContainer().getHeight());
        this.textField.setSize(this.width - this.list.getWidth() - 12, this.getContainer().getHeight());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Subscribe
    public void onButtonClick(final UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.done":
                this.close();
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class AboutItemComponent extends BasicList.ItemComponent<AboutItemData> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();

        private final AboutItemData item;
        private final UILabel label;

        public AboutItemComponent(final MalisisGui gui, final BasicList<AboutItemData> parent, final AboutItemData item) {
            super(gui, parent, item);

            this.item = item;

            this.parent = parent;

            this.label = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.item.getTitle()));
            this.label.setPosition(BasicScreen.getPaddedX(this.item.getImage(), this.item.getPadding()), 2);

            // Add image/label
            this.add(this.item.getImage(), this.label);

            // Set height
            this.setHeight(38);

            // Set padding
            this.setPadding(1, 1);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean onClick(final int x, final int y) {
            final UIComponent component = getComponentAt(x, y);
            if (this.label.equals(component) || this.item.getImage().equals(component) || this.equals(component)) {
                ((BasicList<AboutItemData>) this.parent).setSelectedItem(this.item);
            }
            return true;
        }

        @Override
        public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            if (this.parent instanceof BasicList) {
                final BasicList parent = (BasicList) this.parent;

                final int width = parent.getContentWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 1 : 0);

                setSize(width, getHeight());

                if (this.item == parent.getSelectedItem()) {
                    super.drawBackground(renderer, mouseX, mouseY, partialTick);
                }
            }
        }
    }

    private static final class AboutItemData {

        private final BasicList<AboutItemData> parent;
        private final UIImage image;
        private final Text title;
        private final Text content;
        private final int padding;

        private AboutItemData(final BasicList<AboutItemData> parent, final UIImage image, final int imageX, final int imageY,
                final int imageWidth, final int imageHeight, final int padding, final Text title, final Text content) {
            this.parent = parent;
            this.image = image;
            this.image.setPosition(imageX, imageY, Anchor.MIDDLE | Anchor.LEFT);
            this.image.setSize(imageWidth, imageHeight);

            this.padding = padding;

            this.title = title;
            this.content = content;
        }

        public BasicList<AboutItemData> getParent() {
            return this.parent;
        }

        public UIImage getImage() {
            return this.image;
        }

        public Text getTitle() {
            return this.title;
        }

        public Text getContent() {
            return this.content;
        }

        public int getPadding() {
            return this.padding;
        }
    }
}
