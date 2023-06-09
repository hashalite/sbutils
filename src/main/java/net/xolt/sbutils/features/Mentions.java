package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.xolt.sbutils.SbUtils.MC;

public class Mentions {

    private static final String COMMAND = "mentions";
    private static final String ALIAS = "ment";

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> mentionsNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().mentions = !ModConfig.INSTANCE.getConfig().mentions;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.mentions", ModConfig.INSTANCE.getConfig().mentions);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("excludeServer")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.excludeServerMsgs", ModConfig.INSTANCE.getConfig().excludeServerMsgs);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeServerMsgs = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeServerMsgs", ModConfig.INSTANCE.getConfig().excludeServerMsgs);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("excludeSelf")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.excludeSelfMsgs", ModConfig.INSTANCE.getConfig().excludeSelfMsgs);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().excludeSelfMsgs = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.excludeSelfMsgs", ModConfig.INSTANCE.getConfig().excludeSelfMsgs);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("currentAccount")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.mentionsCurrentAccount", ModConfig.INSTANCE.getConfig().mentionsCurrentAccount);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().mentionsCurrentAccount = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.mentionsCurrentAccount", ModConfig.INSTANCE.getConfig().mentionsCurrentAccount);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("aliases")
                        .executes(context -> {
                            Messenger.printListSetting("message.sbutils.mentions.aliases", ModConfig.INSTANCE.getConfig().mentionsAliases);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("alias", StringArgumentType.greedyString())
                                        .executes(context ->
                                            onAddAliasCommand(StringArgumentType.getString(context, "alias"))
                                        )))
                        .then(ClientCommandManager.literal("del")
                                .then(ClientCommandManager.argument("alias", StringArgumentType.greedyString())
                                        .executes(context ->
                                            onDelAliasCommand(StringArgumentType.getString(context, "alias"))
                                        ))))
                .then(ClientCommandManager.literal("sound")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.mentionSound", ModConfig.INSTANCE.getConfig().mentionSound);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("sound", ModConfig.NotifSound.NotifSoundArgumentType.notifSound())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().mentionSound = ModConfig.NotifSound.NotifSoundArgumentType.getNotifSound(context, "sound");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.mentionSound", ModConfig.INSTANCE.getConfig().mentionSound);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("highlight")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.mentionHighlight", ModConfig.INSTANCE.getConfig().mentionHighlight);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().mentionHighlight = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.mentionHighlight", ModConfig.INSTANCE.getConfig().mentionHighlight);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("color")
                                .executes(context -> {
                                    Messenger.printSetting("text.sbutils.config.option.highlightColor", ModConfig.INSTANCE.getConfig().highlightColor);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(ClientCommandManager.argument("color", ModConfig.Color.ColorArgumentType.color())
                                        .executes(context -> {
                                            ModConfig.INSTANCE.getConfig().highlightColor = ModConfig.Color.ColorArgumentType.getColor(context, "color");
                                            ModConfig.INSTANCE.save();
                                            Messenger.printChangedSetting("text.sbutils.config.option.highlightColor", ModConfig.INSTANCE.getConfig().highlightColor);
                                            return Command.SINGLE_SUCCESS;
                                        })))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(mentionsNode));
    }

    private static int onAddAliasCommand(String name) {
        List<String> names = new ArrayList<>(ModConfig.INSTANCE.getConfig().mentionsAliases);

        if (names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddFail", name);
            return Command.SINGLE_SUCCESS;
        }

        names.add(name);
        ModConfig.INSTANCE.getConfig().mentionsAliases = names;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.mentions.aliasAddSuccess", name);
        return Command.SINGLE_SUCCESS;
    }

    private static int onDelAliasCommand(String name) {
        List<String> names = new ArrayList<>(ModConfig.INSTANCE.getConfig().mentionsAliases);

        if (!names.contains(name)) {
            Messenger.printWithPlaceholders("message.sbutils.mentions.aliasDelFail", name);
            return Command.SINGLE_SUCCESS;
        }

        names.remove(name);
        ModConfig.INSTANCE.getConfig().mentionsAliases = names;
        ModConfig.INSTANCE.save();
        Messenger.printWithPlaceholders("message.sbutils.mentions.aliasDelSuccess", name);
        return Command.SINGLE_SUCCESS;
    }

    public static void processMessage(Text message) {
        if (!ModConfig.INSTANCE.getConfig().mentions || !isValidMessage(message)) {
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

        if (ModConfig.INSTANCE.getConfig().mentionsCurrentAccount) {
            newMessage = highlight(newMessage, MC.player.getGameProfile().getName());
        }

        for (String alias : ModConfig.INSTANCE.getConfig().mentionsAliases) {
            if (!alias.equals("")) {
                newMessage = highlight(newMessage, alias);
            }
        }

        return newMessage;
    }

    public static boolean mentioned(Text message) {
        if (MC.player == null) {
            return false;
        }

        String msgString = message.getString().toLowerCase(Locale.ROOT);

        if (ModConfig.INSTANCE.getConfig().mentionsCurrentAccount && msgString.contains(MC.player.getGameProfile().getName().toLowerCase())) {
            return true;
        }

        for (String alias : ModConfig.INSTANCE.getConfig().mentionsAliases) {
            if (!alias.equals("") && msgString.contains(alias.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidMessage(Text message) {
        if (ModConfig.INSTANCE.getConfig().excludeServerMsgs &&
                !RegexFilters.playerMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.incomingMsgFilter.matcher(message.getString()).matches() &&
                !RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
            return false;
        }

        if (ModConfig.INSTANCE.getConfig().excludeSelfMsgs) {
            if (RegexFilters.outgoingMsgFilter.matcher(message.getString()).matches()) {
                return false;
            }

            ClickEvent clickEvent = message.getStyle().getClickEvent();
            if (MC.player == null || clickEvent == null || !clickEvent.getAction().getName().equals("suggest_command")) {
                return true;
            }

            String sender = clickEvent.getValue().replace("/visit ", "");

            if (sender.equals(MC.player.getGameProfile().getName())) {
                return false;
            }
        }

        return true;
    }

    private static Text highlight(Text text, String target) {
        if (text.getSiblings().size() == 0) {
            return highlight(text.getContent(), text.getStyle(), target);
        } else {
            MutableText highlighted = highlight(text.getContent(), text.getStyle(), target);
            for (Text sibling : text.getSiblings()) {
                highlighted.append(highlight(sibling, target));
            }
            return highlighted;
        }
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
            result.append(Text.literal(stringText.substring(beginningIndex, endIndex)).setStyle(oldStyle.withColor(ModConfig.INSTANCE.getConfig().highlightColor.getFormatting())));
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

        MC.player.playSound(ModConfig.INSTANCE.getConfig().mentionSound.getSound(), 1, 1);
    }
}
