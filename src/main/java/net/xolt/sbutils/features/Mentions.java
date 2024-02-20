package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.argument.ColorArgumentType;
import net.xolt.sbutils.command.CommandHelper;
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
                CommandHelper.toggle(COMMAND, "mentions", () -> ModConfig.HANDLER.instance().mentions.enabled, (value) -> ModConfig.HANDLER.instance().mentions.enabled = value)
                    .then(CommandHelper.bool("playSound", "mentions.playSound", () -> ModConfig.HANDLER.instance().mentions.playSound, (value) -> ModConfig.HANDLER.instance().mentions.playSound = value))
                    .then(CommandHelper.bool("excludeServer", "mentions.excludeServerMsgs", () -> ModConfig.HANDLER.instance().mentions.excludeServerMsgs, (value) -> ModConfig.HANDLER.instance().mentions.excludeServerMsgs = value))
                    .then(CommandHelper.bool("excludeSelf", "mentions.excludeSelfMsgs", () -> ModConfig.HANDLER.instance().mentions.excludeSelfMsgs, (value) -> ModConfig.HANDLER.instance().mentions.excludeSelfMsgs = value))
                    .then(CommandHelper.bool("excludeSender", "mentions.excludeSender", () -> ModConfig.HANDLER.instance().mentions.excludeSender, (value) -> ModConfig.HANDLER.instance().mentions.excludeSender = value))
                    .then(CommandHelper.bool("currentAccount", "mentions.currentAccount", () -> ModConfig.HANDLER.instance().mentions.currentAccount, (value) -> ModConfig.HANDLER.instance().mentions.currentAccount = value))
                    .then(CommandHelper.stringList("aliases", "alias", "mentions.aliases", () -> ModConfig.HANDLER.instance().mentions.aliases, (value) -> ModConfig.HANDLER.instance().mentions.aliases = value))
                    .then(CommandHelper.genericEnum("sound", "sound", "mentions.sound", ModConfig.NotifSound.class, () -> ModConfig.HANDLER.instance().mentions.sound, (value) -> ModConfig.HANDLER.instance().mentions.sound = value))
                    .then(CommandHelper.bool("highlight", "mentions.highlight", () -> ModConfig.HANDLER.instance().mentions.highlight, (value) -> ModConfig.HANDLER.instance().mentions.highlight = value)
                            .then(CommandHelper.getterSetter("color", "color", "mentions.highlightColor", () -> ModConfig.HANDLER.instance().mentions.highlightColor, (value) -> ModConfig.HANDLER.instance().mentions.highlightColor = value, ColorArgumentType.color(), ColorArgumentType::getColor))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(mentionsNode));
    }

    public static void processMessage(Text message) {
        if (!ModConfig.HANDLER.instance().mentions.enabled || !ModConfig.HANDLER.instance().mentions.playSound || !isValidMessage(message)) {
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
        int prefixLen = ModConfig.HANDLER.instance().mentions.excludeSender && playerMsgMatcher.matches() ? playerMsgMatcher.group(1).length() : 0;

        if (ModConfig.HANDLER.instance().mentions.currentAccount) {
            newMessage = highlight(newMessage, MC.player.getGameProfile().getName(), prefixLen);
        }

        for (String alias : ModConfig.HANDLER.instance().mentions.aliases) {
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

        if (ModConfig.HANDLER.instance().mentions.excludeSender) {
            Matcher matcher = RegexFilters.playerMsgFilter.matcher(msgString);
            if (matcher.matches()) {
                msgString = msgString.replace(matcher.group(1), "");
            }
        }

        if (ModConfig.HANDLER.instance().mentions.currentAccount && msgString.contains(MC.player.getGameProfile().getName().toLowerCase())) {
            return true;
        }

        for (String alias : ModConfig.HANDLER.instance().mentions.aliases) {
            if (!alias.equals("") && msgString.contains(alias.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidMessage(Text message) {
        if (ModConfig.HANDLER.instance().mentions.excludeServerMsgs &&
                !RegexFilters.playerMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.incomingMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
            return false;
        }

        if (ModConfig.HANDLER.instance().mentions.excludeSelfMsgs) {
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
            result.append(Text.literal(stringText.substring(beginningIndex, endIndex)).setStyle(oldStyle.withColor(ModConfig.HANDLER.instance().mentions.highlightColor.getRGB())));
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

        MC.player.playSound(ModConfig.HANDLER.instance().mentions.sound.getSound(), 1, 1);
    }
}
