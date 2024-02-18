package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;

public class AutoCommandEntryArgumentType implements ArgumentType<ModConfig.AutoCommandConfig.AutoCommandEntry> {

    public static AutoCommandEntryArgumentType commandEntry() {
        return new AutoCommandEntryArgumentType();
    }

    public static ModConfig.AutoCommandConfig.AutoCommandEntry getCommandEntry(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.AutoCommandConfig.AutoCommandEntry.class);
    }

    @Override public ModConfig.AutoCommandConfig.AutoCommandEntry parse(StringReader stringReader) {
        String command = stringReader.getRemaining();
        stringReader.setCursor(stringReader.getTotalLength());
        return new ModConfig.AutoCommandConfig.AutoCommandEntry(command, 5.0, false);
    }
}
