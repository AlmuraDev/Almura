/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.ingame.residence.IngameResidenceHUD;
import com.almuradev.almura.client.gui.ingame.residence.ResidenceData;
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
        ResidenceData.WITHIN_RESIDENCE = buf.readBoolean();
        if (ResidenceData.WITHIN_RESIDENCE) {
            ResidenceData.NAME = ByteBufUtils.readUTF8String(buf);
            ResidenceData.OWNER_NAME = ByteBufUtils.readUTF8String(buf);
            ResidenceData.LAST_ONLINE = ByteBufUtils.readUTF8String(buf);
            ResidenceData.LEASE_COST = ByteBufUtils.readUTF8String(buf);
            ResidenceData.LEASE_EXPIRATION = ByteBufUtils.readUTF8String(buf);
            ResidenceData.BOUNDS = ByteBufUtils.readUTF8String(buf);
            ResidenceData.SIZE = ByteBufUtils.readUTF8String(buf);
            ResidenceData.VAULT = ByteBufUtils.readUTF8String(buf);
            ResidenceData.CAN_MOVE = buf.readBoolean();
            ResidenceData.CAN_BUILD = buf.readBoolean();
            ResidenceData.HAS_BANK = buf.readBoolean();
            ResidenceData.CAN_PLACE = buf.readBoolean();
            ResidenceData.CAN_DESTROY = buf.readBoolean();
            ResidenceData.CAN_USE = buf.readBoolean();
            ResidenceData.IS_ADMIN = buf.readBoolean();
            ResidenceData.CAN_BUTCHER = buf.readBoolean();
            ResidenceData.HAS_MAYOR = buf.readBoolean();
            ResidenceData.CAN_USE_CONTAINER = buf.readBoolean();
            ResidenceData.CAN_PVP = buf.readBoolean();
            ResidenceData.CAN_TELEPORT = buf.readBoolean();
            ResidenceData.CAN_ICE_MELT = buf.readBoolean();
            ResidenceData.CAN_BLOCK_IGNITE = buf.readBoolean();
            ResidenceData.CAN_FIRE_SPREAD = buf.readBoolean();
            ResidenceData.CAN_USE_BUCKET = buf.readBoolean();
            ResidenceData.CAN_FORM = buf.readBoolean();
            ResidenceData.CAN_LAVA_FLOW = buf.readBoolean();
            ResidenceData.CAN_WATER_FLOW = buf.readBoolean();
            ResidenceData.CAN_CREEPERS_DAMAGE_BLOCKS = buf.readBoolean();
            ResidenceData.CAN_TNT_DAMAGE_BLOCKS = buf.readBoolean();
            ResidenceData.CAN_SPAWN_MONSTERS = buf.readBoolean();
            ResidenceData.CAN_SPAWN_ANIMALS = buf.readBoolean();
            ResidenceData.CAN_FLY = buf.readBoolean();
            ResidenceData.CAN_CREATE_SUBZONES = buf.readBoolean();
            ResidenceData.CAN_HEAL = buf.readBoolean();
            ResidenceData.CAN_PISTONS_OPERATE = buf.readBoolean();
            ResidenceData.CAN_USE_SHEARS = buf.readBoolean();
            ResidenceData.CAN_EGGS_HATCH = buf.readBoolean();
            ResidenceData.CAN_TRAMPLE_CROPS = buf.readBoolean();
            ResidenceData.IS_SOIL_ALWAYS_HYDRATED = buf.readBoolean();
            ResidenceData.CAN_STORM_DAMAGE = buf.readBoolean();
            ResidenceData.CAN_CHAT = buf.readBoolean();
            ResidenceData.IS_SAFE_ZONE = buf.readBoolean();
            ResidenceData.CAN_SPAWN_MOC_AMBIENT = buf.readBoolean();
            ResidenceData.CAN_SPAWN_MOC_AQUATIC = buf.readBoolean();
            ResidenceData.CAN_SPAWN_MOC_MONSTERS = buf.readBoolean();
            ResidenceData.CAN_SPAWN_MOC_PASSIVE = buf.readBoolean();
            ResidenceData.CAN_SPAWN_THC_MONSTERS = buf.readBoolean();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(B03ResidenceInformation message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            if (ResidenceData.WITHIN_RESIDENCE) {
                if (ClientProxy.HUD_RESIDENCE == null) {
                    ClientProxy.HUD_RESIDENCE = new IngameResidenceHUD();
                }
                ClientProxy.HUD_RESIDENCE.updateWidgets();
                ClientProxy.HUD_RESIDENCE.displayOverlay();
            } else {
                if (ClientProxy.HUD_RESIDENCE != null) {
                    ClientProxy.HUD_RESIDENCE.closeOverlay();
                }
            }
        }
        return null;
    }
}
