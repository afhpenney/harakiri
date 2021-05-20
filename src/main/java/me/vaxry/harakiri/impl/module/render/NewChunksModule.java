package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.client.EventSaveConfig;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class NewChunksModule extends Module {

    public final Value<Color> color = new Value<Color>("Color", new String[]{"color", "c"}, "Change the color.", new Color(255, 255, 255));
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"Alp", "Opacity", "a", "o"}, "Edit the alpha.", 127, 1, 255, 1);
    public final Value<Float> width = new Value<Float>("Width", new String[]{"W", "size", "s"}, "Edit the width.", 1.5f, 0.1f, 5.0f, 0.1f);

    public enum Mode {
        BOX, OUTLINE, PLANE
    }

    private Color currentColor;
    private final ICamera frustum = new Frustum();

    private final List<ChunkData> chunkDataList = new ArrayList<>();

    public NewChunksModule() {
        super("NewChunks", new String[]{"ChunkGen"}, "Highlights newly generated chunks.", "NONE", -1, ModuleType.RENDER);
        this.currentColor = new Color(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue(), this.alpha.getValue());
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.chunkDataList.clear();
        this.currentColor = new Color(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue(), this.alpha.getValue());
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                if (!packet.isFullChunk()) {
                    final ChunkData chunk = new ChunkData(packet.getChunkX() * 16, packet.getChunkZ() * 16);

                    if (!this.contains(chunk)) {
                        this.chunkDataList.add(chunk);
                    }
                }
            }
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null)
            return;

        RenderUtil.begin3D();
        for (int i = this.chunkDataList.size() - 1; i >= 0; i--) {
            final ChunkData chunkData = this.chunkDataList.get(i);
            if (chunkData != null) {
                this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                final AxisAlignedBB bb = new AxisAlignedBB(chunkData.x, 0, chunkData.z, chunkData.x + 16, 1, chunkData.z + 16);

                if (frustum.isBoundingBoxInFrustum(bb)) {
                    final int color = ColorUtil.changeAlpha(currentColor.getRGB(), this.alpha.getValue());
                    double x = chunkData.x - mc.getRenderManager().viewerPosX;
                    double y = -mc.getRenderManager().viewerPosY;
                    double z = chunkData.z - mc.getRenderManager().viewerPosZ;
                    final AxisAlignedBB chunkBB = new AxisAlignedBB(0, 0, 0, 16, 0, 16);

                    RenderUtil.drawBoundingBox(x, y, z, chunkBB, this.width.getValue(), color);
                }
            }
        }
        RenderUtil.end3D();
    }

    @Listener
    public void onConfigSave(EventSaveConfig event) {
        this.currentColor = new Color(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue());
    }

    private boolean contains(final ChunkData chunkData) {
        boolean temp = false;
        for (ChunkData data : this.chunkDataList) {
            if (data.x == chunkData.x && data.z == chunkData.z)
                temp = true;
        }
        return temp;
    }

    public static class ChunkData {
        private int x;
        private int z;

        public ChunkData(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }

}
