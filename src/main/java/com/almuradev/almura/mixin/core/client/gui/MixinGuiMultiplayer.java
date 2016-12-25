/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin's objective is to force Almura's server entries (PUBLIC/DEV) into the Minecraft server list.
 */
@Mixin(GuiMultiplayer.class)
public abstract class MixinGuiMultiplayer extends GuiScreen {

    private static final ServerData PUBLIC_SERVER_DATA = new ServerData(I18n.format("almura.menu.liveServer"), "srv1.almuramc.com", false);
    private static final ServerData DEV_SERVER_DATA = new ServerData(I18n.format("almura.menu.devServer"), "dev.almuramc.com", false);
    @Shadow private ServerSelectionList serverListSelector;
    @Shadow private GuiButton btnDeleteServer;
    @Shadow private GuiButton btnEditServer;

    @Shadow public abstract ServerList getServerList();

    @Inject(method = "initGui", at = @At("RETURN"))
    public void onInitGui(CallbackInfo ci) {
        boolean hasPublicData = false;
        boolean hasDevData = false;

        for (int i = 0; i < this.getServerList().countServers(); i++) {
            final ServerData data = this.getServerList().getServerData(i);
            if (data.serverName.equals(PUBLIC_SERVER_DATA.serverName) && data.serverIP.equals(PUBLIC_SERVER_DATA.serverIP)) {
                hasPublicData = true;
            } else if (data.serverName.equals(DEV_SERVER_DATA.serverName) && data.serverIP.equals(DEV_SERVER_DATA.serverIP)) {
                hasDevData = true;
            }
        }

        if (!hasPublicData) {
            this.getServerList().addServerData(PUBLIC_SERVER_DATA);
        }
        if (!hasDevData) {
            this.getServerList().addServerData(DEV_SERVER_DATA);
        }

        this.serverListSelector.updateOnlineServers(this.getServerList());
    }

    @Inject(method = "selectServer", at = @At("RETURN"))
    public void onSelectServer(int index, CallbackInfo ci) {
        final GuiListExtended.IGuiListEntry entry = index < 0 ? null : this.serverListSelector.getListEntry(index);

        if (entry != null && entry instanceof ServerListEntryNormal) {
            final ServerListEntryNormal serverEntry = (ServerListEntryNormal) entry;

            if (serverEntry.getServerData().serverName.equals(PUBLIC_SERVER_DATA.serverName) && serverEntry.getServerData().serverIP
                    .equals(PUBLIC_SERVER_DATA.serverIP)) {
                this.btnDeleteServer.enabled = false;
                this.btnEditServer.enabled = false;
            } else if (serverEntry.getServerData().serverName.equals(DEV_SERVER_DATA.serverName) && serverEntry.getServerData().serverIP
                    .equals(DEV_SERVER_DATA.serverIP)) {
                this.btnDeleteServer.enabled = false;
                this.btnEditServer.enabled = false;
            }
        }
    }
}
