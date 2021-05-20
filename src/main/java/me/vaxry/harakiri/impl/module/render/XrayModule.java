package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockModel;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockSide;
import me.vaxry.harakiri.framework.event.world.EventSetOpaqueCube;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;


import java.util.ArrayList;
import java.util.List;

public final class XrayModule extends Module {

    private List<Integer> ids = new ArrayList<>();

    private float lastGamma;
    private int lastAO;

    public XrayModule() {
        super("Xray", new String[]{"JadeVision", "Jade"}, "Allows you to xray.", "NONE", -1, ModuleType.RENDER);

        if (Harakiri.get().getConfigManager().isFirstLaunch()) {
            this.add("diamond_ore");
            this.add("coal_ore");
            this.add("gold_ore");
            this.add("redstone_ore");
            this.add("iron_ore");
            this.add("lapis_ore");
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        final Minecraft mc = Minecraft.getMinecraft();
        lastGamma = mc.gameSettings.gammaSetting;
        lastAO = mc.gameSettings.ambientOcclusion;

        mc.gameSettings.gammaSetting = 100;
        mc.gameSettings.ambientOcclusion = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Minecraft.getMinecraft().gameSettings.gammaSetting = lastGamma;
        Minecraft.getMinecraft().gameSettings.ambientOcclusion = lastAO;
    }

    @Override
    public void onToggle() {
        super.onToggle();

        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Listener
    public void shouldSideBeRendered(EventRenderBlockSide event) {
        if (this.contains(Block.getIdFromBlock(event.getBlock()))) {
            event.setRenderable(true);
        }
        event.setCanceled(true);
    }

    @Listener
    public void renderBlockModel(EventRenderBlockModel event) {
        final Block block = event.getBlockState().getBlock();
        if (this.contains(Block.getIdFromBlock(block))) {
            if (Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(event.getBlockAccess(), event.getBakedModel(), event.getBlockState(), event.getBlockPos(), event.getBufferBuilder(), event.isCheckSides(), event.getRand())) {
                event.setRenderable(true);
            }
        }
        event.setCanceled(true);
    }

    @Listener
    public void setOpaqueCube(EventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    public void updateRenders() {
        //Minecraft.getMinecraft().renderGlobal.loadRenderers();
        final Minecraft mc = Minecraft.getMinecraft();
        mc.renderGlobal.markBlockRangeForRenderUpdate(
                (int) mc.player.posX - 256,
                (int) mc.player.posY - 256,
                (int) mc.player.posZ - 256,
                (int) mc.player.posX + 256,
                (int) mc.player.posY + 256,
                (int) mc.player.posZ + 256);
    }

    public boolean contains(int id) {
        return this.ids.contains(id);
    }

    public void add(int id) {
        if (!contains(id)) {
            this.ids.add(id);
        }
    }

    public void add(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (!contains(id)) {
                this.ids.add(id);
            }
        }
    }

    public void remove(int id) {
        for (Integer i : this.ids) {
            if (id == i) {
                this.ids.remove(i);
                break;
            }
        }
    }

    public void remove(String name) {
        final Block blockFromName = Block.getBlockFromName(name);
        if (blockFromName != null) {
            final int id = Block.getIdFromBlock(blockFromName);
            if (contains(id)) {
                this.ids.remove(id);
            }
        }
    }

    public int clear() {
        final int count = this.ids.size();
        this.ids.clear();
        return count;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
