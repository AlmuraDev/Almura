/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.multiplayer.MultiplayerScreen;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ConnectingGui extends BasicScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel messageLabel, messageLabel2, magic;
    private BasicForm form;
    private UIButton buttonClose;

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;
    @Inject private static PluginContainer container;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private boolean cancel;
    private final MultiplayerScreen previousGuiScreen;
    private ServerData serverEntry;

    public ConnectingGui(MultiplayerScreen previousGuiScreen, ServerData serverDataIn) {
        this.previousGuiScreen = previousGuiScreen;
        this.serverEntry = serverEntry;
        ServerAddress serveraddress = ServerAddress.fromString(serverDataIn.serverIP);
        //Minecraft.getMinecraft().loadWorld((WorldClient)null);
        Minecraft.getMinecraft().setServerData(serverDataIn);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        this.form = new BasicForm(this, 200, 200, "Server Status");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(false);
        this.form.setClosable(false);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        // Almura header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 5, Anchor.TOP | Anchor.CENTER);

        final UISeparator aboveLogoSeparator = new UISeparator(this);
        aboveLogoSeparator.setSize(this.form.getWidth() -5, 1);
        aboveLogoSeparator.setPosition(0, 107, Anchor.TOP | Anchor.CENTER);
        this.form.add(aboveLogoSeparator);

        this.messageLabel = new UILabel(this, "Connecting from Server...");
        this.messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.messageLabel.setPosition(0, 115, Anchor.CENTER | Anchor.TOP);
        this.form.add(this.messageLabel);

        this.messageLabel2 = new UILabel(this, "Synchronizing data...");
        this.messageLabel2.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        this.messageLabel2.setPosition(0, 128, Anchor.CENTER | Anchor.TOP);
        this.form.add(this.messageLabel2);

        this.magic = new UILabel(this, "valuesvalues");
        magic.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).obfuscated(true).scale(1.0F).build());
        magic.setPosition(0, -25, Anchor.CENTER | Anchor.BOTTOM);
        this.form.add(magic);

        final UISeparator aboveButtonsSeparator = new UISeparator(this);
        aboveButtonsSeparator.setSize(this.form.getWidth() -5, 1);
        aboveButtonsSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);
        this.form.add(aboveButtonsSeparator);

        // Close button
        this.buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                //.text(I18n.format("almura.menu_button.quit"))
                .text("Return to Server Menu")
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build())
                .listener(this)
                .build("button.return");

        this.form.add(almuraHeader, this.buttonClose);

        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {

        switch (event.getComponent().getName().toLowerCase()) {
            case "button.return":
                this.cancel = true;
                if (Almura.networkManager != null) {
                    Almura.networkManager.closeChannel(new TextComponentString("Aborted"));
                }
                new MultiplayerScreen(null).display();
                break;
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (Almura.networkManager == null) {
            this.messageLabel.setText("Connecting to server...");
            this.messageLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
            this.messageLabel2.setVisible(false);
            this.form.setSize(200, 190);
        } else {
            this.messageLabel.setText("Connected.");
            this.messageLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.1F).build());
            this.messageLabel2.setVisible(true);
            this.form.setSize(200, 200);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (Almura.networkManager != null) {
            if (Almura.networkManager.isChannelOpen()) {
                Almura.networkManager.processReceivedPackets();
            } else {
                Almura.networkManager.handleDisconnection();
            }
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            new MultiplayerScreen(null).display();
        }
        super.keyTyped(keyChar, keyCode);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    private void connect(final String ip, final int port) {
        LOGGER.info("Connecting to {}, {}", ip, Integer.valueOf(port));
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            public void run() {
                InetAddress inetaddress = null;

                try {
                    if (ConnectingGui.this.cancel) {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);
                    if (Almura.networkManager != null) {
                        Almura.networkManager = null;
                    }
                    Almura.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, ConnectingGui.this.mc.gameSettings.isUsingNativeTransport());
                    Almura.networkManager.setNetHandler(new NetHandlerLoginClient(Almura.networkManager, ConnectingGui.this.mc, ConnectingGui.this.previousGuiScreen));
                    Almura.networkManager.sendPacket(new C00Handshake(ip, port, EnumConnectionState.LOGIN));
                    Almura.networkManager.sendPacket(new CPacketLoginStart(ConnectingGui.this.mc.getSession().getProfile()));
                } catch (UnknownHostException unknownhostexception) {
                    if (ConnectingGui.this.cancel) {
                        return;
                    }

                    ConnectingGui.LOGGER.error("Couldn't connect to server", (Throwable)unknownhostexception);
                    new DisconnectedGui("Could not connect to server.").display();
                } catch (Exception exception) {
                    if (ConnectingGui.this.cancel) {
                        return;
                    }

                    ConnectingGui.LOGGER.error("Couldn't connect to server", (Throwable)exception);
                    String s = exception.toString();

                    if (inetaddress != null) {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }

                    new DisconnectedGui("Exception encountered.  Could not connect to server.").display();
                }
            }
        }).start();
    }
}
