package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.util.CommandUtils;
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
        final LiteralCommandNode<FabricClientCommandSource> autoRaffleNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "autoRaffle", () -> ModConfig.INSTANCE.autoRaffle.autoRaffle, (value) -> ModConfig.INSTANCE.autoRaffle.autoRaffle = value)
                    .then(CommandUtils.integer("sbTickets", "amount", "autoRaffle.skyblockRaffleTickets", () -> ModConfig.INSTANCE.autoRaffle.skyblockRaffleTickets, (value) -> ModConfig.INSTANCE.autoRaffle.skyblockRaffleTickets = value))
                    .then(CommandUtils.integer("ecoTickets", "amount", "autoRaffle.economyRaffleTickets", () -> ModConfig.INSTANCE.autoRaffle.economyRaffleTickets, (value) -> ModConfig.INSTANCE.autoRaffle.economyRaffleTickets = value))
                    .then(CommandUtils.doubl("checkDelay", "seconds", "autoRaffle.grassCheckDelay", () -> ModConfig.INSTANCE.autoRaffle.grassCheckDelay, (value) -> ModConfig.INSTANCE.autoRaffle.grassCheckDelay = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoRaffleNode));
    }

    public static void tick() {
        if (enabled != ModConfig.INSTANCE.autoRaffle.autoRaffle) {
            enabled = ModConfig.INSTANCE.autoRaffle.autoRaffle;
            reset();
        }

        if (!ModConfig.INSTANCE.autoRaffle.autoRaffle || MC.getNetworkHandler() == null) {
            return;
        }

        if (waitingToBuy && System.currentTimeMillis() - checkedForGrassAt > ModConfig.INSTANCE.autoRaffle.grassCheckDelay * 1000.0) {
            buyTickets();
        }
    }

    public static void processMessage(Text message) {
        if (ModConfig.INSTANCE.autoRaffle.autoRaffle && RegexFilters.raffleEndFilter.matcher(message.getString()).matches()) {
            reset();
        }
    }

    public static void onJoinGame() {
        if (ModConfig.INSTANCE.autoRaffle.autoRaffle) {
            reset();
        }
    }

    public static void buyTickets() {
        if (!ServerDetector.isOnSkyblock()) {
            return;
        } else {
            switch (ServerDetector.currentServer) {
                case SKYBLOCK:
                    buySkyblockTickets();
                    break;
                case ECONOMY:
                    buyEconomyTickets();
                    break;
                default:
                    break;
            }
        }
    }

    private static void buySkyblockTickets() {
        if (MC.getNetworkHandler() == null) {
            return;
        }

        int numTickets = Math.min(Math.max(ModConfig.INSTANCE.autoRaffle.skyblockRaffleTickets, 1), 2);
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

        int buyAmount = Math.min(Math.max(ModConfig.INSTANCE.autoRaffle.economyRaffleTickets, 1), 5);
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
