/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.client.gui.inventory;

import com.almuradev.almura.extension.sign.IExtendedTileEntitySign;
import com.almuradev.almura.util.Color;
import com.almuradev.almura.util.Colors;
import com.google.common.base.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEditSign.class)
public abstract class MixinGuiEditSign extends GuiScreen {

    private static String ALLOWED_CHARACTERS = new String(ChatAllowedCharacters.allowedCharactersArray);

    @Shadow
    private TileEntitySign tileSign;

    @Shadow
    private int editLine;

    private int editColumn;

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    public void onOnGuiClosed(CallbackInfo ci) {
        tileSign.lineBeingEdited = -1;
        ((IExtendedTileEntitySign) tileSign).setColumnBeingEdited(-1);
        ((IExtendedTileEntitySign) tileSign).recalculateText();
        if (mc.theWorld.isRemote) {
            for (int i = 0; i < tileSign.signText.length; i++) {
                final String line = tileSign.signText[i];

                if (line != null) {
                    tileSign.signText[i] = line.replaceAll("(&([a-zA-Z0-9]))", "\u00A7$2");
                }
            }
        }
    }

    @Overwrite
    protected void keyTyped(char c, int key) {
        if (ALLOWED_CHARACTERS == null) {
            ALLOWED_CHARACTERS = new String(ChatAllowedCharacters.allowedCharactersArray);
        }

        if (key == 200) { // Up
            editLine = editLine - 1 & 3;
            editColumn = tileSign.signText[editLine].length();
        }

        if (key == 208 || key == 28 || key == 156) { // Down
            editLine = editLine + 1 & 3;
            editColumn = tileSign.signText[editLine].length();
        }

        if (key == 205) { // Right
            editColumn++;
            if (editColumn > tileSign.signText[editLine].length()) {
                editColumn--;
            }
        }

        if (key == 203) {// Left
            editColumn--;
            if (editColumn < 0) {
                editColumn = 0;
            }
        }

        if (key == 14 && tileSign.signText[editLine].length() > 0) { // Backspace
            String line = tileSign.signText[editLine];
            int endColumnStart = Math.min(editColumn, line.length());
            String before = "";
            if (endColumnStart > 0) {
                before = line.substring(0, endColumnStart);
            }
            String after = "";
            if (line.length() - editColumn > 0) {
                after = line.substring(editColumn, line.length());
            }
            if (before.length() > 0) {
                before = before.substring(0, before.length() - 1);
                line = before + after;
                tileSign.signText[editLine] = line;
                endColumnStart--;
                editColumn = endColumnStart;
                if (editColumn < 0) {
                    editColumn = 0;
                }
            }
        }

        if (ChatAllowedCharacters.isAllowedCharacter(c) && tileSign.signText[editLine].length() < 30) { // Enter
            String line = tileSign.signText[editLine];

            // Prevent out of bounds on the substring call
            int endColumnStart = Math.min(editColumn, line.length());

            String before = "";
            if (endColumnStart > 0) {
                before = line.substring(0, endColumnStart);
            }

            String after = "";
            if (line.length() - endColumnStart > 0) {
                after = line.substring(endColumnStart, line.length());
            }

            before += c;

            line = before + after;
            tileSign.signText[editLine] = line;
            endColumnStart++;
            editColumn = endColumnStart;
        }

        if (key == 211) { // Delete
            String line = tileSign.signText[editLine];
            String before = line.substring(0, editColumn);
            String after = "";
            if (line.length() - editColumn > 0) {
                after = line.substring(editColumn, line.length());
            }
            if (after.length() > 0) {
                after = after.substring(1, after.length());
                line = before + after;
                tileSign.signText[editLine] = line;
            }
        }

        ((IExtendedTileEntitySign) tileSign).recalculateText();
    }

    @Inject(method = "drawScreen", at = @At(value = "JUMP", opcode = Opcodes.IFNE, shift = At.Shift.BEFORE), cancellable = true)
    public void onDrawScreen(int x, int y, float delta, CallbackInfo ci) {
        tileSign.lineBeingEdited = editLine;
        ((IExtendedTileEntitySign) tileSign).setColumnBeingEdited(editColumn);

        TileEntityRendererDispatcher.instance.renderTileEntityAt(tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
        tileSign.lineBeingEdited = -1;
        ((IExtendedTileEntitySign) tileSign).setColumnBeingEdited(-1);
        GL11.glPopMatrix();
        super.drawScreen(x, y, delta);
        for (int c = 0; c < 22; c++) {
            if (c == 16) {
                continue;
            }
            Optional<Color> value = Colors.getByCode(c);
            if (!value.isPresent()) {
                value = Optional.of(Colors.BLACK);
            }
            String name = value.get().getName();
            boolean lastUnderscore = true;
            String parsedName = "";
            for (int chr = 0; chr < name.length(); chr++) {
                char ch = name.charAt(chr);
                if (lastUnderscore) {
                    ch = Character.toUpperCase(ch);
                }
                if (ch == '_') {
                    lastUnderscore = true;
                    ch = ' ';
                } else {
                    lastUnderscore = false;
                }
                parsedName += ch;
            }
            char code = (char) ('0' + c);
            if (c >= 10) {
                code = (char) ('a' + c - 10);
            }
            Minecraft.getMinecraft().fontRendererObj.drawString("&" + code + " - " + value.get() + parsedName, width - 90, 50 + c * 10, 0xffffffff);
        }
        ci.cancel();
    }
}