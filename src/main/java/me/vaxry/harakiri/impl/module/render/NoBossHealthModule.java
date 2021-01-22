package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.api.event.render.EventRenderBossHealth;
import me.vaxry.harakiri.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * created by noil on 10/1/2019 at 6:37 PM
 */
public final class NoBossHealthModule extends Module {

    public NoBossHealthModule() {
        super("NoBossHealth", new String[]{"NoBossHealthBar", "NoBossBar"}, "Disables the rendering of the boss health-bar located at the top of the screen.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRenderBossHealth(EventRenderBossHealth event) {
        event.setCanceled(true);
    }
}
