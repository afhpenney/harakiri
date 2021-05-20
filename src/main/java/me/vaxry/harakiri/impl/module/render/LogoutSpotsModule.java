package me.vaxry.harakiri.impl.module.render;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventPlayerJoin;
import me.vaxry.harakiri.framework.event.player.EventPlayerLeave;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;


import java.util.Map;

public final class LogoutSpotsModule extends Module {

    public final Value<Integer> removeDistance = new Value<Integer>("RemoveDistance", new String[]{"RD", "RemoveRange"}, "Minimum distance in blocks the player must be away from the spot for it to be deleted.", 200, 1, 2000, 1);

    private final Map<String, EntityPlayer> playerCache = Maps.newConcurrentMap();
    private final Map<String, PlayerData> logoutCache = Maps.newConcurrentMap();

    public LogoutSpotsModule() {
        super("LogoutSpots", new String[]{"Logout", "Spots"}, "Draws the location of player logouts.", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        playerCache.clear();
        logoutCache.clear();
    }

    @Listener
    public void onPlayerUpdate(EventPlayerUpdate event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        for (EntityPlayer player : mc.world.playerEntities) {
            if (player == null || player.equals(mc.player))
                continue;

            this.updatePlayerCache(player.getGameProfile().getId().toString(), player);
        }
    }

    @Listener
    public void onRenderWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (String uuid : this.logoutCache.keySet()) {
            final PlayerData data = this.logoutCache.get(uuid);

            if (this.isOutOfRange(data)) {
                this.logoutCache.remove(uuid);
                continue;
            }

            data.ghost.prevLimbSwingAmount = 0;
            data.ghost.limbSwing = 0;
            data.ghost.limbSwingAmount = 0;
            data.ghost.hurtTime = 0;

            GlStateManager.pushMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.color(1, 1, 1, 0.3f);
            mc.getRenderManager().renderEntity(data.ghost, data.position.x - mc.getRenderManager().renderPosX, data.position.y - mc.getRenderManager().renderPosY, data.position.z - mc.getRenderManager().renderPosZ, data.ghost.rotationYaw, mc.getRenderPartialTicks(), false);
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (String uuid : this.logoutCache.keySet()) {
            final PlayerData data = this.logoutCache.get(uuid);

            if (this.isOutOfRange(data)) {
                this.logoutCache.remove(uuid);
                continue;
            }

            final GLUProjection.Projection projection = GLUProjection.getInstance().project(data.position.x - mc.getRenderManager().renderPosX, data.position.y - mc.getRenderManager().renderPosY, data.position.z - mc.getRenderManager().renderPosZ, GLUProjection.ClampMode.NONE, true);
            if (projection != null && projection.isType(GLUProjection.Projection.Type.INSIDE)) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(projection.getX(), projection.getY(), 0);
                String text = data.profile.getName() + " logout";
                float textWidth = Harakiri.get().getTTFFontUtil().getStringWidth(text);
                Harakiri.get().getTTFFontUtil().drawStringWithShadow(text, -(textWidth / 2), 0, -1);
                GlStateManager.translate(-projection.getX(), -projection.getY(), 0);
                GlStateManager.popMatrix();
            }
        }
    }

    @Listener
    public void onPlayerLeave(EventPlayerLeave event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (String uuid : this.playerCache.keySet()) {
            if (!uuid.equals(event.getUuid())) // not matching uuid
                continue;

            final EntityPlayer player = this.playerCache.get(uuid);

            //final Vec3d interp = MathUtil.interpolateEntity(player, mc.getRenderPartialTicks());
            final PlayerData data = new PlayerData(player.getPositionVector(), player.getGameProfile(), player);

            if (!this.hasPlayerLogged(uuid)) {
                this.logoutCache.put(uuid, data);
            }
        }

        this.playerCache.clear();
    }

    @Listener
    public void onPlayerJoin(EventPlayerJoin event) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (String uuid : this.logoutCache.keySet()) {
            if (!uuid.equals(event.getUuid())) // not matching uuid
                continue;

            this.logoutCache.remove(uuid);
        }

        this.playerCache.clear();
    }

    private void cleanLogoutCache(String uuid) {
        this.logoutCache.remove(uuid);
    }

    private void updatePlayerCache(String uuid, EntityPlayer player) {
        this.playerCache.put(uuid, player);
    }

    private boolean hasPlayerLogged(String uuid) {
        return this.logoutCache.containsKey(uuid);
    }

    private boolean isOutOfRange(PlayerData data) {
        Vec3d position = data.position;
        return Minecraft.getMinecraft().player.getDistance(position.x, position.y, position.z) > this.removeDistance.getValue();
    }

    public Map<String, EntityPlayer> getPlayerCache() {
        return playerCache;
    }

    public Map<String, PlayerData> getLogoutCache() {
        return logoutCache;
    }

    private class PlayerData {
        Vec3d position;
        GameProfile profile;
        EntityPlayer ghost;

        public PlayerData(Vec3d position, GameProfile profile, EntityPlayer ghost) {
            this.position = position;
            this.profile = profile;
            this.ghost = ghost;
        }
    }
}