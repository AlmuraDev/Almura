package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.model.PackShape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.item.ItemSeeds;

public class PackSeeds extends ItemSeeds implements IPackObject, IClipContainer, IShapeContainer {

    private final ContentPack pack;

    public PackSeeds(ContentPack pack, String identifier, String textureName, boolean showInCreativeTab,
                     String creativeTabName, Block crop, Block soil) {
        super(crop, soil);
        this.pack = pack;
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return new ClippedIcon[0];
    }

    @Override
    public PackShape getShape() {
        return null;
    }

    @Override
    public void refreshShape() {

    }
}
