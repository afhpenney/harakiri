package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;


public final class GodModeModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The mode of the godmode exploit.", Mode.PORTAL);

    private enum Mode {
        PORTAL, RIDING, LOCK, PACKET
    }

    public final Value<Boolean> footsteps = new Value<Boolean>("FootSteps", new String[]{"Foot"}, "If enabled, others will be able to see your footsteps during godmode.", false);

    private Entity riding;

    public GodModeModule() {
        super("GodMode", new String[]{"God", "Pgm", "PortalGodmode", "PortalGod", "SickoMode"}, "Makes you invincible in certain situations.", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (this.mode.getValue() == Mode.RIDING) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
                mc.world.removeEntity(this.riding);
                mc.player.setPosition(mc.player.getPosition().getX(), mc.player.getPosition().getY() - 1, mc.player.getPosition().getZ());
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.mode.getValue() == Mode.RIDING) {
            if (this.riding != null) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketUseEntity(this.riding, EnumHand.MAIN_HAND));
            }
        }
        if (this.mode.getValue() == Mode.LOCK) {
            Minecraft.getMinecraft().player.respawnPlayer();
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.RIDING) {
                if (this.riding != null) {
                    this.riding.posX = mc.player.posX;
                    this.riding.posY = mc.player.posY + (this.footsteps.getValue() ? 0.3f : 0.0f);
                    this.riding.posZ = mc.player.posZ;
                    this.riding.rotationYaw = Minecraft.getMinecraft().player.rotationYaw;
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
                    mc.player.connection.sendPacket(new CPacketInput(mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe, false, false));
                    mc.player.connection.sendPacket(new CPacketVehicleMove(this.riding));
                }
            }
        }
    }

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.LOCK) {
                if (mc.currentScreen instanceof GuiGameOver) {
                    mc.displayGuiScreen(null);
                }
                mc.player.setHealth(20.0f);
                mc.player.getFoodStats().setFoodLevel(20);
                mc.player.isDead = false;
            }
        }
    });

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getValue() == Mode.PORTAL) {
                if (event.getPacket() instanceof CPacketConfirmTeleport) {
                    event.setCanceled(true);
                }
            }
            if (this.mode.getValue() == Mode.RIDING) {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    final Minecraft mc = Minecraft.getMinecraft();
                    final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
                    if (this.riding != null) {
                        final Entity entity = packet.getEntityFromWorld(mc.world);
                        if (entity != null) {
                            this.riding.posX = entity.posX;
                            this.riding.posY = entity.posY;
                            this.riding.posZ = entity.posZ;
                            this.riding.rotationYaw = mc.player.rotationYaw;
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
                            mc.player.connection.sendPacket(new CPacketInput(mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe, false, false));
                            mc.player.connection.sendPacket(new CPacketVehicleMove(this.riding));
                        }
                    }
                }

                if (event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
                    event.setCanceled(true);
                }
            }
            if (this.mode.getValue() == Mode.PACKET) {
                if (event.getPacket() instanceof CPacketPlayer) {
                    event.setCanceled(true);
                }
            }
        }
    });

    Attender<EventDisplayGui> onDisplayGUI = new Attender<>(EventDisplayGui.class, event -> {
        if (event.getScreen() != null && event.getScreen() instanceof GuiGameOver && this.mode.getValue() == Mode.LOCK) {
            event.setCanceled(true);
        }
    });
}
