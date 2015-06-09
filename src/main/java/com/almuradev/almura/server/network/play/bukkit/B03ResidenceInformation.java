/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.ingame.HUDData;
import com.almuradev.almura.client.gui.ingame.residence.IngameResidenceHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class B03ResidenceInformation implements IMessage, IMessageHandler<B03ResidenceInformation, IMessage> {

    public B03ResidenceInformation() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        HUDData.WITHIN_RESIDENCE = buf.readBoolean();
        if (HUDData.WITHIN_RESIDENCE) {
            HUDData.NAME = ByteBufUtils.readUTF8String(buf);
            HUDData.OWNER_NAME = ByteBufUtils.readUTF8String(buf);
            HUDData.LAST_ONLINE = ByteBufUtils.readUTF8String(buf);
            HUDData.LEASE_COST = ByteBufUtils.readUTF8String(buf);
            HUDData.LEASE_EXPIRATION = ByteBufUtils.readUTF8String(buf);
            HUDData.BOUNDS = ByteBufUtils.readUTF8String(buf);
            HUDData.SIZE = ByteBufUtils.readUTF8String(buf);
            HUDData.VAULT = ByteBufUtils.readUTF8String(buf);
            HUDData.CAN_MOVE = buf.readBoolean();
            HUDData.CAN_BUILD = buf.readBoolean();
            HUDData.HAS_BANK = buf.readBoolean();
            HUDData.CAN_PLACE = buf.readBoolean();
            HUDData.CAN_DESTROY = buf.readBoolean();
            HUDData.CAN_USE = buf.readBoolean();
            HUDData.IS_ADMIN = buf.readBoolean();
            HUDData.CAN_BUTCHER = buf.readBoolean();
            HUDData.HAS_MAYOR = buf.readBoolean();
            HUDData.CAN_USE_CONTAINER = buf.readBoolean();
            HUDData.CAN_PVP = buf.readBoolean();
            HUDData.CAN_TELEPORT = buf.readBoolean();
            HUDData.CAN_ICE_MELT = buf.readBoolean();
            HUDData.CAN_BLOCK_IGNITE = buf.readBoolean();
            HUDData.CAN_FIRE_SPREAD = buf.readBoolean();
            HUDData.CAN_USE_BUCKET = buf.readBoolean();
            HUDData.CAN_FORM = buf.readBoolean();
            HUDData.CAN_LAVA_FLOW = buf.readBoolean();
            HUDData.CAN_WATER_FLOW = buf.readBoolean();
            HUDData.CAN_CREEPERS_DAMAGE_BLOCKS = buf.readBoolean();
            HUDData.CAN_TNT_DAMAGE_BLOCKS = buf.readBoolean();
            HUDData.CAN_SPAWN_MONSTERS = buf.readBoolean();
            HUDData.CAN_SPAWN_ANIMALS = buf.readBoolean();
            HUDData.CAN_FLY = buf.readBoolean();
            HUDData.CAN_CREATE_SUBZONES = buf.readBoolean();
            HUDData.CAN_HEAL = buf.readBoolean();
            HUDData.CAN_PISTONS_OPERATE = buf.readBoolean();
            HUDData.CAN_USE_SHEARS = buf.readBoolean();
            HUDData.CAN_EGGS_HATCH = buf.readBoolean();
            HUDData.CAN_TRAMPLE_CROPS = buf.readBoolean();
            HUDData.IS_SOIL_ALWAYS_HYDRATED = buf.readBoolean();
            HUDData.CAN_STORM_DAMAGE = buf.readBoolean();
            HUDData.CAN_CHAT = buf.readBoolean();
            HUDData.IS_SAFE_ZONE = buf.readBoolean();
            HUDData.CAN_SPAWN_MOC_AMBIENT = buf.readBoolean();
            HUDData.CAN_SPAWN_MOC_AQUATIC = buf.readBoolean();
            HUDData.CAN_SPAWN_MOC_MONSTERS = buf.readBoolean();
            HUDData.CAN_SPAWN_MOC_PASSIVE = buf.readBoolean();
            HUDData.CAN_SPAWN_THC_MONSTERS = buf.readBoolean();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B03ResidenceInformation message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            if (HUDData.WITHIN_RESIDENCE) {
                if (ClientProxy.HUD_RESIDENCE == null) {
                    ClientProxy.HUD_RESIDENCE = new IngameResidenceHUD();
                }
                ClientProxy.HUD_RESIDENCE.updateWidgets();
                ClientProxy.HUD_RESIDENCE.displayOverlay();
            } else {
                if (ClientProxy.HUD_RESIDENCE != null) {
                    ClientProxy.HUD_RESIDENCE.closeOverlay();
                    ClientProxy.HUD_RESIDENCE = null;
                }
            }
        }
        return null;
    }
}
