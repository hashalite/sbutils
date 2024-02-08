package net.xolt.sbutils.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.KeyValueController;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.AutoKit;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class Messenger {

    public static void printMessage(String message) {
        printMessage(message, true);
    }

    public static void printMessage(String message, boolean showPrefix) {
        printMessage(message, getMessageColor(), showPrefix);
    }

    public static void printMessage(String message, Formatting formatting) {
        printMessage(message, formatting, true);
    }

    public static void printMessage(String message, Formatting formatting, boolean showPrefix) {
        printMessage(Text.translatable(message).formatted(formatting), showPrefix);
    }

    public static void printMessage(Text message) {
        printMessage(message, true);
    }

    public static void printMessage(Text message, boolean showPrefix) {
        if (MC.player == null)
            return;

        if (!showPrefix) {
            MC.player.sendMessage(message);
            return;
        }

        MutableText sbutilsText = Text.literal("sbutils").formatted(ModConfig.HANDLER.instance().sbutilsColor.getFormatting());
        MutableText prefix = insertPlaceholders(Text.literal(ModConfig.HANDLER.instance().prefixFormat + " ").formatted(ModConfig.HANDLER.instance().prefixColor.getFormatting()), sbutilsText);

        MC.player.sendMessage(prefix.append(message));
    }

    public static void sendPlaceholderTitle(String message, Formatting placeholderFormat, Object ... args) {
        MutableText messageText = Text.translatable(message).formatted(getMessageColor());
        List<MutableText> placeholders = new ArrayList<>();
        for (Object placeholder : args) {
            placeholders.add(Text.translatable(String.valueOf(placeholder)).formatted(placeholderFormat));
        }
        sendTitle(insertPlaceholders(messageText, placeholders.toArray(MutableText[]::new)));
    }

    public static void sendTitle(String message) {
        sendTitle(message, getMessageColor());
    }

    public static void sendTitle(String message, Formatting formatting) {
        sendTitle(Text.translatable(message).formatted(formatting));
    }

    public static void sendTitle(Text message) {
        sendTitle(message, 5, 30, 5);
    }

    public static void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        MC.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
        MC.inGameHud.setTitle(message);
    }

    private static Formatting getMessageColor() {
        return ModConfig.HANDLER.instance().messageColor.getFormatting();
    }

    private static Formatting getValueColor() {
        return ModConfig.HANDLER.instance().valueColor.getFormatting();
    }

    public static void printWithPlaceholders(String message, Object ... args) {
        MutableText messageText = Text.translatable(message).formatted(getMessageColor());
        List<MutableText> placeholders = new ArrayList<>();
        for (Object placeholder : args) {
            placeholders.add(format(placeholder));
        }
        printMessage(insertPlaceholders(messageText, placeholders.toArray(MutableText[]::new)));
    }

    private static MutableText insertPlaceholders(MutableText message, MutableText ... args) {
        String messageString = message.getString();
        if (!messageString.contains("%s")) {
            return message;
        }
        MutableText formatted = Text.empty();
        Style style = message.getStyle();
        List<String> pieces = new ArrayList<>(Arrays.asList(messageString.split("%s")));

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
        MutableText settingText = Text.translatable(setting).formatted(getValueColor());
        if (value instanceof Boolean) {
            MutableText message = Text.translatable(changed ? "message.sbutils.changeBooleanSetting" : "message.sbutils.printBooleanSetting").formatted(getMessageColor());
            printMessage(insertPlaceholders(message, settingText, format(value)));
        } else if (value instanceof NameableEnum || value instanceof Number || value instanceof String) {
            MutableText message = Text.translatable(changed ? "message.sbutils.changeOtherSetting" : "message.sbutils.printOtherSetting").formatted(getMessageColor());
            printMessage(insertPlaceholders(message, settingText, format(value)));
        }
    }

    private static <T> MutableText format(T input) {
        MutableText result;
        if (input instanceof MutableText) {
            result = (MutableText) input;
        } else if (input instanceof Boolean) {
            result = boolToText((Boolean) input);
        } else if (input instanceof NameableEnum) {
            result = ((NameableEnum)input).getDisplayName().copy().formatted(getValueColor());
        } else if (input instanceof Number) {
            result = Text.literal(String.valueOf(input)).formatted(getValueColor());
        } else if (input instanceof String) {
            result = Text.literal(!((String) input).isEmpty() ? (String) input : "nothing").formatted(!((String) input).isEmpty() ? getValueColor() : getMessageColor());
        } else {
            result = Text.literal(String.valueOf(input)).formatted(getValueColor());
        }
        return result;
    }

    private static MutableText boolToText(boolean bool) {
        return Text.translatable(bool ? "message.sbutils.enabled" : "message.sbutils.disabled").formatted(getBooleanColor(bool));
    }

    public static void printChangedSetting(String setting, Object value) {
        printSetting(setting, value, true);
    }

    public static void printSetting(String setting, Object value) {
        printSetting(setting, value, false);
    }

    public static void printAutoAdvertInfo(boolean enabled, boolean onSkyblock, int adIndex, int remainingDelay, boolean userWhitelisted, boolean whitelistEnabled) {
        if (!enabled) {
            printSetting("text.sbutils.config.category.autoAdvert", false);
            return;
        }

        if (!onSkyblock) {
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
        MutableText message = Text.translatable("message.sbutils.convert.header").formatted(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(input).formatted(getValueColor())));

        MutableText itemsText = Text.literal("- " + items).formatted(getValueColor());
        printMessage(itemsText, false);
        MutableText stacksText = Text.literal("- " + stacks).formatted(getValueColor());
        printMessage(stacksText, false);
        MutableText dcsText = Text.literal("- " + dcs).formatted(getValueColor());
        printMessage(dcsText, false);
        MutableText stacksAndRemainderText = Text.literal("- " + stacksAndRemainer).formatted(getValueColor());
        printMessage(stacksAndRemainderText, false);
        MutableText dcsAndRemainderText = Text.literal("- " + dcsAndRemainder).formatted(getValueColor());
        printMessage(dcsAndRemainderText, false);
    }

    public static <T> void printListSetting(String message, List<T> list) {
        printMessage(message);
        printNumberedList(list);
    }

    private static <T> void  printNumberedList(List<T> items) {
        if (MC.player == null) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            printMessage(Text.literal((i + 1) + ". ").formatted(getValueColor()).append(format(items.get(i))), false);
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
            printMessage(prefix.append(name).append(enabledText), false);
        }
    }

    public static void printEnchantCooldown(double cooldownTime) {
        MutableText message = Text.translatable("message.sbutils.enchantAll.cooldown").formatted(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(String.valueOf(cooldownTime)).formatted(getValueColor())));
    }

    public static void printAutoFixInfo(boolean enabled, boolean fixing, int mostDamagedItem, int remainingDelay) {
        if (!enabled) {
            printSetting("text.sbutils.config.category.autoFix", false);
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

    public static void printAutoCommands(List<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>> commands, HashMap<KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>>, Long> cmdsLastSentAt, boolean enabled) {
        printMessage("message.sbutils.autoCommand.commands");
        MutableText commandEntryFormat = Text.translatable("message.sbutils.autoCommand.commandEntry").formatted(getMessageColor());
        Formatting valueColor = getValueColor();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < commands.size(); i++) {
            KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = commands.get(i);
            MutableText commandText = format(command.getKey());
            double delay = command.getValue().getKey();
            MutableText delayText = Text.literal(formatTime(delay)).formatted(valueColor);
            boolean cmdEnabled = command.getValue().getValue();
            MutableText enabledText = format(cmdEnabled);
            Long cmdLastSentAt = cmdsLastSentAt.get(command);
            MutableText delayLeftText;
            if (!enabled || !cmdEnabled || cmdLastSentAt == null) {
              delayLeftText = Text.literal("N/A").formatted(valueColor);
            } else {
                long delayLeftMillis = (long)(delay * 1000.0) - (currentTime - cmdLastSentAt);
                double delayLeft = (double)Math.max(delayLeftMillis, 0) / 1000.0;
                delayLeftText = Text.literal(formatTime(delayLeft)).formatted(valueColor);
            }
            MutableText numberPrefix = Text.literal(i + 1 + ". ");
            printMessage(numberPrefix.append(insertPlaceholders(commandEntryFormat, commandText, delayText, enabledText, delayLeftText)), false);
        }
    }

    public static void printAutoCommandToggled(KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command, boolean enabled) {
        Formatting valueColor = getValueColor();
        MutableText format = Text.translatable("message.sbutils.autoCommand.commandToggleSuccess");
        MutableText commandText = Text.literal(command.getKey()).formatted(valueColor);
        MutableText enabledText = boolToText(enabled);
        insertPlaceholders(format, commandText, enabledText);
    }

    public static void printAutoKitInfo(PriorityQueue<AutoKit.KitQueueEntry> kitQueue, List<AutoKit.KitQueueEntry> invFullList) {
        if (MC.player == null) {
            return;
        }

        printMessage("message.sbutils.autoKit.info");

        List<AutoKit.KitQueueEntry> kitList = new ArrayList<>(kitQueue);
        kitList.sort(kitQueue.comparator());
        for (int i = 0; i < invFullList.size(); i++) {
            AutoKit.KitQueueEntry kit = invFullList.get(i);
            MutableText message = Text.literal(i + 1 + ". ").formatted(getValueColor());
            message.append(insertPlaceholders(Text.translatable("message.sbutils.autoKit.infoFormat").formatted(getMessageColor()),
                    Text.literal(kit.kit.asString()).formatted(getValueColor()),
                    Text.literal("INV FULL").formatted(Formatting.RED)));
            printMessage(message, false);
        }
        for (int i = 0; i < kitList.size(); i++) {
            AutoKit.KitQueueEntry kit = kitList.get(i);
            MutableText message = Text.literal(i + invFullList.size() + 1 + ". ").formatted(getValueColor());
            double timeLeft = Math.max(0, (kit.claimAt - System.currentTimeMillis())) / 1000.0;
            message.append(insertPlaceholders(Text.translatable("message.sbutils.autoKit.infoFormat").formatted(getMessageColor()),
                    Text.literal(kit.kit.asString()).formatted(getValueColor()),
                    Text.literal(formatTime(timeLeft)).formatted(getValueColor())));
            printMessage(message, false);
        }
    }

    public static void printMapArtSuitability(int size, int[] extraSpace) {
        String expansion = size + "x" + size;
        Messenger.printWithPlaceholders("message.sbutils.centered.suitable", expansion);
        Messenger.printWithPlaceholders("message.sbutils.centered.extraSpace", extraSpace[3], extraSpace[2], extraSpace[0], extraSpace[1]);
    }

    private static Formatting getBooleanColor(boolean bool) {
        return bool ? Formatting.GREEN : Formatting.RED;
    }

    public static void printAutoMineEnabledFor(long disableAt) {
        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.enabledFor", Text.translatable("text.sbutils.config.category.autoMine").formatted(getValueColor()), formatTime(timeLeft));
    }

    public static void printAutoMineTime(long disableAt) {
        if (disableAt == -1) {
            Messenger.printMessage("message.sbutils.autoMine.timerNotSet");
            return;
        }

        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.disabledIn", Text.translatable("text.sbutils.config.category.autoMine").formatted(getValueColor()), formatTime(timeLeft));
    }

    private static String formatTime(double seconds) {
        int days = (int)(seconds / 86400.0);
        int hours = (int)((seconds % 86400.0)  / 3600.0);
        int minutes = (int)(((seconds % 86400.0)  % 3600.0) / 60.0);
        double secs = ((seconds % 86400.0)  % 3600.0) % 60.0;
        return (days > 0 ? days + "d " : "") + (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + String.format("%.1f", secs) + "s";
    }

    private static String formatTime(long millis) {
        return formatTime((double)millis/1000.0);
    }
}
