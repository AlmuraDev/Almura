package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.component.entity.RenderEntityAngle;
import com.almuradev.almura.client.gui.component.entity.UIEntity;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class UITargetPanel extends UIHUDPanel {

    private final UIPropertyBar targetHealthBar;
    private final EntityLivingBase entity;
    @Nullable private UIImage playerImage;
    @Nullable private UIEntity entityModel;
    @Nullable private UILabel usernameLabel;

    public UITargetPanel(MalisisGui gui, int width, int height, EntityLivingBase entity) {
        super(gui, width, height);
        this.entity = entity;

        // Health bar
        this.targetHealthBar = new UIPropertyBar(gui, width - 30, 9)
                .setPosition(-4, 0, Anchor.MIDDLE | Anchor.RIGHT)
                .setColor(org.spongepowered.api.util.Color.ofRgb(187, 19, 19).getRgb())
                .setIcons(Constants.Gui.VANILLA_ICON_HEART_BACKGROUND, Constants.Gui.VANILLA_ICON_HEART_FOREGROUND);

        // Player image
        if (entity instanceof EntityPlayer) {
            this.playerImage = new UIImage(gui, new GuiRemoteTexture(
                    Constants.Gui.AVATAR_GENERIC_LOCATION,
                    new ResourceLocation(Constants.Plugin.ID, "textures/gui/skins/avatars/" + entity.getUniqueID() + ".png"),
                    String.format(Constants.Gui.SKIN_URL_BASE, entity.getUniqueID().toString(), 16),
                    16, 16), null);
            this.playerImage.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);
            this.usernameLabel = new UILabel(gui, "");
            this.usernameLabel.setPosition(SimpleScreen.getPaddedX(this.playerImage, 2), 2);

            add(this.playerImage, this.usernameLabel);
        } else { // Entity model
            this.entityModel = new UIEntity(gui, 16, 16, entity, RenderEntityAngle.Angles.BACK);
            this.entityModel.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);

            add(this.entityModel);
        }

        add(this.targetHealthBar);
    }

    public final EntityLivingBase getEntity() {
        return this.entity;
    }

    public void updateHealth() {
        this.targetHealthBar.setAmount(MathUtil.ConvertToRange(this.entity.getHealth(),0f, this.entity.getMaxHealth(), 0f, 1f));
    }

    public void updateUsername() {
        if (this.usernameLabel == null) {
            return;
        }
        // TODO Nickname needed
        this.usernameLabel.setText(this.entity.getName());
    }
}
