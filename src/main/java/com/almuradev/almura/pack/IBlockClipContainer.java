package com.almuradev.almura.pack;

import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockClipContainer extends IClipContainer {

    ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata);
}
