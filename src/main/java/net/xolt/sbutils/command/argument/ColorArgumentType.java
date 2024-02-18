package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;

public class ColorArgumentType implements ArgumentType<Color> {

    private static final DynamicCommandExceptionType COLOR_PARSE_EXCEPTION = new DynamicCommandExceptionType(input -> Text.stringifiedTranslatable("message.sbutils.colorArgumentType.invalidColorFormat", input));

    public static ColorArgumentType color() {
        return new ColorArgumentType();
    }

    public static Color getColor(CommandContext<?> context, String id) {
        return context.getArgument(id, Color.class);
    }

    @Override public Color parse(StringReader reader) throws CommandSyntaxException {
        StringBuilder builder = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ')
            builder.append(reader.read());
        String text = builder.toString();
        String colorCode = text.startsWith("#") || text.startsWith("&") ? text.substring(1) : text;
        if (colorCode.length() == 1) {
            Formatting color = Formatting.byCode(colorCode.charAt(0));
            if (color == null || color.getColorValue() == null)
                throw COLOR_PARSE_EXCEPTION.create(colorCode);
            return new Color(color.getColorValue());
        }
        if (colorCode.length() == 6) {
            try {
                int color = Integer.valueOf(colorCode, 16);
                return new Color(color);
            } catch (NumberFormatException nfe) {
                throw COLOR_PARSE_EXCEPTION.create(colorCode);
            }
        }
        throw COLOR_PARSE_EXCEPTION.create(colorCode);
    }
}
