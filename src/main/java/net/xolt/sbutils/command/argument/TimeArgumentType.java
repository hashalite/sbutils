package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Based on net.minecraft.command.argument.TimeArgumentType
 */
public class TimeArgumentType implements ArgumentType<Double> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0h", "0m", "0");
    private static final SimpleCommandExceptionType INVALID_UNIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("message.sbutils.timeArgumentType.invalidUnitException"));
    private static final Dynamic2CommandExceptionType TIME_TOO_LOW_EXCEPTION = new Dynamic2CommandExceptionType((value, minimum) -> Text.stringifiedTranslatable("message.sbutils.timeArgumentType.timeTooLowException", minimum, value));
    private static final Object2DoubleMap<String> UNITS = new Object2DoubleOpenHashMap<>();
    final double minimum;

    private TimeArgumentType(double minimum) {
        this.minimum = minimum;
    }

    public static TimeArgumentType time() {
        return new TimeArgumentType(0);
    }

    public static TimeArgumentType time(int minimum) {
        return new TimeArgumentType(minimum);
    }

    @Override public Double parse(StringReader stringReader) throws CommandSyntaxException {
        double value = stringReader.readDouble();
        String string = stringReader.readUnquotedString();
        double unit = UNITS.getOrDefault(string, 0.0);
        if (unit == 0) {
            throw INVALID_UNIT_EXCEPTION.create();
        } else {
            double seconds = Math.round(value * unit);
            if (seconds < this.minimum) {
                throw TIME_TOO_LOW_EXCEPTION.create(seconds, this.minimum);
            } else {
                return seconds;
            }
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getRemaining());

        try {
            stringReader.readDouble();
        } catch (CommandSyntaxException var5) {
            return builder.buildFuture();
        }

        return CommandSource.suggestMatching(UNITS.keySet(), builder.createOffset(builder.getStart() + stringReader.getCursor()));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static {
        UNITS.put("", 1.0);
        UNITS.put("m", 60.0);
        UNITS.put("h", 3600.0);
        UNITS.put("d", 86400.0);
    }
}
