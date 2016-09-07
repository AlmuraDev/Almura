/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.server.network.play;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.Page;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.Date;

/**
 * Instructs a client to synchronize a {@link Page}'s information or to store a new one.
 */
public class S00PageInformation implements IMessage, IMessageHandler<S00PageInformation, IMessage> {

    public int index;
    public String identifier, title, author, lastContributor, contents;
    public Date created, lastModified;

    public S00PageInformation() {
    }

    public S00PageInformation(String identifier, int index, String title, Date created, String author, Date lastModified, String lastContributor,
            String contents) {
        this.identifier = identifier;
        this.index = index;
        this.title = title;
        this.created = created;
        this.author = author;
        this.lastModified = lastModified;
        this.lastContributor = lastContributor;
        this.contents = contents;
    }

    public S00PageInformation(Page page) {
        this(page.getIdentifier(), page.getIndex(), page.getName(), page.getCreated(), page.getAuthor(),
                page.getLastModified(), page.getLastContributor(), page.getContents());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        identifier = ByteBufUtils.readUTF8String(buf);
        index = buf.readInt();
        title = ByteBufUtils.readUTF8String(buf);
        created = new Date(buf.readLong());
        author = ByteBufUtils.readUTF8String(buf);
        lastModified = new Date(buf.readLong());
        lastContributor = ByteBufUtils.readUTF8String(buf);
        contents = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, identifier);
        buf.writeInt(index);
        ByteBufUtils.writeUTF8String(buf, title);
        buf.writeLong(created.getTime());
        ByteBufUtils.writeUTF8String(buf, author);
        buf.writeLong(lastModified.getTime());
        ByteBufUtils.writeUTF8String(buf, lastContributor);
        ByteBufUtils.writeUTF8String(buf, contents);
    }

    @Override
    public IMessage onMessage(S00PageInformation message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Almura.PROXY.handlePageInformation(ctx, message);
        }
        return null;
    }
}
