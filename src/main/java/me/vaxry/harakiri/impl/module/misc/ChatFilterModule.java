package me.vaxry.harakiri.impl.module.misc;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;


import java.util.ArrayList;
import java.util.List;

public final class ChatFilterModule extends Module {

    public final Value<Boolean> unicode = new Value<>("Unicode", new String[]{"uc"}, "Reverts \"Fancy Chat\" characters back into normal ones. ", true);
    public final Value<Boolean> spam = new Value<>("Spam", new String[]{"sp", "s"}, "Attempts to prevent spam.", true);
    public final Value<Boolean> death = new Value<>("Death", new String[]{"dead", "d"}, "Attempts to prevent death messages.", false);

    private final List<String> cache = new ArrayList<>();

    public ChatFilterModule() {
        super("ChatFilter", new String[]{"CFilter"}, "Filters out malicious or annoying chat messages", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.cache.clear();
    }

    Attender<EventReceivePacket> onReceivePacket = new Attender<>(EventReceivePacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();
                boolean is9b9tOr2b2t = false;

                if (!Minecraft.getMinecraft().isSingleplayer()) {
                    if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                        final String currentServerIP = Minecraft.getMinecraft().getCurrentServerData().serverIP;
                        is9b9tOr2b2t = currentServerIP.equalsIgnoreCase("2b2t.org") || currentServerIP.equalsIgnoreCase("2b2t.com") || currentServerIP.equalsIgnoreCase("9b9t.com") || currentServerIP.equalsIgnoreCase("9b9t.org");
                    }
                }

                if (this.death.getValue()) {
                    if (packet.getChatComponent().getFormattedText().contains("\2474") || packet.getChatComponent().getFormattedText().contains("\247c")) {
                        event.setCanceled(true);
                    }
                }

                if (this.spam.getValue()) {
                    final String chat = packet.getChatComponent().getUnformattedText();

                    if (this.cache.size() > 0) {
                        for (String s : this.cache) {
                            final double diff = StringUtil.levenshteinDistance(s, chat);

                            if (diff >= 0.75f) {
                                event.setCanceled(true);
                            }
                        }
                    }

                    this.cache.add(chat);

                    if (this.cache.size() >= 10) {
                        this.cache.remove(0);
                    }
                }

                if (this.unicode.getValue()) {
                    if (packet.getChatComponent() instanceof TextComponentString) {
                        final TextComponentString component = (TextComponentString) packet.getChatComponent();

                        final StringBuilder sb = new StringBuilder();

                        boolean containsUnicode = false;

                        for (String s : component.getFormattedText().split(" ")) {

                            String line = "";

                            for (char c : s.toCharArray()) {
                                if (c >= 0xFEE0) {
                                    c -= 0xFEE0;
                                    containsUnicode = true;
                                }

                                line += c;
                            }

                            sb.append(line + " ");
                        }

                        if (containsUnicode) {
                            packet.chatComponent = new TextComponentString(sb.toString());
                        }
                    }
                }
            }
        }
    });
}
