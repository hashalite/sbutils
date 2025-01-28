package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.TextUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Convert extends Feature<ModConfig> {

    private static final Map<String, Integer> units = new LinkedHashMap<>();

    static {
        units.put("dc", 3456);
        units.put("sc", 1728);
        units.put("s", 64);
    }

    public Convert() {
        super("sbutils", "convert", "convert", "cv");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> convertNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(ClientCommandManager.argument("input", StringArgumentType.greedyString())
                        .executes(toStacks ->
                                processCommand(StringArgumentType.getString(toStacks, "input"))
                        )));
        registerAlias(dispatcher, convertNode);
    }

    public static int processCommand(String input) {
        Double parsed = parseInput(input);
        printConversions(input, calculateItems(parsed), calculateStacks(parsed), calculateDcs(parsed), calculateStacksAndRemainder(parsed), calculateDcsAndRemainder(parsed));
        return Command.SINGLE_SUCCESS;
    }

    private static String calculateItems(Double input) {
        return TextUtils.formatDouble(Math.round(input * 10.0) / 10.0);
    }

    private static String calculateStacks(Double input) {
        return TextUtils.formatDouble(Math.round((input / 64.0) * 100.0) / 100.0) + "s";
    }

    private static String calculateDcs(Double input) {
        return TextUtils.formatDouble(Math.round((input / 3456.0) * 1000.0) / 1000.0) + "dc";
    }

    private static String calculateStacksAndRemainder(Double input) {
        int stacks = (int)(input / 64);
        double items = input % 64.0;
        return stacks + "s + " + TextUtils.formatDouble(Math.round(items * 10.0) / 10.0);
    }

    private static String calculateDcsAndRemainder(Double input) {
        double dcs = (int)(input / 864) / 4.0;
        int stacks = (int)(input % 864) / 64;
        double items = (input % 864.0) % 64.0;
        return TextUtils.formatDouble(dcs) + "dc + " + stacks + "s + " + TextUtils.formatDouble(Math.round(items * 10.0) / 10.0);
    }

    private static Double parseInput(String input) {
        for (String unit : units.keySet())
            input = input.replaceAll("([0-9]+(.[0-9]+)?) *" + unit, "($1*" + units.get(unit) + ")");

        return new ExpressionBuilder(input)
                .build()
                .evaluate();
    }

    private static void printConversions(String input, String items, String stacks, String dcs, String stacksAndRemainer, String dcsAndRemainder) {
        MutableComponent message = Component.translatable("message.sbutils.convert.header");
        ChatUtils.printWithPlaceholders(message, Component.literal(input));

        MutableComponent itemsText = Component.literal("- " + items);
        ChatUtils.printMessage(itemsText, false);
        MutableComponent stacksText = Component.literal("- " + stacks);
        ChatUtils.printMessage(stacksText, false);
        MutableComponent dcsText = Component.literal("- " + dcs);
        ChatUtils.printMessage(dcsText, false);
        MutableComponent stacksAndRemainderText = Component.literal("- " + stacksAndRemainer);
        ChatUtils.printMessage(stacksAndRemainderText, false);
        MutableComponent dcsAndRemainderText = Component.literal("- " + dcsAndRemainder);
        ChatUtils.printMessage(dcsAndRemainderText, false);
    }
}
