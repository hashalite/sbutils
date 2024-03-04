package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.xolt.sbutils.config.ModConfig;

public class FilterEntryArgumentType implements ArgumentType<ModConfig.ChatFiltersConfig.FilterEntry> {

    public static FilterEntryArgumentType filterEntry() {
        return new FilterEntryArgumentType();
    }

    public static ModConfig.ChatFiltersConfig.FilterEntry getFilterEntry(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.ChatFiltersConfig.FilterEntry.class);
    }

    @Override public ModConfig.ChatFiltersConfig.FilterEntry parse(StringReader stringReader) {
        String regex = stringReader.getRemaining();
        stringReader.setCursor(stringReader.getTotalLength());
        return new ModConfig.ChatFiltersConfig.FilterEntry(regex, false);
    }
}
