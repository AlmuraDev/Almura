/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.store.client.gui.StoreScreen;
import com.almuradev.almura.feature.store.client.gui.StoreListScreen;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class FeaturesGUI extends SimpleScreen {

  private int lastUpdate = 0;
  private boolean unlockMouse = true;
  private boolean isAdmin;
  private UIButton guideButton, manageTitleButton, nicknameButton, manageExchangeButton, specificExchangeButton, serverShopButton, npcShopButton,
    accessoriesButton, claimButton;

  private World world;
  private EntityPlayerSP player;

  @Inject private static ClientExchangeManager exchangeManager;
  @Inject private static ClientPageManager guideManager;
  @Inject private static ClientNickManager nickManager;
  @Inject private static ClientTitleManager titleManager;
  @Inject private static ClientClaimManager claimManager;

  public FeaturesGUI(EntityPlayerSP player, World worldIn, boolean isAdmin) {
    this.player = player;
    this.world = worldIn;
    this.isAdmin = isAdmin;
  }

  @Override
  public void construct() {
    guiscreenBackground = false;
    Keyboard.enableRepeatEvents(true);

    // Main Panel
    final UIForm form = new UIForm(this, 150, 230, "Almura Features");

    // Guide button
    guideButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .text("Guide")
      .listener(this)
      .build("button.guide");

    // Nickname button
    nicknameButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .text("Nicknames")
      .position(0, guideButton.getY() + 18)
      .listener(this)
      .build("button.nickname");

    // Manage Titles button
    manageTitleButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .text("Manage Titles")
      .position(0, nicknameButton.getY() + 18)
      .listener(this)
      .build("button.title.manage");

    // Manage Exchange button
    manageExchangeButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, manageTitleButton.getY() + 18)
      .visible(isAdmin)
      .text("Manage Exchanges")
      .listener(this)
      .build("button.exchange.manage");

    // Specific Exchange button
    specificExchangeButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, manageExchangeButton.getY() + 18)
      .visible(isAdmin)
      .text("Global Exchange")
      .listener(this)
      .build("button.exchange.specific");

    // Server Shop Configuration GUI button
    serverShopButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, specificExchangeButton.getY() + 18)
      .visible(isAdmin)
      .text("Server Shops")
      .listener(this)
      .build("button.servershop");

    // Server Shop - NPC Shop GUI button
    npcShopButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, serverShopButton.getY() + 18)
      .visible(isAdmin)
      .text("NPC Shop")
      .listener(this)
      .build("button.npcshop");

    // Accessories button
    accessoriesButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, npcShopButton.getY() + 18)
      .text("Accessories")
      .visible(isAdmin)
      .listener(this)
      .build("button.accessories");

    // Claims button
    claimButton = new UIButtonBuilder(this)
      .width(100)
      .anchor(Anchor.TOP | Anchor.CENTER)
      .position(0, accessoriesButton.getY() + 18)
      .text("Claim Management")
      .listener(this)
      .build("button.claim");

    // Close button
    final UIButton buttonClose = new UIButtonBuilder(this)
      .width(40)
      .anchor(Anchor.BOTTOM | Anchor.RIGHT)
      .text(Text.of("almura.button.close"))
      .listener(this)
      .build("button.close");

    form.add(guideButton, specificExchangeButton, manageExchangeButton, manageTitleButton, nicknameButton, accessoriesButton,
      serverShopButton, npcShopButton, claimButton, buttonClose);

    addToScreen(form);
  }

  @Subscribe
  public void onUIButtonClickEvent(UIButton.ClickEvent event) {
    switch (event.getComponent().getName().toLowerCase()) {
      case "button.exchange.manage":
        exchangeManager.requestExchangeManageGui();
        break;
      case "button.exchange.specific":
        exchangeManager.requestExchangeSpecificGui("almura.exchange.global");
        break;
      case "button.npcshop":
        // Todo: need packet based request here.
        new StoreScreen(true).display(); //isAdmin TRUE
        break;
      case "button.servershop":
        // Todo: need packet based request here.
        new StoreListScreen().display();
        break;
      case "button.title.manage":
        titleManager.requestManageTitlesGui();
        break;
      case "button.guide":
        guideManager.requestGuideGUI();
        break;
      case "button.nickname":
        nickManager.requestNicknameGUI();
        break;
      case "button.claim":
        claimManager.requestClaimGUI();
        break;
      case "button.close":
        this.close();
        break;
    }
  }

  @Override
  public void update(int mouseX, int mouseY, float partialTick) {
    super.update(mouseX, mouseY, partialTick);
    if (unlockMouse && this.lastUpdate == 25) {
      Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
      unlockMouse = false; // Only unlock once per session.
    }

    if (++this.lastUpdate > 100) {}
  }

  @Override
  protected void keyTyped(char keyChar, int keyCode) {
    super.keyTyped(keyChar, keyCode);
    this.lastUpdate = 0; // Reset the timer when key is typed.
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    super.mouseClicked(x, y, button);
    this.lastUpdate = 0; // Reset the timer when mouse is pressed.
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
  }
}

