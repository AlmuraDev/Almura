package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.PackUtil;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Stage {
    private final int id;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    public Item toDrop;
    public int dropAmount;
    public int fortuneModifier;
    public ClippedIcon[] clippedIcons;

    public Stage(int id, Map<Integer, List<Integer>> textureCoordinatesByFace) {
        this.id = id;
        this.textureCoordinatesByFace = textureCoordinatesByFace == null ? Maps.<Integer, List<Integer>>newHashMap() : textureCoordinatesByFace;
    }

    public ArrayList<ItemStack> getDrops(int fortune) {
        if (toDrop == null) {
            return new ArrayList<>();
        }

        final ArrayList<ItemStack> drops = new ArrayList<>();

        for (int i = 0; i < fortuneModifier + fortune; i++) {
            if (PackCrops.RANDOM.nextInt(15) <= id) {
                drops.add(new ItemStack(toDrop, dropAmount));
            }
        }

        return drops;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIcon icon, Path path, String textureName) {
        clippedIcons = PackUtil.generateClippedIconsFromCoords(icon, path, textureName, textureCoordinatesByFace);
    }
}
