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
import com.almuradev.almura.feature.store.client.gui.StoreListScreen;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class FeaturesGUI extends SimpleScreen {

  private int lastUpdate = 0;
  private boolean unlockMouse = true;
  private boolean isAdmin;
  private UIButton guideButton, titleButton, nicknameButton, manageExchangeButton, manageStoreButton, accessoriesButton, claimButton;

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
    final UIForm form = new UIForm(this, 150, 220, "Almura Features");

    if (!isAdmin) {
      form.setSize(form.getWidth(), 150);
    }

    // Guide button
    guideButton = new UIButtonBuilder(this)
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Guide")
        .position(0, 4)
        .listener(this)
        .build("button.guide");

    // Claims button
    claimButton = new UIButtonBuilder(this)
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, guideButton.getY() + 18)
        .text("Claim Management")
        .listener(this)
        .build("button.claim");

    // Nickname button
    nicknameButton = new UIButtonBuilder(this)
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Nicknames")
        .position(0, claimButton.getY() + 18)
        .listener(this)
        .build("button.nickname");

    // Manage Titles button
    titleButton = new UIButtonBuilder(this)
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Titles")
        .position(0, nicknameButton.getY() + 18)
        .listener(this)
        .build("button.title.manage");

    // Accessories button
    accessoriesButton = new UIButtonBuilder(this)
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, titleButton.getY() + 18)
        .text("Accessories")
        .enabled(isAdmin)
        .listener(this)
        .build("button.accessories");

    final UISeparator functionsSeparator = new UISeparator(this);
    functionsSeparator.setSize(form.getWidth(), 1);
    functionsSeparator.setPosition(0, accessoriesButton.getY() + 22, Anchor.TOP | Anchor.CENTER);
    functionsSeparator.setVisible(isAdmin);

    final UILabel adminLabel = new UILabel(this, "Administrator");
    adminLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
    adminLabel.setPosition(0, functionsSeparator.getY() + 4, Anchor.TOP | Anchor.CENTER);
    adminLabel.setVisible(isAdmin);

    final UISeparator functionsSeparator2 = new UISeparator(this);
    functionsSeparator2.setSize(form.getWidth(), 1);
    functionsSeparator2.setPosition(0, accessoriesButton.getY() + 37, Anchor.TOP | Anchor.CENTER);
    functionsSeparator2.setVisible(isAdmin);

    // Manage Exchange button
    manageExchangeButton = new UIButtonBuilder(this)
        .width(125)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, functionsSeparator2.getY() + 10)
        .visible(isAdmin)
        .text("Manage Exchanges")
        .listener(this)
        .build("button.exchange.manage");

    // Server Shop Configuration GUI button
    manageStoreButton = new UIButtonBuilder(this)
        .width(125)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, manageExchangeButton.getY() + 18)
        .visible(isAdmin)
        .text("Manage Shops")
        .listener(this)
        .build("button.shop.manage");

    // Close button
    final UIButton buttonClose = new UIButtonBuilder(this)
        .width(40)
        .anchor(Anchor.BOTTOM | Anchor.RIGHT)
        .text(Text.of("almura.button.close"))
        .listener(this)
        .build("button.close");

    form.add(guideButton, manageExchangeButton, titleButton, nicknameButton, functionsSeparator, adminLabel, functionsSeparator2, accessoriesButton, manageStoreButton, claimButton, buttonClose);

    addToScreen(form);
  }

  @Subscribe
  public void onUIButtonClickEvent(UIButton.ClickEvent event) {
    switch (event.getComponent().getName().toLowerCase()) {
      case "button.exchange.manage":
        exchangeManager.requestExchangeManageGui();
        break;
      case "button.exchange.specific":
        //exchangeManager.requestExchangeSpecificGui("almura.exchange.global");
        break;
      case "button.shop.specific":
        // Todo: need packet based request here.
        //new StoreScreen(true).display(); //isAdmin TRUE
        break;
      case "button.shop.manage":
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

