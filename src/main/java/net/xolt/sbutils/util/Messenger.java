package net.xolt.sbutils.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.gui.KeyValueController;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.AutoKit;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    public static void printMessage(String message, int color) {
        printMessage(message, color, true);
    }

    public static void printMessage(String message, int color, boolean showPrefix) {
        printMessage(Text.translatable(message).withColor(color), showPrefix);
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

        MutableText sbutilsText = Text.literal("sbutils").withColor(ModConfig.HANDLER.instance().sbutilsColor.getRGB());
        MutableText prefix = insertPlaceholders(Text.literal(ModConfig.HANDLER.instance().prefixFormat + " ").withColor(ModConfig.HANDLER.instance().prefixColor.getRGB()), sbutilsText);

        MC.player.sendMessage(prefix.append(message));
    }

    public static void sendTitle(String message) {
        sendTitle(message, getMessageColor());
    }
    
    public static void sendTitle(String message, int color) {
        sendTitle(Text.translatable(message).withColor(color), 5, 30, 5);
    }

    public static void sendTitle(String message, int color, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        sendTitle(Text.translatable(message).withColor(color), fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void sendTitle(Text message) {
        sendTitle(message, 5, 30, 5);
    }

    public static void sendTitle(Text message, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        MC.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
        MC.inGameHud.setTitle(message);
    }

    public static void sendPlaceholderTitle(String message, Object ... args) {
        sendTitle(insertPlaceholders(message, format(args)));
    }

    public static void printWithPlaceholders(String message, Object ... args) {
        printMessage(insertPlaceholders(message, format(args)));
    }


    private static MutableText insertPlaceholders(String message, MutableText ... args) {
        MutableText messageText = Text.translatable(message).withColor(getMessageColor());
        return insertPlaceholders(messageText, args);
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

    public static void printChangedSetting(String setting, Object value) {
        printSetting(setting, value, true);
    }

    public static void printSetting(String setting, Object value) {
        printSetting(setting, value, false);
    }

    private static void printSetting(String setting, Object value, boolean changed) {
        MutableText settingText = Text.translatable(setting).withColor(getValueColor());
        if (value instanceof Boolean) {
            MutableText message = Text.translatable(changed ? "message.sbutils.changeBooleanSetting" : "message.sbutils.printBooleanSetting").withColor(getMessageColor());
            printMessage(insertPlaceholders(message, settingText, format(value)));
            return;
        }
        MutableText message = Text.translatable(changed ? "message.sbutils.changeOtherSetting" : "message.sbutils.printOtherSetting").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, settingText, format(value)));
    }

    public static <T> void printListSetting(String setting, List<T> list) {
        printListSetting(setting, list, false);
    }

    public static <T> void printListSetting(String setting, List<T> list, boolean numbered) {
        printWithPlaceholders("message.sbutils.printListSetting", Text.translatable(setting).withColor(getValueColor()));
        printList(list, numbered);
    }

    private static <T> void printList(List<T> items, boolean numbered) {
        for (int i = 0; i < items.size(); i++)
            printMessage(Text.literal(" " + (numbered ? (i + 1) + ". " : "- ")).withColor(getValueColor()).append(format(items.get(i))), false);
    }

    public static void printInvalidListIndex(String setting, int index) {
        printWithPlaceholders("message.sbutils.invalidListIndex", index, Text.translatable(setting).withColor(getValueColor()));
    }

    public static void printListSizeError(String setting, int maxSize) {
        printWithPlaceholders("message.sbutils.listSizeError", maxSize, Text.translatable(setting).withColor(getValueColor()));
    }

    public static <T> void printListDupeError(String setting, T dupe) {
        printWithPlaceholders("message.sbutils.listDupeError", dupe, Text.translatable(setting).withColor(getValueColor()));
    }

    public static <T> void printListAddSuccess(String setting, T added) {
        printWithPlaceholders("message.sbutils.listAddSuccess", added, Text.translatable(setting).withColor(getValueColor()));
    }

    public static <T> void printListDelSuccess(String setting, T removed) {
        printWithPlaceholders("message.sbutils.listDelSuccess", removed, Text.translatable(setting).withColor(getValueColor()));
    }

    public static <T> void printListDelFail(String setting, T removed) {
        printWithPlaceholders("message.sbutils.listDelFail", removed, Text.translatable(setting).withColor(getValueColor()));
    }

    public static void sendLlamaTitle() {
        sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Text.translatable("message.sbutils.eventNotifier.vpLlama").withColor(getValueColor()));
    }

    public static void sendTraderTitle() {
        sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Text.translatable("message.sbutils.eventNotifier.wanderingTrader").withColor(getValueColor()));
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
        MutableText index = Text.literal("#" + (adIndex + 1)).withColor(getValueColor());
        MutableText seconds = Text.literal(String.valueOf(delaySeconds)).withColor(getValueColor());

        if (delayMinutes > 0) {
            message = Text.translatable("message.sbutils.autoAdvert.infoWithMinutes").withColor(getMessageColor());
            MutableText minutes = Text.literal(String.valueOf(delayMinutes)).withColor(getValueColor());
            printMessage(insertPlaceholders(message, index, minutes, seconds));
        } else {
            message = Text.translatable("message.sbutils.autoAdvert.infoJustSeconds").withColor(getMessageColor());
            printMessage(insertPlaceholders(message, index, seconds));
        }
    }

    public static void printStaffNotification(PlayerListEntry player, boolean joined) {
        MutableText message = Text.translatable("message.sbutils.staffDetector.notification").withColor(getMessageColor());
        MutableText staff = Text.literal(player.getProfile().getName()).withColor(getValueColor());
        MutableText status = Text.translatable(joined ? "message.sbutils.online" : "message.sbutils.offline").formatted(getBooleanColor(joined));
        printMessage(insertPlaceholders(message, staff, status));
    }

    public static void printConversions(String input, String items, String stacks, String dcs, String stacksAndRemainer, String dcsAndRemainder) {
        MutableText message = Text.translatable("message.sbutils.convert.header").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(input).withColor(getValueColor())));

        MutableText itemsText = Text.literal("- " + items).withColor(getValueColor());
        printMessage(itemsText, false);
        MutableText stacksText = Text.literal("- " + stacks).withColor(getValueColor());
        printMessage(stacksText, false);
        MutableText dcsText = Text.literal("- " + dcs).withColor(getValueColor());
        printMessage(dcsText, false);
        MutableText stacksAndRemainderText = Text.literal("- " + stacksAndRemainer).withColor(getValueColor());
        printMessage(stacksAndRemainderText, false);
        MutableText dcsAndRemainderText = Text.literal("- " + dcsAndRemainder).withColor(getValueColor());
        printMessage(dcsAndRemainderText, false);
    }

    public static void printEnabledFilters(String message, List<ChatFilter> filters) {
        printMessage(message);
        List<MutableText> formatted = filters.stream().map((filter) -> {
            boolean enabled = filter.isEnabled();
            MutableText name = Text.translatable(filter.getKey()).append(": ").withColor(getMessageColor());
            MutableText enabledText = Text.literal(enabled ? "enabled" : "disabled").formatted(getBooleanColor(enabled));
            return name.append(enabledText);
        }).toList();
        printList(formatted, false);
    }

    public static void printEnchantCooldown(double cooldownTime) {
        MutableText message = Text.translatable("message.sbutils.enchantAll.cooldown").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, Text.literal(String.valueOf(cooldownTime)).withColor(getValueColor())));
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
        MutableText commandEntryFormat = Text.translatable("message.sbutils.autoCommand.commandEntry").withColor(getMessageColor());
        int valueColor = getValueColor();
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < commands.size(); i++) {
            KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command = commands.get(i);
            MutableText commandText = format(command.getKey());
            double delay = command.getValue().getKey();
            MutableText delayText = Text.literal(formatTime(delay)).withColor(valueColor);
            boolean cmdEnabled = command.getValue().getValue();
            MutableText enabledText = format(cmdEnabled);
            Long cmdLastSentAt = cmdsLastSentAt.get(command);
            MutableText delayLeftText;
            if (!enabled || !cmdEnabled || cmdLastSentAt == null) {
              delayLeftText = Text.literal("N/A").withColor(valueColor);
            } else {
                long delayLeftMillis = (long)(delay * 1000.0) - (currentTime - cmdLastSentAt);
                double delayLeft = (double)Math.max(delayLeftMillis, 0) / 1000.0;
                delayLeftText = Text.literal(formatTime(delayLeft)).withColor(valueColor);
            }
            MutableText numberPrefix = Text.literal(i + 1 + ". ");
            printMessage(numberPrefix.append(insertPlaceholders(commandEntryFormat, commandText, delayText, enabledText, delayLeftText)), false);
        }
    }

    public static void printAutoCommandToggled(KeyValueController.KeyValuePair<String, KeyValueController.KeyValuePair<Double, Boolean>> command, boolean enabled) {
        MutableText format = Text.translatable("message.sbutils.autoCommand.commandToggleSuccess");
        MutableText commandText = Text.literal(command.getKey()).withColor(getValueColor());
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
            MutableText message = Text.literal(i + 1 + ". ").withColor(getValueColor());
            message.append(insertPlaceholders(Text.translatable("message.sbutils.autoKit.infoFormat").withColor(getMessageColor()),
                    Text.literal(kit.kit.asString()).withColor(getValueColor()),
                    Text.literal("INV FULL").formatted(Formatting.RED)));
            printMessage(message, false);
        }
        for (int i = 0; i < kitList.size(); i++) {
            AutoKit.KitQueueEntry kit = kitList.get(i);
            MutableText message = Text.literal(i + invFullList.size() + 1 + ". ").withColor(getValueColor());
            double timeLeft = Math.max(0, (kit.claimAt - System.currentTimeMillis())) / 1000.0;
            message.append(insertPlaceholders(Text.translatable("message.sbutils.autoKit.infoFormat").withColor(getMessageColor()),
                    Text.literal(kit.kit.asString()).withColor(getValueColor()),
                    Text.literal(formatTime(timeLeft)).withColor(getValueColor())));
            printMessage(message, false);
        }
    }

    public static void printMapArtSuitability(int size, int[] extraSpace) {
        String expansion = size + "x" + size;
        Messenger.printWithPlaceholders("message.sbutils.centered.suitable", expansion);
        Messenger.printWithPlaceholders("message.sbutils.centered.extraSpace", extraSpace[3], extraSpace[2], extraSpace[0], extraSpace[1]);
    }

    public static void printInvCleanFailed(String featureName) {
        printWithPlaceholders("message.sbutils.invCleaner.cleanFailed", Text.translatable(featureName).withColor(getValueColor()));
    }

    public static void printAutoMineEnabledFor(long disableAt) {
        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.enabledFor", Text.translatable("text.sbutils.config.category.autoMine").withColor(getValueColor()), formatTime(timeLeft));
    }

    public static void printAutoMineTime(long disableAt) {
        if (disableAt == -1) {
            Messenger.printMessage("message.sbutils.autoMine.timerNotSet");
            return;
        }

        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.disabledIn", Text.translatable("text.sbutils.config.category.autoMine").withColor(getValueColor()), formatTime(timeLeft));
    }

    private static <T> MutableText format(T input) {
        MutableText result;
        if (input instanceof MutableText) {
            result = (MutableText) input;
        } else if (input instanceof Boolean) {
            result = boolToText((Boolean) input);
        } else if (input instanceof NameableEnum) {
            result = ((NameableEnum)input).getDisplayName().copy().withColor(getValueColor());
        } else if (input instanceof Number) {
            result = Text.literal(String.valueOf(input)).withColor(getValueColor());
        } else if (input instanceof String) {
            result = Text.literal(!((String) input).isEmpty() ? (String) input : "nothing").withColor(!((String) input).isEmpty() ? getValueColor() : getMessageColor());
            if (((String) input).isEmpty())
                result = result.formatted(Formatting.ITALIC);
        } else if (input instanceof Color) {
            result = Text.literal("#" + String.format("%06x", ((Color) input).getRGB() & 0x00FFFFFF)).withColor(((Color) input).getRGB());
        } else {
            result = Text.literal(String.valueOf(input)).withColor(getValueColor());
        }
        return result;
    }

    private static <T> MutableText[] format(T[] input) {
        MutableText[] result = new MutableText[input.length];
        for (int i = 0; i < input.length; i++)
            result[i] = format(input[i]);
        return result;
    }

    private static MutableText boolToText(boolean bool) {
        return Text.translatable(bool ? "message.sbutils.enabled" : "message.sbutils.disabled").formatted(getBooleanColor(bool));
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

    private static int getMessageColor() {
        return ModConfig.HANDLER.instance().messageColor.getRGB();
    }

    private static int getValueColor() {
        return ModConfig.HANDLER.instance().valueColor.getRGB();
    }

    private static Formatting getBooleanColor(boolean bool) {
        return bool ? Formatting.GREEN : Formatting.RED;
    }
}
