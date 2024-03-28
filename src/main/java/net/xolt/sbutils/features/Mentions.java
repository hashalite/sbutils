package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class Mentions {

    private static final String COMMAND = "mentions";
    private static final String ALIAS = "ment";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> mentionsNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "mentions", () -> ModConfig.INSTANCE.mentions.enabled, (value) -> ModConfig.INSTANCE.mentions.enabled = value)
                    .then(CommandUtils.bool("playSound", "mentions.playMentionSound", () -> ModConfig.INSTANCE.mentions.playMentionSound, (value) -> ModConfig.INSTANCE.mentions.playMentionSound = value))
                    .then(CommandUtils.bool("excludeServer", "mentions.excludeServerMsgs", () -> ModConfig.INSTANCE.mentions.excludeServerMsgs, (value) -> ModConfig.INSTANCE.mentions.excludeServerMsgs = value))
                    .then(CommandUtils.bool("excludeSelf", "mentions.excludeSelfMsgs", () -> ModConfig.INSTANCE.mentions.excludeSelfMsgs, (value) -> ModConfig.INSTANCE.mentions.excludeSelfMsgs = value))
                    .then(CommandUtils.bool("excludeSender", "mentions.excludeSender", () -> ModConfig.INSTANCE.mentions.excludeSender, (value) -> ModConfig.INSTANCE.mentions.excludeSender = value))
                    .then(CommandUtils.bool("currentAccount", "mentions.mentionsCurrentAccount", () -> ModConfig.INSTANCE.mentions.mentionsCurrentAccount, (value) -> ModConfig.INSTANCE.mentions.mentionsCurrentAccount = value))
                    .then(CommandUtils.stringList("aliases", "alias", "message.sbutils.mentions.aliases",
                            () -> ModConfig.INSTANCE.mentions.mentionsAliases,
                            Mentions::onAddAliasCommand,
                            Mentions::onDelAliasCommand,
                            Mentions::onInsertAliasCommand))
                    .then(CommandUtils.getterSetter("sound", "sound", "mentions.mentionSound", () -> ModConfig.INSTANCE.mentions.mentionSound, (value) -> ModConfig.INSTANCE.mentions.mentionSound = value, ModConfig.NotifSound.NotifSoundArgumentType.notifSound(), ModConfig.NotifSound.NotifSoundArgumentType::getNotifSound))
                    .then(CommandUtils.bool("highlight", "mentions.mentionHighlight", () -> ModConfig.INSTANCE.mentions.mentionHighlight, (value) -> ModConfig.INSTANCE.mentions.mentionHighlight = value)
                            .then(CommandUtils.getterSetter("color", "color", "mentions.highlightColor", () -> ModConfig.INSTANCE.mentions.highlightColor, (value) -> ModConfig.INSTANCE.mentions.highlightColor = value, ModConfig.Color.ColorArgumentType.color(), ModConfig.Color.ColorArgumentType::getColor)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(mentionsNode));
    }

    private static void onAddAliasCommand(String name) {
        List<String> names = new ArrayList<>(ModConfig.INSTANCE.mentions.mentionsAliases);

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddFail", name);
            return;
        }

        names.add(name);
        ModConfig.INSTANCE.mentions.mentionsAliases = names;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddSuccess", name);
    }

    private static void onDelAliasCommand(int index) {
        List<String> names = new ArrayList<>(ModConfig.INSTANCE.mentions.mentionsAliases);

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex >= names.size()) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasInvalidIndex", index);
            return;
        }

        String name = names.remove(adjustedIndex);
        ModConfig.INSTANCE.mentions.mentionsAliases = names;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.mentions.aliasDelSuccess", name);
    }

    private static void onInsertAliasCommand(int index, String name) {
        List<String> names = new ArrayList<>(ModConfig.INSTANCE.mentions.mentionsAliases);

        int adjustedIndex = index - 1;
        if (adjustedIndex < 0 || adjustedIndex > names.size()) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasInvalidIndex", index);
            return;
        }

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddFail", name);
            return;
        }

        names.add(adjustedIndex, name);
        ModConfig.INSTANCE.mentions.mentionsAliases = names;
        ModConfig.HOLDER.save();
        Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddSuccess", name);
    }

    public static void processMessage(Text message) {
        if (!ModConfig.INSTANCE.mentions.enabled || !ModConfig.INSTANCE.mentions.playMentionSound || !isValidMessage(message)) {
            return;
        }

        if (mentioned(message)) {
            playSound();
        }
    }

    public static Text modifyMessage(Text message) {
        if (MC.player == null) {
            return message;
        }

        Text newMessage = message;

        Matcher playerMsgMatcher = RegexFilters.playerMsgFilter.matcher(message.getString());
        int prefixLen = ModConfig.INSTANCE.mentions.excludeSender && playerMsgMatcher.matches() ? playerMsgMatcher.group(1).length() : 0;

        if (ModConfig.INSTANCE.mentions.mentionsCurrentAccount) {
            newMessage = highlight(newMessage, MC.player.getGameProfile().getName(), prefixLen);
        }

        for (String alias : ModConfig.INSTANCE.mentions.mentionsAliases) {
            if (!alias.equals("")) {
                newMessage = highlight(newMessage, alias, prefixLen);
            }
        }

        return newMessage;
    }

    public static boolean mentioned(Text message) {
        if (MC.player == null) {
            return false;
        }

        String msgString = message.getString().toLowerCase(Locale.ROOT);

        if (ModConfig.INSTANCE.mentions.excludeSender) {
            Matcher matcher = RegexFilters.playerMsgFilter.matcher(msgString);
            if (matcher.matches()) {
                msgString = msgString.replace(matcher.group(1), "");
            }
        }

        if (ModConfig.INSTANCE.mentions.mentionsCurrentAccount && msgString.contains(MC.player.getGameProfile().getName().toLowerCase())) {
            return true;
        }

        for (String alias : ModConfig.INSTANCE.mentions.mentionsAliases) {
            if (!alias.equals("") && msgString.contains(alias.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidMessage(Text message) {
        if (ModConfig.INSTANCE.mentions.excludeServerMsgs &&
                !RegexFilters.playerMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.incomingMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
            return false;
        }

        if (ModConfig.INSTANCE.mentions.excludeSelfMsgs) {
            if (RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
                return false;
            }

            ClickEvent clickEvent = message.getStyle().getClickEvent();
            if (MC.player == null || clickEvent == null || clickEvent.getAction() != ClickEvent.Action.SUGGEST_COMMAND) {
                return true;
            }

            String sender = clickEvent.getValue().replace("/visit ", "");

            if (sender.equals(MC.player.getGameProfile().getName())) {
                return false;
            }
        }

        return true;
    }

    private static Text highlight(Text text, String target, int prefixLen) {
        int prefixRemaining = prefixLen;
        MutableText highlighted;
        if (prefixRemaining > 0) {
            highlighted = MutableText.of(text.getContent()).setStyle(text.getStyle());
            prefixRemaining -= highlighted.getString().length();
        } else {
            highlighted = highlight(text.getContent(), text.getStyle(), target);
        }

        for (Text sibling : text.getSiblings()) {
            if (prefixRemaining > 0) {
                highlighted.append(sibling);
                prefixRemaining -= sibling.getString().length();
                continue;
            }
            highlighted.append(highlight(sibling, target, 0));
        }
        return highlighted;
    }

    private static MutableText highlight(TextContent content, Style oldStyle, String target) {
        StringBuilder sb = new StringBuilder();
        content.visit(string -> {
            sb.append(string);
            return Optional.empty();
        });
        String stringText = sb.toString();
        String lowerText = stringText.toLowerCase();

        String lowerTarget = target.toLowerCase();
        if (!lowerText.contains(lowerTarget)) {
            return Text.literal(stringText).setStyle(oldStyle);
        }

        int index = 0;
        String format = "";
        MutableText result = Text.literal("");
        while (lowerText.indexOf(lowerTarget, index) != -1) {
            int beginningIndex = lowerText.indexOf(lowerTarget, index);
            int endIndex = beginningIndex + lowerTarget.length();
            String preText = format + stringText.substring(index, beginningIndex);
            result.append(Text.literal(preText).setStyle(oldStyle));
            result.append(Text.literal(stringText.substring(beginningIndex, endIndex)).setStyle(oldStyle.withColor(ModConfig.INSTANCE.mentions.highlightColor.getFormatting())));
            int formatSignIndex = preText.lastIndexOf("\u00a7");
            if (formatSignIndex != -1 && formatSignIndex + 2 <= preText.length()) {
                format = preText.substring(formatSignIndex, formatSignIndex + 2);
            }
            index = endIndex;
        }

        if (index < stringText.length()) {
            result.append(Text.literal(format + stringText.substring(index)).setStyle(oldStyle));
        }

        return result;
    }

    private static void playSound() {
        if (MC.player == null) {
            return;
        }

        MC.player.playSound(ModConfig.INSTANCE.mentions.mentionSound.getSound(), 1, 1);
    }
}
