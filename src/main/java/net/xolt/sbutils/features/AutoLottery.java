package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoLottery {

    private static boolean enabled;
    private static boolean waitingToBuy;
    private static long checkedForGrassAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> autoLotteryNode = dispatcher.register(ClientCommandManager.literal("autolottery")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoLottery = !ModConfig.INSTANCE.getConfig().autoLottery;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.autolottery", ModConfig.INSTANCE.getConfig().autoLottery);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("tickets")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.lotteryTickets", ModConfig.INSTANCE.getConfig().lotteryTickets);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().lotteryTickets = IntegerArgumentType.getInteger(context, "amount");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.lotteryTickets", ModConfig.INSTANCE.getConfig().lotteryTickets);
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

        dispatcher.register(ClientCommandManager.literal("autolot")
                .executes(context ->
                        dispatcher.execute("autolottery", context.getSource())
                )
                .redirect(autoLotteryNode));
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.getConfig().autoLottery) {
            enabled = ModConfig.INSTANCE.getConfig().autoLottery;
            waitingToBuy = enabled;
        }

        if (!ModConfig.INSTANCE.getConfig().autoLottery || MC.getNetworkHandler() == null || MC.currentScreen instanceof ProgressScreen) {
            return;
        }

        if (waitingToBuy && System.currentTimeMillis() - checkedForGrassAt > ModConfig.INSTANCE.getConfig().grassCheckDelay * 1000.0) {
            buyTickets();
        }
    }

    public static void processMessage(Text message) {
        if (ModConfig.INSTANCE.getConfig().autoLottery && RegexFilters.lotteryEndFilter.matcher(message.getString()).matches()) {
            waitingToBuy = true;
        }
    }

    public static void onGameJoin() {
        if (ModConfig.INSTANCE.getConfig().autoLottery) {
            waitingToBuy = true;
        }
    }

    private static void buyTickets() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        int numTickets = Math.min(Math.max(ModConfig.INSTANCE.getConfig().lotteryTickets, 1), 2);
        if (getGrassCount() < numTickets) {
            waitingToBuy = true;
            checkedForGrassAt = System.currentTimeMillis();
            return;
        }

        MC.getNetworkHandler().sendChatCommand("lot buy " + numTickets);
        waitingToBuy = false;
        Messenger.printMessage("message.sbutils.autoLottery.buying");
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
}
