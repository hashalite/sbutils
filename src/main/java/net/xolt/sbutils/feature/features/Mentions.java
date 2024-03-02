package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.argument.ColorArgumentType;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.RegexFilters;

import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class Mentions extends Feature {
    private final OptionBinding<Boolean> enabled = new OptionBinding<>("mentions.enabled", Boolean.class, (config) -> config.mentions.enabled, (config, value) -> config.mentions.enabled = value);
    private final OptionBinding<Boolean> playSound = new OptionBinding<>("mentions.playSound", Boolean.class, (config) -> config.mentions.playSound, (config, value) -> config.mentions.playSound = value);
    private final OptionBinding<ModConfig.NotifSound> sound = new OptionBinding<>("mentions.sound", ModConfig.NotifSound.class, (config) -> config.mentions.sound, (config, value) -> config.mentions.sound = value);
    private final OptionBinding<Boolean> highlight = new OptionBinding<>("mentions.highlight", Boolean.class, (config) -> config.mentions.highlight, (config, value) -> config.mentions.highlight = value);
    private final OptionBinding<Color> highlightColor = new OptionBinding<>("mentions.highlightColor", Color.class, (config) -> config.mentions.highlightColor, (config, value) -> config.mentions.highlightColor = value);
    private final OptionBinding<Boolean> excludeServerMsgs = new OptionBinding<>("mentions.excludeServerMsgs", Boolean.class, (config) -> config.mentions.excludeServerMsgs, (config, value) -> config.mentions.excludeServerMsgs = value);
    private final OptionBinding<Boolean> excludeSelfMsgs = new OptionBinding<>("mentions.excludeSelfMsgs", Boolean.class, (config) -> config.mentions.excludeSelfMsgs, (config, value) -> config.mentions.excludeSelfMsgs = value);
    private final OptionBinding<Boolean> excludeSender = new OptionBinding<>("mentions.excludeSender", Boolean.class, (config) -> config.mentions.excludeSender, (config, value) -> config.mentions.excludeSender = value);
    private final OptionBinding<Boolean> currentAccount = new OptionBinding<>("mentions.currentAccount", Boolean.class, (config) -> config.mentions.currentAccount, (config, value) -> config.mentions.currentAccount = value);
    private final ListOptionBinding<String> aliases = new ListOptionBinding<>("mentions.aliases", "", String.class, (config) -> config.mentions.aliases, (config, value) -> config.mentions.aliases = value);

    public Mentions() {
        super("mentions", "mentions", "ment");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, playSound, sound, highlight, highlightColor, excludeServerMsgs, excludeSelfMsgs, excludeSender, currentAccount, aliases);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> mentionsNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.bool("playSound", playSound))
                    .then(CommandHelper.bool("excludeServer", excludeServerMsgs))
                    .then(CommandHelper.bool("excludeSelf", excludeSelfMsgs))
                    .then(CommandHelper.bool("excludeSender", excludeSender))
                    .then(CommandHelper.bool("currentAccount", currentAccount))
                    .then(CommandHelper.stringList("aliases", "alias", aliases))
                    .then(CommandHelper.genericEnum("sound", "sound", sound))
                    .then(CommandHelper.bool("highlight", highlight)
                            .then(CommandHelper.getterSetter("color", "color", highlightColor, ColorArgumentType.color(), ColorArgumentType::getColor))));
        registerAlias(dispatcher, mentionsNode);
    }

    public static void processMessage(Component message) {
        if (!ModConfig.HANDLER.instance().mentions.enabled || !ModConfig.HANDLER.instance().mentions.playSound || !isValidMessage(message) || !mentioned(message))
            return;
        playSound();
    }

    public static Component modifyMessage(Component message) {
        if (MC.player == null)
            return message;

        Component newMessage = message;

        Matcher playerMsgMatcher = RegexFilters.playerMsgFilter.matcher(message.getString());
        int prefixLen = ModConfig.HANDLER.instance().mentions.excludeSender && playerMsgMatcher.matches() ? playerMsgMatcher.group(1).length() : 0;

        if (ModConfig.HANDLER.instance().mentions.currentAccount)
            newMessage = highlight(newMessage, MC.player.getGameProfile().getName(), prefixLen);

        for (String alias : ModConfig.HANDLER.instance().mentions.aliases) {
            if (!alias.equals(""))
                newMessage = highlight(newMessage, alias, prefixLen);
        }

        return newMessage;
    }

    public static boolean mentioned(Component message) {
        if (MC.player == null)
            return false;

        String msgString = message.getString().toLowerCase(Locale.ROOT);

        if (ModConfig.HANDLER.instance().mentions.excludeSender) {
            Matcher matcher = RegexFilters.playerMsgFilter.matcher(msgString);
            if (matcher.matches())
                msgString = msgString.replace(matcher.group(1), "");
        }

        if (ModConfig.HANDLER.instance().mentions.currentAccount && msgString.contains(MC.player.getGameProfile().getName().toLowerCase()))
            return true;

        for (String alias : ModConfig.HANDLER.instance().mentions.aliases)
            if (!alias.isEmpty() && msgString.contains(alias.toLowerCase()))
                return true;

        return false;
    }

    public static boolean isValidMessage(Component message) {
        if (ModConfig.HANDLER.instance().mentions.excludeServerMsgs &&
                !RegexFilters.playerMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.incomingMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
            return false;
        }

        if (ModConfig.HANDLER.instance().mentions.excludeSelfMsgs) {
            if (RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches())
                return false;

            ClickEvent clickEvent = message.getStyle().getClickEvent();
            if (MC.player == null || clickEvent == null || clickEvent.getAction() != ClickEvent.Action.SUGGEST_COMMAND)
                return true;

            String sender = clickEvent.getValue().replace("/visit ", "");

            if (sender.equals(MC.player.getGameProfile().getName()))
                return false;
        }

        return true;
    }

    private static Component highlight(Component text, String target, int prefixLen) {
        int prefixRemaining = prefixLen;
        MutableComponent highlighted;
        if (prefixRemaining > 0) {
            highlighted = MutableComponent.create(text.getContents()).setStyle(text.getStyle());
            prefixRemaining -= highlighted.getString().length();
        } else {
            highlighted = highlight(text.getContents(), text.getStyle(), target);
        }

        for (Component sibling : text.getSiblings()) {
            if (prefixRemaining > 0) {
                highlighted.append(sibling);
                prefixRemaining -= sibling.getString().length();
                continue;
            }
            highlighted.append(highlight(sibling, target, 0));
        }
        return highlighted;
    }

    private static MutableComponent highlight(ComponentContents content, Style oldStyle, String target) {
        StringBuilder sb = new StringBuilder();
        content.visit(string -> {
            sb.append(string);
            return Optional.empty();
        });
        String stringText = sb.toString();
        String lowerText = stringText.toLowerCase();

        String lowerTarget = target.toLowerCase();
        if (!lowerText.contains(lowerTarget)) {
            return Component.literal(stringText).setStyle(oldStyle);
        }

        int index = 0;
        String format = "";
        MutableComponent result = Component.literal("");
        while (lowerText.indexOf(lowerTarget, index) != -1) {
            int beginningIndex = lowerText.indexOf(lowerTarget, index);
            int endIndex = beginningIndex + lowerTarget.length();
            String preText = format + stringText.substring(index, beginningIndex);
            result.append(Component.literal(preText).setStyle(oldStyle));
            result.append(Component.literal(stringText.substring(beginningIndex, endIndex)).setStyle(oldStyle.withColor(ModConfig.HANDLER.instance().mentions.highlightColor.getRGB())));
            int formatSignIndex = preText.lastIndexOf("\u00a7");
            if (formatSignIndex != -1 && formatSignIndex + 2 <= preText.length())
                format = preText.substring(formatSignIndex, formatSignIndex + 2);
            index = endIndex;
        }

        if (index < stringText.length())
            result.append(Component.literal(format + stringText.substring(index)).setStyle(oldStyle));

        return result;
    }

    private static void playSound() {
        if (MC.player == null)
            return;

        MC.player.playSound(ModConfig.HANDLER.instance().mentions.sound.getSound(), 1, 1);
    }
}
