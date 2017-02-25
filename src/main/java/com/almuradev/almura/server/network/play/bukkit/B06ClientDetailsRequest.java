/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play.bukkit;

import com.almuradev.almura.client.network.play.B02ClientDetailsResponse;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.launchwrapper.Launch;

import java.util.HashSet;
import java.util.Map;

public class B06ClientDetailsRequest implements IMessage, IMessageHandler<B06ClientDetailsRequest, B02ClientDetailsResponse> {

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @SuppressWarnings("unchecked")
	@Override
    public B02ClientDetailsResponse onMessage(B06ClientDetailsRequest message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            HashSet<String> names = (HashSet<String>) Launch.blackboard.get("AllTweakerNames");
            HashSet<String> modNames = Sets.newHashSet();

            for (Map.Entry<?, ?> name : ((Map<?, ?>) Launch.blackboard.get("modList")).entrySet()) {
                modNames.add(((String) name.getKey()).split(":")[1]);
            }
            return new B02ClientDetailsResponse(names, modNames);
        }
        return null;
    }
}
