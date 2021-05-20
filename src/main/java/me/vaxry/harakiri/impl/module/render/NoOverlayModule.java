package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.gui.EventRenderHelmet;
import me.vaxry.harakiri.framework.event.gui.EventRenderPortal;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.render.EventRenderOverlay;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;


public final class NoOverlayModule extends Module {

    public final Value<Boolean> portal = new Value<Boolean>("Portal", new String[]{}, "Disables the portal overlay.", true);
    public final Value<Boolean> helmet = new Value<Boolean>("Helmet", new String[]{}, "Disables the pumpkin overlay.", true);
    public final Value<Boolean> block = new Value<Boolean>("Block", new String[]{}, "Disables the block-side screen overlay.", true);
    public final Value<Boolean> water = new Value<Boolean>("Water", new String[]{}, "Disables the water overlay.", true);
    public final Value<Boolean> fire = new Value<Boolean>("Fire", new String[]{}, "Disables the fire overlay.", true);
    public final Value<Boolean> lava = new Value<Boolean>("Lava", new String[]{}, "Disables the lava overlay.", true);

    public NoOverlayModule() {
        super("NoOverlay", new String[]{"AntiOverlay"}, "Removes some overlay effects.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventRenderOverlay> onoverlay = new Attender<>(EventRenderOverlay.class, event -> {
        if (this.block.getValue() && event.getType() == EventRenderOverlay.OverlayType.BLOCK) {
            event.setCanceled(true);
        }
        if (this.water.getValue() && event.getType() == EventRenderOverlay.OverlayType.LIQUID) {
            event.setCanceled(true);
        }
        if (this.fire.getValue() && event.getType() == EventRenderOverlay.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    });

    Attender<EventRenderHelmet> onhelmet = new Attender<>(EventRenderHelmet.class, event -> {
        if (this.helmet.getValue()) {
            event.setCanceled(true);
        }
    });

    Attender<EventRenderPortal> onportal = new Attender<>(EventRenderPortal.class, event -> {
        if (this.portal.getValue()) {
            event.setCanceled(true);
        }
    });
}
