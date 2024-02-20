package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.xolt.sbutils.config.ModConfig;

public class JoinCommandsEntryArgumentType implements ArgumentType<ModConfig.JoinCommandsConfig.JoinCommandsEntry> {

    public static JoinCommandsEntryArgumentType commandEntry() {
        return new JoinCommandsEntryArgumentType();
    }

    public static ModConfig.JoinCommandsConfig.JoinCommandsEntry getCommandEntry(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.JoinCommandsConfig.JoinCommandsEntry.class);
    }

    @Override public ModConfig.JoinCommandsConfig.JoinCommandsEntry parse(StringReader stringReader) {
        String command = stringReader.getRemaining();
        stringReader.setCursor(stringReader.getTotalLength());
        return new ModConfig.JoinCommandsConfig.JoinCommandsEntry(command, "");
    }
}
