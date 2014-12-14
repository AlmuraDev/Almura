/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.DropsNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PackBlock extends Block implements IPackObject, IBlockClipContainer, IBlockShapeContainer, INodeContainer {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    private final String shapeName;
    private final ConcurrentMap<Class<? extends INode>, INode> nodes = Maps.newConcurrentMap();
    private RenderNode renderNode;
    private RotationNode rotationNode;
    private BreakNode breakNode;
    private CollisionNode collisionNode;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;

    public PackBlock(Pack pack, String identifier, String textureName, Map<Integer, List<Integer>> textureCoordinates, String shapeName, float hardness,
                     float resistance, boolean showInCreativeTab, String creativeTabName, RotationNode rotationNode, LightNode lightNode,
                     RenderNode renderNode) {
        super(Material.rock);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.renderNode = addNode(renderNode);
        this.rotationNode = addNode(rotationNode);
        addNode(lightNode);
        setBlockName(pack.getName() + "\\" + identifier);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":images/" + textureName);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightNode.getEmission());
        setLightOpacity(lightNode.getOpacity());
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
        return shape == null && renderNode.getValue();
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
        for (ToolsNode prop : breakNode.getValue()) {
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
                final ItemStack toDrop = new ItemStack(src.getSource(), src.getAmountProperty().getValueWithinRange(), src.getDamage());
                if (src.getBonusProperty().getSource()) {
                    final double chance = src.getBonusProperty().getValueWithinRange();
                    if (RangeProperty.RANDOM.nextDouble() <= 100 / chance) {
                        toDrop.stackSize += src.getBonusProperty().getBonusAmountRange().getValueWithinRange();
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
        if (rotationNode.isEnabled() && (rotationNode.isDefaultRotate() || rotationNode.isDefaultMirrorRotate())) {
            final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
            final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);
            world.setBlockMetadataWithNotify(x, y, z, RotationMeta.Rotation.getState(cameraDir, playerDir).getId(), 3);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (shape == null) {
            return vanillaBB;
        }
        return shape.getCollisionBoundingBoxFromPool(vanillaBB, world, x, y, z);
    }

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
            opaque = renderNode.isOpaque();
        }
    }

    @Override
    public String getShapeName() {
        return shapeName;
    }

    @Override
    public <T extends INode> T addNode(T node) {
        nodes.put(node.getClass(), node);
        MinecraftForge.EVENT_BUS.post(new AddNodeEvent(this, node));
        return node;
    }

    @Override
    public void addNodes(INode... nodes) {
        for (INode node : nodes) {
            addNode(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode> T getNode(Class<T> clazz) {
        return (T) nodes.get(clazz);
    }

    @Override
    public <T extends INode> boolean hasNode(Class<T> clazz) {
        return getNode(clazz) != null;
    }

    @SubscribeEvent
    public void onAddNodeEvent(AddNodeEvent event) {
        if (event.getNode() instanceof BreakNode) {
            breakNode = (BreakNode) event.getNode();
        } else if (event.getNode() instanceof CollisionNode) {
            collisionNode = (CollisionNode) event.getNode();
        }
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }
}
