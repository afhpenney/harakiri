package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.api.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 7/28/2019 @ 9:43 AM.
 */
public final class ServerBrandComponent extends DraggableHudComponent {

    public ServerBrandComponent() {
        super("ServerBrand");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);


        final String brand = mc.getCurrentServerData() == null ? ChatFormatting.GRAY + "Vanilla" :ChatFormatting.GRAY + mc.getCurrentServerData().gameVersion;

        this.setW(mc.fontRenderer.getStringWidth(brand));
        mc.fontRenderer.drawStringWithShadow(brand, this.getX(), this.getY(), -1);

    }

}