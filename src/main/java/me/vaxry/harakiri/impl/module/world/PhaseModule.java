package me.vaxry.harakiri.impl.module.world;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.gui.EventRenderHelmet;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.*;
import me.vaxry.harakiri.framework.event.render.EventRenderOverlay;
import me.vaxry.harakiri.framework.event.world.EventAddCollisionBox;
import me.vaxry.harakiri.framework.event.world.EventSetOpaqueCube;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;



public final class PhaseModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The phase mode to use.", Mode.SAND);

    private enum Mode {
        SAND, PACKET, SKIP, NOCLIP
    }

    public final Value<Boolean> floor = new Value<Boolean>("Floor", new String[]{"Fl"}, "Prevents falling out of the world if enabled.", true);

    public PhaseModule() {
        super("Phase", new String[]{"NoClip"}, "Allows you to go through blocks.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    Attender<EventSetOpaqueCube> onSetOpaqueCube = new Attender<>(EventSetOpaqueCube.class, event -> event.setCanceled(true));
    Attender<EventRenderOverlay> onRenderOverlay = new Attender<>(EventRenderOverlay.class, event -> event.setCanceled(true));
    Attender<EventRenderHelmet> onRenderHelmet = new Attender<>(EventRenderHelmet.class, event -> event.setCanceled(true));
    Attender<EventPushOutOfBlocks> onPushOutOfBlocks = new Attender<>(EventPushOutOfBlocks.class, event -> event.setCanceled(true));
    Attender<EventPushedByWater> onPushedByWater = new Attender<>(EventPushedByWater.class, event -> event.setCanceled(true));
    Attender<EventApplyCollision> onApplyCollision = new Attender<>(EventApplyCollision.class, event -> event.setCanceled(true));


    Attender<EventAddCollisionBox> onAddCollisionBox = new Attender<>(EventAddCollisionBox.class, event -> {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player != null) {

            final boolean floor = this.floor.getValue() ? event.getPos().getY() >= 1 : true;

            if (this.mode.getValue() == Mode.SAND) {
                if (mc.player.getRidingEntity() != null && event.getEntity() == mc.player.getRidingEntity()) {
                    if (mc.gameSettings.keyBindSprint.isKeyDown() && floor) {
                        event.setCanceled(true);
                    } else {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && event.getPos().getY() >= mc.player.getRidingEntity().posY) {
                            event.setCanceled(true);
                        }
                        if (event.getPos().getY() >= mc.player.getRidingEntity().posY) {
                            event.setCanceled(true);
                        }
                    }
                } else if (event.getEntity() == mc.player) {
                    if (mc.gameSettings.keyBindSneak.isKeyDown() && floor) {
                        event.setCanceled(true);
                    } else {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && event.getPos().getY() >= mc.player.posY) {
                            event.setCanceled(true);
                        }
                        if (event.getPos().getY() >= mc.player.posY) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }

        if (this.mode.getValue() == Mode.NOCLIP) {
            if (event.getEntity() == mc.player || mc.player.getRidingEntity() != null && event.getEntity() == mc.player.getRidingEntity()) {
                event.setCanceled(true);
            }
        }
    });

    Attender<EventSendPacket> onSendPacket = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getValue() == Mode.NOCLIP) {
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.setCanceled(true);
                }
            }
        }
    });

    Attender<EventUpdateWalkingPlayer> onUpdateWalkingPlayer = new Attender<>(EventUpdateWalkingPlayer.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.NOCLIP) {
                mc.player.setVelocity(0, 0, 0);
                if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                    final double[] speed = MathUtil.directionSpeed(0.06f);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + speed[0], mc.player.posY, mc.player.posZ + speed[1], mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.06f, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.06f, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }
            }
        }
    });

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.SAND) {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof EntityBoat) {
                        final EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                        if (boat.onGround) {
                            boat.motionY = 0.42f;
                        }
                    }
                }
            }

            if (this.mode.getValue() == Mode.PACKET) {
                final Vec3d dir = MathUtil.direction(mc.player.rotationYaw);
                if (dir != null) {
                    if (mc.player.onGround && mc.player.collidedHorizontally) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.00001f, mc.player.posY, mc.player.posZ + dir.z * 0.0001f, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 2.0f, mc.player.posY, mc.player.posZ + dir.z * 2.0f, mc.player.onGround));
                    }
                }
            }

            if (this.mode.getValue() == Mode.SKIP) {
                final Vec3d dir = MathUtil.direction(mc.player.rotationYaw);
                if (dir != null) {
                    if (mc.player.onGround && mc.player.collidedHorizontally) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.001f, mc.player.posY + 0.1f, mc.player.posZ + dir.z * 0.001f, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.03f, 0, mc.player.posZ + dir.z * 0.03f, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.06f, mc.player.posY, mc.player.posZ + dir.z * 0.06f, mc.player.onGround));
                    }
                }
            }
        }
    });
}
