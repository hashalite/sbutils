package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;

public class GenericEnumArgumentType<T extends Enum<T> & StringRepresentable> extends StringRepresentableArgument<T> {

    private GenericEnumArgumentType(Class<T> type) {
        super(StringRepresentable.fromEnum(type::getEnumConstants), type::getEnumConstants);
    }

    public static <S extends Enum<S> & StringRepresentable> GenericEnumArgumentType<S> genericEnum(Class<S> type) {

        return new GenericEnumArgumentType<>(type);
    }

    public static <S extends Enum<S> & StringRepresentable> S getGenericEnum(CommandContext<?> context, String id, Class<S> type) {
        return context.getArgument(id, type);
    }
}
