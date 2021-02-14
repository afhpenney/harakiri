package me.vaxry.harakiri.impl.module.render;

import io.netty.util.internal.ReflectionUtil;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.render.EventRenderEntity;
import me.vaxry.harakiri.framework.extd.ShaderGroupExt;
import me.vaxry.harakiri.framework.layeredit.LayerEnderman;
import me.vaxry.harakiri.framework.layeredit.LayerSpider;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.util.*;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.client.renderer.entity.layers.LayerEndermanEyes;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerSpiderEyes;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */

public final class ESPModule extends Module {

    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Items", "i"}, "Draw Items", false);
    public final Value<Boolean> hostile = new Value<Boolean>("Hostile", new String[]{"Hostile", "h"}, "Draw Hostile Entities", false);
    public final Value<Boolean> passive = new Value<Boolean>("Passive", new String[]{"Passive", "p"}, "Draw Hostile Entities", false);
    public final Value<Boolean> crystals = new Value<Boolean>("Crystals", new String[]{"Crystals", "c"}, "Draw Crystals", false);
    public final Value<Boolean> players = new Value<Boolean>("Players", new String[]{"Players", "pl"}, "Draw Players", false);

    private ResourceLocation shader;
    private boolean toLoadShader = true;
    private ICamera cam = new Frustum();

    private float ITEM_A = 0.35f;
    private HashMap<EntityItem, Float> opacity = new HashMap<>();
    private Timer timer = new Timer();

    private ArrayList<EntityLivingBase> livingBases = new ArrayList<>();
    private static List<LayerRenderer<EntityLivingBase>> renderLivingBaseLayerRenderersField;

    // scoreboard stuff //
    private Scoreboard board;
    private ScorePlayerTeam green;
    private ScorePlayerTeam red;
    private ScorePlayerTeam purple;
    private ScorePlayerTeam lblue;

    private Class[] hostileMobs = {EntitySpider.class, EntityCaveSpider.class, EntityEnderman.class,EntityPigZombie.class,
        EntityEvoker.class, EntityVindicator.class, EntityVex.class, EntityEndermite.class, EntityGuardian.class, EntityElderGuardian.class,
        EntityShulker.class, EntityHusk.class, EntityStray.class, EntityBlaze.class, EntityCreeper.class, EntityGhast.class, EntityMagmaCube.class,
        EntitySilverfish.class, EntitySkeleton.class, EntitySlime.class, EntityZombie.class, EntityZombieVillager.class, EntityDragon.class, EntityWitch.class};
    private List<Class> hostileMobsList = new ArrayList<>(Arrays.asList(hostileMobs));

    public ESPModule() {
        super("ESP", new String[]{"ESP"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);

        Minecraft mc = Minecraft.getMinecraft();

        shader = new ResourceLocation("shaders/post/esp.json");
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = 100.f;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    @Listener
    public void OnRender3D(EventRender3D event){
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        if(this.items.getValue()){
            RenderUtil.begin3D();

            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

            // Render itemz
            for(Entity e : mc.world.getLoadedEntityList()){
                if(!(e instanceof EntityItem))
                    continue;

                cam.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                AxisAlignedBB bb = new AxisAlignedBB(
                        e.posX - ITEM_A/2.f - mc.getRenderManager().viewerPosX,
                        e.posY - mc.getRenderManager().viewerPosY,
                        e.posZ - ITEM_A/2.f - mc.getRenderManager().viewerPosZ,
                        e.posX + ITEM_A/2.f - mc.getRenderManager().viewerPosX,
                        e.posY + ITEM_A - mc.getRenderManager().viewerPosY,
                        e.posZ + ITEM_A/2.f - mc.getRenderManager().viewerPosZ);

                if (!cam.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    continue;
                }

                if(!opacity.containsKey(e)){
                    opacity.put((EntityItem)e, 0.f);
                }

                int color = 0x1000000 * opacity.get(e).intValue() + 0xCCFF33;

                RenderUtil.drawBoundingBox(bb, 0.5f, color);
            }

            float jitter = getJitter();

            for(Map.Entry<EntityItem, Float> entry : opacity.entrySet()){
                opacity.put(entry.getKey(), Math.min(255.f, entry.getValue() + jitter));
            }

            RenderUtil.end3D();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        toLoadShader = true;
    }

    @SubscribeEvent
    public void onRenderLivingBasePre(RenderLivingEvent.Specials.Pre<EntityLivingBase> ent){
        Minecraft mc = Minecraft.getMinecraft();
        if(toLoadShader) {

            // Create the shader
            try {
                this.board = mc.player.getWorldScoreboard();
                this.green = board.createTeam("haraGreen");
                this.red = board.createTeam("haraRed");
                this.purple = board.createTeam("haraPurple");
                this.lblue = board.createTeam("haraLBlue");
                this.green.setPrefix(TextFormatting.GREEN.toString());
                this.red.setPrefix(TextFormatting.RED.toString());
                this.purple.setPrefix(TextFormatting.LIGHT_PURPLE.toString());
                this.lblue.setPrefix(TextFormatting.BLUE.toString());

                mc.renderGlobal.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                mc.renderGlobal.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                mc.renderGlobal.entityOutlineFramebuffer = mc.renderGlobal.entityOutlineShader.getFramebufferRaw("final");

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            }catch(Throwable t){
                Harakiri.INSTANCE.logChat("Shader failed: " + t.getMessage());
                //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }

            toLoadShader = false;
        }

        if(!livingBases.contains(ent.getEntity())){
            livingBases.add(ent.getEntity());
            replaceLayers(ent.getEntity());
        }

        if(this.isEnabled() &&
                ((hostileMobsList.contains(ent.getEntity().getClass()) && hostile.getValue()) ||
                        (!hostileMobsList.contains(ent.getEntity().getClass()) && passive.getValue())) ||
                            (ent.getEntity() instanceof EntityPlayer && players.getValue())) {
            ent.getEntity().setGlowing(true);
        } else
            ent.getEntity().setGlowing(false);


        if(hostileMobsList.contains(ent.getEntity().getClass())) {
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
        }
        else if(!(ent.getEntity() instanceof EntityPlayer)){
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), green.getName());
        }else{
            // Player.
            if(Harakiri.INSTANCE.getFriendManager().isFriend(ent.getEntity()) != null){
                //friend
                board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), lblue.getName());
            }else{
                // Not friend.
                board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
            }
        }
    }

    @Listener
    public void onRenderEntity(EventRenderEntity event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(toLoadShader) {

            // Create the shader
            try {
                this.board = mc.player.getWorldScoreboard();
                this.green = board.createTeam("haraGreen");
                this.red = board.createTeam("haraRed");
                this.purple = board.createTeam("haraPurple");
                this.lblue = board.createTeam("haraLBlue");
                this.green.setPrefix(TextFormatting.GREEN.toString());
                this.red.setPrefix(TextFormatting.RED.toString());
                this.purple.setPrefix(TextFormatting.LIGHT_PURPLE.toString());
                this.lblue.setPrefix(TextFormatting.BLUE.toString());

                mc.renderGlobal.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                mc.renderGlobal.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                mc.renderGlobal.entityOutlineFramebuffer = mc.renderGlobal.entityOutlineShader.getFramebufferRaw("final");

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            }catch(Throwable t){
                Harakiri.INSTANCE.logChat("Shader failed: " + t.getMessage());
                //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }

            toLoadShader = false;
        }

        if(this.isEnabled() && event.getEntity() instanceof EntityEnderCrystal && this.crystals.getValue()){
            event.getEntity().setGlowing(true);
            board.addPlayerToTeam(event.getEntity().getUniqueID().toString(), purple.getName());
        }else if(event.getEntity() instanceof EntityEnderCrystal){
            event.getEntity().setGlowing(false);
        }

    }

    @SubscribeEvent
    public void onRenderLivingBasePost(RenderLivingEvent.Specials.Post<EntityLivingBase> ent){

        // if(ent.getEntity() instanceof EntityPlayer)
        //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), red);
        // else
        //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), green);

    }

    private static void replaceLayers(EntityLivingBase livingBase)
    {
        Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(livingBase);
        renderLivingBaseLayerRenderersField = ReflectionHelper.getPrivateValue(RenderLivingBase.class, (RenderLivingBase)render, "field_177097_h", "layerRenderers");
        try
        {
            List<LayerRenderer<EntityLivingBase>> list = renderLivingBaseLayerRenderersField;
            for (LayerRenderer layer : list.toArray(new LayerRenderer[list.size()]))
            {
                if (layer instanceof LayerSpiderEyes)
                {
                    list.remove(layer);
                    list.add(new LayerSpider((RenderSpider) render));
                }
                else if (layer instanceof LayerEndermanEyes)
                {
                    list.remove(layer);
                    list.add(new LayerEnderman((RenderEnderman) render));
                }
            }
        }
        catch (Throwable t)
        {
            //oops
        }
    }
}
