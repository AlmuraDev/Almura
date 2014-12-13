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
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.DropsNode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackBlock extends Block implements IPackObject, IBlockClipContainer, IBlockShapeContainer, IRotatable, IRecipeContainer {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    private final boolean rotate;
    private final boolean mirrorRotate;
    private final RenderNode renderProperty;
    private final BreakNode breakProperty;
    private final boolean hasRecipe;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackBlock(Pack pack, String identifier, String textureName, float hardness, float resistance, boolean rotate,
                     boolean mirrorRotate, LightNode lightProperty, boolean showInCreativeTab, String creativeTabName,
                     Map<Integer, List<Integer>> textureCoordinates, String shapeName, RenderNode renderProperty, BreakNode breakProperty, boolean hasRecipe) {
        super(Material.rock);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.rotate = rotate;
        this.mirrorRotate = mirrorRotate;
        this.renderProperty = renderProperty;
        this.breakProperty = breakProperty;
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

    //TODO Check this come 1.8
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        final ItemStack held = player.getHeldItem();
        ToolsNode toolsProperty = null;
        for (ToolsNode prop : breakProperty.getValue()) {
            if (held == null && prop instanceof ToolsNode.OffHand) {
                toolsProperty = prop;
                break;
            }
            if (held != null && prop.getTool() == held.getItem()) {
                toolsProperty = prop;
                break;
            }
        }

        if (toolsProperty == null) {
            return;
        }

        player.addExhaustion(toolsProperty.getExhaustion().getValueWithinRange());
        final ArrayList<ItemStack> drops = Lists.newArrayList();
        for (DropsNode prop : toolsProperty.getValue()) {
            for (DropProperty src : prop.getValue()) {
                final ItemStack toDrop = new ItemStack(src.getSource(), src.getAmount(), src.getDamage());
                if (src.getBonus().getSource()) {
                    final double chance = src.getBonus().getValueWithinRange();
                    if (RangeProperty.RANDOM.nextDouble() <= 100 / chance) {
                        toDrop.stackSize += src.getBonus().getBonusAmountRange().getValueWithinRange();
                    }
                }
            }
        }
        harvesters.set(player);
        if (!world.isRemote && !world.restoringBlockSnapshots) {
            final int fortune = EnchantmentHelper.getFortuneModifier(player);
            final float modchance = ForgeEventFactory.fireBlockHarvesting(drops, world, this, x, y, z, metadata, fortune, 1.0f, false, harvesters.get());
            for (ItemStack is : drops) {
                if (modchance <= RangeProperty.RANDOM.nextFloat() && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
                    if (captureDrops.get()) {
                        capturedDrops.get().add(is);
                        return;
                    }
                    final float f = 0.7F;
                    final double d0 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    final double d1 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    final double d2 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    final EntityItem item = new EntityItem(world, (double)x + d0, (double)y + d1, (double)z + d2, is);
                    item.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(item);
                }
            }
        }
        harvesters.set(null);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
        if (rotate || mirrorRotate) {
            final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
            final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);
            world.setBlockMetadataWithNotify(x, y, z, Rotation.getState(cameraDir, playerDir).getId(), 3);
        }
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
        return mirrorRotate;
    }

    @Override
    public boolean canRotate(IBlockAccess access, int x, int y, int z, int metadata) {
        return rotate;
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
