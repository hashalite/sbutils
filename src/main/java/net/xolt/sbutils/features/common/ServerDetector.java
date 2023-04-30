package net.xolt.sbutils.features.common;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.features.AutoAdvert;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

public class ServerDetector {

    public static SbServer currentServer;

    public static void processMessage(Text message) {
        if (RegexFilters.skyblockJoinFilter.matcher(message.getString()).matches()) {
            List<Text> siblings = message.getSiblings();
            if (siblings.size() < 1) {
                return;
            }

            TextColor serverColor = siblings.get(siblings.size() - 1).getStyle().getColor();
            if (serverColor.equals(TextColor.fromFormatting(Formatting.GREEN))) {
                currentServer = SbServer.SKYBLOCK;
                onSwitchServer();
            } else if (serverColor.equals(TextColor.fromFormatting(Formatting.LIGHT_PURPLE))) {
                currentServer = SbServer.ECONOMY;
                onSwitchServer();
            } else if (serverColor.equals(TextColor.fromFormatting(Formatting.YELLOW))) {
                currentServer = SbServer.CLASSIC;
                onSwitchServer();
            }
        }
    }

    public static void onJoinGame() {
        resetServer();
    }

    public static void onDisconnect() {
        resetServer();
    }

    public static void onSwitchServer() {
        AutoAdvert.refreshPrevAdlist();
    }

    private static void resetServer() {
        currentServer = null;
    }

    public enum SbServer {
        SKYBLOCK,
        ECONOMY,
        CLASSIC;
    }
}
