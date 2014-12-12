/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.IRecipeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IRotatable;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.property.LightProperty;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.property.RenderProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
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

public class PackBlock extends Block implements IPackObject, IBlockClipContainer, IBlockShapeContainer, IRotatable, IRecipeContainer {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    private final int dropAmount;
    private final boolean rotation;
    private final boolean mirrorRotation;
    private final RenderProperty renderProperty;
    private final boolean hasRecipe;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackBlock(Pack pack, String identifier, String textureName, float hardness, int dropAmount, float resistance, boolean rotation,
                     boolean mirrorRotation, LightProperty lightProperty, boolean showInCreativeTab, String creativeTabName,
                     Map<Integer, List<Integer>> textureCoordinates, String shapeName, RenderProperty renderProperty, boolean hasRecipe) {
        super(Material.rock);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.dropAmount = dropAmount;
        this.rotation = rotation;
        this.mirrorRotation = mirrorRotation;
        this.renderProperty = renderProperty;
        this.hasRecipe = hasRecipe;
        setBlockName(pack.getName() + "\\" + identifier);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightProperty.getEmission());

        setLightOpacity(lightProperty.getOpacity());
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
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(blockIcon, textureName, textureCoordinatesByFace);
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
        return shape == null && renderProperty.getValue();
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
    public Pack getPack() {
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
    public void setShape(PackShape shape) {
        this.shape = shape;
        if (shape != null) {
            if (!shape.useVanillaBlockBounds) {
                setBlockBounds(shape.blockBoundsCoordinates.get(0).floatValue(), shape.blockBoundsCoordinates.get(1).floatValue(),
                               shape.blockBoundsCoordinates.get(2).floatValue(), shape.blockBoundsCoordinates.get(3).floatValue(),
                               shape.blockBoundsCoordinates.get(4).floatValue(), shape.blockBoundsCoordinates.get(5).floatValue());
            }
            opaque = false;
        } else {
            opaque = renderProperty.isOpaque();
        }
    }

    @Override
    public boolean hasRecipe() {
        return hasRecipe;
    }

    @Override
    public String getShapeName() {
        return shapeName;
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
        return "PackBlock {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
