package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.api.event.player.EventApplyCollision;
import me.vaxry.harakiri.api.event.player.EventPushOutOfBlocks;
import me.vaxry.harakiri.api.event.player.EventPushedByWater;
import me.vaxry.harakiri.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/9/2019 @ 12:52 AM.
 */
public final class NoPushModule extends Module {

    public NoPushModule() {
        super("NoPush", new String[]{"AntiPush"}, "Disable collision with entities, blocks and water", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void pushOutOfBlocks(EventPushOutOfBlocks event) {
        event.setCanceled(true);
    }

    @Listener
    public void pushedByWater(EventPushedByWater event) {
        event.setCanceled(true);
    }

    @Listener
    public void applyCollision(EventApplyCollision event) {
        event.setCanceled(true);
    }

}
