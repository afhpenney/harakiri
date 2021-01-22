package me.vaxry.harakiri.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.world.EventAddEntity;
import me.vaxry.harakiri.api.event.world.EventRemoveEntity;
import me.vaxry.harakiri.api.friend.Friend;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 11/10/2019 @ 4:20 AM.
 */
public final class VisualRangeModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.NOTIFICATION);

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    private int prevPlayer = -1;

    public VisualRangeModule() {
        super("VisualRange", new String[]{"VisRange", "VRange", "VR"}, "Notifies you when players enter and leave your visual range", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onEntityAdded(EventAddEntity event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        if (!Minecraft.getMinecraft().player.isDead && event.getEntity() instanceof EntityPlayer && !event.getEntity().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getName())) {
            final Friend friend = Harakiri.INSTANCE.getFriendManager().isFriend(event.getEntity());

            final String msg = (friend != null ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED) + (friend != null ? friend.getAlias() : event.getEntity().getName()) + ChatFormatting.WHITE + " has entered your visual range.";

            if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                Harakiri.INSTANCE.getNotificationManager().addNotification("", msg);
            }

            if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                Harakiri.INSTANCE.logChat(msg);
            }

            if (event.getEntity().getEntityId() == this.prevPlayer) {
                this.prevPlayer = -1;
            }
        }
    }

    @Listener
    public void onEntityRemove(EventRemoveEntity event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        if (!Minecraft.getMinecraft().player.isDead && event.getEntity() instanceof EntityPlayer && !event.getEntity().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getName())) {
            if (this.prevPlayer != event.getEntity().getEntityId()) {
                this.prevPlayer = event.getEntity().getEntityId();
                final Friend friend = Harakiri.INSTANCE.getFriendManager().isFriend(event.getEntity());
                final String msg = (friend != null ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED) + (friend != null ? friend.getAlias() : event.getEntity().getName()) + ChatFormatting.WHITE + " has left your visual range.";

                if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                    Harakiri.INSTANCE.getNotificationManager().addNotification("", msg);
                }

                if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                    Harakiri.INSTANCE.logChat(msg);
                }

            }
        }
    }


}
