package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;


public final class KillAuraModule extends Module {

    public boolean killAuraHit = false;

    public final Value<Boolean> players = new Value<>("Players", new String[]{"Player"}, "Choose to target players.", true);
    public final Value<Boolean> mobs = new Value<>("Mobs", new String[]{"Mob"}, "Choose to target mobs.", true);
    public final Value<Boolean> animals = new Value<>("Animals", new String[]{"Animal"}, "Choose to target animals.", true);
    public final Value<Boolean> vehicles = new Value<>("Vehicles", new String[]{"Vehic", "Vehicle"}, "Choose to target vehicles.", true);
    public final Value<Boolean> projectiles = new Value<>("Projectiles", new String[]{"Projectile", "Proj"}, "Choose to target projectiles.", true);

    public final Value<Float> range = new Value<>("Range", new String[]{"Dist"}, "The minimum range to attack.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> coolDown = new Value<>("CoolDown", new String[]{"CoolD"}, "Delay your hits to gain damage.", true);
    public final Value<Boolean> sync = new Value<>("Sync", new String[]{"snc"}, "Sync your hits with the server's estimated TPS.", true);
    //public final Value<Boolean> teleport = new Value<>("Teleport", new String[]{"tp"}, "Teleports to your target(Only works on vanilla).", false);

    private final RotationTask rotationTask = new RotationTask("KillAuraTask", 5);

    private Entity currentTarget = null;

    public KillAuraModule() {
        super("KillAura", new String[]{"Aura"}, "Automatically aims and attacks enemies", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        switch (event.getStage()) {
            case PRE:
                this.currentTarget = this.findTarget();
                if (this.currentTarget != null) {
                    final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), this.currentTarget.getPositionEyes(mc.getRenderPartialTicks()));

                    Harakiri.get().getRotationManager().startTask(this.rotationTask);
                    if (this.rotationTask.isOnline()) {
                        Harakiri.get().getRotationManager().setPlayerRotations(angle[0], angle[1]);
                    }
                }
                break;
            case POST:
                if (this.currentTarget != null) {
                    final float ticks = 20.0f - Harakiri.get().getTickRateManager().getTickRate();
                    final boolean canAttack = this.rotationTask.isOnline() && (!this.coolDown.getValue() || (mc.player.getCooledAttackStrength(this.sync.getValue() ? -ticks : 0.0f) >= 1));
                    if (canAttack) {
                        killAuraHit = true;
                        final ItemStack stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
                        if (!stack.isEmpty() && stack.getItem() == Items.SHIELD) {
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                        }

                        mc.player.connection.sendPacket(new CPacketUseEntity(this.currentTarget));
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        mc.player.resetCooldown();
                        killAuraHit = false;
                    }
                } else {
                    Harakiri.get().getRotationManager().finishTask(this.rotationTask);
                }
                break;
        }
    }

    private Entity findTarget() {
        Entity ent = null;

        final Minecraft mc = Minecraft.getMinecraft();

        float maxDist = this.range.getValue();

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.checkFilter(e)) {
                    float currentDist = mc.player.getDistance(e);

                    if (currentDist <= maxDist) {
                        maxDist = currentDist;
                        ent = e;
                    }
                }
            }
        }

        return ent;
    }

    private boolean checkFilter(Entity entity) {
        boolean ret = false;

        if (this.players.getValue() && entity instanceof EntityPlayer && entity != Minecraft.getMinecraft().player && Harakiri.get().getFriendManager().isFriend(entity) == null && !entity.getName().equals(Minecraft.getMinecraft().player.getName())) {
            ret = true;
        }

        if (this.mobs.getValue() && entity instanceof IMob) {
            ret = true;
        }

        if (this.animals.getValue() && entity instanceof IAnimals && !(entity instanceof IMob)) {
            ret = true;
        }

        if (this.vehicles.getValue() && (entity instanceof EntityBoat || entity instanceof EntityMinecart || entity instanceof EntityMinecartContainer)) {
            ret = true;
        }

        if (this.projectiles.getValue() && (entity instanceof EntityShulkerBullet || entity instanceof EntityFireball)) {
            ret = true;
        }

        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            if (entityLivingBase.getHealth() <= 0) {
                ret = false;
            }
        }

        return ret;
    }

}
