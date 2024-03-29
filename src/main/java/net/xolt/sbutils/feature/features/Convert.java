package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Convert extends Feature {

    private static final Map<String, Integer> units = new LinkedHashMap<>();

    static {
        units.put("dc", 3456);
        units.put("sc", 1728);
        units.put("s", 64);
    }

    public Convert() {
        super("convert", "convert", "cv");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
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
        ChatUtils.printConversions(input, calculateItems(parsed), calculateStacks(parsed), calculateDcs(parsed), calculateStacksAndRemainder(parsed), calculateDcsAndRemainder(parsed));
        return Command.SINGLE_SUCCESS;
    }

    private static String calculateItems(Double input) {
        return formatDouble(Math.round(input * 10.0) / 10.0);
    }

    private static String calculateStacks(Double input) {
        return formatDouble(Math.round((input / 64.0) * 100.0) / 100.0) + "s";
    }

    private static String calculateDcs(Double input) {
        return formatDouble(Math.round((input / 3456.0) * 1000.0) / 1000.0) + "dc";
    }

    private static String calculateStacksAndRemainder(Double input) {
        int stacks = (int)(input / 64);
        double items = input % 64.0;
        return stacks + "s + " + formatDouble(Math.round(items * 10.0) / 10.0);
    }

    private static String calculateDcsAndRemainder(Double input) {
        double dcs = (int)(input / 864) / 4.0;
        int stacks = (int)(input % 864) / 64;
        double items = (input % 864.0) % 64.0;
        return formatDouble(dcs) + "dc + " + stacks + "s + " + formatDouble(Math.round(items * 10.0) / 10.0);
    }

    private static String formatDouble(double input) {
        if (input == (long)input) {
            return String.format("%d", (long)input);
        } else {
            return String.format("%s", input);
        }
    }

    private static Double parseInput(String input) {
        for (String unit : units.keySet())
            input = input.replaceAll("([0-9]+(.[0-9]+)?) *" + unit, "($1*" + units.get(unit) + ")");

        return new ExpressionBuilder(input)
                .build()
                .evaluate();
    }
}
