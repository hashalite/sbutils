package net.xolt.sbutils.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.xolt.sbutils.config.ModConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextUtils {
    public static MutableComponent insertPlaceholders(String message, Object ... args) {
        return insertPlaceholders(message, formatPlaceholders(args));
    }

    public static MutableComponent insertPlaceholders(String message, MutableComponent ... args) {
        MutableComponent messageText = Component.translatable(message);
        return insertPlaceholders(messageText, args);
    }

    public static MutableComponent insertPlaceholders(MutableComponent message, MutableComponent ... args) {
        String messageString = message.getString();
        if (!messageString.contains("%s")) {
            return message;
        }
        MutableComponent formatted = Component.empty();
        Style style = message.getStyle();
        if (style.isEmpty())
            style = style.withColor(getMessageColor());
        List<String> pieces = new ArrayList<>(Arrays.asList(messageString.split("%s")));

        for (int i = 0; i < pieces.size(); i++) {
            MutableComponent piece = Component.literal(pieces.get(i));
            piece.setStyle(style);
            formatted.append(piece);

            if (i < args.length) {
                formatted.append(args[i]);
            }
        }

        return formatted;
    }

    public static int getMessageColor() {
        return ModConfig.HANDLER.instance().messageColor.getRGB();
    }

    public static int getValueColor() {
        return ModConfig.HANDLER.instance().valueColor.getRGB();
    }

    public static int getBooleanColor(boolean bool) {
        Integer green = ChatFormatting.GREEN.getColor();
        Integer red = ChatFormatting.RED.getColor();
        if (green == null || red == null)
            return 0;
        return bool ? green : red;
    }

    public static String formatDouble(double input) {
        if (input == (long)input) {
            return String.format("%d", (long)input);
        } else {
            return String.format("%s", input);
        }
    }

    public static <T> MutableComponent format(T input) {
        if (input instanceof ModConfig.MultiValue multiValue)
            return multiValue.format();
        return formatPlaceholder(input);
    }

    public static <T> MutableComponent[] format(T[] input) {
        MutableComponent[] result = new MutableComponent[input.length];
        for (int i = 0; i < input.length; i++)
            result[i] = format(input[i]);
        return result;
    }

    public static <T> MutableComponent formatPlaceholder(T input) {
        MutableComponent result;
        if (input instanceof MutableComponent text) {
            result = text;
            if (result.getStyle().isEmpty())
                result = result.withColor(getValueColor());
        } else if (input instanceof Boolean bool) {
            result = formatEnabledDisabled(bool);
        } else if (input instanceof NameableEnum nameableEnum) {
            result = nameableEnum.getDisplayName().copy().withColor(getValueColor());
        } else if (input instanceof Number) {
            result = Component.literal(String.valueOf(input)).withColor(getValueColor());
        } else if (input instanceof String string) {
            result = Component.literal(!string.isEmpty() ? string : "nothing").withColor(!string.isEmpty() ? getValueColor() : getMessageColor());
            if (((String) input).isEmpty())
                result = result.withStyle(ChatFormatting.ITALIC);
        } else if (input instanceof Color color) {
            result = Component.literal("#" + String.format("%06x", color.getRGB() & 0x00FFFFFF)).withColor(color.getRGB());
        } else {
            result = Component.literal(String.valueOf(input)).withColor(getValueColor());
        }
        return result;
    }

    public static <T> MutableComponent[] formatPlaceholders(T[] input) {
        MutableComponent[] result = new MutableComponent[input.length];
        for (int i = 0; i < input.length; i++)
            result[i] = formatPlaceholder(input[i]);
        return result;
    }

    public static MutableComponent formatEnabledDisabled(boolean bool) {
        return Component.translatable(bool ? "message.sbutils.enabled" : "message.sbutils.disabled").withColor(getBooleanColor(bool));
    }

    public static MutableComponent formatOnlineOffline(boolean online) {
        return Component.translatable(online ? "message.sbutils.online" : "message.sbutils.offline").withColor(getBooleanColor(online));
    }

    public static String formatTime(double seconds) {
        int days = (int)(seconds / 86400.0);
        int hours = (int)((seconds % 86400.0)  / 3600.0);
        int minutes = (int)(((seconds % 86400.0)  % 3600.0) / 60.0);
        double secs = ((seconds % 86400.0)  % 3600.0) % 60.0;
        return (days > 0 ? days + "d " : "") + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + String.format("%.1f", secs) + "s";
    }

    public static String formatTime(long millis) {
        return formatTime((double)millis/1000.0);
    }
}
