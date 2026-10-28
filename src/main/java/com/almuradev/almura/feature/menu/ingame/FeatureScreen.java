/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.client.ClientBankManager;
import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.membership.ClientMembershipManager;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.notification.history.NotificationHistoryScreen;
import com.almuradev.almura.feature.skills.client.gui.SkillsScreen;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.title.ClientTitleManager;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class FeatureScreen extends BasicScreen {

  private int lastUpdate = 0;
  private boolean unlockMouse = true;
  private boolean isAdmin;
  private UIButton guideButton, titleButton, nicknameButton, manageExchangeButton, manageStoreButton, accessoriesButton, claimButton, membershipButton, bankingButton, skillsButton, notificationsButton;

  private World world;
  private EntityPlayerSP player;

  @Inject private static ClientExchangeManager exchangeManager;
  @Inject private static ClientPageManager guideManager;
  @Inject private static ClientNickManager nickManager;
  @Inject private static ClientTitleManager titleManager;
  @Inject private static ClientClaimManager claimManager;
  @Inject private static ClientStoreManager storeManager;
  @Inject private static ClientMembershipManager membershipManager;
  @Inject private static ClientAccessoryManager accessoryManager;
  @Inject private static ClientBankManager bankManager;

  public FeatureScreen(EntityPlayerSP player, World worldIn, boolean isAdmin) {
    this.player = player;
    this.world = worldIn;
    this.isAdmin = isAdmin;
  }

  @Override
  public void construct() {
    guiscreenBackground = false;
    Keyboard.enableRepeatEvents(true);

    // Main Panel
    final BasicForm form = new BasicForm(150, 274, "Almura Features");

    if (!isAdmin) {
      form.setSize(form.getWidth(), 204);
    }

    // Guide button
    guideButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Guide")
        .position(0, 4)
        .onClick(() -> guideManager.requestGuideGUI())
        .build("button.guide");

    // Claims button
    claimButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, guideButton.getY() + 18)
        .text("Claim Management")
        .onClick(() -> claimManager.requestClaimGUI())
        .build("button.claim");

    // Nickname button
    nicknameButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Nicknames")
        .position(0, claimButton.getY() + 18)
        .onClick(() -> nickManager.requestNicknameGUI())
        .build("button.nickname");

    // Manage Titles button
    titleButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .text("Titles")
        .position(0, nicknameButton.getY() + 18)
        .onClick(() -> titleManager.requestManageTitlesGui())
        .build("button.title.manage");

    // Accessories button
    accessoriesButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, titleButton.getY() + 18)
        .text("Accessories")
        .enabled(true)
        .onClick(() -> accessoryManager.requestGui())
        .build("button.accessories");

    // Accessories button
    membershipButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, accessoriesButton.getY() + 18)
        .text("Memberships")
        .onClick(() -> membershipManager.requestMembershipGUI())
        .build("button.memberships");

    // Banking button
    bankingButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, membershipButton.getY() + 18)
        .text("Banking")
        .onClick(() -> bankManager.request(BankAction.OPEN, -1))
        .build("button.banking");

    // Skills button
    skillsButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, bankingButton.getY() + 18)
        .text("Skills")
        .onClick(() -> new SkillsScreen(this).display())
        .build("button.skills");

    // Notifications button
    notificationsButton = new UIButtonBuilder()
        .width(100)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, skillsButton.getY() + 18)
        .text("Notifications")
        .onClick(() -> new NotificationHistoryScreen(this).display())
        .build("button.notifications");

    final UISeparator functionsSeparator = new UISeparator(this);
    functionsSeparator.setSize(form.getWidth(), 1);
    functionsSeparator.setPosition(0, notificationsButton.getY() + 22, Anchor.TOP | Anchor.CENTER);
    functionsSeparator.setVisible(isAdmin);

    final UILabel adminLabel = new UILabel("Administrator");
    adminLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
    adminLabel.setPosition(0, functionsSeparator.getY() + 4, Anchor.TOP | Anchor.CENTER);
    adminLabel.setVisible(isAdmin);

    final UISeparator functionsSeparator2 = new UISeparator(this);
    functionsSeparator2.setSize(form.getWidth(), 1);
    functionsSeparator2.setPosition(0, notificationsButton.getY() + 37, Anchor.TOP | Anchor.CENTER);
    functionsSeparator2.setVisible(isAdmin);

    // Manage Exchange button
    manageExchangeButton = new UIButtonBuilder()
        .width(125)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, functionsSeparator2.getY() + 10)
        .visible(isAdmin)
        .text("Manage Exchanges")
        .onClick(() -> exchangeManager.requestExchangeManageGui())
        .build("button.exchange.manage");

    // Server Shop Configuration GUI button
    manageStoreButton = new UIButtonBuilder()
        .width(125)
        .anchor(Anchor.TOP | Anchor.CENTER)
        .position(0, manageExchangeButton.getY() + 18)
        .visible(isAdmin)
        .text("Manage Stores")
        .onClick(() -> storeManager.requestStoreManage())
        .build("button.stores.manage");

    // Close button
    final UIButton buttonClose = new UIButtonBuilder()
        .width(40)
        .anchor(Anchor.BOTTOM | Anchor.RIGHT)
        .text(I18n.format("almura.button.close"))
        .onClick(this::close)
        .build("button.close");

    form.add(guideButton, manageExchangeButton, titleButton, nicknameButton, functionsSeparator, adminLabel, functionsSeparator2, accessoriesButton, membershipButton, bankingButton, skillsButton, notificationsButton, manageStoreButton, claimButton, buttonClose);

    addToScreen(form);
  }

  @Override
  public void update() {
    super.update();
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
  protected void mousePressed(final UIComponent component, final int button) {
    super.mousePressed(component, button);
    this.lastUpdate = 0; // Reset the timer when mouse is pressed.
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
  }
}

