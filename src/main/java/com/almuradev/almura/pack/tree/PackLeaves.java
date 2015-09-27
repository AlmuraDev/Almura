package com.almuradev.almura.pack.tree;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.IItemBlockInformation;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.DecayNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.GameObjectProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.almuradev.almura.tabs.Tabs;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class PackLeaves extends BlockLeavesBase implements IPackObject, IBlockClipContainer, IBlockModelContainer, INodeContainer,
        IItemBlockInformation {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private final Map<Integer, List<Integer>> textureCoordinates;
    private final String modelName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final String textureName;
    private final List<String> tooltip;
    private ClippedIcon[] clippedIcons;
    private Optional<PackModelContainer> modelContainer;
    private RenderNode renderNode;
    private BreakNode breakNode;
    private DecayNode decayNode;
    private CollisionNode collisionNode;
    int[] metaCache;

    public PackLeaves(Pack pack, String identifier, List<String> tooltip, String textureName, Map<Integer, List<Integer>> textureCoordinates,
            String modelName, PackModelContainer modelContainer, float hardness, float resistance, boolean showInCreativeTab, String creativeTabName,
            LightNode lightNode, RenderNode renderNode) {
        super(Material.leaves, true);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.textureName = textureName;
        this.modelName = modelName;
        this.renderNode = addNode(renderNode);
        addNode(lightNode);
        this.tooltip = tooltip;
        setModelContainer(modelContainer);
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        setTextureName(Almura.MOD_ID + ":images/" + textureName);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightNode.getEmission());
        setLightOpacity(lightNode.getOpacity());
        setTickRandomly(true);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
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
    public Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata) {
        return modelContainer;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer() {
        return modelContainer;
    }

    @Override
    public void setModelContainer(PackModelContainer modelContainer) {
        this.modelContainer = Optional.fromNullable(modelContainer);

        if (Configuration.IS_CLIENT && this.modelContainer.isPresent()) {
            if (this.modelContainer.get().getModel().isPresent()) {
                fullBlock = false;
            } else {
                fullBlock = renderNode.isOpaque();
            }
        }
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
        if (node.getClass() == BreakNode.class) {
            breakNode = (BreakNode) node;
        } else if (node.getClass() == CollisionNode.class) {
            collisionNode = (CollisionNode) node;
        } else if (node.getClass() == DecayNode.class) {
            decayNode = (DecayNode) node;
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
        if (!(obj instanceof PackLeaves)) {
            return false;
        }

        final PackLeaves other = (PackLeaves) obj;
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
    public int getRenderType() {
        return renderId;
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (!modelContainer.isPresent()) {
            return vanillaBB;
        }
        return modelContainer.get().getPhysics().getCollision(vanillaBB, world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        if (!modelContainer.isPresent()) {
            return vanillaBB;
        }
        return modelContainer.get().getPhysics().getWireframe(vanillaBB, world, x, y, z);
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
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        byte b0 = 1;
        int i1 = b0 + 1;

        if (worldIn.checkChunksExist(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
            for (int j1 = -b0; j1 <= b0; ++j1) {
                for (int k1 = -b0; k1 <= b0; ++k1) {
                    for (int l1 = -b0; l1 <= b0; ++l1) {
                        final Block block = worldIn.getBlock(x + j1, y + k1, z + l1);
                        if (block == this) {
                            block.beginLeavesDecay(worldIn, x + j1, y + k1, z + l1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote && decayNode != null && decayNode.isEnabled()) {
            byte b0 = 4;
            int i1 = b0 + 1;
            byte b1 = 32;
            int j1 = b1 * b1;
            int k1 = b1 / 2;

            if (this.metaCache == null) {
                this.metaCache = new int[b1 * b1 * b1];
            }

            int l1;

            if (worldIn.checkChunksExist(x - i1, y - i1, z - i1, x + i1, y + i1, z + i1)) {
                int i2;
                int j2;

                for (l1 = -b0; l1 <= b0; ++l1) {
                    for (i2 = -b0; i2 <= b0; ++i2) {
                        for (j2 = -b0; j2 <= b0; ++j2) {
                            Block block = worldIn.getBlock(x + l1, y + i2, z + j2);
                            final int blockMetadata = worldIn.getBlockMetadata(x + l1, y + i2, z + j2);
                            GameObject found = null;
                            // Check if we have something that prevents decaying nearby
                            for (GameObjectProperty prop : decayNode.getPreventDecayProperties()) {
                                final Object minecraftObject = prop.getSource().minecraftObject instanceof ItemBlock ? ((ItemBlock) prop.getSource()
                                        .minecraftObject).blockInstance : prop.getSource().minecraftObject;
                                if (minecraftObject == block && prop.getSource().data == blockMetadata) {
                                    found = prop.getSource();
                                    break;
                                }
                            }

                            if (found == null) {
                                if (block == this) {
                                    this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                } else {
                                    this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                }
                            } else {
                                this.metaCache[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                            }
                        }
                    }
                }

                for (l1 = 1; l1 <= 4; ++l1) {
                    for (i2 = -b0; i2 <= b0; ++i2) {
                        for (j2 = -b0; j2 <= b0; ++j2) {
                            for (int k2 = -b0; k2 <= b0; ++k2) {
                                if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1) {
                                    if (this.metaCache[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                        this.metaCache[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.metaCache[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2) {
                                        this.metaCache[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.metaCache[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2) {
                                        this.metaCache[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.metaCache[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2) {
                                        this.metaCache[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                    }

                                    if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2) {
                                        this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                    }

                                    if (this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2) {
                                        this.metaCache[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            l1 = this.metaCache[k1 * j1 + k1 * b1 + k1];

            if (l1 < 0) {
                this.removeLeaves(worldIn, x, y, z);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        startVec = startVec.addVector((double) (-x), (double) (-y), (double) (-z));
        endVec = endVec.addVector((double) (-x), (double) (-y), (double) (-z));
        Vec3 vec32, vec33, vec34, vec35, vec36, vec37 = null;
        if (modelContainer.isPresent()) {
            modelContainer.get().getPhysics().findRayframe(vanillaBB, world, x, y, z);
            vec32 = startVec.getIntermediateWithXValue(endVec, modelContainer.get().getPhysics().getRayMinX());
            vec33 = startVec.getIntermediateWithXValue(endVec, modelContainer.get().getPhysics().getRayMaxX());
            vec34 = startVec.getIntermediateWithYValue(endVec, modelContainer.get().getPhysics().getRayMinY());
            vec35 = startVec.getIntermediateWithYValue(endVec, modelContainer.get().getPhysics().getRayMaxY());
            vec36 = startVec.getIntermediateWithZValue(endVec, modelContainer.get().getPhysics().getRayMinZ());
            vec37 = startVec.getIntermediateWithZValue(endVec, modelContainer.get().getPhysics().getRayMaxZ());
        } else {
            vec32 = startVec.getIntermediateWithXValue(endVec, minX);
            vec33 = startVec.getIntermediateWithXValue(endVec, maxX);
            vec34 = startVec.getIntermediateWithYValue(endVec, minY);
            vec35 = startVec.getIntermediateWithYValue(endVec, maxY);
            vec36 = startVec.getIntermediateWithZValue(endVec, minZ);
            vec37 = startVec.getIntermediateWithZValue(endVec, maxZ);
        }

        if (!this.isVecInsideYZBounds(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInsideYZBounds(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInsideXZBounds(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInsideXZBounds(vec35)) {
            vec35 = null;
        }

        if (!this.isVecInsideXYBounds(vec36)) {
            vec36 = null;
        }

        if (!this.isVecInsideXYBounds(vec37)) {
            vec37 = null;
        }

        Vec3 vec38 = null;

        if (vec32 != null && (vec38 == null || startVec.squareDistanceTo(vec32) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec32;
        }

        if (vec33 != null && (vec38 == null || startVec.squareDistanceTo(vec33) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec33;
        }

        if (vec34 != null && (vec38 == null || startVec.squareDistanceTo(vec34) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec34;
        }

        if (vec35 != null && (vec38 == null || startVec.squareDistanceTo(vec35) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec35;
        }

        if (vec36 != null && (vec38 == null || startVec.squareDistanceTo(vec36) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec36;
        }

        if (vec37 != null && (vec38 == null || startVec.squareDistanceTo(vec37) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec37;
        }

        if (vec38 == null) {
            return null;
        } else {
            byte b0 = -1;

            if (vec38 == vec32) {
                b0 = 4;
            }

            if (vec38 == vec33) {
                b0 = 5;
            }

            if (vec38 == vec34) {
                b0 = 0;
            }

            if (vec38 == vec35) {
                b0 = 1;
            }

            if (vec38 == vec36) {
                b0 = 2;
            }

            if (vec38 == vec37) {
                b0 = 3;
            }

            return new MovingObjectPosition(x, y, z, b0, vec38.addVector((double) x, (double) y, (double) z));
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        final ArrayList<ItemStack> drops = Lists.newArrayList();

        if (decayNode != null && decayNode.isEnabled()) {
            if (world.rand.nextInt(20) == 0) {
                for (DropProperty prop : decayNode.getValue()) {
                    final GameObject object = prop.getSource();
                    final int meta = prop.getData();

                    final ItemStack drop;

                    if (object.minecraftObject instanceof Block) {
                        drop = new ItemStack((Block) object.minecraftObject, prop.getAmountProperty().getValueWithinRange(), meta);
                    } else {
                        drop = new ItemStack((Item) object.minecraftObject, prop.getAmountProperty().getValueWithinRange(), meta);
                    }

                    drops.add(drop);
                }
            }
        }
        return drops;
    }

    private void removeLeaves(World world, int x, int y, int z) {
        this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
        world.setBlockToAir(x, y, z);
    }

    private boolean isVecInsideYZBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point != null && point.yCoord >= modelContainer.get().getPhysics().getRayMinY() && point.yCoord <= modelContainer.get()
                    .getPhysics().getRayMaxY() && point.zCoord >= modelContainer.get().getPhysics().getRayMinZ() && point.zCoord <= modelContainer
                    .get().getPhysics().getRayMaxZ();
        } else {
            return point != null && point.yCoord >= minY && point.yCoord <= maxY && point.zCoord >= minZ && point.zCoord <= maxZ;
        }
    }

    private boolean isVecInsideXZBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point != null && point.xCoord >= modelContainer.get().getPhysics().getRayMinX() && point.xCoord <= modelContainer.get()
                    .getPhysics().getRayMaxX() && point.zCoord >= modelContainer.get().getPhysics().getRayMinZ() && point.zCoord <= modelContainer
                    .get().getPhysics().getRayMaxZ();
        } else {
            return point != null && point.xCoord >= minX && point.xCoord <= maxX && point.zCoord >= minZ && point.zCoord <= maxZ;
        }
    }

    private boolean isVecInsideXYBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point != null && point.xCoord >= modelContainer.get().getPhysics().getRayMinX() && point.xCoord <= modelContainer.get()
                    .getPhysics().getRayMaxX() && point.yCoord >= modelContainer.get().getPhysics().getRayMinY() && point.yCoord <= modelContainer
                    .get().getPhysics().getRayMaxY();
        } else {
            return point != null && point.xCoord >= minX && point.xCoord <= maxX && point.yCoord >= minY && point.yCoord <= maxY;

        }
    }
}
