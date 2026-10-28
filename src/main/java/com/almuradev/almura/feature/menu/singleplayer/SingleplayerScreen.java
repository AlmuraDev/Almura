/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.singleplayer;

import com.almuradev.almura.feature.menu.main.AlmuraLogoHeader;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.screen.PanoramicScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiWorldEdit;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.util.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

/**
 * Almura's world list, in place of vanilla's GuiWorldSelection (MainMenuManager swaps that for this wherever it is opened). Playing goes
 * through Forge's world loader like vanilla's; creating, editing and re-creating use vanilla's screens, which come back here.
 */
@SideOnly(Side.CLIENT)
public final class SingleplayerScreen extends PanoramicScreen {

    private static final Logger LOGGER = LogManager.getLogger("Almura");
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    private static final int CONTENT_WIDTH = 260, GAP = 4, ICON_SIZE = 32, ROW_HEIGHT = ICON_SIZE + 4, ROW_SPACING = 1, LIST_FRAME = 5;
    private static final int SCREEN_MARGIN = 20, MIN_ROWS = 2, MAX_ROWS = 6;

    private final Map<String, ResourceLocation> icons = new HashMap<>();
    private BasicList<WorldSummary> worldList;
    private UILabel emptyLabel;
    private UIButton playButton, editButton, deleteButton, recreateButton;
    // Set when a vanilla screen (create, edit, re-create) is opened from here: the worlds are listed again when it comes back
    private boolean refreshOnReturn;

    public SingleplayerScreen(@Nullable final BasicScreen parent) {
        super(parent);
    }

    @Override
    public void construct() {
        this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);

        // Room for the logo only when at least three worlds still fit under it; otherwise a title in its place
        final int below = 9 + 2 + UIConstants.Button.HEIGHT + 2 + UIConstants.Button.HEIGHT;
        final boolean logo = this.height - SCREEN_MARGIN - AlmuraLogoHeader.HEIGHT - below >= listHeight(3);
        final int headerHeight = logo ? AlmuraLogoHeader.HEIGHT : 12;
        final int rows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, (this.height - SCREEN_MARGIN - headerHeight - below - LIST_FRAME) /
            (ROW_HEIGHT + ROW_SPACING)));

        // Container to cover the screen (fixes issues with stuck button highlighting)
        final BasicContainer<?> screenContainer = new BasicContainer<>(UIComponent.INHERITED, UIComponent.INHERITED);
        screenContainer.setBackgroundAlpha(0);

        final BasicContainer<?> contentContainer = new BasicContainer<>(CONTENT_WIDTH, headerHeight + below + listHeight(rows));
        contentContainer.setPosition(0, logo ? -10 : 0, Anchor.MIDDLE | Anchor.CENTER);
        contentContainer.setBackgroundAlpha(0);
        contentContainer.setClipContent(false); // The logo's figure reaches above it

        final UIComponent<?> header;
        if (logo) {
            header = AlmuraLogoHeader.create(this);
            // A pixel lower than its container's top, to line up with the main menu's logo
            header.setPosition(0, 1, Anchor.TOP | Anchor.CENTER);
        } else {
            header = new UILabel(TextFormatting.WHITE + I18n.format("selectWorld.title"));
            header.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        }

        this.worldList = new BasicList<>(CONTENT_WIDTH, listHeight(rows));
        this.worldList.setPosition(0, BasicScreen.getPaddedY(header, 9), Anchor.TOP | Anchor.CENTER);
        this.worldList.setColor(0x000000);
        this.worldList.setBackgroundAlpha(185);
        this.worldList.setItemComponentSpacing(ROW_SPACING);
        this.worldList.setBorder(0xFFFFFF, 1, 185);
        this.worldList.setPadding(2);
        this.worldList.setItemComponentFactory(WorldItemComponent::new);
        this.worldList.setSelectConsumer(world -> this.updateButtons());

        this.emptyLabel = new UILabel(TextFormatting.GRAY + I18n.format("almura.menu.singleplayer.empty"));
        this.emptyLabel.setPosition(0, this.worldList.getY() + listHeight(rows) / 2 - 4, Anchor.TOP | Anchor.CENTER);

        // Play, create
        final int halfWidth = (CONTENT_WIDTH - GAP) / 2;
        this.playButton = new UIButtonBuilder()
            .text(I18n.format("selectWorld.select"))
            .width(halfWidth)
            .height(UIConstants.Button.HEIGHT)
            .position(0, BasicScreen.getPaddedY(this.worldList, 2))
            .anchor(Anchor.TOP | Anchor.LEFT)
            .onClick(this::play)
            .build("button.play");

        final UIButton createButton = new UIButtonBuilder()
            .text(I18n.format("selectWorld.create"))
            .width(halfWidth)
            .height(UIConstants.Button.HEIGHT)
            .position(0, this.playButton.getY())
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .onClick(this::create)
            .build("button.create");

        // Edit, delete, re-create, back
        final int quarterWidth = (CONTENT_WIDTH - GAP * 3) / 4;
        final int secondRow = BasicScreen.getPaddedY(this.playButton, 2);
        this.editButton = new UIButtonBuilder()
            .text(I18n.format("selectWorld.edit"))
            .width(quarterWidth)
            .height(UIConstants.Button.HEIGHT)
            .position(0, secondRow)
            .anchor(Anchor.TOP | Anchor.LEFT)
            .onClick(this::edit)
            .build("button.edit");

        this.deleteButton = new UIButtonBuilder()
            .text(I18n.format("selectWorld.delete"))
            .fontOptions(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
            .hoverFontOptions(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
            .width(quarterWidth)
            .height(UIConstants.Button.HEIGHT)
            .position(quarterWidth + GAP, secondRow)
            .anchor(Anchor.TOP | Anchor.LEFT)
            .onClick(this::delete)
            .build("button.delete");

        this.recreateButton = new UIButtonBuilder()
            .text(I18n.format("selectWorld.recreate"))
            .width(quarterWidth)
            .height(UIConstants.Button.HEIGHT)
            .position((quarterWidth + GAP) * 2, secondRow)
            .anchor(Anchor.TOP | Anchor.LEFT)
            .onClick(this::recreate)
            .build("button.recreate");

        final UIButton backButton = new UIButtonBuilder()
            .text(I18n.format("gui.back"))
            .width(quarterWidth)
            .height(UIConstants.Button.HEIGHT)
            .position(0, secondRow)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .onClick(this::close)
            .build("button.back");

        contentContainer.add(header, this.worldList, this.emptyLabel, this.playButton, createButton, this.editButton, this.deleteButton,
            this.recreateButton, backButton);
        screenContainer.add(contentContainer);
        this.addToScreen(screenContainer);

        this.refresh(null);
    }

    @Override
    public void updateScreen() {
        if (this.refreshOnReturn) {
            this.refreshOnReturn = false;
            final WorldSummary selected = this.worldList.getSelectedItem();
            this.refresh(selected == null ? null : selected.getFileName());
        }
        super.updateScreen();
    }

    @Override
    public void onClose() {
        this.releaseIcons();
        super.onClose();
    }

    private static int listHeight(final int rows) {
        return rows * (ROW_HEIGHT + ROW_SPACING) - ROW_SPACING + LIST_FRAME;
    }

    /**
     * Lists the worlds again (most recently played first), selecting the one named if it's still there, otherwise the first.
     */
    private void refresh(@Nullable final String selectFileName) {
        List<WorldSummary> worlds;
        try {
            worlds = new ArrayList<>(this.mc.getSaveLoader().getSaveList());
            Collections.sort(worlds);
        } catch (final Exception e) {
            LOGGER.error("Couldn't list the single player worlds", e);
            worlds = new ArrayList<>();
        }

        this.releaseIcons();
        this.worldList.setItems(worlds);
        this.worldList.setSelectedItem(worlds.stream()
            .filter(world -> world.getFileName().equals(selectFileName))
            .findFirst()
            .orElse(worlds.isEmpty() ? null : worlds.get(0)));
        this.emptyLabel.setVisible(worlds.isEmpty());
        this.updateButtons();
    }

    private void updateButtons() {
        final boolean selected = this.worldList.getSelectedItem() != null;
        this.playButton.setEnabled(selected);
        this.editButton.setEnabled(selected);
        this.deleteButton.setEnabled(selected);
        this.recreateButton.setEnabled(selected);
    }

    private void play() {
        final WorldSummary world = this.worldList.getSelectedItem();
        if (world == null) {
            return;
        }
        if (world.askToOpenWorld()) {
            // Last played in a newer version
            new MenuConfirmScreen(this, I18n.format("selectWorld.versionQuestion"), I18n.format("selectWorld.versionWarning", world.getVersionName()),
                I18n.format("selectWorld.versionJoinButton"), true, () -> this.load(world)).display();
        } else {
            this.load(world);
        }
    }

    private void load(final WorldSummary world) {
        if (this.mc.getSaveLoader().canLoadWorld(world.getFileName())) {
            // Forge only wants a world selection screen to come back to if its missing mods prompt is cancelled, which MainMenuManager then
            // swaps for this screen again
            FMLClientHandler.instance().tryLoadExistingWorld(new GuiWorldSelection(null), world);
        }
    }

    private void create() {
        this.refreshOnReturn = true;
        this.mc.displayGuiScreen(new GuiCreateWorld(this));
    }

    private void edit() {
        final WorldSummary world = this.worldList.getSelectedItem();
        if (world != null) {
            this.refreshOnReturn = true;
            this.mc.displayGuiScreen(new GuiWorldEdit(this, world.getFileName()));
        }
    }

    private void recreate() {
        final WorldSummary world = this.worldList.getSelectedItem();
        if (world == null) {
            return;
        }
        final ISaveHandler handler = this.mc.getSaveLoader().getSaveLoader(world.getFileName(), false);
        final WorldInfo info = handler.loadWorldInfo();
        handler.flush();
        if (info != null) {
            final GuiCreateWorld screen = new GuiCreateWorld(this);
            screen.recreateFromExistingWorld(info);
            this.refreshOnReturn = true;
            this.mc.displayGuiScreen(screen);
        }
    }

    private void delete() {
        final WorldSummary world = this.worldList.getSelectedItem();
        if (world == null) {
            return;
        }
        new MenuConfirmScreen(this, I18n.format("selectWorld.deleteQuestion"), "'" + displayName(world) + "' " + I18n.format("selectWorld.deleteWarning"),
            I18n.format("selectWorld.deleteButton"), true, () -> {
                final ISaveFormat saves = this.mc.getSaveLoader();
                saves.flushCache();
                saves.deleteWorldDirectory(world.getFileName());
                this.refresh(null);
                this.display();
            }).display();
    }

    private static String displayName(final WorldSummary world) {
        return StringUtils.isEmpty(world.getDisplayName()) ? world.getFileName() : world.getDisplayName();
    }

    /**
     * Gets the world's icon (its icon.png, loaded once per listing), or vanilla's unknown icon.
     */
    private ResourceLocation icon(final WorldSummary world) {
        return this.icons.computeIfAbsent(world.getFileName(), fileName -> {
            final File file = this.mc.getSaveLoader().getFile(fileName, "icon.png");
            if (file == null || !file.isFile()) {
                return ICON_MISSING;
            }
            try {
                final BufferedImage image = ImageIO.read(file);
                if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
                    return ICON_MISSING; // vanilla only accepts 64x64 icons
                }
                final DynamicTexture texture = new DynamicTexture(image.getWidth(), image.getHeight());
                final ResourceLocation location = new ResourceLocation("almura", "worlds/" + fileName.toLowerCase() + "/icon");
                this.mc.getTextureManager().loadTexture(location, texture);
                image.getRGB(0, 0, image.getWidth(), image.getHeight(), texture.getTextureData(), 0, image.getWidth());
                texture.updateDynamicTexture();
                return location;
            } catch (final Exception e) {
                LOGGER.error("Invalid icon for world {}", fileName, e);
                return ICON_MISSING;
            }
        });
    }

    private void releaseIcons() {
        this.icons.values().stream().filter(location -> location != ICON_MISSING).forEach(this.mc.getTextureManager()::deleteTexture);
        this.icons.clear();
    }

    private static final class WorldItemComponent extends BasicList.ItemComponent<WorldSummary> {

        WorldItemComponent(final MalisisGui gui, final BasicList<WorldSummary> parent, final WorldSummary item) {
            super(gui, parent, item);
            this.setOnDoubleClickConsumer(world -> ((SingleplayerScreen) gui).play());
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setHeight(ROW_HEIGHT);

            final UIWorldIcon icon = new UIWorldIcon(gui, ((SingleplayerScreen) gui).icon(this.item), ICON_SIZE);
            icon.setPosition(1, 0, Anchor.LEFT | Anchor.MIDDLE);

            final int textX = ICON_SIZE + 5;
            final UILabel nameLabel = new UILabel(TextFormatting.WHITE + displayName(this.item));
            nameLabel.setPosition(textX, 1);

            final UILabel fileLabel = new UILabel(TextFormatting.GRAY + this.item.getFileName() + " (" +
                DATE_FORMAT.format(new Date(this.item.getLastTimePlayed())) + ")");
            fileLabel.setPosition(textX, 12);

            final UILabel infoLabel = new UILabel(TextFormatting.GRAY + info(this.item));
            infoLabel.setPosition(textX, 23);

            this.add(icon, nameLabel, fileLabel, infoLabel);
        }

        /**
         * The game mode, cheats and version line, as vanilla's world list shows it.
         */
        private static String info(final WorldSummary world) {
            if (world.requiresConversion()) {
                return I18n.format("selectWorld.conversion");
            }
            String info = world.isHardcoreModeEnabled()
                ? TextFormatting.DARK_RED + I18n.format("gameMode.hardcore") + TextFormatting.GRAY
                : I18n.format("gameMode." + world.getEnumGameType().getName());
            if (world.getCheatsEnabled()) {
                info += ", " + I18n.format("selectWorld.cheats");
            }
            final String version = world.getVersionName();
            if (world.markVersionInList()) {
                final TextFormatting style = world.askToOpenWorld() ? TextFormatting.RED : TextFormatting.ITALIC;
                info += ", " + I18n.format("selectWorld.version") + " " + style + version + TextFormatting.RESET + TextFormatting.GRAY;
            } else {
                info += ", " + I18n.format("selectWorld.version") + " " + version;
            }
            return info;
        }
    }
}
