package com.almuradev.almura.feature.special;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.special.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.special.wand.item.LightRepairWand;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class SpecialItems {

    @GameRegistry.ObjectHolder(Almura.ID + ":normal/farming/farmers_almanac")
    public static final FarmersAlmanacItem FARMERS_ALMANAC = null;

    @GameRegistry.ObjectHolder(Almura.ID + ":normal/tool/light_repair_wand")
    public static final LightRepairWand WAND_LIGHT_REPAIR = null;

    @GameRegistry.ObjectHolder(Almura.ID + ":normal/farming/irrigation_pipe")
    public static final ItemBlock IRRIGATION_PIPE = null;
}
