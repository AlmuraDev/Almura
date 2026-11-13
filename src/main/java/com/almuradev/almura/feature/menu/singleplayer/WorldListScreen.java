/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.singleplayer;

import com.almuradev.almura.feature.menu.AlmuraLogoHeader;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextOverflow;
import org.inspirenxe.strata.api.visual.PixelBuffer;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

/**
 * Almura's single-player world list, in place of vanilla's, as on 1.12: the logo when there is room (a title otherwise), the worlds most
 * recently played first with their icon, name, folder and date, and mode and version, then Play and Create New World, and Edit, Delete (red),
 * Re-Create and Back. Playing, editing, deleting and re-creating are vanilla's own, with its warnings and backups.
 */
public final class WorldListScreen implements Screen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ICON_MISSING = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");

    // 1.12's sizes
    private static final float CONTENT_WIDTH = 260f, GAP = 4f, ICON = 32f, ROW_HEIGHT = ICON + 4f, LIST_FRAME = 5f, BUTTON_HEIGHT = 20f;
    private static final float SCREEN_MARGIN = 20f, HEADER_LOGO = 139f, HEADER_TITLE = 12f, HEADER_GAP = 9f, BUTTON_GAP = 2f;
    private static final int MIN_ROWS = 2, MAX_ROWS = 6;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFF808080, QUIT_RED = 0xFFFF5555;

    private final Map<String, PixelBuffer> icons = new HashMap<>();
    // Vanilla's entries made to play, edit, delete or re-create a world; closed with the screen (they hold its icon)
    private final List<AutoCloseable> entries = new ArrayList<>();
    private @Nullable PixelBuffer missingIcon;
    private @Nullable ListView<LevelSummary> list;
    private @Nullable Label empty;
    private @Nullable Button play, edit, delete, recreate;

    @Override
    public Text title() {
        return Text.translatable("selectWorld.title");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Minecraft minecraft = Minecraft.getInstance();
        // Room for the logo only when at least three worlds still fit under it; a title in its place otherwise
        final float height = minecraft.getWindow().getGuiScaledHeight();
        final float below = HEADER_GAP + BUTTON_GAP + BUTTON_HEIGHT + BUTTON_GAP + BUTTON_HEIGHT;
        final boolean logo = height - SCREEN_MARGIN - HEADER_LOGO - below >= listHeight(3);
        final float header = logo ? HEADER_LOGO : HEADER_TITLE;
        final int rows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, (int) ((height - SCREEN_MARGIN - header - below - LIST_FRAME) / ROW_HEIGHT)));

        this.list = factory.<LevelSummary>listView(world -> this.row(factory, world));
        this.list.setRowHeight(ROW_HEIGHT);
        this.list.setSize(CONTENT_WIDTH, listHeight(rows));
        this.list.setActivateOnClick(false);
        this.list.onEvent(Selection.SELECTED, event -> this.updateButtons());
        // Double-click or Enter on a world plays it
        this.list.onEvent(Activatable.ACTIVATED, event -> this.play());

        this.empty = factory.label(Text.translatable("almura.menu.singleplayer.empty").color(GRAY));
        final AnchorPanel listArea = factory.anchorPanel()
            .add(this.list.setAnchor(Anchor.FILL))
            .add(this.empty.setAnchor(Anchor.CENTER));
        listArea.setSize(CONTENT_WIDTH, listHeight(rows));

        final float half = (CONTENT_WIDTH - GAP) / 2f, quarter = (CONTENT_WIDTH - GAP * 3f) / 4f;
        this.play = factory.button(Text.translatable("selectWorld.select")).setSize(half, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.play());
        final Button create = factory.button(Text.translatable("selectWorld.create")).setSize(half, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> CreateWorldScreen.openFresh(minecraft, () -> minecraft.gui.setScreen(this.host())));
        this.edit = factory.button(Text.translatable("selectWorld.edit")).setSize(quarter, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.withEntry(WorldSelectionList.WorldListEntry::editWorld));
        this.delete = factory.button(Text.translatable("selectWorld.delete").color(QUIT_RED)).setSize(quarter, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.withEntry(WorldSelectionList.WorldListEntry::deleteWorld));
        this.recreate = factory.button(Text.translatable("selectWorld.recreate")).setSize(quarter, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.withEntry(WorldSelectionList.WorldListEntry::recreateWorld));
        final Button back = factory.button(Text.translatable("gui.back")).setSize(quarter, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> minecraft.gui.screen().onClose());

        final StackPanel firstRow = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(GAP).add(this.play).add(create);
        final StackPanel secondRow = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(GAP)
            .add(this.edit).add(this.delete).add(this.recreate).add(back);

        final Component<?> top = logo ? AlmuraLogoHeader.create(factory) : factory.label(this.title().color(WHITE));
        final StackPanel column = factory.stackPanel()
            .add(top.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(centred(listArea).setMargin(0f, HEADER_GAP, 0f, 0f))
            .add(centred(firstRow).setMargin(0f, BUTTON_GAP, 0f, 0f))
            .add(centred(secondRow).setMargin(0f, BUTTON_GAP, 0f, 0f));
        column.setAnchor(Anchor.CENTER).setMargin(0f, logo ? -20f : 0f, 0f, 0f);

        this.refresh();
        return factory.anchorPanel().add(column).setAnchor(Anchor.FILL);
    }

    @Override
    public void closed() {
        for (final AutoCloseable entry : this.entries) {
            try {
                entry.close();
            } catch (final Exception ignored) {
                // Only its icon
            }
        }
        this.entries.clear();
    }

    private static float listHeight(final int rows) {
        return rows * ROW_HEIGHT + LIST_FRAME;
    }

    /**
     * Lists the worlds (most recently played first), off the client thread as vanilla does, keeping the selection or else selecting the first.
     */
    private void refresh() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LevelStorageSource source = minecraft.getLevelSource();
        this.updateButtons();
        try {
            source.loadLevelSummaries(source.findLevelCandidates()).whenComplete((worlds, error) -> minecraft.execute(() -> {
                if (error != null) {
                    LOGGER.error("Couldn't list the single player worlds", error);
                }
                this.show(error == null ? worlds : List.of());
            }));
        } catch (final Exception e) {
            LOGGER.error("Couldn't list the single player worlds", e);
            this.show(List.of());
        }
    }

    private void show(final List<LevelSummary> found) {
        if (this.list == null || this.empty == null) {
            return;
        }
        final List<LevelSummary> worlds = new ArrayList<>(found);
        worlds.sort(null);
        final String selected = this.list.selected().map(LevelSummary::getLevelId).orElse(null);
        this.list.setItems(worlds);
        worlds.stream().filter(world -> world.getLevelId().equals(selected)).findFirst()
            .or(() -> worlds.stream().findFirst())
            .ifPresent(this.list::selectItem);
        this.empty.setVisible(worlds.isEmpty());
        this.updateButtons();
    }

    private void updateButtons() {
        final Optional<LevelSummary> world = this.list == null ? Optional.empty() : this.list.selected();
        if (this.play == null) {
            return;
        }
        // "Play Selected World", or vanilla's own words when it must be upgraded first
        this.play.setText(world.map(summary -> MinecraftText.of(summary.primaryActionMessage())).orElse(Text.translatable("selectWorld.select")));
        this.play.setEnabled(world.map(LevelSummary::primaryActionActive).orElse(false));
        this.edit.setEnabled(world.map(LevelSummary::canEdit).orElse(false));
        this.delete.setEnabled(world.map(LevelSummary::canDelete).orElse(false));
        this.recreate.setEnabled(world.map(LevelSummary::canRecreate).orElse(false));
    }

    private void play() {
        this.withEntry(WorldSelectionList.WorldListEntry::joinWorld);
    }

    /**
     * Does something to the selected world through vanilla's own entry for it, which comes back to this screen when done.
     */
    private void withEntry(final Consumer<WorldSelectionList.WorldListEntry> action) {
        final LevelSummary world = this.list == null ? null : this.list.selected().orElse(null);
        if (world == null) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        // Vanilla's list, never shown: what its entries go back to is this screen as vanilla sees it
        final WorldSelectionList vanilla = new WorldSelectionList.Builder(minecraft, this.host()).width(1).height(1).build();
        vanilla.clearEntries();
        final WorldSelectionList.WorldListEntry entry = vanilla.new WorldListEntry(vanilla, world);
        this.entries.add(entry);
        action.accept(entry);
    }

    private net.minecraft.client.gui.screens.Screen host() {
        return Minecraft.getInstance().gui.screen();
    }

    /**
     * A world's row: its icon, then its name, folder and date, and mode and version, as vanilla shows them.
     */
    private Component<?> row(final ComponentFactory factory, final LevelSummary world) {
        final Image icon = factory.image();
        icon.setPixels(this.icon(world));
        icon.setImageSize(ICON, ICON).setSize(ICON, ICON);

        String folder = world.getLevelId();
        if (world.getLastPlayed() != -1L) {
            folder += " (" + WorldSelectionList.DATE_FORMAT.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(world.getLastPlayed()),
                ZoneId.systemDefault())) + ")";
        }
        final StackPanel text = factory.stackPanel().setSpacing(2f)
            .add(line(factory, Text.of(world.getLevelName()).color(WHITE)))
            .add(line(factory, Text.of(folder).color(GRAY)))
            .add(line(factory, MinecraftText.of(world.getInfo()).color(GRAY)));
        // Across the rest of the row, each line cut short with "..." when too long, as vanilla's are
        final AnchorPanel row = factory.anchorPanel()
            .add(icon.setAnchor(Anchor.MIDDLE_LEFT).setMargin(1f, 0f, 0f, 0f))
            .add(text.setAnchor(Anchor.FILL).setMargin(ICON + 5f, 2f, 2f, 0f));
        // A press anywhere on the row selects it
        icon.setHitTestVisible(false);
        text.setHitTestVisible(false);
        return row.setAnchor(Anchor.FILL);
    }

    private static Label line(final ComponentFactory factory, final Text text) {
        return factory.label(text).setOverflow(TextOverflow.ELLIPSIS).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    /**
     * The world's icon.png (64 by 64, read once), or vanilla's unknown icon.
     */
    private PixelBuffer icon(final LevelSummary world) {
        return this.icons.computeIfAbsent(world.getLevelId(), id -> {
            final Path file = world.getIcon();
            if (file != null && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                try (InputStream in = Files.newInputStream(file)) {
                    final PixelBuffer pixels = pixels(ImageIO.read(in));
                    if (pixels != null) {
                        return pixels;
                    }
                } catch (final IOException e) {
                    LOGGER.error("Invalid icon for world {}", id, e);
                }
            }
            return this.missingIcon();
        });
    }

    private PixelBuffer missingIcon() {
        if (this.missingIcon == null) {
            PixelBuffer pixels = null;
            final Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(ICON_MISSING);
            if (resource.isPresent()) {
                try (InputStream in = resource.get().open()) {
                    pixels = pixels(ImageIO.read(in));
                } catch (final IOException e) {
                    LOGGER.error("Couldn't read the unknown world icon", e);
                }
            }
            this.missingIcon = pixels != null ? pixels : PixelBuffer.of(1, 1);
        }
        return this.missingIcon;
    }

    // Vanilla only takes 64 by 64 icons
    private static @Nullable PixelBuffer pixels(final @Nullable BufferedImage image) {
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            return null;
        }
        final PixelBuffer pixels = PixelBuffer.of(64, 64);
        image.getRGB(0, 0, 64, 64, pixels.array(), 0, 64);
        return pixels.changed();
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
