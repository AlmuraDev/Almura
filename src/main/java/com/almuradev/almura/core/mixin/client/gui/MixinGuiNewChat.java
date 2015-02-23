/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.gui;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {

    private Minecraft mc;

    @Overwrite    
    public void drawChat(int p_146230_1_, CallbackInfo ci) {
        mc = Minecraft.getMinecraft();

        if (Minecraft.getMinecraft().gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int j = this.func_146232_i();
            boolean flag = false;
            int k = 0;
            int l = this.field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (l > 0)
            {
                if (this.getChatOpen())
                {
                    flag = true;
                }

                float f1 = this.func_146244_h();
                int i1 = MathHelper.ceiling_float_int((float)this.func_146228_f() / f1);
                GL11.glPushMatrix();
                GL11.glTranslatef(2.0F, 20.0F, 0.0F);
                GL11.glScalef(f1, f1, 1.0F);
                int j1;
                int k1;
                int i2;

                for (j1 = 0; j1 + this.field_146250_j < this.field_146253_i.size() && j1 < j; ++j1)
                {
                    ChatLine chatline = (ChatLine)this.field_146253_i.get(j1 + this.field_146250_j);

                    if (chatline != null)
                    {
                        k1 = p_146230_1_ - chatline.getUpdatedCounter();

                        if (k1 < 200 || flag)
                        {
                            double d0 = (double)k1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;

                            if (d0 < 0.0D)
                            {
                                d0 = 0.0D;
                            }

                            if (d0 > 1.0D)
                            {
                                d0 = 1.0D;
                            }

                            d0 *= d0;
                            i2 = (int)(255.0D * d0);

                            if (flag)
                            {
                                i2 = 255;
                            }

                            i2 = (int)((float)i2 * f);
                            ++k;

                            if (i2 > 3)
                            {
                                byte b0 = 0;
                                int j2 = -j1 * 9;
                                boolean mentioned = false;
                                String s = chatline.func_151461_a().getFormattedText();
                                // Almura Add
                                String newChat = chatline.func_151461_a().getUnformattedText();
                                if (true) {
                                    String[] split = newChat.toLowerCase().split(":");                                    
                                    if (split.length == 1) {
                                        split = newChat.toLowerCase().split(">");                                        
                                    }
                                    if (split.length > 1) {
                                        String name = this.mc.thePlayer.getCommandSenderName().toLowerCase();
                                        String displayName = this.mc.thePlayer.getDisplayName();
                                        String[] customName = displayName.toLowerCase().split("~");
                                        if (!split[0].contains(name)) {
                                            for (int part = 1; part < split.length; part++) {
                                                System.out.println("Part: " + part + " " + split[part]);
                                                if (split[part].contains(name)) {
                                                    mentioned = true;                                                    
                                                    break;
                                                }
                                                if (customName != null && customName.length>1) {
                                                    if (split[part].contains(customName[1])) {
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
                                }
                                else {
                                    drawRect(b0, j2 - 9, b0 + i1 + 4, j2, i2 / 2 << 24);
                                }
                                // End Almura
                                GL11.glEnable(GL11.GL_BLEND); // FORGE: BugFix MC-36812 Chat Opacity Broken in 1.7.x
                                this.mc.fontRenderer.drawStringWithShadow(s, b0, j2 - 8, 16777215 + (i2 << 24));
                                GL11.glDisable(GL11.GL_ALPHA_TEST);
                            }
                        }
                    }
                }

                if (flag)
                {
                    j1 = this.mc.fontRenderer.FONT_HEIGHT;
                    GL11.glTranslatef(-3.0F, 0.0F, 0.0F);
                    int k2 = l * j1 + l;
                    k1 = k * j1 + k;
                    int l2 = this.field_146250_j * k1 / l;
                    int l1 = k1 * k1 / k2;

                    if (k2 != k1)
                    {
                        i2 = l2 > 0 ? 170 : 96;
                        int i3 = this.field_146251_k ? 13382451 : 3355562;
                        drawRect(0, -l2, 2, -l2 - l1, i3 + (i2 << 24));
                        drawRect(2, -l2, 1, -l2 - l1, 13421772 + (i2 << 24));
                    }
                }

                GL11.glPopMatrix();
            }
        }
    }
}
