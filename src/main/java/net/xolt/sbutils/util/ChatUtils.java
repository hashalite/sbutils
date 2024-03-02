package net.xolt.sbutils.util;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.AutoKit;

import java.awt.*;
import java.util.*;
import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class ChatUtils {

    public static void printMessage(String message) {
        printMessage(message, true);
    }

    public static void printMessage(String message, boolean showPrefix) {
        printMessage(message, getMessageColor(), showPrefix);
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
            MC.player.sendSystemMessage(message);
            return;
        }

        MutableComponent sbutilsText = Component.literal("sbutils").withColor(ModConfig.HANDLER.instance().sbutilsColor.getRGB());
        MutableComponent prefix = insertPlaceholders(Component.literal(ModConfig.HANDLER.instance().prefixFormat + " ").withColor(ModConfig.HANDLER.instance().prefixColor.getRGB()), sbutilsText);

        MC.player.sendSystemMessage(prefix.append(message));
    }

    public static void sendTitle(String message) {
        sendTitle(message, getMessageColor());
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
        sendTitle(insertPlaceholders(message, formatPlaceholders(args)));
    }

    public static void printWithPlaceholders(String message, Object ... args) {
        printMessage(insertPlaceholders(message, formatPlaceholders(args)));
    }


    public static MutableComponent insertPlaceholders(String message, Object ... args) {
        return insertPlaceholders(message, formatPlaceholders(args));
    }

    public static MutableComponent insertPlaceholders(String message, MutableComponent ... args) {
        MutableComponent messageText = Component.translatable(message).withColor(getMessageColor());
        return insertPlaceholders(messageText, args);
    }

    public static MutableComponent insertPlaceholders(MutableComponent message, MutableComponent ... args) {
        String messageString = message.getString();
        if (!messageString.contains("%s")) {
            return message;
        }
        MutableComponent formatted = Component.empty();
        Style style = message.getStyle();
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

    public static void printChangedSetting(String setting, Object value) {
        printSetting(setting, value, true);
    }

    public static void printSetting(String setting, Object value) {
        printSetting(setting, value, false);
    }

    private static void printSetting(String setting, Object value, boolean changed) {
        MutableComponent settingText = Component.translatable(setting).withColor(getValueColor());
        if (value instanceof Boolean) {
            MutableComponent message = Component.translatable(changed ? "message.sbutils.changeBooleanSetting" : "message.sbutils.printBooleanSetting").withColor(getMessageColor());
            printMessage(insertPlaceholders(message, settingText, formatPlaceholder(value)));
            return;
        }
        MutableComponent message = Component.translatable(changed ? "message.sbutils.changeOtherSetting" : "message.sbutils.printOtherSetting").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, settingText, formatPlaceholder(value)));
    }

    public static <T> void printListSetting(String setting, List<T> list) {
        printListSetting(setting, list, false);
    }

    public static <T> void printListSetting(String setting, List<T> list, boolean numbered) {
        printWithPlaceholders("message.sbutils.printListSetting", Component.translatable(setting).withColor(getValueColor()));
        printList(list, numbered);
    }

    private static <T> void printList(List<T> items, boolean numbered) {
        for (int i = 0; i < items.size(); i++)
            printMessage(Component.literal(" " + (numbered ? (i + 1) + ". " : "- ")).withColor(getValueColor()).append(format(items.get(i))), false);
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

        MutableComponent message;
        MutableComponent index = Component.literal("#" + (adIndex + 1)).withColor(getValueColor());
        MutableComponent seconds = Component.literal(String.valueOf(delaySeconds)).withColor(getValueColor());

        if (delayMinutes > 0) {
            message = Component.translatable("message.sbutils.autoAdvert.infoWithMinutes").withColor(getMessageColor());
            MutableComponent minutes = Component.literal(String.valueOf(delayMinutes)).withColor(getValueColor());
            printMessage(insertPlaceholders(message, index, minutes, seconds));
        } else {
            message = Component.translatable("message.sbutils.autoAdvert.infoJustSeconds").withColor(getMessageColor());
            printMessage(insertPlaceholders(message, index, seconds));
        }
    }

    public static void printStaffNotification(PlayerInfo player, boolean joined) {
        MutableComponent message = Component.translatable("message.sbutils.staffDetector.notification").withColor(getMessageColor());
        MutableComponent staff = Component.literal(player.getProfile().getName()).withColor(getValueColor());
        MutableComponent status = Component.translatable(joined ? "message.sbutils.online" : "message.sbutils.offline").withStyle(getBooleanColor(joined));
        printMessage(insertPlaceholders(message, staff, status));
    }

    public static void printConversions(String input, String items, String stacks, String dcs, String stacksAndRemainer, String dcsAndRemainder) {
        MutableComponent message = Component.translatable("message.sbutils.convert.header").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, Component.literal(input).withColor(getValueColor())));

        MutableComponent itemsText = Component.literal("- " + items).withColor(getValueColor());
        printMessage(itemsText, false);
        MutableComponent stacksText = Component.literal("- " + stacks).withColor(getValueColor());
        printMessage(stacksText, false);
        MutableComponent dcsText = Component.literal("- " + dcs).withColor(getValueColor());
        printMessage(dcsText, false);
        MutableComponent stacksAndRemainderText = Component.literal("- " + stacksAndRemainer).withColor(getValueColor());
        printMessage(stacksAndRemainderText, false);
        MutableComponent dcsAndRemainderText = Component.literal("- " + dcsAndRemainder).withColor(getValueColor());
        printMessage(dcsAndRemainderText, false);
    }

    public static void printEnabledFilters(String message, List<ChatFilter> filters) {
        printMessage(message);
        List<MutableComponent> formatted = filters.stream().map((filter) -> {
            boolean enabled = filter.isEnabled();
            MutableComponent name = filter.getName().append(": ").withColor(getMessageColor());
            MutableComponent enabledText = Component.literal(enabled ? "enabled" : "disabled").withStyle(getBooleanColor(enabled));
            return name.append(enabledText);
        }).toList();
        printList(formatted, false);
    }

    public static void printEnchantCooldown(double cooldownTime) {
        MutableComponent message = Component.translatable("message.sbutils.enchantAll.cooldown").withColor(getMessageColor());
        printMessage(insertPlaceholders(message, Component.literal(String.valueOf(cooldownTime)).withColor(getValueColor())));
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

    public static void printAutoKitInfo(PriorityQueue<AutoKit.KitQueueEntry> kitQueue, List<AutoKit.KitQueueEntry> invFullList) {
        if (MC.player == null) {
            return;
        }

        printMessage("message.sbutils.autoKit.info");

        List<AutoKit.KitQueueEntry> kitList = new ArrayList<>(kitQueue);
        kitList.sort(kitQueue.comparator());
        for (int i = 0; i < invFullList.size(); i++) {
            AutoKit.KitQueueEntry kit = invFullList.get(i);
            MutableComponent message = Component.literal(i + 1 + ". ").withColor(getValueColor());
            message.append(insertPlaceholders(Component.translatable("message.sbutils.autoKit.infoFormat").withColor(getMessageColor()),
                    Component.literal(kit.kit.getSerializedName()).withColor(getValueColor()),
                    Component.literal("INV FULL").withStyle(ChatFormatting.RED)));
            printMessage(message, false);
        }
        for (int i = 0; i < kitList.size(); i++) {
            AutoKit.KitQueueEntry kit = kitList.get(i);
            MutableComponent message = Component.literal(i + invFullList.size() + 1 + ". ").withColor(getValueColor());
            double timeLeft = Math.max(0, (kit.claimAt - System.currentTimeMillis())) / 1000.0;
            message.append(insertPlaceholders(Component.translatable("message.sbutils.autoKit.infoFormat").withColor(getMessageColor()),
                    Component.literal(kit.kit.getSerializedName()).withColor(getValueColor()),
                    Component.literal(formatTime(timeLeft)).withColor(getValueColor())));
            printMessage(message, false);
        }
    }

    public static void printMapArtSuitability(int size, int[] extraSpace) {
        String expansion = size + "x" + size;
        ChatUtils.printWithPlaceholders("message.sbutils.centered.suitable", expansion);
        ChatUtils.printWithPlaceholders("message.sbutils.centered.extraSpace", extraSpace[3], extraSpace[2], extraSpace[0], extraSpace[1]);
    }

    public static void printInvCleanFailedCritical(String featureName) {
        printWithPlaceholders("message.sbutils.invCleaner.cleanFailedCritical", Component.translatable(featureName).withColor(getValueColor()));
    }

    public static void printAutoMineEnabledFor(long disableAt) {
        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.enabledFor", Component.translatable("text.sbutils.config.category.autoMine").withColor(getValueColor()), formatTime(timeLeft));
    }

    public static void printAutoMineTime(long disableAt) {
        if (disableAt == -1) {
            ChatUtils.printMessage("message.sbutils.autoMine.timerNotSet");
            return;
        }

        long timeLeft = disableAt - System.currentTimeMillis();
        printWithPlaceholders("message.sbutils.autoMine.disabledIn", Component.translatable("text.sbutils.config.category.autoMine").withColor(getValueColor()), formatTime(timeLeft));
    }

    private static <T> MutableComponent format(T input) {
        if (input instanceof ModConfig.MultiValue multiValue)
            return multiValue.format();
        return formatPlaceholder(input);
    }

    private static <T> MutableComponent[] format(T[] input) {
        MutableComponent[] result = new MutableComponent[input.length];
        for (int i = 0; i < input.length; i++)
            result[i] = format(input[i]);
        return result;
    }

    private static <T> MutableComponent formatPlaceholder(T input) {
        MutableComponent result;
        if (input instanceof MutableComponent text) {
            result = text;
            if (result.getStyle().isEmpty())
                result = result.withColor(getValueColor());
        } else if (input instanceof Boolean bool) {
            result = formatBoolean(bool);
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

    private static <T> MutableComponent[] formatPlaceholders(T[] input) {
        MutableComponent[] result = new MutableComponent[input.length];
        for (int i = 0; i < input.length; i++)
            result[i] = formatPlaceholder(input[i]);
        return result;
    }

    private static MutableComponent formatBoolean(boolean bool) {
        return Component.translatable(bool ? "message.sbutils.enabled" : "message.sbutils.disabled").withStyle(getBooleanColor(bool));
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

    private static int getMessageColor() {
        return ModConfig.HANDLER.instance().messageColor.getRGB();
    }

    private static int getValueColor() {
        return ModConfig.HANDLER.instance().valueColor.getRGB();
    }

    private static ChatFormatting getBooleanColor(boolean bool) {
        return bool ? ChatFormatting.GREEN : ChatFormatting.RED;
    }
}
