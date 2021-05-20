package me.vaxry.harakiri.impl.module.combat;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;


public final class RespawnModule extends Module {

    public RespawnModule() {
        super("AutoRespawn", new String[]{"AutoRespawn", "Resp"}, "Automatically respawns you after death", "NONE", -1, ModuleType.COMBAT);
    }

    Attender<EventDisplayGui> onDisplayGUI = new Attender<>(EventDisplayGui.class, event -> {
        if (event.getScreen() != null && event.getScreen() instanceof GuiGameOver) {
            Minecraft.getMinecraft().player.respawnPlayer();
        }
    });
}
