/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.permissions.ServerPermissions;
import com.almuradev.almura.permissions.Permissible;
import com.almuradev.almura.permissions.PermissionsManagerClient;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.eq2online.permissions.ReplicatedPermissionsContainer;

/**
 * Two-way communication of net.eq2online.permissions:
 *
 * C to S
 *     Sends the server all registered permissibles.
 * S to C
 *     Sends the client the status of the permissibles (true or false enable status)
 */
public class S00PacketPermissions implements IMessage, IMessageHandler<S00PacketPermissions, IMessage> {

    public ReplicatedPermissionsContainer container;

    public S00PacketPermissions() {
    }

    public S00PacketPermissions(ReplicatedPermissionsContainer container) {
        this.container = container;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        container = ReplicatedPermissionsContainer.fromBytes(buf.readBytes(buf.readableBytes()).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(container.getBytes());
    }

    @Override
    public IMessage onMessage(S00PacketPermissions message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            ServerPermissions permissions = null;

            try {
                permissions = new ServerPermissions(message.container);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (permissions != null && permissions.getModName() != null) {
                PermissionsManagerClient.getInstance().putServerPermissions(permissions.getModName(), permissions);
                Permissible permissible = PermissionsManagerClient.getInstance().getForMod(permissions.getModName());
                if (permissible != null) {
                    permissible.onPermissionsChanged(PermissionsManagerClient.getInstance());
                }
            }
        }

        return null;
    }
}
