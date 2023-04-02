package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.util.Messenger;

import java.util.LinkedHashMap;
import java.util.Map;

public class Convert {
    private static final Map<String, Integer> units = new LinkedHashMap<>();

    static {
        units.put("dc", 3456);
        units.put("sc", 1728);
        units.put("s", 64);
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> convertNode = dispatcher.register(ClientCommandManager.literal("convert")
                .then(ClientCommandManager.argument("input", StringArgumentType.greedyString())
                        .executes(toStacks ->
                                processCommand(StringArgumentType.getString(toStacks, "input"))
                        )));

        dispatcher.register(ClientCommandManager.literal("cv")
                .executes(context ->
                        dispatcher.execute("convert", context.getSource())
                )
                .redirect(convertNode));
    }

    public static int processCommand(String input) {
        Double parsed = parseInput(input);
        if (parsed == null) {
            return Command.SINGLE_SUCCESS;
        }

        Messenger.printConversions(input, calculateItems(parsed), calculateStacks(parsed), calculateDcs(parsed), calculateStacksAndRemainder(parsed), calculateDcsAndRemainder(parsed));

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
        String lowercase = input.toLowerCase();
        String noSpaces = lowercase.replaceAll(" ", "");
        String[] split = noSpaces.split("[,+]");
        double total = 0.0;
        for (String operand : split) {
            for (String unit : units.keySet()) {
                operand = operand.replaceAll(unit, "*" + units.get(unit));
            }
            String[] factors = operand.split("\\*");
            double product = 1.0;
            for (String factor : factors) {
                try {
                    product *= Double.parseDouble(factor);
                } catch (NumberFormatException e) {
                    Messenger.printMessage("message.sbutils.convert.invalidCharacter", Formatting.RED);
                    return null;
                }
            }
            total += product;
        }
        return total;
    }
}