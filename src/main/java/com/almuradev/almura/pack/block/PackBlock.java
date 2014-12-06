/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IRotatable;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PackBlock extends Block implements IPackObject, IBlockClipContainer, IBlockShapeContainer, IRotatable {

    public static int renderId;
    private final ContentPack pack;
    private final String identifier;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    private final int dropAmount;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    private final boolean rotation;
    private final boolean mirrorRotation;
    private final boolean renderAsNormalBlock;
    private final boolean renderAsOpaque;

    public PackBlock(ContentPack pack, String identifier, String textureName, float hardness, int dropAmount, float resistance, boolean rotation,
                     boolean mirrorRotation, float lightLevel, int lightOpacity, boolean showInCreativeTab, String creativeTabName,
                     Map<Integer, List<Integer>> textureCoordinates, String shapeName, boolean renderAsNormalBlock, boolean renderAsOpaque) {
        super(Material.rock);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.dropAmount = dropAmount;
        this.rotation = rotation;
        this.mirrorRotation = mirrorRotation;
        this.renderAsNormalBlock = renderAsNormalBlock;
        this.renderAsOpaque = renderAsOpaque;
        setBlockName(pack.getName() + "\\" + identifier);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightLevel);

        setLightOpacity(lightOpacity);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":images/" + textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoords(blockIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmpty(clippedIcons)) {
            return super.getIcon(side, type);
        }
        ClippedIcon sideIcon;

        if (side >= clippedIcons.length) {
            sideIcon = clippedIcons[0];
        } else {
            sideIcon = clippedIcons[side];

            if (sideIcon == null) {
                sideIcon = clippedIcons[0];
            }
        }

        return sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock() {
        return shape == null && renderAsNormalBlock;
    }

    @Override
    public boolean isOpaqueCube() {
        return opaque;
    }

    @Override
    public int quantityDropped(Random p_149745_1_) {
        return dropAmount;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
        final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
        final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);
        world.setBlockMetadataWithNotify(x, y, z, Rotation.getState(cameraDir, playerDir).getId(), 3);
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (shape == null) {
            return vanillaBB;
        }
        return shape.getCollisionBoundingBoxFromPool(vanillaBB, world, x, y, z);
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        if (shape == null) {
            return vanillaBB;
        }
        return shape.getSelectedBoundingBoxFromPool(vanillaBB, world, x, y, z);
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        return clippedIcons;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public PackShape getShape(IBlockAccess access, int x, int y, int z, int metadata) {
        return shape;
    }

    @Override
    public PackShape getShape() {
        return shape;
    }

    @Override
    public void refreshShape() {
        shape = null;

        if (shapeName != null) {
            for (PackShape shape : ContentPack.getShapes()) {
                if (shape.getName().equalsIgnoreCase(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
        if (shape != null) {
            if (!shape.useVanillaBlockBounds) {
                setBlockBounds(shape.blockBoundsCoordinates.get(0).floatValue(), shape.blockBoundsCoordinates.get(1).floatValue(),
                               shape.blockBoundsCoordinates.get(2).floatValue(), shape.blockBoundsCoordinates.get(3).floatValue(),
                               shape.blockBoundsCoordinates.get(4).floatValue(), shape.blockBoundsCoordinates.get(5).floatValue());
            }
            opaque = false;
        } else {
            opaque = renderAsOpaque;
        }
    }

    @Override
    public boolean canMirrorRotate(IBlockAccess access, int x, int y, int z, int metadata) {
        return mirrorRotation;
    }

    @Override
    public boolean canRotate(IBlockAccess access, int x, int y, int z, int metadata) {
        return rotation;
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }
}
