package com.almuradev.almura.server.network.play;

import com.almuradev.almura.client.gui.ingame.IngameBlockInformation;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class S01OpenBlockInformationGui implements IMessage, IMessageHandler<S01OpenBlockInformationGui, IMessage> {
    public int x, y, z, metadata;
    public float hardness;
    public ItemStack stack;

    public S01OpenBlockInformationGui() {
    }

    public S01OpenBlockInformationGui(ItemStack stack, int x, int y, int z, int metadata, float hardness) {
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
        this.metadata = metadata;
        this.hardness = hardness;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        metadata = buf.readInt();
        hardness = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(metadata);
        buf.writeFloat(hardness);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(S01OpenBlockInformationGui message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().displayGuiScreen(new IngameBlockInformation(null, Minecraft.getMinecraft().theWorld.getBlock(message.x, message.y, message.z), message.stack, message.metadata, message.hardness));
        }

        return null;
    }
}
