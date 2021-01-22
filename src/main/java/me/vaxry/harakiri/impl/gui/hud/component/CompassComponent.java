package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.api.util.ColorUtil;
import me.vaxry.harakiri.api.util.MathUtil;
import me.vaxry.harakiri.api.util.RenderUtil;
import me.vaxry.harakiri.impl.module.world.WaypointsModule;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/**
 * created by noil on 9/29/2019 at 9:36 AM
 */
public final class CompassComponent extends DraggableHudComponent {

    public CompassComponent() {
        super("Compass");

        this.setW(100);
        this.setH(9);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        // Background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);

        if (mc.world != null) {
            final ScaledResolution sr = new ScaledResolution(mc);
            final String host = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "localhost";
            float playerYaw = mc.player.rotationYaw;
            float rotationYaw = MathUtil.wrap(playerYaw);

            // Begin scissor area
            RenderUtil.glScissor(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), sr);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            // 0, 0
            final float zeroZeroYaw = MathUtil.wrap((float) (Math.atan2(0 - mc.player.posZ, 0 - mc.player.posX) * 180.0d / Math.PI) - 90.0f);
            RenderUtil.drawLine(this.getX() - rotationYaw + (this.getW() / 2) + zeroZeroYaw, this.getY() + 2, this.getX() - rotationYaw + (this.getW() / 2) + zeroZeroYaw, this.getY() + this.getH() - 2, 2, 0xFFFF1010);

            // Waypoints
            if (!Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().isEmpty()) {
                for (WaypointsModule.WaypointData waypointData : Harakiri.INSTANCE.getWaypointManager().getWaypointDataList()) {
                    if (!waypointData.getHost().equals(host) || waypointData.getDimension() != mc.player.dimension)
                        continue;

                    final float waypointDataYaw = MathUtil.wrap((float) (Math.atan2(waypointData.getZ() - mc.player.posZ, waypointData.getX() - mc.player.posX) * 180.0d / Math.PI) - 90.0f);
                    RenderUtil.drawTriangle(this.getX() - rotationYaw + (this.getW() / 2) + waypointDataYaw, this.getY() + (this.getH() / 2), 3, 180, ColorUtil.changeAlpha(waypointData.getColor(), 0xFF));
                }
            }

            // North
            //RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) + 180, this.getY(), (this.getX() - rotationYaw + (this.getW() / 2)) + 180, this.getY() + this.getH(), 2, 0xFFFFFFFF);
            //RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) - 180, this.getY(), (this.getX() - rotationYaw + (this.getW() / 2)) - 180, this.getY() + this.getH(), 2, 0xFFFFFFFF);
            // East
            //RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) - 90, this.getY(), (this.getX() - rotationYaw + (this.getW() / 2)) - 90, this.getY() + this.getH(), 2, 0xFFFFFFFF);
            // South
            //RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)), this.getY(), (this.getX() - rotationYaw + (this.getW() / 2)), this.getY() + this.getH(), 2, 0xFFFFFFFF);
            // West
            //RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) + 90, this.getY(), (this.getX() - rotationYaw + (this.getW() / 2)) + 90, this.getY() + this.getH(), 2, 0xFFFFFFFF);
            // South west
            RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) + 45, this.getY() + 2, (this.getX() - rotationYaw + (this.getW() / 2)) + 45, this.getY() + this.getH() - 2, 2, 0xFFFFFFFF);
            // South east
            RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) - 45, this.getY() + 2, (this.getX() - rotationYaw + (this.getW() / 2)) - 45, this.getY() + this.getH() - 2, 2, 0xFFFFFFFF);
            // North west
            RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) + 135, this.getY() + 2, (this.getX() - rotationYaw + (this.getW() / 2)) + 135, this.getY() + this.getH() - 2, 2, 0xFFFFFFFF);
            // North east
            RenderUtil.drawLine((this.getX() - rotationYaw + (this.getW() / 2)) - 135, this.getY() + 2, (this.getX() - rotationYaw + (this.getW() / 2)) - 135, this.getY() + this.getH() - 2, 2, 0xFFFFFFFF);

            // Text
            mc.fontRenderer.drawStringWithShadow("n", (this.getX() - rotationYaw + (this.getW() / 2)) + 180 - mc.fontRenderer.getStringWidth("n") / 2.0f, this.getY(), 0xFFFFFFFF);
            mc.fontRenderer.drawStringWithShadow("n", (this.getX() - rotationYaw + (this.getW() / 2)) - 180 - mc.fontRenderer.getStringWidth("n") / 2.0f, this.getY(), 0xFFFFFFFF);
            mc.fontRenderer.drawStringWithShadow("e", (this.getX() - rotationYaw + (this.getW() / 2)) - 90 - mc.fontRenderer.getStringWidth("e") / 2.0f, this.getY(), 0xFFFFFFFF);
            mc.fontRenderer.drawStringWithShadow("s", (this.getX() - rotationYaw + (this.getW() / 2)) - mc.fontRenderer.getStringWidth("s") / 2.0f, this.getY(), 0xFFFFFFFF);
            mc.fontRenderer.drawStringWithShadow("w", (this.getX() - rotationYaw + (this.getW() / 2)) + 90 - mc.fontRenderer.getStringWidth("w") / 2.0f, this.getY(), 0xFFFFFFFF);

            // Centered line
            RenderUtil.drawLine((this.getX() + this.getW() / 2), this.getY() + 1, (this.getX() + this.getW() / 2), this.getY() + this.getH() - 1, 2, 0xFF909090);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            mc.fontRenderer.drawStringWithShadow("(compass)", (this.getX() + (this.getW() / 2.0f)) - mc.fontRenderer.getStringWidth("(compass)") / 2.0f, this.getY(), 0xFFAAAAAA);
        }
    }
}
