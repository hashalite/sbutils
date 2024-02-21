package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.ServerDetector;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoRaffle {

    private static final String COMMAND = "autoraffle";
    private static final String ALIAS = "autoraf";

    private static boolean enabled;
    private static boolean waitingToBuy;
    private static boolean checkForGrass;
    private static boolean shouldSendErrorMessage;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoRaffleNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoRaffle", () -> ModConfig.HANDLER.instance().autoRaffle.enabled, (value) -> ModConfig.HANDLER.instance().autoRaffle.enabled = value)
                    .then(CommandHelper.integer("sbTickets", "amount", "autoRaffle.sbTickets", () -> ModConfig.HANDLER.instance().autoRaffle.sbTickets, (value) -> ModConfig.HANDLER.instance().autoRaffle.sbTickets = value))
                    .then(CommandHelper.integer("ecoTickets", "amount", "autoRaffle.ecoTickets", () -> ModConfig.HANDLER.instance().autoRaffle.ecoTickets, (value) -> ModConfig.HANDLER.instance().autoRaffle.ecoTickets = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoRaffleNode));
    }

    public static void tick() {
        if (enabled != ModConfig.HANDLER.instance().autoRaffle.enabled) {
            enabled = ModConfig.HANDLER.instance().autoRaffle.enabled;
            reset();
        }

        if (!enabled || MC.getConnection() == null) {
            return;
        }

        if (waitingToBuy && checkForGrass) {
            buyTickets();
        }
    }

    public static void onUpdateInventory() {
        if (enabled && waitingToBuy) {
            checkForGrass = true;
        }
    }

    public static void processMessage(Component message) {
        if (ModConfig.HANDLER.instance().autoRaffle.enabled && RegexFilters.raffleEndFilter.matcher(message.getString()).matches()) {
            reset();
        }
    }

    public static void onJoinGame() {
        if (ModConfig.HANDLER.instance().autoRaffle.enabled) {
            reset();
        }
    }

    public static void buyTickets() {
        if (!ServerDetector.isOnSkyblock())
            return;

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

    private static void buySkyblockTickets() {
        if (MC.getConnection() == null) {
            return;
        }

        int numTickets = Math.min(Math.max(ModConfig.HANDLER.instance().autoRaffle.sbTickets, 1), 2);
        int grassCount = getGrassCount();
        if (grassCount < 1) {
            waitingToBuy = true;
            checkForGrass = false;
            if (shouldSendErrorMessage) {
                Messenger.printMessage("message.sbutils.autoRaffle.notEnoughGrass");
                shouldSendErrorMessage = false;
            }
            return;
        }

        int buyAmount = Math.min(numTickets, grassCount);
        Messenger.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
        MC.getConnection().sendCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
    }

    private static void buyEconomyTickets() {
        if (MC.getConnection() == null) {
            return;
        }

        int buyAmount = Math.min(Math.max(ModConfig.HANDLER.instance().autoRaffle.ecoTickets, 1), 5);
        Messenger.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
        MC.getConnection().sendCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
    }

    private static int getGrassCount() {
        if (MC.player == null) {
            return -1;
        }

        int counter = 0;
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = MC.player.getInventory().getItem(i);
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
        checkForGrass = true;
    }
}
