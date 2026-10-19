/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.skills.network.handler;

import com.almuradev.almura.feature.skills.client.ClientSkillsManager;
import com.almuradev.almura.feature.skills.network.ClientboundSkillsPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundSkillsPacketHandler implements MessageHandler<ClientboundSkillsPacket> {

    private final ClientSkillsManager skillsManager;

    @Inject
    private ClientboundSkillsPacketHandler(final ClientSkillsManager skillsManager) {
        this.skillsManager = skillsManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundSkillsPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                this.skillsManager.setSkills(message.skills);
            }
        }
    }
}
