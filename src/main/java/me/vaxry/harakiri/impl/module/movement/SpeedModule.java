package me.vaxry.harakiri.impl.module.movement;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;


public final class SpeedModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The speed mode to use.", Mode.VANILLA);

    private enum Mode {
        VANILLA, BHOP
    }

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd", "Amount", "A", "S"}, "Speed multiplier.", 0.1f, 0.0f, 10.0f, 0.1f);

    private int tick;
    private double prevDistance;
    private double movementSpeed;

    public SpeedModule() {
        super("Speed", new String[]{"Spd"}, "Allows you to move faster.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (Minecraft.getMinecraft().world != null) {
            this.movementSpeed = getDefaultSpeed();
            this.tick = 1;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null) {
            this.movementSpeed = getDefaultSpeed();
            this.prevDistance = 0;
            this.tick = 4;
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    private double getDefaultSpeed() {
        final Minecraft mc = Minecraft.getMinecraft();
        double defaultSpeed = 0.2873D;

        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            defaultSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }

        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            defaultSpeed /= (1.0D + 0.2D * (amplifier + 1));
        }

        return defaultSpeed;
    }

    Attender<EventMove> onMove = new Attender<>(EventMove.class, event -> {
        if (this.mode.getValue() == Mode.BHOP) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (MathUtil.round(mc.player.posY - (int) mc.player.posY, 3) == MathUtil.round(0.138D, 3)) {
                mc.player.motionY -= 1.0D;
                event.setY(event.getY() - 0.0931D);
                mc.player.posY -= 0.0931D;
            }
            if ((this.tick == 2) && ((mc.player.moveForward != 0.0F) || (mc.player.moveStrafing != 0.0F))) {
                event.setY((mc.player.motionY = 0.39936D));
                this.movementSpeed *= 1.547D;
            } else if (this.tick == 3) {
                final double difference = 0.66D * (this.prevDistance - this.getDefaultSpeed());
                this.movementSpeed = (this.prevDistance - difference);
            } else {
                if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0D, mc.player.motionY, 0.0D)).size() > 0) || (mc.player.collidedVertically)) {
                    this.tick = 1;
                }
                this.movementSpeed = (this.prevDistance - this.prevDistance / 159.0D);
            }

            this.movementSpeed = Math.max(this.movementSpeed, this.getDefaultSpeed());
            final double[] direction = MathUtil.directionSpeed(this.movementSpeed);

            mc.player.motionX = direction[0];
            mc.player.motionZ = direction[1];

            this.tick += 1;
        }
    });

    Attender<EventUpdateWalkingPlayer> onUpdateWalkingPlayer = new Attender<>(EventUpdateWalkingPlayer.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getValue() == Mode.BHOP) {
                final Minecraft mc = Minecraft.getMinecraft();
                final double deltaX = (mc.player.posX - mc.player.prevPosX);
                final double deltaZ = (mc.player.posZ - mc.player.prevPosZ);
                this.prevDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            }
        }
    });

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            final Entity riding = mc.player.getRidingEntity();

            if (riding != null) {
                final double[] dir = MathUtil.directionSpeed(this.speed.getValue());

                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    riding.motionX = dir[0];
                    riding.motionZ = dir[1];
                } else {
                    riding.motionX = 0;
                    riding.motionZ = 0;
                }
            }
        }
    });
}
