package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.NumberConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoRaffle extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "autoRaffle.enabled", Boolean.class, (config) -> config.autoRaffle.enabled, (config, value) -> config.autoRaffle.enabled = value);
    private final OptionBinding<ModConfig, Integer> sbTickets = new OptionBinding<>("sbutils", "autoRaffle.sbTickets", Integer.class, (config) -> config.autoRaffle.sbTickets, (config, value) -> config.autoRaffle.sbTickets = value, new NumberConstraints<>(1, 2));
    private final OptionBinding<ModConfig, Integer> ecoTickets = new OptionBinding<>("sbutils", "autoRaffle.ecoTickets", Integer.class, (config) -> config.autoRaffle.ecoTickets, (config, value) -> config.autoRaffle.ecoTickets = value, new NumberConstraints<>(1, 5));

    private boolean waitingToBuy;
    private boolean checkForGrass;
    private boolean shouldSendErrorMessage;

    public AutoRaffle() {
        super("sbutils", "autoRaffle", "autoraffle", "autoraf");
        enabled.addListener(this::onToggle);
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, sbTickets, ecoTickets);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoRaffleNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.integer("sbTickets", "amount", sbTickets, ModConfig.HANDLER))
                    .then(CommandHelper.integer("ecoTickets", "amount", ecoTickets, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, autoRaffleNode);
    }

    public void tick() {
        if (!ModConfig.instance().autoRaffle.enabled || MC.getConnection() == null)
            return;

        if (waitingToBuy && checkForGrass)
            buyTickets();
    }

    private void onToggle(Boolean oldValue, Boolean newValue) {
        reset();
    }

    public void onUpdateInventory() {
        if (ModConfig.instance().autoRaffle.enabled && waitingToBuy)
            checkForGrass = true;
    }

    public void processMessage(Component message) {
        if (ModConfig.instance().autoRaffle.enabled && RegexFilters.raffleEndFilter.matcher(message.getString()).matches())
            reset();
    }

    public void onJoinGame() {
        if (ModConfig.instance().autoRaffle.enabled)
            reset();
    }

    public void buyTickets() {
        if (!SbUtils.SERVER_DETECTOR.isOnSkyblock())
            return;

        switch (SbUtils.SERVER_DETECTOR.getCurrentServer()) {
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

    private void buySkyblockTickets() {
        if (MC.getConnection() == null)
            return;

        int numTickets = Math.min(Math.max(ModConfig.instance().autoRaffle.sbTickets, 1), 2);
        int grassCount = getGrassCount();
        if (grassCount < 1) {
            waitingToBuy = true;
            checkForGrass = false;
            if (shouldSendErrorMessage) {
                ChatUtils.printMessage("message.sbutils.autoRaffle.notEnoughGrass");
                shouldSendErrorMessage = false;
            }
            return;
        }

        int buyAmount = Math.min(numTickets, grassCount);
        ChatUtils.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
        SbUtils.COMMAND_SENDER.sendCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
    }

    private void buyEconomyTickets() {
        if (MC.getConnection() == null)
            return;

        int buyAmount = Math.min(Math.max(ModConfig.instance().autoRaffle.ecoTickets, 1), 5);
        ChatUtils.printWithPlaceholders("message.sbutils.autoRaffle.buying", buyAmount);
        SbUtils.COMMAND_SENDER.sendCommand("raffle buy " + buyAmount);
        waitingToBuy = false;
    }

    private void reset() {
        waitingToBuy = true;
        shouldSendErrorMessage = true;
        checkForGrass = true;
    }

    private static int getGrassCount() {
        if (MC.player == null)
            return -1;

        int counter = 0;
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (!(itemStack.getItem().equals(Items.GRASS_BLOCK)))
                continue;
            counter += itemStack.getCount();
        }
        return counter;
    }
}
