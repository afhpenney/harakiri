package me.vaxry.harakiri.impl.module.movement;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;


public final class SneakModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The sneak mode to use.", Mode.VANILLA);

    private enum Mode {
        VANILLA, NCP
    }

    public SneakModule() {
        super("Sneak", new String[]{"Sneak"}, "Allows you to sneak at full speed.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null && !Minecraft.getMinecraft().player.isSneaking()) {
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    Attender<EventUpdateWalkingPlayer> onUpdateWalkingPlayer = new Attender<>(EventUpdateWalkingPlayer.class, event -> {
        final Minecraft mc = Minecraft.getMinecraft();
        if (event.getStage() == EventStageable.EventStage.PRE) {
            switch (this.mode.getValue()) {
                case VANILLA:
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    break;
                case NCP:
                    if (!mc.player.isSneaking()) {
                        if (this.isMoving()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                        } else {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        }
                    }
                    break;
            }
        } else {
            if (this.mode.getValue() == Mode.NCP) {
                if (this.isMoving()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
            }
        }
    });

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !Minecraft.getMinecraft().player.isSneaking()) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    });

    private boolean isMoving() {
        return GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindBack);
    }

}
