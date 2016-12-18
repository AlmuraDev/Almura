package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.screen.ingame.hud.HUDData;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

@SideOnly(Side.CLIENT)
public class UIUserPanel extends UIHUDPanel {

    private final UIImage currencyImage, userImage;
    private final UILabel currencyLabel, levelLabel, usernameLabel;
    private final UIPropertyBar experienceBar;

    public UIUserPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        // Avatar
        this.userImage = new UIImage(gui, new GuiRemoteTexture(
                GuiConstants.AVATAR_GENERIC_LOCATION,
                new ResourceLocation(Almura.PLUGIN_ID, "textures/gui/skins/avatars/" + Minecraft.getMinecraft().player.getUniqueID() + ".png"),
                String.format(GuiConstants.SKIN_URL_BASE, Minecraft.getMinecraft().player.getUniqueID().toString(), 16),
                16, 16), null);
        this.userImage.setPosition(2, 2);
        this.userImage.setSize(16, 16);

        // Username
        // TODO Needs to pull player nickname
        this.usernameLabel = new UILabel(gui, TextFormatting.WHITE + Minecraft.getMinecraft().player.getName());
        this.usernameLabel.setPosition(SimpleScreen.getPaddedX(this.userImage, 3), 2);

        // Level
        this.levelLabel = new UILabel(gui, "");
        this.levelLabel.setPosition(SimpleScreen.getPaddedX(this.userImage, 3), SimpleScreen.getPaddedY(this.usernameLabel, 0));
        this.levelLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);
        this.experienceBar = new UIPropertyBar(gui, width - 10, 5);
        this.experienceBar.setPosition(0, -3, Anchor.BOTTOM | Anchor.CENTER);
        this.experienceBar.setColor(org.spongepowered.api.util.Color.ofRgb(0, 211, 0).getRgb());

        // Currency
        this.currencyImage = new UIImage(gui, MalisisGui.BLOCK_TEXTURE, Icon.from(Items.EMERALD));
        this.currencyImage.setSize(8, 8);
        this.currencyLabel = new UILabel(gui, "");

        this.add(this.userImage, this.usernameLabel, this.levelLabel, this.experienceBar, this.currencyImage, this.currencyLabel);
    }

    public void updateCurrency() {
        this.currencyImage.setVisible(HUDData.IS_ECONOMY_PRESENT);
        this.currencyLabel.setVisible(HUDData.IS_ECONOMY_PRESENT);
        if (HUDData.IS_ECONOMY_PRESENT) {
            this.currencyImage.setPosition(SimpleScreen.getPaddedX(this.levelLabel, 2), SimpleScreen.getPaddedY(this.usernameLabel, 0));
            this.currencyLabel.setText(TextFormatting.WHITE + HUDData.PLAYER_CURRENCY);
            this.currencyLabel.setPosition(SimpleScreen.getPaddedX(this.currencyImage, 2), SimpleScreen.getPaddedY(this.usernameLabel, 1));
        }
    }

    public void updateExperience() {
        final int experienceCap = Minecraft.getMinecraft().player.xpBarCap();
        final int experience = (int) (Minecraft.getMinecraft().player.experience * experienceCap);

        this.experienceBar.setText(Text.of(experience + "/" + experienceCap));
        this.experienceBar.setAmount(MathUtil.ConvertToRange(experience, 0, experienceCap, 0f, 1f));
    }

    public void updateLevel() {
        this.levelLabel.setText("Lv. " + Minecraft.getMinecraft().player.experienceLevel);
    }
}
