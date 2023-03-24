package net.xolt.sbutils.util;

import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class Messenger {

    public static void printMessage(String message) {
        printMessage(message, ModConfig.INSTANCE.getConfig().messageColor.getFormatting());
    }

    public static void printMessage(String message, Formatting formatting) {
        printMessage(Text.translatable(message).formatted(formatting));
    }

    public static void printMessage(Text message) {
        if (MC.player == null) {
            return;
        }

        MutableText sbutilsText = Text.literal("sbutils").formatted(ModConfig.INSTANCE.getConfig().sbutilsColor.getFormatting());
        MutableText prefix = insertPlaceholders(Text.literal(ModConfig.INSTANCE.getConfig().messagePrefix + " ").formatted(getMessageColor()), sbutilsText);

        MC.player.sendMessage(prefix.append(message));
    }

    private static Formatting getMessageColor() {
        return ModConfig.INSTANCE.getConfig().messageColor.getFormatting();
    }

    private static Formatting getValueColor() {
        return ModConfig.INSTANCE.getConfig().valueColor.getFormatting();
    }

    public static void printWithPlaceholders(String message, Object ... args) {
        MutableText messageText = Text.translatable(message).formatted(getMessageColor());
        List<MutableText> placeholders = new ArrayList<>();
        for (Object placeholder : args) {
            placeholders.add(Text.translatable(String.valueOf(placeholder)).formatted(getValueColor()));
        }
        printMessage(insertPlaceholders(messageText, placeholders.toArray(MutableText[]::new)));
    }

    private static MutableText insertPlaceholders(MutableText message, MutableText ... args) {
        String messageString = message.getString();
        if (!messageString.contains("@")) {
            return message;
        }
        MutableText formatted = Text.empty();
        Style style = message.getStyle();
        List<String> pieces = new ArrayList<>(Arrays.asList(messageString.split("@")));

        for (int i = 0; i < pieces.size(); i++) {
            MutableText piece = Text.literal(pieces.get(i));
            piece.setStyle(style);
            formatted.append(piece);

            if (i < args.length) {
                formatted.append(args[i]);
            }
        }

        return formatted;
    }

    private static void printSetting(String setting, Object value, boolean changed) {
        if (value instanceof Boolean) {
            MutableText message = Text.translatable(changed ? "message.sbutils.changeBooleanSetting" : "message.sbutils.printBooleanSetting").formatted(getMessageColor());
            MutableText settingText = Text.translatable(setting).formatted(getValueColor());
            MutableText valueText = Text.translatable((Boolean) value ? "message.sbutils.enabled" : "message.sbutils.disabled").formatted(getBooleanColor((Boolean) value));
            printMessage(insertPlaceholders(message, settingText, valueText));
        } else if (value instanceof NameableEnum || value instanceof Number || value instanceof String) {
            MutableText message = Text.translatable(changed ? "message.sbutils.changeOtherSetting" : "message.sbutils.printOtherSetting").formatted(getMessageColor());
            MutableText settingText = Text.translatable(setting).formatted(getValueColor());
            MutableText valueText;
            if (value instanceof NameableEnum) {
                valueText = ((NameableEnum)value).getDisplayName().copy().formatted(getValueColor());
            } else if (value instanceof Number){
                valueText = Text.literal(String.valueOf(value)).formatted(getValueColor());
            } else {
                valueText = Text.literal((String) value).formatted(getValueColor());
            }
            printMessage(insertPlaceholders(message, settingText, valueText));
        }
    }

    public static void printChangedSetting(String setting, Object value) {
        printSetting(setting, value, true);
    }

    public static void printSetting(String setting, Object value) {
        printSetting(setting, value, false);
    }

    public static void printAutoAdvertInfo(boolean enabled, boolean serverNull, int adIndex, int remainingDelay, boolean userWhitelisted, boolean whitelistEnabled) {
        if (!enabled) {
            printSetting("text.sbutils.config.category.autoadvert", false);
            return;
        }

        if (serverNull) {
            printMessage("message.sbutils.autoAdvert.notOnSkyblock");
            return;
        }

        if (whitelistEnabled && !userWhitelisted) {
            printMessage("message.sbutils.autoAdvert.notWhitelisted");
            return;
        }

        int delayMinutes = remainingDelay / 60000;
        double delaySeconds = Math.round((double)(remainingDelay % 60000) / 100.0) / 10.0;

        MutableText message;
        MutableText index = Text.literal("#" + (adIndex + 1)).formatted(getValueColor());
        MutableText seconds = Text.literal(String.valueOf(delaySeconds)).formatted(getValueColor());

        if (delayMinutes > 0) {
            message = Text.translatable("message.sbutils.autoAdvert.infoWithMinutes").formatted(getMessageColor());
            MutableText minutes = Text.literal(String.valueOf(delayMinutes)).formatted(getValueColor());
            printMessage(insertPlaceholders(message, index, minutes, seconds));
        } else {
            message = Text.translatable("message.sbutils.autoAdvert.infoJustSeconds").formatted(getMessageColor());
            printMessage(insertPlaceholders(message, index, seconds));
        }
    }

    public static void printStaffNotification(PlayerListEntry player, boolean joined) {
        MutableText message = Text.translatable("message.sbutils.staffDetector.notification").formatted(getMessageColor());
        MutableText staff = Text.literal(player.getProfile().getName()).formatted(getValueColor());
        MutableText status = Text.translatable(joined ? "message.sbutils.online" : "message.sbutils.offline").formatted(getBooleanColor(joined));
        printMessage(insertPlaceholders(message, staff, status));
    }

    public static void printConversions(String input, String items, String stacks, String dcs, String stacksAndRemainer, String dcsAndRemainder) {
        if (MC.player == null) {
            return;
        }

        MutableText message = Text.translatable("message.sbutils.convert.header").formatted(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(input).formatted(getValueColor())));

        MutableText itemsText = Text.literal("- " + items).formatted(getValueColor());
        MC.player.sendMessage(itemsText, false);
        MutableText stacksText = Text.literal("- " + stacks).formatted(getValueColor());
        MC.player.sendMessage(stacksText, false);
        MutableText dcsText = Text.literal("- " + dcs).formatted(getValueColor());
        MC.player.sendMessage(dcsText, false);
        MutableText stacksAndRemainderText = Text.literal("- " + stacksAndRemainer).formatted(getValueColor());
        MC.player.sendMessage(stacksAndRemainderText, false);
        MutableText dcsAndRemainderText = Text.literal("- " + dcsAndRemainder).formatted(getValueColor());
        MC.player.sendMessage(dcsAndRemainderText, false);
    }

    public static void printListSetting(String message, List<String> list) {
        printMessage(message);
        printNumberedList(list);
    }

    private static void printNumberedList(List<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            String command = strings.get(i);
            MC.player.sendMessage(Text.literal((i + 1) + ". " + command).formatted(getValueColor()));
        }
    }

    public static void printEnabledFilters(String message, List<ChatFilter> filters) {
        if (MC.player == null) {
            return;
        }

        printMessage(message);

        for (ChatFilter filter : filters) {
            boolean enabled = filter.isEnabled();
            MutableText prefix = Text.literal("- ").formatted(getValueColor());
            MutableText name = Text.translatable(filter.getKey()).append(": ").formatted(getMessageColor());
            MutableText enabledText = Text.literal(enabled ? "enabled" : "disabled").formatted(getBooleanColor(enabled));
            MC.player.sendMessage(prefix.append(name).append(enabledText), false);
        }
    }

    public static void printEnchantCooldown(double cooldownTime) {
        MutableText message = Text.translatable("message.sbutils.enchantAll.cooldown").formatted(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(String.valueOf(cooldownTime)).formatted(getValueColor())));
    }

    public static void printAutoFixInfo(boolean enabled, boolean fixing, int mostDamagedItem, int remainingDelay) {
        if (!enabled) {
            printSetting("text.sbutils.config.category.autofix", false);
            return;
        }

        if (fixing) {
            printMessage("message.sbutils.autoFix.currentlyFixing");
            return;
        }

        if (remainingDelay == 0 && mostDamagedItem == -1) {
            printMessage("message.sbutils.autoFix.waiting");
            return;
        }

        int minutes = remainingDelay / 60000;
        double seconds = Math.round((remainingDelay % 60000) / 100.0) / 10.0;

        if (minutes != 0) {
            printWithPlaceholders("message.sbutils.autoFix.infoWithMinutes", minutes, seconds);
        } else {
            printWithPlaceholders("message.sbutils.autoFix.infoJustSeconds", seconds);
        }
    }

    public static void printAutoCommandInfo(boolean enabled, int remainingDelay) {
        if (!enabled) {
            printSetting("text.sbutils.config.category.autocommand", false);
            return;
        }

        int minutes = remainingDelay / 60000;
        double seconds = Math.round((remainingDelay % 60000) / 100.0) / 10.0;

        if (minutes != 0) {
            printWithPlaceholders("message.sbutils.autoCommand.infoWithMinutes", minutes, seconds);
        } else {
            printWithPlaceholders("message.sbutils.autoCommand.infoJustSeconds", seconds);
        }
    }

    public static void printChatAppendStatus(String type, boolean enabled, String value) {
        MutableText message = Text.translatable("message.sbutils.chatAppend.status").formatted(getMessageColor());
        MutableText typeText = Text.translatable(type).formatted(getValueColor());
        MutableText enabledText = Text.translatable(enabled ? "message.sbutils.enabled" : "message.sbutils.disabled").formatted(getBooleanColor(enabled));
        MutableText valueText = Text.literal(value).formatted(getValueColor());
        printMessage(insertPlaceholders(message, typeText, enabledText, valueText));
    }

    private static Formatting getBooleanColor(boolean bool) {
        return bool ? Formatting.GREEN : Formatting.RED;
    }
}
