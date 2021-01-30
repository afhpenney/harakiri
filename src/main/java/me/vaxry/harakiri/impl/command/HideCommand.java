package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.impl.config.ModuleConfig;

/**
 * Author Seth
 * 4/16/2019 @ 10:01 PM.
 */
public final class HideCommand extends Command {

    public HideCommand() {
        super("Hide", new String[]{"Hid"}, "Allows you to hide modules from the arraylist", "Hide <Module>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Harakiri.INSTANCE.getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Harakiri.INSTANCE.errorChat("Cannot hide " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                mod.setHidden(!mod.isHidden());
                Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);

                if (mod.isHidden()) {
                    Harakiri.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f is now hidden");
                } else {
                    Harakiri.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f is no longer hidden");
                }
            }
            //TODO config
        } else {
            Harakiri.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Harakiri.INSTANCE.getModuleManager().findSimilar(split[1]);

            if (similar != null) {
                Harakiri.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }
}