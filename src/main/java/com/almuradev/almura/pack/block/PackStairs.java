/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockTextureContainer;
import com.almuradev.almura.pack.IItemBlockInformation;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.almuradev.almura.tabs.Tabs;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class PackStairs extends BlockStairs implements IPackObject, IBlockTextureContainer, INodeContainer, IItemBlockInformation {

    private final Pack pack;
    private final String identifier;
    private Map<Integer, List<Integer>> textureCoordinates;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final String textureName;
    private final List<String> tooltip;
    private ClippedIcon[] clippedIcons;
    private BreakNode breakNode;
    @SuppressWarnings("unused")
	private CollisionNode collisionNode;

    public PackStairs(Pack pack, String identifier, List<String> tooltip, String textureName, Map<Integer, List<Integer>> textureCoordinates,
            float hardness, float resistance, boolean showInCreativeTab, String creativeTabName, LightNode lightNode) {
        super(Blocks.stone, 0);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.textureName = textureName;
        this.tooltip = tooltip;
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        setTextureName(Almura.MOD_ID + ":images/" + textureName);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightNode.getEmission());
        setLightOpacity(lightNode.getOpacity());
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        return clippedIcons;
    }

    @Override
    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinates;
    }

    @Override
    public void setTextureCoordinates(Map<Integer, List<Integer>> coordinates) {
        this.textureCoordinates = coordinates;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
        if (node.getClass() == BreakNode.class) {
            breakNode = (BreakNode) node;
        } else if (node.getClass() == CollisionNode.class) {
            collisionNode = (CollisionNode) node;
        }
        MinecraftForge.EVENT_BUS.post(new AddNodeEvent(this, node));
        return node;
    }

    @Override
    public void addNodes(INode<?>... nodes) {
        for (INode<?> node : nodes) {
            addNode(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T getNode(Class<T> clazz) {
        return (T) nodes.get(clazz);
    }

    @Override
    public <T extends INode<?>> boolean hasNode(Class<T> clazz) {
        return getNode(clazz) != null;
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
    public boolean equals(Object obj) {
        if (!(obj instanceof PackStairs)) {
            return false;
        }

        final PackStairs other = (PackStairs) obj;
        return pack.equals(other.pack) && identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("packName", pack.getName())
                .add("identifier", identifier)
                .toString();
    }

    // ---------------------------------------------- MINECRAFT OVERRIDES --------------------------------------------------------------

    @Override
    public String getTextureName() {
        return textureName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(blockIcon, textureName, textureCoordinates);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
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
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        final ItemStack held = player.getHeldItem();
        ToolsNode found = null;

        if (breakNode.isEnabled()) {
            for (ToolsNode toolsNode : breakNode.getValue()) {
                if (toolsNode instanceof ToolsNode.OffHand) {
                    if (held == null) {
                        found = toolsNode;
                        break;
                    }
                    continue;
                }
                if (held != null && toolsNode.getTool().minecraftObject == held.getItem()) {
                    found = toolsNode;
                    break;
                }
            }

            if (found == null) {
                found = breakNode.getToolByIdentifier("", "none");
                if (found == null) {
                    return;
                }
            }
        } else {
            return;
        }

        player.addExhaustion(found.getExhaustionRange().getValueWithinRange());
        final ArrayList<ItemStack> drops = Lists.newArrayList();
        for (DropProperty src : found.getValue().getValue()) {
            final GameObject source = src.getSource();
            final ItemStack toDrop;
            if (source.isBlock()) {
                toDrop = new ItemStack((Block) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            } else {
                toDrop = new ItemStack((Item) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            }
            if (src.getBonusProperty().getSource()) {
                final double chance = src.getBonusProperty().getValueWithinRange();
                if (RangeProperty.RANDOM.nextDouble() <= (chance / 100)) {
                    toDrop.stackSize += src.getBonusProperty().getValueWithinRange();
                }
            }
            drops.add(toDrop);
        }
        harvesters.set(player);
        if (!world.isRemote && !world.restoringBlockSnapshots) {
            final int fortune = EnchantmentHelper.getFortuneModifier(player);
            final float
            modchance =
            ForgeEventFactory.fireBlockHarvesting(drops, world, this, x, y, z, metadata, fortune, 1.0f, false, harvesters.get());
            for (ItemStack is : drops) {
                if (RangeProperty.RANDOM.nextFloat() <= modchance && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
                    if (captureDrops.get()) {
                        capturedDrops.get().add(is);
                        return;
                    }
                    final float f = 0.7F;
                    final double d0 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final EntityItem item = new EntityItem(world, (double) x + d0, (double) y + d1, (double) z + d2, is);
                    item.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(item);
                }
            }
        }
        harvesters.set(null);
    }
    
    @Override
    public void onBlockClicked(World worldIn, int x, int y, int z, EntityPlayer player) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, int x, int y, int z, int meta) {
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return this.blockResistance / 5.0F;
    }

    @Override
    public int tickRate(World worldIn) {
        return 10;
    }

    @Override
    public void modifyEntityVelocity(World worldIn, int x, int y, int z, Entity entityIn, Vec3 velocity) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess worldIn, int x, int y, int z) {
        final Block block = worldIn.getBlock(x, y, z);
        return worldIn.getLightBrightnessForSkyBlocks(x, y, z, block.getLightValue(worldIn, x, y, z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return Blocks.stone.getSelectedBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean canStopRayTrace(int meta, boolean includeLiquid) {
        return this.isCollidable();
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return worldIn.getBlock(x, y, z).isReplaceable(worldIn, x, y, z);
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        this.onNeighborBlockChange(worldIn, x, y, z, Blocks.air);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        if (hasTileEntity(meta)) {
            worldIn.removeTileEntity(x, y, z);
        }
    }

    @Override
    public void onEntityWalking(World worldIn, int x, int y, int z, Entity entityIn) {
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        return false;
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, int x, int y, int z, Explosion explosionIn) {
    }

    @Override
    public MapColor getMapColor(int meta) {
        return this.getMaterial().getMaterialMapColor();
    }
}
