package net.xolt.sbutils.util.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.config.ModConfig;

public class ColorArgumentType extends EnumArgumentType<ModConfig.Color> {
    private ColorArgumentType() {
        super(StringIdentifiable.createCodec(ModConfig.Color::values), ModConfig.Color::values);
    }

    public static ColorArgumentType color() {
        return new ColorArgumentType();
    }

    public static ModConfig.Color getColor(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.Color.class);
    }
}
