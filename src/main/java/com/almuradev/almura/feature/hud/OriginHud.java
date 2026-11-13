/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.Almura;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;

/**
 * Almura's in-game HUD ("Origin"), as on 1.12: the player in the top left (face, name, level, and bars for experience, health, armour,
 * hunger, stamina, air and the mount's health, in place of vanilla's), the compass at the top middle, and where the player is with the
 * frame rate and the server's ticks per second in the top right. Refreshed from the game every frame by {@link Huds}.
 */
final class OriginHud implements Screen {

    // The panels and the experience orb, drawn in the 1.12 HUD's look
    private static final SpriteId PANEL = SpriteId.of(Almura.ID, "hud/panel");
    private static final SpriteId XP_ORB = SpriteId.of(Almura.ID, "hud/xp_orb");
    private static final SpriteId STAMINA = SpriteId.of(Almura.ID, "hud/stamina");
    private static final SpriteId HEART_BACK = SpriteId.of("minecraft", "hud/heart/container");
    private static final SpriteId HEART = SpriteId.of("minecraft", "hud/heart/full");
    private static final SpriteId ARMOR = SpriteId.of("minecraft", "hud/armor_full");
    private static final SpriteId FOOD_BACK = SpriteId.of("minecraft", "hud/food_empty");
    private static final SpriteId FOOD = SpriteId.of("minecraft", "hud/food_full");
    private static final SpriteId AIR = SpriteId.of("minecraft", "hud/air");
    private static final SpriteId MOUNT = SpriteId.of("minecraft", "hud/heart/vehicle_full");

    private static final float USER_WIDTH = 124f, PADDING = 3f, AVATAR = 16f, COMPASS_MIN_WIDTH = 50f, COMPASS_MARGIN = 7f;
    private static final int WHITE = 0xFFFFFFFF, GOLD = 0xFFFFAA00, GRAY = 0xFFAAAAAA, DARK_GRAY = 0xFF555555, GREEN = 0xFF55FF55,
        YELLOW = 0xFFFFFF55, RED = 0xFFFF5555;
    private static final String COMPASS = "S.......W.......N.......E.......";
    private static final DecimalFormat COORDINATE = new DecimalFormat("0.##"), TPS = new DecimalFormat("0.0");


    private @Nullable AnchorPanel userPanel, worldPanel, detailsPanel;
    private @Nullable Image avatar;
    private @Nullable GameProfile avatarProfile;
    private @Nullable Label name, level, compass, coordinates, performance, ping;
    private @Nullable HudBar experience, health, armor, hunger, stamina, air, mount;
    private @Nullable Image clock;

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final float inner = USER_WIDTH - PADDING * 2f;

        // The player: face, name and level, then the bars
        this.avatar = factory.image();
        this.avatar.setImageSize(AVATAR, AVATAR).setSize(AVATAR, AVATAR);
        this.name = factory.label("");
        this.level = factory.label("").setTextScale(0.7f);
        final StackPanel nameAndLevel = factory.stackPanel().add(this.name).add(this.level);
        final StackPanel header = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(3f).add(this.avatar).add(nameAndLevel);

        this.experience = new HudBar(factory, inner, 0xFF009600, null, XP_ORB);
        this.health = new HudBar(factory, inner, 0xFFBB1313, HEART_BACK, HEART);
        this.armor = new HudBar(factory, inner, 0xFF66676D, null, ARMOR);
        this.hunger = new HudBar(factory, inner, 0xFF89592F, FOOD_BACK, FOOD);
        this.stamina = new HudBar(factory, inner, 0xFF0094FF, null, STAMINA);
        this.air = new HudBar(factory, inner, 0xFF0094FF, null, AIR);
        this.mount = new HudBar(factory, inner, 0xFFEF7E4A, null, MOUNT);
        final StackPanel user = factory.stackPanel().setSpacing(1f)
            .add(header.setMargin(0f, 0f, 0f, 2f))
            .add(this.experience.component())
            .add(this.health.component())
            .add(this.armor.component())
            .add(this.hunger.component())
            .add(this.stamina.component())
            .add(this.air.component())
            .add(this.mount.component());
        this.userPanel = panel(factory, user);
        this.userPanel.setWidth(USER_WIDTH);
        this.userPanel.setAnchor(Anchor.TOP_LEFT);

        // The compass, at the top middle
        this.compass = plain(factory.label(""));
        this.worldPanel = panel(factory, this.compass);
        // Further in at the sides than the other panels, as on 1.12
        this.compass.setMargin(PADDING + COMPASS_MARGIN, PADDING, PADDING + COMPASS_MARGIN, PADDING);
        this.worldPanel.setMinSize(COMPASS_MIN_WIDTH, 0f);
        this.worldPanel.setAnchor(Anchor.TOP_CENTER);

        // Where the player is, the frame rate and the server's speed, with the clock, in the top right
        this.coordinates = factory.label("");
        this.performance = factory.label("");
        this.ping = factory.label("");
        final StackPanel lines = factory.stackPanel().add(this.coordinates).add(this.performance).add(this.ping);
        this.clock = factory.image();
        this.clock.setImageSize(16f, 16f).setSize(16f, 16f);
        final StackPanel details = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(6f)
            .add(lines)
            .add(this.clock.setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        this.detailsPanel = panel(factory, details);
        this.detailsPanel.setAnchor(Anchor.TOP_RIGHT);

        return factory.anchorPanel().add(this.userPanel).add(this.worldPanel).add(this.detailsPanel).setAnchor(Anchor.FILL);
    }

    /**
     * A HUD panel: the dark tile 1.12's panels had behind its content.
     */
    private static AnchorPanel panel(final ComponentFactory factory, final Component<?> content) {
        final Image background = factory.image(PANEL).setImageSize(1f, 1f).setScaling(Image.Scaling.STRETCH);
        return factory.anchorPanel()
            .add(background.setAnchor(Anchor.FILL))
            .add(content.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(PADDING));
    }

    private static Label plain(final Label label) {
        return label.setStyle(TextStyle.DEFAULT.withShadow(false));
    }

    /**
     * Shows the game as it is now; called each frame before drawing.
     */
    void refresh() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || this.name == null) {
            return;
        }
        this.refreshUser(player);
        this.refreshCompass(player);
        this.refreshDetails();
    }

    private void refreshUser(final LocalPlayer player) {
        final GameProfile profile = player.getGameProfile();
        if (profile != this.avatarProfile) {
            this.avatarProfile = profile;
            this.avatar.setVisual(MinecraftVisuals.playerFace(profile));
        }
        this.name.setText(Text.of(player.getDisplayName().getString()).color(WHITE));
        this.level.setText(Text.of("Lv. " + player.experienceLevel).color(WHITE));

        final int needed = player.getXpNeededForNextLevel();
        this.experience.set((int) (player.experienceProgress * needed), needed);
        this.health.set(player.getHealth(), player.getMaxHealth());

        // Armour as the durability left in what is worn, as 1.12 showed it
        int most = 0, left = 0;
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            final ItemStack stack = player.getItemBySlot(slot);
            if (stack.isDamageableItem()) {
                most += stack.getMaxDamage();
                left += stack.getMaxDamage() - stack.getDamageValue();
            }
        }
        this.armor.setVisible(most > 0);
        this.armor.set(left, most);

        this.hunger.set(player.getFoodData().getFoodLevel(), 20f);
        // Stamina: the food's saturation, while there is any
        final float saturation = player.getFoodData().getSaturationLevel();
        this.stamina.setVisible(saturation > 0f);
        this.stamina.set(saturation, 20f);

        this.air.setVisible(player.isUnderWater());
        this.air.set(Math.max(player.getAirSupply(), 0), player.getMaxAirSupply());

        final Entity vehicle = player.getVehicle();
        this.mount.setVisible(vehicle instanceof LivingEntity);
        if (vehicle instanceof final LivingEntity living) {
            this.mount.set(living.getHealth(), living.getMaxHealth());
        }
    }

    /**
     * The compass: the 16 points either side of where the player faces, brightest in the middle.
     */
    private void refreshCompass(final LocalPlayer player) {
        final int position = (int) ((((player.getYRot() + 5.25) % 360 + 360) % 360) / 360 * 32);
        Text text = Text.empty();
        for (int offset = -8; offset <= 8; offset++) {
            final int color = offset == 0 ? WHITE : Math.abs(offset) == 1 ? GRAY : DARK_GRAY;
            text = text.append(Text.of(String.valueOf(COMPASS.charAt((position + offset) & 31))).color(color));
        }
        this.compass.setText(text);
    }

    private void refreshDetails() {
        final Entity view = Minecraft.getInstance().getCameraEntity() != null ? Minecraft.getInstance().getCameraEntity() : Minecraft.getInstance().player;
        this.coordinates.setText(Text.concat(
            coordinate("X", view.getX()), Text.of("\n"), coordinate("Y", view.getY()), Text.of("\n"), coordinate("Z", view.getZ())));

        this.performance.setText(Text.concat(
            Text.of("FPS: ").color(GOLD), Text.of(Minecraft.getInstance().getFps() + "  ").color(WHITE), Text.of("TPS: ").color(GOLD), this.tps()));

        // The round trip to the server, playing on one; the server tells each player theirs
        final PlayerInfo info = Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getConnection() == null ? null
            : Minecraft.getInstance().getConnection().getPlayerInfo(Minecraft.getInstance().player.getUUID());
        this.ping.setVisible(info != null);
        if (info != null) {
            final int ping = info.getLatency();
            this.ping.setText(Text.concat(Text.of("Ping: ").color(GOLD),
                Text.of(ping + "ms").color(ping <= 50 ? GREEN : ping <= 150 ? YELLOW : RED)));
        }

        if (this.clock.visual().isEmpty()) {
            this.clock.setVisual(MinecraftVisuals.item(new ItemStack(Items.CLOCK)));
        }
    }

    private static Text coordinate(final String axis, final double value) {
        return Text.concat(Text.of(axis + ": ").color(GOLD), Text.of(COORDINATE.format(value)).color(WHITE));
    }

    /**
     * The server's ticks per second, green when healthy, yellow when slowing, red when lagging. Playing on this computer it's read from the
     * game's own server; from a remote server it needs Almura's server side, so "--" until that is ported.
     */
    private Text tps() {
        final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return Text.of("--").color(GRAY);
        }
        final float millis = server.getCurrentSmoothedTickTime();
        final double tps = millis <= 50f ? 20.0 : 1000.0 / millis;
        return Text.of(TPS.format(tps)).color(tps >= 18.0 ? GREEN : tps >= 15.0 ? YELLOW : RED);
    }

    /**
     * @return How far down the panels at the top middle and top right reach, for what vanilla draws there (boss bars, the player list, effects)
     */
    float worldPanelBottom() {
        return bottom(this.worldPanel);
    }

    float detailsPanelBottom() {
        return bottom(this.detailsPanel);
    }

    private static float bottom(final @Nullable Component<?> panel) {
        if (panel == null || !panel.isVisible()) {
            return 0f;
        }
        final Rect bounds = panel.bounds();
        return bounds.y() + bounds.height();
    }
}
