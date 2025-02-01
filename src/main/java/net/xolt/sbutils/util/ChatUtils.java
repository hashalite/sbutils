package net.xolt.sbutils.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.ModConfig;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class ChatUtils {

    public static void printMessage(String message) {
        printMessage(message, true);
    }

    public static void printMessage(String message, boolean showPrefix) {
        printMessage(message, TextUtils.getMessageColor(), showPrefix);
    }

    public static void printMessage(String message, ChatFormatting formatting) {
        printMessage(message, formatting, true);
    }

    public static void printMessage(String message, ChatFormatting formatting, boolean showPrefix) {
        printMessage(Component.translatable(message).withStyle(formatting), showPrefix);
    }

    public static void printMessage(String message, int color) {
        printMessage(message, color, true);
    }

    public static void printMessage(String message, int color, boolean showPrefix) {
        printMessage(Component.translatable(message).withColor(color), showPrefix);
    }

    public static void printMessage(Component message) {
        printMessage(message, true);
    }

    public static void printMessage(Component message, boolean showPrefix) {
        if (MC.player == null)
            return;

        if (!showPrefix) {
            MC.player.displayClientMessage(message, false);
            return;
        }

        MutableComponent sbutilsText = Component.literal("sbutils").withColor(ModConfig.HANDLER.instance().sbutilsColor.getRGB());
        MutableComponent prefix = TextUtils.insertPlaceholders(Component.literal(ModConfig.HANDLER.instance().prefixFormat + " ").withColor(ModConfig.HANDLER.instance().prefixColor.getRGB()), sbutilsText);

        MC.player.displayClientMessage(prefix.append(message), false);
    }

    public static void sendTitle(String message) {
        sendTitle(message, TextUtils.getMessageColor());
    }
    
    public static void sendTitle(String message, int color) {
        sendTitle(Component.translatable(message).withColor(color), 5, 30, 5);
    }

    public static void sendTitle(String message, int color, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        sendTitle(Component.translatable(message).withColor(color), fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void sendTitle(Component message) {
        sendTitle(message, 5, 30, 5);
    }

    public static void sendTitle(Component message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        MC.gui.setTimes(fadeInTicks, stayTicks, fadeOutTicks);
        MC.gui.setTitle(message);
    }

    public static void sendPlaceholderTitle(String message, Object ... args) {
        sendTitle(TextUtils.insertPlaceholders(message, TextUtils.formatPlaceholders(args)));
    }

    public static void sendPlaceholderTitle(MutableComponent message, Object ... args) {
        sendTitle(TextUtils.insertPlaceholders(message, TextUtils.formatPlaceholders(args)));
    }

    public static void printWithPlaceholders(String message, Object ... args) {
        printMessage(TextUtils.insertPlaceholders(message, TextUtils.formatPlaceholders(args)));
    }

    public static void printWithPlaceholders(MutableComponent message, Object ... args) {
        printMessage(TextUtils.insertPlaceholders(message, TextUtils.formatPlaceholders(args)));
    }

    public static void printChangedSetting(String setting, Object value) {
        printSetting(setting, value, true);
    }

    public static void printSetting(String setting, Object value) {
        printSetting(setting, value, false);
    }

    private static void printSetting(String setting, Object value, boolean changed) {
        MutableComponent settingText = Component.translatable(setting);
        MutableComponent message;
        if (value instanceof Boolean) {
            message = Component.translatable(changed ? "message.sbutils.changeBooleanSetting" : "message.sbutils.printBooleanSetting");
        } else {
            message = Component.translatable(changed ? "message.sbutils.changeOtherSetting" : "message.sbutils.printOtherSetting");
        }
        printWithPlaceholders(message, settingText, TextUtils.format(value));
    }

    public static <T> void printListSetting(String setting, List<T> list) {
        printListSetting(setting, list, false);
    }

    public static <T> void printListSetting(String setting, List<T> list, boolean numbered) {
        printWithPlaceholders("message.sbutils.printListSetting", Component.translatable(setting));
        printList(list, numbered);
    }

    public static <T> void printList(List<T> items, boolean numbered) {
        for (int i = 0; i < items.size(); i++) {
            MutableComponent prefix = Component.literal(" " + (numbered ? (i + 1) + ". " : "- ")).withColor(TextUtils.getValueColor());
            printMessage(prefix.append(TextUtils.format(items.get(i))), false);
        }
    }
}
