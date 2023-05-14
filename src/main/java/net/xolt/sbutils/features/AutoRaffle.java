package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoRaffle {

    private static final String COMMAND = "autoraffle";
    private static final String ALIAS = "autoraf";

    private static boolean enabled;
    private static boolean waitingToBuy;
    private static boolean shouldSendErrorMessage;
    private static long checkedForGrassAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoRaffleNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoRaffle = !ModConfig.INSTANCE.getConfig().autoRaffle;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autoraffle", ModConfig.INSTANCE.getConfig().autoRaffle);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("sbTickets")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.skyblockRaffleTickets", ModConfig.INSTANCE.getConfig().skyblockRaffleTickets);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().skyblockRaffleTickets = Math.min(Math.max(IntegerArgumentType.getInteger(context, "amount"), 1), 2);
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.skyblockRaffleTickets", ModConfig.INSTANCE.getConfig().skyblockRaffleTickets);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("ecoTickets")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.economyRaffleTickets", ModConfig.INSTANCE.getConfig().economyRaffleTickets);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().economyRaffleTickets = Math.min(Math.max(IntegerArgumentType.getInteger(context, "amount"), 1), 5);
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.economyRaffleTickets", ModConfig.INSTANCE.getConfig().economyRaffleTickets);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("checkDelay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.grassCheckDelay", ModConfig.INSTANCE.getConfig().grassCheckDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().grassCheckDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.grassCheckDelay", ModConfig.INSTANCE.getConfig().grassCheckDelay);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("autoraffle", context.getSource())
                )
                .redirect(autoRaffleNode));
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.getConfig().autoRaffle) {
            enabled = ModConfig.INSTANCE.getConfig().autoRaffle;
            reset();
        }

        if (!ModConfig.INSTANCE.getConfig().autoRaffle || MC.getNetworkHandler() == null) {
            return;
        }

        if (waitingToBuy && System.currentTimeMillis() - checkedForGrassAt > ModConfig.INSTANCE.getConfig().grassCheckDelay * 1000.0) {
            buyTickets();
        }
    }

    public static void processMessage(Text message) {
        if (ModConfig.INSTANCE.getConfig().autoRaffle && RegexFilters.raffleEndFilter.matcher(message.getString()).matches()) {
            reset();
        }
    }

    public static void onJoinGame() {
        if (ModConfig.INSTANCE.getConfig().autoRaffle) {
            reset();
        }
    }

    public static void buyTickets() {
        if (ServerDetector.currentServer == null) {
            return;
        } else {
            switch (ServerDetector.currentServer) {
                case ECONOMY:
                    buyEconomyTickets();
                    break;
                case CLASSIC:
                    break;
                default:
                    buySkyblockTickets();
                    break;
            }
        }
    }

    private static void buySkyblockTickets() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        int numTickets = Math.min(Math.max(ModConfig.INSTANCE.getConfig().skyblockRaffleTickets, 1), 2);
        int grassCount = getGrassCount();
        if (grassCount < 1) {
            waitingToBuy = true;
            checkedForGrassAt = System.currentTimeMillis();
            if (shouldSendErrorMessage) {
                Messenger.printMessage("message.sbutils.autoRaffle.notEnoughGrass");
                shouldSendErrorMessage = false;
            }
            return;
        }

        int buyAmount = Math.min(numTickets, grassCount);
        MC.getNetworkHandler().sendChatCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
        Messenger.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
    }

    private static void buyEconomyTickets() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        int buyAmount = Math.min(Math.max(ModConfig.INSTANCE.getConfig().economyRaffleTickets, 1), 5);
        MC.getNetworkHandler().sendChatCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
        Messenger.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
    }

    private static int getGrassCount() {
        if (MC.player == null) {
            return -1;
        }

        int counter = 0;
        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack itemStack = MC.player.getInventory().getStack(i);
            if (!(itemStack.getItem().equals(Items.GRASS_BLOCK))) {
                continue;
            }

            counter += itemStack.getCount();
        }
        return counter;
    }

    private static void reset() {
        waitingToBuy = true;
        shouldSendErrorMessage = true;
    }
}
