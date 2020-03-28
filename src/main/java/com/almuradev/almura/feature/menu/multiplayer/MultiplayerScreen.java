/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.multiplayer;

import com.almuradev.almura.feature.menu.main.ConnectingGui;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.screen.PanoramicScreen;
import com.almuradev.almura.shared.util.Query;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.UIConstants;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SideOnly(Side.CLIENT)
public class MultiplayerScreen extends PanoramicScreen {

  private static final List<ServerEntry> serverEntries = new ArrayList<>();

  static {
    serverEntries.add(new ServerEntry(I18n.format("almura.menu.server.live.name"), "srv1.almuramc.com", 25566));
    serverEntries.add(new ServerEntry(I18n.format("almura.menu.server.dev.name"), "dev.almuramc.com", 25566));
  }

  private final Timer queryTimer = new Timer();
  private boolean timerRunning = false;
  private BasicList<ServerEntry> serverList;
  private UIButton joinButton;

  public MultiplayerScreen(final BasicScreen parent) {
    super(parent);
  }

  @Override
  public void construct() {
    this.renderer.setDefaultTexture(GuiConfig.SpriteSheet.ALMURA);

    // Container to cover the screen (fixes issues with stuck button highlighting)
    final BasicContainer<?> screenContainer = new BasicContainer(this);
    screenContainer.setBackgroundAlpha(0);

    // Primary content
    final BasicContainer<?> contentContainer = new BasicContainer(this, UIConstants.Button.WIDTH_LONG, 206);
    contentContainer.setPosition(0, -10, Anchor.MIDDLE | Anchor.CENTER);
    contentContainer.setBackgroundAlpha(0);

    // Create logo
    final UIImage logoImage = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
    logoImage.setSize(60, 99);
    logoImage.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

    // Container for server entries
    this.serverList = new BasicList<>(this, UIConstants.Button.WIDTH_LONG - 2, 53);
    this.serverList.setPosition(0, BasicScreen.getPaddedY(logoImage, 10), Anchor.TOP | Anchor.CENTER);
    this.serverList.setColor(0x000000);
    this.serverList.setBackgroundAlpha(185);
    this.serverList.setItemComponentSpacing(1);
    this.serverList.setBorder(0xFFFFFF, 1, 185);
    this.serverList.setPadding(2);
    this.serverList.setItemComponentFactory(ServerEntryItemComponent::new);
    this.serverList.setSelectConsumer(i -> this.updateButtons());
    this.serverList.setItems(serverEntries);

    // Join button
    this.joinButton = new UIButtonBuilder(this)
      .text(I18n.format("almura.menu_button.join"))
      .enabled(false)
      .width(UIConstants.Button.WIDTH_LONG)
      .height(UIConstants.Button.HEIGHT)
      .position(0, BasicScreen.getPaddedY(this.serverList, 2))
      .anchor(Anchor.TOP | Anchor.LEFT)
      .onClick(this::join)
      .build("button.join");

    // Back button
    final UIButton backButton = new UIButtonBuilder(this)
      .text(I18n.format("gui.back"))
      .width(UIConstants.Button.WIDTH_SHORT)
      .height(UIConstants.Button.HEIGHT)
      .position(0, BasicScreen.getPaddedY(this.joinButton, 2))
      .anchor(Anchor.TOP | Anchor.LEFT)
      .onClick(this::close)
      .build("button.back");

    // Other button
    final UIButton otherButton = new UIButtonBuilder(this)
      .text(I18n.format("almura.menu_button.other"))
      .width(UIConstants.Button.WIDTH_SHORT)
      .height(UIConstants.Button.HEIGHT)
      .position(0, BasicScreen.getPaddedY(this.joinButton, 2))
      .anchor(Anchor.TOP | Anchor.RIGHT)
      .onClick(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(this)))
      .build("button.other");

    // Add everything to the bowl
    contentContainer.add(logoImage, this.serverList, this.joinButton, backButton, otherButton);
    screenContainer.add(contentContainer);

    // Bake that cake
    this.addToScreen(screenContainer);

    // Select the first entry
    this.serverList.setSelectedItem(serverEntries.stream().findFirst().orElse(null));
  }

  public void startTimer() {
    if (!timerRunning) {
      this.queryTimer.schedule(new TimerTask() {
        @Override public void run() {
          queryServers();
          serverList.getComponents().stream().filter(i -> i instanceof ServerEntryItemComponent).forEach(i -> ((ServerEntryItemComponent) i).update());
          updateButtons();
          timerRunning = true;
        }
      }, 0L, 3000L);
    }
  }

  @Override
  public void onClose() {
    // Cancel the timer
    timerRunning = false;
    this.queryTimer.cancel();

    // Proceed with closing
    super.onClose();
  }

  @Override
  public void display() {
    super.display();
    this.startTimer();
  }

  private void updateButtons() {
    final ServerEntry entry = this.serverList.getSelectedItem();
    this.joinButton.setEnabled(entry != null && entry.status == ServerStatus.ONLINE);
  }

  private void join() {
    // If we don't have a selected server or the server isn't online then don't continue
    final ServerEntry entry = this.serverList.getSelectedItem();
    if (entry == null || entry.status != ServerStatus.ONLINE) {
      return;
    }

    FMLClientHandler.instance().setupServerList();
    //FMLClientHandler.instance().connectToServer(this, new ServerData(entry.name, entry.address + ":" + entry.port, false));
    new ConnectingGui(this, new ServerData(entry.name, entry.address + ":" + entry.port, false)).display();
  }

  private void queryServers() {
    serverEntries.forEach(e -> {
      // Determine the online status
      e.status = e.query.pingServer() ? ServerStatus.ONLINE : ServerStatus.OFFLINE;

      if (e.status == ServerStatus.ONLINE) {
        // Query the server
        e.query.sendQuery();

        // Determine if it is in the process of restarting
        if (e.query.getMaxPlayers().equalsIgnoreCase("-1")) {
          e.status = ServerStatus.RESTARTING;
          e.players = 0;
          e.maxPlayers = 0;
        } else {
          // Update player counts
          e.players = Integer.valueOf(e.query.getPlayers());
          e.maxPlayers = Integer.valueOf(e.query.getMaxPlayers());
          e.motd = e.query.getMotd();
        }
      }
    });
  }

  private static class ServerEntry {

    private final String name;
    private final String address;
    private final int port;
    private final Query query;
    private String motd;
    private int players;
    private int maxPlayers;
    private ServerStatus status = ServerStatus.UPDATING;

    ServerEntry(final String name, final String address, final int port) {
      this.name = name;
      this.address = address;
      this.port = port;
      this.query = new Query(address, port);
    }
  }

  private static class ServerEntryItemComponent extends BasicList.ItemComponent<ServerEntry> {

    private UIExpandingLabel itemLabel;
    private UILabel serverStatusLabel;
    private BasicContainer<?> statusIndicatorContainer;

    ServerEntryItemComponent(final MalisisGui gui, final BasicList<ServerEntry> parent, final ServerEntry item) {
      super(gui, parent, item);

      this.setOnDoubleClickConsumer(itemStack -> {
        ((MultiplayerScreen) gui).join();
      });
    }

    @Override
    protected void construct(final MalisisGui gui) {
      this.setHeight(24);

      this.statusIndicatorContainer = new BasicContainer<>(gui, 5, this.height - (this.getLeftBorderSize() + this.getRightBorderSize()));
      this.statusIndicatorContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);

      // Add components
      this.itemLabel = new UIExpandingLabel(gui, TextFormatting.WHITE + this.item.name);
      this.itemLabel.setPosition(4, 0, Anchor.LEFT | Anchor.MIDDLE);

      this.serverStatusLabel = new UILabel(gui, TextFormatting.WHITE + "??/??");
      this.serverStatusLabel.setPosition(BasicScreen.getPaddedX(this.statusIndicatorContainer, 4, Anchor.RIGHT), 0, Anchor.RIGHT | Anchor.MIDDLE);

      this.add(this.itemLabel, this.serverStatusLabel, this.statusIndicatorContainer);

      this.update();
    }

    public void update() {
      // Update status indicator
      this.statusIndicatorContainer.setColor(this.item.status.color);
      // Update server status label
      if (this.item.status == ServerStatus.ONLINE) {
        this.serverStatusLabel.setText(TextFormatting.WHITE + "" + this.item.players + "/" + this.item.maxPlayers);
      } else {
        this.serverStatusLabel.setText(this.item.status.getFormattedName());
      }


      // Update MOTD tooltip
      this.setTooltip((this.item.motd == null || this.item.motd.isEmpty()) ? null : new UITooltip(this.getGui(), this.item.motd));
    }
  }

  private enum ServerStatus {
    OFFLINE("almura.multiplayer.status.offline", TextFormatting.RED, 0xFF5555),
    ONLINE("almura.multiplayer.status.online", TextFormatting.GREEN, 0x055FF55),
    RESTARTING("almura.multiplayer.status.restarting", TextFormatting.YELLOW, 0xFFFFAA),
    UPDATING("almura.multiplayer.status.updating", TextFormatting.DARK_PURPLE, 0xAA00AA);

    private final String translationKey;
    private final TextFormatting textFormatting;
    private final int color;
    ServerStatus(final String translationKey, final TextFormatting textFormatting, final int color) {
      this.translationKey = translationKey;
      this.textFormatting = textFormatting;
      this.color = color;
    }

    private String getFormattedName() {
      return this.textFormatting + I18n.format(this.translationKey);
    }
  }
}
