package me.vaxry.harakiri.impl.module.misc;

import com.google.common.collect.Maps;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.hidden.CommandsModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;


import java.util.Map;

public final class StorageAlertModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.BOTH);
    public final Value<Boolean> chests = new Value<Boolean>("Chests", new String[]{"Chests", "chest"}, "Count chests.", true);
    public final Value<Boolean> echests = new Value<Boolean>("EnderChests", new String[]{"EnderChests", "echest", "echest"}, "Count ender chests.", false);
    public final Value<Boolean> dispensers = new Value<Boolean>("Dispensers", new String[]{"Dispensers", "disp"}, "Count dispensers.", false);
    public final Value<Boolean> shulkers = new Value<Boolean>("ShulkerBoxes", new String[]{"ShulkerBoxes", "shul"}, "Count shulkers.", false);
    public final Value<Boolean> stands = new Value<Boolean>("BrewingStands", new String[]{"BrewingStands", "brew"}, "Count brewing stands.", false);
    public final Value<Boolean> droppers = new Value<Boolean>("Droppers", new String[]{"Droppers", "drop"}, "Count droppers.", false);
    public final Value<Boolean> hoppers = new Value<Boolean>("Hoppers", new String[]{"Hoppers", "hopp"}, "Count hoppers.", false);

    private CommandsModule commandsModule;

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    public StorageAlertModule() {
        super("StorageAlert", new String[]{"StorageAlerts", "ChestAlert"}, "Alerts you how many storage blocks are in a chunk when it's loaded", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                final Vec2f position = new Vec2f(packet.getChunkX() * 16, packet.getChunkZ() * 16);
                final Map<String, Vec2f> foundStorage = Maps.newLinkedHashMap();

                for (NBTTagCompound tag : packet.getTileEntityTags()) {
                    final String id = tag.getString("id");
                    if (
                            (this.chests.getValue() && (id.equals("minecraft:chest") || id.equals("minecraft:trapped_chest"))) ||
                                    (this.echests.getValue() && id.equals("minecraft:ender_chest")) ||
                                    (this.shulkers.getValue() && id.equals("minecraft:shulker_box")) ||
                                    (this.hoppers.getValue() && id.equals("minecraft:hopper")) ||
                                    (this.droppers.getValue() && id.equals("minecraft:dropper")) ||
                                    (this.dispensers.getValue() && id.equals("minecraft:dispenser")) ||
                                    (this.stands.getValue() && id.equals("minecraft:brewing_stand"))
                    ) {
                        foundStorage.put(id, position);
                    }
                }

                if (foundStorage.size() > 0) {
                    String id = "storage block" + (foundStorage.size() == 1 ? "" : "s");

                    for (String type : foundStorage.keySet()) {
                        final Vec2f otherPosition = foundStorage.get(type);
                        if (position.equals(otherPosition)) {
                            id = type.replaceAll("minecraft:", "");
                        }
                    }

                    final String message = foundStorage.size() + " " + id + " located";
                    if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                        if (this.commandsModule == null) {
                            this.commandsModule = (CommandsModule) Harakiri.get().getModuleManager().find(CommandsModule.class);
                        } else {
                            final TextComponentString textComponent = new TextComponentString(ChatFormatting.YELLOW + message);
                            textComponent.appendSibling(new TextComponentString("(*)")
                                    .setStyle(new Style()
                                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("\2476" + "Create a waypoint for this position" + "\n" + ChatFormatting.WHITE + "X: " + position.x + ", Z: " + position.y)))
                                            .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandsModule.getPrefix().getValue() + "waypoint add " + String.format("x%s_z%s", position.x, position.y) + " " + position.x + " 120 " + position.y))));
                            Harakiri.get().logcChat(textComponent);
                        }
                    }
                    if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                        Harakiri.get().getNotificationManager().addNotification("", message);
                    }
                }
            }
        }
    }
}
