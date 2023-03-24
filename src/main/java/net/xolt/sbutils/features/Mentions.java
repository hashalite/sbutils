package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.xolt.sbutils.SbUtils.MC;

public class Mentions {

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> mentionsNode = dispatcher.register(ClientCommandManager.literal("mentions")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().mentions = !ModConfig.INSTANCE.getConfig().mentions;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.mentions", ModConfig.INSTANCE.getConfig().mentions);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("currentAccount")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.mentionsCurrentAccount", ModConfig.INSTANCE.getConfig().mentionsCurrentAccount);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().mentionsCurrentAccount = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.mentionsCurrentAccount", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().mentionsCurrentAccount = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.mentionsCurrentAccount", false);
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
                                        )))));

        dispatcher.register(ClientCommandManager.literal("ment")
                .executes(context ->
                        dispatcher.execute("mentions", context.getSource())
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
        if (!ModConfig.INSTANCE.getConfig().mentions || MC.player == null) {
            return;
        }

        String msgString = message.getString().toLowerCase(Locale.ROOT);

        if (ModConfig.INSTANCE.getConfig().mentionsCurrentAccount && msgString.contains(MC.player.getGameProfile().getName().toLowerCase())) {
            playSound();
            return;
        }

        for (String alias : ModConfig.INSTANCE.getConfig().mentionsAliases) {
            if (!alias.equals("") && msgString.contains(alias.toLowerCase())) {
                playSound();
                return;
            }
        }
    }

    private static void playSound() {
        if (MC.player == null) {
            return;
        }

        MC.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
