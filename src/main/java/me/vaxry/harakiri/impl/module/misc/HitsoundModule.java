package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.sound.sampled.*;
import java.io.File;

public class HitsoundModule extends Module {
    String audiopath;

    public HitsoundModule(){
        super("Hitsounds", new String[]{"Hitsounds"}, "Plays a sound when you hit an entity.", "NONE", -1, ModuleType.MISC);
        audiopath = System.getenv("APPDATA") + "\\.minecraft\\Harakiri\\hitsound.wav";
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event){
        if(!this.isEnabled())
            return;

        if(event.getSource().getTrueSource().equals(Minecraft.getMinecraft().player)){

            PlaySound(new File(audiopath));
        }
    }

    private void PlaySound(File in){
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(in));
            clip.start();
        }catch(Throwable t){
            Harakiri.INSTANCE.logChat("The sound file was not found or is incorrectly exported. In the 99% of cases where this is your mistake, please refer to the FAQ about hitsounds. In the case of that 1%, please contact vaxry.");
        }
    }
}