package me.vaxry.harakiri.impl.module.misc;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.event.player.EventDestroyBlock;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.world.EventSetBlockState;
import me.vaxry.harakiri.impl.module.world.NukerModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;


public final class NoGlitchBlocks extends Module {

    private NukerModule nukerModule;

    public final Value<Boolean> plac = new Value<Boolean>("Place", new String[]{"Place", "p"}, "Place desync correction.", false);
    public final Value<Boolean> brek = new Value<Boolean>("Break", new String[]{"Break", "b"}, "Break desync correction.", false);

    public NoGlitchBlocks() {
        super("NoGlitchBlocks", new String[]{"NoGlitchBlocks", "AntiDesync", "NoDe-sync"}, "Prevents the client from desyncing blocks.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onFullLoad() {
        super.onFullLoad();
        nukerModule = (NukerModule) Harakiri.get().getModuleManager().find(NukerModule.class);
    }

    public Attender<EventDestroyBlock> onDestroyBlock = new Attender<>(EventDestroyBlock.class, event -> {
        if(!brek.getValue())
            return;

        if(!(nukerModule.isEnabled() && nukerModule.mode.getValue() == NukerModule.Mode.CREATIVE))
            Minecraft.getMinecraft().world.playEvent(2001, event.getPos(), Block.getStateId(Minecraft.getMinecraft().world.getBlockState(event.getPos())));

        event.setCanceled(true);
    });

    Attender<EventSetBlockState> onSetBlockState = new Attender<>(EventSetBlockState.class, event -> {
        if(!plac.getValue())
            return;

        if(event.flags != 3 && !Minecraft.getMinecraft().isSingleplayer())
            event.setCanceled(true);
    });
}
