/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.membership.Membership;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The memberships window, as 3.5's {@code MembershipGui}: a panel for each membership with its logo, Details, and the ways to take it (in-game
 * money, or the skills total with the Skills plugin), the player's skills total at the bottom. 3.5's Details, purchase and skills
 * confirmations and Congratulations windows are dialogs over it.
 * <p>
 * 3.5's donation buttons (to the web shop) are left out for now, as asked.
 */
final class MembershipScreen implements Screen {

    // 3.5's sizes: the window, a membership's panel, its logo (190 by 165, drawn at its proportions) and the option buttons
    private static final float WIDTH = 310f, HEIGHT = 207f, PANEL_WIDTH = 100f, PANEL_HEIGHT = 162f, LOGO_WIDTH = 99f, LOGO_HEIGHT = 86f;
    private static final float OPTION_WIDTH = 90f, BUTTON_HEIGHT = 16f, SMALL_TEXT = 9f * 0.7f, DETAILS_TEXT = 9f * 0.8f, MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, BLUE = 0xFF5555FF, GRAY = 0xFFAAAAAA;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);

    private final boolean admin;
    private final int skillsTotal;
    private final BigDecimal funds;
    private final int current;
    private @Nullable ComponentFactory factory;

    MembershipScreen(final boolean admin, final int skillsTotal, final String funds, final int current) {
        this.admin = admin;
        this.skillsTotal = skillsTotal;
        this.funds = parse(funds);
        this.current = current;
    }

    @Override
    public Text title() {
        return Text.of("Almura Memberships");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final AnchorPanel content = factory.anchorPanel()
            .add(this.panel(factory, Membership.CITIZEN).setAnchor(Anchor.TOP_LEFT))
            .add(this.panel(factory, Membership.EXPLORER).setAnchor(Anchor.TOP_CENTER))
            .add(this.panel(factory, Membership.PIONEER).setAnchor(Anchor.TOP_RIGHT))
            .add(factory.separator().setAnchor(Anchor.FILL).setMargin(0f, PANEL_HEIGHT + 4f, 0f, 0f)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        // The skills total, with Skills on the server; a negative total means it has none, as 3.5
        if (this.skillsTotal >= 0) {
            content.add(factory.label(Text.of("Current Skills Total: ").color(WHITE).append(Text.of(String.valueOf(this.skillsTotal)).color(BLUE)))
                .setTextScale(0.8f).setAnchor(Anchor.BOTTOM_LEFT).setMargin(6f, 0f, 0f, 4f));
        }
        final Button close = button(factory, Text.translatable("almura.button.close"), 40f, () -> Minecraft.getInstance().gui.screen().onClose());
        content.add(close.setAnchor(Anchor.BOTTOM_RIGHT));
        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * A membership's panel: its logo, Details, "Choose One", and the ways to take it, each enabled when the player can.
     */
    private AnchorPanel panel(final ComponentFactory factory, final Membership membership) {
        final boolean available = membership.availableFrom(this.current);
        final StackPanel column = factory.stackPanel().setSpacing(2f);
        column.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        column.add(centred(factory.image(SpriteId.of(Almura.ID, "membership/" + membership.group())).setImageSize(LOGO_WIDTH, LOGO_HEIGHT)
            .setSize(LOGO_WIDTH, LOGO_HEIGHT)));
        column.add(centred(button(factory, small(Text.of("Details"), DETAILS_TEXT), 40f, () -> this.details(membership))));
        column.add(centred(factory.label(Text.of("Choose One").color(WHITE).underline()).setTextScale(0.8f)).setMargin(0f, 4f, 0f, 0f));
        // 3.5's donation button ("$25.00 Donation" and up, to the web shop) went here; hidden for now
        column.add(centred(button(factory, small(Text.of(membership.priceButton()), SMALL_TEXT), OPTION_WIDTH,
            () -> this.confirmPurchase(membership))
            .setEnabled(available && this.funds.compareTo(BigDecimal.valueOf(membership.fee())) >= 0)));
        if (this.skillsTotal >= 0) {
            column.add(centred(button(factory, small(Text.of("Skills total at " + membership.skillsTotal() + "+"), SMALL_TEXT), OPTION_WIDTH,
                () -> this.confirmSkills(membership)).setEnabled(available && this.skillsTotal >= membership.skillsTotal())));
        }
        final AnchorPanel panel = factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(3f)
            .add(column.setAnchor(Anchor.TOP_CENTER));
        panel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        return panel;
    }

    // ---- Dialogs

    /**
     * 3.5's Details: the worlds a membership unlocks, its features and (Pioneer) its commands.
     */
    private void details(final Membership membership) {
        final ComponentFactory factory = this.factory;
        if (factory == null) {
            return;
        }
        final List<String> worlds = new ArrayList<>(List.of("Cemaria", "Highgarden", "ForestRiver"));
        if (membership.level() >= 2) {
            worlds.addAll(List.of("Atlantis", "Keystone", "Marion"));
        }
        if (membership.level() == 3) {
            worlds.addAll(List.of("Zeal", "Tollana", "Othala"));
        }
        final List<String> features = new ArrayList<>(List.of("Keep Inventory on Death", "Keep Experience on Death", "Ability to Revive",
            "90 day Grief Prevention Leases", "Priority Player Technical Support", "Early access to new content"));
        if (membership.level() >= 2) {
            features.add("Ability to Fly!");
        }
        features.add(switch (membership) {
            case CITIZEN -> "100 item slots on Exchange";
            case EXPLORER -> "500 item slots on Exchange";
            case PIONEER -> "5,000 item slots on Exchange";
        });
        final StackPanel column = column(factory);
        final StackPanel worldRow = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(5f)
            .add(factory.label(Text.of("Unlocks Worlds:").color(WHITE)).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        final StackPanel worldList = factory.stackPanel().setSpacing(1f);
        worlds.forEach(world -> worldList.add(factory.label(Text.of(world).color(GRAY))));
        column.add(worldRow.add(worldList).setMargin(15f, 0f, 0f, 0f));
        column.add(factory.label(Text.of("Features:").color(WHITE)).setMargin(15f, 6f, 0f, 0f));
        features.forEach(feature -> column.add(factory.label(Text.of("- " + feature).color(GRAY)).setMargin(25f, 0f, 0f, 0f)));
        if (membership == Membership.PIONEER) {
            column.add(factory.label(Text.of("Additional Commands:").color(WHITE)).setMargin(15f, 6f, 0f, 0f));
            for (final String command : List.of("/back", "/nickname", "/anvil", "/enchantingtable")) {
                column.add(factory.label(Text.of("- " + command).color(GRAY)).setMargin(25f, 0f, 0f, 0f));
            }
        }
        final Overlay[] overlay = new Overlay[1];
        column.add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(0f, 6f, 0f, 0f));
        column.add(button(factory, Text.translatable("almura.button.close"), 40f, () -> overlay[0].close())
            .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        overlay[0] = factory.overlays().dialog(dialogWindow(factory, membership.displayName(), 250f).add(column));
    }

    /**
     * 3.5's purchase confirmation: the membership and its price, Purchase or Cancel.
     */
    private void confirmPurchase(final Membership membership) {
        this.confirm("Do you wish to purchase:", membership, Text.of("Amount: ").color(WHITE).append(Text.of(membership.price()).color(0xFFFFAA00)),
            "Purchase", () -> ClientMemberships.requestUpgrade(membership.level(), false));
    }

    /**
     * 3.5's skills upgrade confirmation: the membership, Upgrade Me! or Cancel.
     */
    private void confirmSkills(final Membership membership) {
        this.confirm("Do you wish to upgrade to:", membership, null, "Upgrade Me!", () -> ClientMemberships.requestUpgrade(membership.level(), true));
    }

    private void confirm(final String question, final Membership membership, final @Nullable Text detail, final String yes, final Runnable send) {
        final ComponentFactory factory = this.factory;
        if (factory == null) {
            return;
        }
        final Overlay[] overlay = new Overlay[1];
        final StackPanel column = column(factory)
            .add(centred(factory.label(Text.of(question).color(WHITE))))
            .add(centred(factory.label(Text.of(membership.displayName()).color(0xFF55FF55).bold())));
        if (detail != null) {
            column.add(centred(factory.label(detail)));
        }
        column.add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(0f, 4f, 0f, 0f));
        column.add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4f)
            .add(button(factory, Text.of(yes), 70f, () -> {
                send.run();
                overlay[0].close();
            }))
            .add(button(factory, Text.translatable("almura.button.cancel"), 40f, () -> overlay[0].close()))
            .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP));
        overlay[0] = factory.overlays().dialog(dialogWindow(factory, "Confirmation", 150f).add(column));
    }

    /**
     * 3.5's Congratulations, for a membership just bought; its Close closes the window, as the memberships shown are out of date.
     */
    void applied(final String membership) {
        final ComponentFactory factory = this.factory;
        if (factory == null) {
            return;
        }
        final StackPanel column = column(factory)
            .add(centred(factory.label(Text.of("Congratulations!").color(WHITE))))
            .add(centred(factory.label(Text.of(membership).color(0xFF55FF55).bold())))
            .add(centred(factory.label(Text.of("Successfully applied!").color(WHITE))))
            .add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(0f, 4f, 0f, 0f))
            .add(button(factory, Text.translatable("almura.button.close"), 40f, () -> Minecraft.getInstance().gui.screen().onClose())
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        factory.overlays().dialog(dialogWindow(factory, "Almura Memberships", 150f).add(column));
    }

    // ---- Helpers

    private static Window dialogWindow(final ComponentFactory factory, final String title, final float width) {
        return factory.window(Text.of(title))
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .setWidth(width);
    }

    private static StackPanel column(final ComponentFactory factory) {
        final StackPanel column = factory.stackPanel().setSpacing(2f);
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        return column;
    }

    private static Button button(final ComponentFactory factory, final Text text, final float width, final Runnable press) {
        return factory.button(text).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    /**
     * A caption drawn smaller, as 3.5's 0.7 and 0.8 scaled button text.
     */
    private static Text small(final Text text, final float size) {
        return text.style(style -> style.withSize(size));
    }

    private static <C extends Component<C>> C centred(final C component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    private static BigDecimal parse(final String amount) {
        try {
            return new BigDecimal(amount);
        } catch (final NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
