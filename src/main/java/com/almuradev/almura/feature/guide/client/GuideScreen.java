/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.Entry;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundPage;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The Guide, as 3.5's page view: the pages on the left, a server page's name split at " - " into its title and, in grey under it, its
 * subject; the selected page's text on the right; Show Guide on Login under them and Close in the bottom right. Selecting a page asks the
 * server for it. Client only.
 */
final class GuideScreen implements Screen {

    // 3.5's size, smaller on a small screen
    private static final float WIDTH = 582f, HEIGHT = 315f, LIST_WIDTH = 156f, ROW_HEIGHT = 25f, GAP = 4f, BOTTOM = 24f;
    private static final float CLOSE_WIDTH = 50f, BUTTON_HEIGHT = 16f, MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);

    private @Nullable ComponentFactory factory;
    private @Nullable ListView<Entry> list;
    private @Nullable ScrollPanel content;
    private @Nullable String shown;
    // Selecting a page from the server's message, not the player
    private boolean selecting;

    @Override
    public Text title() {
        return Text.of("Guide");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final ListView<Entry> list = factory.<Entry>listView(entry -> row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        list.setWidth(LIST_WIDTH);
        list.onEvent(Selection.SELECTED, event -> list.selected().ifPresent(this::request));
        this.list = list;

        final ScrollPanel content = factory.scrollPanel();
        content.setBackground(BOX).setBorder(BOX_BORDER).setPadding(6f, 4f);
        this.content = content;

        final CheckBox onLogin = factory.checkBox("Show Guide on Login")
            .setState(AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_GUIDE_ON_LOGIN) ? CheckState.CHECKED : CheckState.UNCHECKED);
        onLogin.onEvent(CheckBox.CHANGED, event ->
            AlmuraClientConfig.set(AlmuraClientConfig.DISPLAY_GUIDE_ON_LOGIN, event.value() == CheckState.CHECKED));
        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        final AnchorPanel page = factory.anchorPanel()
            .add(list.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, BOTTOM).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH))
            .add(content.setAnchor(Anchor.FILL).setMargin(LIST_WIDTH + GAP, 0f, 0f, BOTTOM))
            .add(onLogin.setAnchor(Anchor.BOTTOM_CENTER).setMargin(0f, 0f, 0f, 2f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));

        final Minecraft minecraft = Minecraft.getInstance();
        final float width = Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN);
        final float height = Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN);
        final Window window = factory.window(this.title())
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAnchor(Anchor.FILL))
            .setSize(width, height)
            .setAnchor(Anchor.CENTER);
        this.showEntries(ClientGuide.entries(), ClientGuide.wanted());
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * A page in the list: a server page's name split at " - " into its title and its subject under it, in grey.
     */
    private static Component<?> row(final ComponentFactory factory, final Entry entry) {
        final StackPanel lines = factory.stackPanel().setSpacing(1f);
        final int split = entry.name().indexOf(" - ");
        if (split < 0) {
            lines.add(factory.label(MinecraftText.of("§f" + entry.name())));
        } else {
            lines.add(factory.label(MinecraftText.of("§f" + entry.name().substring(0, split))));
            lines.add(factory.label(Text.of(GuideText.stripped(entry.name().substring(split + 3))).color(GRAY)));
        }
        lines.setHitTestVisible(false);
        return factory.anchorPanel().add(lines.setAnchor(Anchor.MIDDLE_LEFT).setMargin(3f, 0f, 0f, 0f)).setAnchor(Anchor.FILL);
    }

    /**
     * Shows the pages, selecting the one to show: the one asked for, else the one shown if it's still there, else the first.
     */
    void showEntries(final List<Entry> entries, final @Nullable String wanted) {
        if (this.list == null) {
            return;
        }
        this.selecting = true;
        try {
            this.list.setItems(entries);
            Entry select = null;
            for (final Entry entry : entries) {
                if (entry.id().equals(wanted) || select == null && entry.id().equals(this.shown)) {
                    select = entry;
                }
            }
            if (select == null && !entries.isEmpty()) {
                select = entries.getFirst();
            }
            if (select != null) {
                this.list.selectItem(select);
                if (!select.id().equals(this.shown) || this.content == null || this.content.children().isEmpty()) {
                    ClientPacketDistributor.sendToServer(new ServerboundPage(select.id()));
                }
            } else {
                this.shown = null;
                this.showText("§7There are no pages for you to read yet.");
            }
        } finally {
            this.selecting = false;
        }
    }

    private void request(final Entry entry) {
        if (!this.selecting && !entry.id().equals(this.shown)) {
            ClientPacketDistributor.sendToServer(new ServerboundPage(entry.id()));
        }
    }

    /**
     * Shows a page the server sent.
     */
    void showPage(final ClientboundPage page) {
        this.shown = page.id();
        this.showText(page.content());
    }

    private void showText(final String text) {
        if (this.content == null || this.factory == null) {
            return;
        }
        this.content.clear();
        this.content.add(GuideText.build(this.factory, text).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        this.content.setScroll(0f, 0f);
    }
}
