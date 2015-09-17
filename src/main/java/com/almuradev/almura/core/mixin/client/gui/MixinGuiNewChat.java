/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.gui;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.util.Colors;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {

    @Shadow
    public Minecraft mc;
    @Shadow
    public List field_146253_i;
    @Shadow
    public int scrollPos;
    @Shadow
    public boolean isScrolled;
    @Shadow
    public List chatLines;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Shadow
    public abstract void deleteChatLine(int line);

    @Shadow
    public abstract String formatColors(String str);

    @Shadow
    public abstract void scroll(int amount);

    public void drawChat(int p_146230_1_) {

        if (Minecraft.getMinecraft().gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int j = this.getLineCount();
            boolean flag = false;
            int k = 0;
            int l = this.field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (l > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int i1 = MathHelper.ceiling_float_int((float) this.getChatWidth() / f1);
                GL11.glPushMatrix();
                GL11.glTranslatef(2.0F, 20.0F, 0.0F);
                GL11.glScalef(f1, f1, 1.0F);
                int j1;
                int k1;
                int i2;

                for (j1 = 0; j1 + this.scrollPos < this.field_146253_i.size() && j1 < j; ++j1) {
                    ChatLine chatline = (ChatLine) this.field_146253_i.get(j1 + this.scrollPos);

                    if (chatline != null) {
                        k1 = p_146230_1_ - chatline.getUpdatedCounter();

                        if (k1 < 200 || flag) {
                            double d0 = (double) k1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;

                            if (d0 < 0.0D) {
                                d0 = 0.0D;
                            }

                            if (d0 > 1.0D) {
                                d0 = 1.0D;
                            }

                            d0 *= d0;
                            i2 = (int) (255.0D * d0);

                            if (flag) {
                                i2 = 255;
                            }

                            i2 = (int) ((float) i2 * f);
                            ++k;

                            if (i2 > 3) {
                                byte b0 = 0;
                                int j2 = -j1 * 9;
                                boolean mentioned = false;
                                boolean hasNickName = false;
                                String s = chatline.getChatComponent().getFormattedText();
                                // Almura Add
                                if (Configuration.CHAT_NOTIFICATIONS) {
                                    String newChat = chatline.getChatComponent().getUnformattedText();
                                    String[] split = newChat.toLowerCase().split(":");
                                    String nickName = "";
                                    if (split.length == 1) {
                                        split = newChat.toLowerCase().split(">");
                                    }
                                    if (split.length > 1) {
                                        String name = this.mc.thePlayer.getCommandSenderName().toLowerCase();
                                        String displayName = this.mc.thePlayer.getDisplayName();
                                        if (displayName.contains("~")) {
                                            final String[] splitDisplayName = displayName.toLowerCase().split("~");
                                            nickName = Colors.stripColors(splitDisplayName[1]);
                                            hasNickName = true;
                                        }

                                        if (!split[0].contains(name)) {
                                            for (int part = 1; part < split.length; part++) {
                                                if (split[part].contains(name)) {
                                                    mentioned = true;
                                                    break;
                                                }

                                                if (hasNickName) {
                                                    if (split[part].contains(nickName)) {
                                                        mentioned = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (mentioned) {
                                    drawRect(b0, j2 - 9, b0 + i1 + 4, j2, Color.magenta.getRGB());
                                } else {
                                    drawRect(b0, j2 - 9, b0 + i1 + 4, j2, i2 / 2 << 24);
                                }
                                // End Almura
                                GL11.glEnable(GL11.GL_BLEND); // FORGE: BugFix MC-36812 Chat Opacity Broken in 1.7.x
                                this.mc.fontRendererObj.drawStringWithShadow(s, b0, j2 - 8, 16777215 + (i2 << 24));
                                GL11.glDisable(GL11.GL_ALPHA_TEST);
                            }
                        }
                    }
                }

                if (flag) {
                    j1 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GL11.glTranslatef(-3.0F, 0.0F, 0.0F);
                    int k2 = l * j1 + l;
                    k1 = k * j1 + k;
                    int l2 = this.scrollPos * k1 / l;
                    int l1 = k1 * k1 / k2;

                    if (k2 != k1) {
                        i2 = l2 > 0 ? 170 : 96;
                        int i3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -l2, 2, -l2 - l1, i3 + (i2 << 24));
                        drawRect(2, -l2, 1, -l2 - l1, 13421772 + (i2 << 24));
                    }
                }

                GL11.glPopMatrix();
            }
        }
    }

    private void setChatLine(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_) {
        if (p_146237_2_ != 0) {
            this.deleteChatLine(p_146237_2_);
        }

        int k = MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale());
        int l = 0;
        ChatComponentText chatcomponenttext = new ChatComponentText("");
        ArrayList arraylist = Lists.newArrayList();
        ArrayList arraylist1 = Lists.newArrayList(p_146237_1_);

        for (int i1 = 0; i1 < arraylist1.size(); ++i1) {
            IChatComponent ichatcomponent1 = (IChatComponent) arraylist1.get(i1);
            String s = this.formatColors(ichatcomponent1.getChatStyle().getFormattingCode() + ichatcomponent1.getUnformattedTextForChat());
            int j1 = this.mc.fontRendererObj.getStringWidth(s);
            ChatComponentText chatcomponenttext1 = new ChatComponentText(s);
            chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
            boolean flag1 = false;

            if (l + j1 > k) {
                String s1 = this.mc.fontRendererObj.trimStringToWidth(s, k - l, false);
                String s2 = s1.length() < s.length() ? s.substring(s1.length()) : null;

                if (s2 != null && s2.length() > 0) {
                    int k1 = s1.lastIndexOf(" ");

                    if (k1 >= 0 && this.mc.fontRendererObj.getStringWidth(s.substring(0, k1)) > 0) {
                        s1 = s.substring(0, k1);
                        s2 = s.substring(k1);
                    }

                    ChatComponentText chatcomponenttext2 = new ChatComponentText(s2);
                    chatcomponenttext2.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
                    arraylist1.add(i1 + 1, chatcomponenttext2);
                }

                j1 = this.mc.fontRendererObj.getStringWidth(s1);
                chatcomponenttext1 = new ChatComponentText(s1);
                chatcomponenttext1.setChatStyle(ichatcomponent1.getChatStyle().createShallowCopy());
                flag1 = true;
            }

            if (l + j1 <= k) {
                l += j1;
                chatcomponenttext.appendSibling(chatcomponenttext1);
            } else {
                flag1 = true;
            }

            if (flag1) {
                arraylist.add(chatcomponenttext);
                l = 0;
                chatcomponenttext = new ChatComponentText("");
            }
        }

        arraylist.add(chatcomponenttext);
        boolean flag2 = this.getChatOpen();
        IChatComponent ichatcomponent2;

        for (Iterator iterator = arraylist.iterator(); iterator.hasNext();
             this.field_146253_i.add(0, new ChatLine(p_146237_3_, ichatcomponent2, p_146237_2_))) {
            ichatcomponent2 = (IChatComponent) iterator.next();

            if (flag2 && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }
        }

        while (this.field_146253_i.size() > 100) {
            this.field_146253_i.remove(this.field_146253_i.size() - 1);
        }

        if (!p_146237_4_) {
            // Almura Add - Play sound when pinged
            String newChat = p_146237_1_.getUnformattedText();
            String[] split = newChat.toLowerCase().split(":");
            if (split.length == 1) {
                split = newChat.toLowerCase().split(">");
            }
            boolean mentioned = false;

            if (split.length > 1) {
                String name = this.mc.thePlayer.getCommandSenderName().toLowerCase();
                String displayName = this.mc.thePlayer.getDisplayName();
                String[] customName = displayName.toLowerCase().split("~");
                if (split[0].contains(name)) {
                    for (int part = 1; part < split.length; part++) {
                        if (split[part].contains(name)) {
                            mentioned = true;
                            break;
                        }
                        if (customName.length > 1) {
                            if (split[part].contains(customName[1])) {
                                mentioned = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (mentioned) {
                Minecraft.getMinecraft().getSoundHandler()
                        .playSound(PositionedSoundRecord.createPositionedSoundRecord(new ResourceLocation("note.harp"), 1.0f));
            }
            // Almura end

            this.chatLines.add(0, new ChatLine(p_146237_3_, p_146237_1_, p_146237_2_));

            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

}
