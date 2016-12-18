package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.client.gui.screen.ingame.hud.HUDData;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIWorldPanel extends UIHUDPanel {

    private final UILabel compassLabel, worldLabel;

    public UIWorldPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.compassLabel = new UILabel(gui, "");
        this.compassLabel.setPosition(0, -1, Anchor.BOTTOM | Anchor.CENTER);

        this.worldLabel = new UILabel(gui, "");
        this.worldLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);
        this.worldLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);

        this.add(this.compassLabel, this.worldLabel);
    }

    public void updateCompass() {
        this.compassLabel.setText(HUDData.getCompass());
        this.compassLabel.setPosition(0, -1, Anchor.BOTTOM | Anchor.CENTER);
    }

    public void updateWorld() {
        this.worldLabel.setText(HUDData.WORLD_NAME);
        this.worldLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);
    }
}
