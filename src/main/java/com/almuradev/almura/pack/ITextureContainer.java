/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import net.malisis.core.renderer.icon.ClippedIcon;

import java.util.List;
import java.util.Map;

public interface ITextureContainer {
    Map<Integer, List<Integer>> getTextureCoordinates();
    void setTextureCoordinates(Map<Integer, List<Integer>> coordinates);
    ClippedIcon[] getClipIcons();
}
