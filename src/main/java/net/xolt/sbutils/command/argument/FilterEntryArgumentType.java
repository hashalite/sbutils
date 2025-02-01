package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.xolt.sbutils.config.ModConfig;

public class FilterEntryArgumentType implements ArgumentType<ModConfig.ChatFiltersConfig.CustomFilter> {

    public static FilterEntryArgumentType filterEntry() {
        return new FilterEntryArgumentType();
    }

    public static ModConfig.ChatFiltersConfig.CustomFilter getFilterEntry(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.ChatFiltersConfig.CustomFilter.class);
    }

    @Override public ModConfig.ChatFiltersConfig.CustomFilter parse(StringReader stringReader) throws CommandSyntaxException {
        String type = stringReader.readStringUntil(':');
        ModConfig.FilterTarget target = ModConfig.FilterTarget.CHAT;
        if (type.equals("title"))
            target = ModConfig.FilterTarget.TITLE;
        String regex = stringReader.getRemaining();
        stringReader.setCursor(stringReader.getTotalLength());
        return new ModConfig.ChatFiltersConfig.CustomFilter(regex, target, false);
    }
}
