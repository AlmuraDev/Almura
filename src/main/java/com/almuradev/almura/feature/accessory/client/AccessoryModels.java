/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 3.5's accessory models, as model parts in the units of the player model (a pixel each), on 64 by 32 textures. Each is built once; the
 * accessory layer places them on the head, body or arm, and moves the tail and wings with the pose it gives them, so a part's own pose never
 * changes.
 */
final class AccessoryModels {

    private static final int TEXTURE_WIDTH = 64, TEXTURE_HEIGHT = 32;
    /**
     * Where hat brims sit, as 3.5: their top just above the skin's hat layer (which reaches y -8.5), so the head and hair never show
     * through the brim.
     */
    private static final float BRIM_TOP = -8.6F;

    static final ModelPart TOP_HAT = bake(root -> root.addOrReplaceChild("hat", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-5.5F, BRIM_TOP, -5.5F, 11, 2, 11)
        .texOffs(0, 13).addBox(-3.5F, BRIM_TOP - 8F, -3.5F, 7, 8, 7), PartPose.ZERO));

    static final ModelPart NOTCH_HAT = bake(root -> root.addOrReplaceChild("hat", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-5F, BRIM_TOP, -5F, 10, 1, 10)
        .texOffs(0, 11).addBox(-4F, BRIM_TOP - 4F, -4F, 8, 4, 8), PartPose.ZERO));

    static final ModelPart CAT_HAT = bake(root -> root.addOrReplaceChild("hat", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-5F, BRIM_TOP, -5F, 10, 2, 10)
        .texOffs(0, 12).addBox(-4F, BRIM_TOP - 10F, -4F, 8, 10, 8), PartPose.ZERO));

    /**
     * A round ring (radius 5, wider than the 8 wide head) floating above the head: 16 short segments, each turned a step further round.
     */
    static final ModelPart HALO = bake(root -> {
        final int segments = 16;
        for (int i = 0; i < segments; i++) {
            root.addOrReplaceChild("segment" + i, CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -0.5F, -5.5F, 2, 1, 1),
                PartPose.offsetAndRotation(0F, -10F, 0F, 0F, (float) (i * 2 * Math.PI / segments), 0F));
        }
    });

    /**
     * The sunglasses textures are painted like a skin's head layer, so they are a shell round the head, just outside the hat layer, a pixel
     * up to sit at eye level.
     */
    static final ModelPart SUNGLASSES = bake(root -> root.addOrReplaceChild("shell", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-4F, -9F, -4F, 8, 8, 8, new CubeDeformation(0.6F)), PartPose.ZERO));

    static final ModelPart EAR = bake(root -> root.addOrReplaceChild("ear", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-3F, -6F, -1F, 6, 6, 1), PartPose.ZERO));

    static final ModelPart TAIL = bake(root -> root.addOrReplaceChild("tail", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-0.5F, 0F, -0.5F, 1, 10, 1), PartPose.ZERO));

    static final ModelPart BRACELET_WIDE = bake(root -> root.addOrReplaceChild("bracelet", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-1F, 7F, -3F, 4, 1, 1)
        .texOffs(10, 0).addBox(-2F, 7F, -3F, 1, 1, 6)
        .texOffs(0, 2).addBox(-1F, 7F, 2F, 4, 1, 1)
        .texOffs(0, 4).addBox(3F, 7F, -3F, 1, 1, 6), PartPose.ZERO));

    static final ModelPart BRACELET_SLIM = bake(root -> root.addOrReplaceChild("bracelet", CubeListBuilder.create()
        .texOffs(0, 0).addBox(-1F, 7F, -3F, 3, 1, 1)
        .texOffs(10, 0).addBox(-2F, 7F, -3F, 1, 1, 6)
        .texOffs(0, 2).addBox(-1F, 7F, 2F, 3, 1, 1)
        .texOffs(0, 4).addBox(2F, 7F, -3F, 1, 1, 6), PartPose.ZERO));

    /**
     * 3.5's wings: nine feathers a side, each a 1 by 10 slab of its depth fanned out round the spine. Each feather's turn is in
     * {@link #WING_ANGLES} (left, then right: x, y, z), applied by the layer with the flap added to its y.
     */
    static final int FEATHERS = 9;
    static final float[][] WING_ANGLES = new float[FEATHERS * 2][];
    static final ModelPart WINGS;

    static {
        final int[][] left = {{60, 0, -1, 2, 2, 1}, {56, 0, -1, 1, 3, 1}, {50, 0, -1, 0, 4, 2}, {46, 0, -1, -1, 6, 1}, {38, 0, -1, -2, 7, 3},
            {34, 0, -1, -1, 10, 1}, {30, 0, -1, 0, 11, 1}, {26, 0, -1, 1, 12, 1}, {22, 0, -1, 3, 13, 1}};
        final float[][] leftAngles = {{0F, 0.4833219F, 0F}, {0F, 0.5007752F, 0.0174533F}, {0F, 0.5182285F, 0.0349066F},
            {0F, 0.5356818F, 0.0523599F}, {0F, 0.5531351F, 0.0698132F}, {0F, 0.5531351F, 0.0523599F}, {0F, 0.5705884F, 0.0349066F},
            {0F, 0.5880417F, 0.0174533F}, {0F, 0.5880417F, 0F}};
        final int[][] right = {{60, 21, 0, 2, 2, 1}, {56, 21, 0, 1, 3, 1}, {50, 20, 0, 0, 4, 2}, {46, 21, 0, -1, 6, 1}, {38, 19, 0, -2, 7, 3},
            {34, 21, 0, -1, 10, 1}, {30, 21, 0, 0, 11, 1}, {26, 21, 0, 1, 12, 1}, {22, 21, 0, 3, 13, 1}};
        final float[][] rightAngles = {{0F, -0.4833166F, 0F}, {0F, -0.5007699F, -0.0174533F}, {0F, -0.5182232F, -0.0349066F},
            {0.0174533F, -0.5356765F, -0.0523599F}, {0.0174533F, -0.5531297F, -0.0698132F}, {0.0174533F, -0.570583F, -0.0523599F},
            {0.0174533F, -0.5880363F, -0.0349066F}, {0.0174533F, -0.6054896F, -0.0174533F}, {0.0174533F, -0.6229429F, 0F}};
        WINGS = bake(root -> {
            for (int i = 0; i < FEATHERS; i++) {
                feather(root, "left" + i, left[i]);
                feather(root, "right" + i, right[i]);
            }
        });
        for (int i = 0; i < FEATHERS; i++) {
            WING_ANGLES[i] = leftAngles[i];
            WING_ANGLES[FEATHERS + i] = rightAngles[i];
        }
    }

    private AccessoryModels() {
    }

    // u, v, x, y, z, depth: a 1 wide, 10 high slab
    private static void feather(final PartDefinition root, final String name, final int[] f) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(f[0], f[1]).addBox(f[2], f[3], f[4], 1, 10, f[5]), PartPose.ZERO);
    }

    private interface Parts {

        void add(PartDefinition root);
    }

    private static ModelPart bake(final Parts parts) {
        final MeshDefinition mesh = new MeshDefinition();
        parts.add(mesh.getRoot());
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT).bakeRoot();
    }
}
