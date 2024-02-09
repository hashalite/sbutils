package net.xolt.sbutils.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

public class GenericEnumArgumentType<T extends Enum<T> & StringIdentifiable> extends EnumArgumentType<T> {

    private GenericEnumArgumentType(Class<T> type) {
        super(StringIdentifiable.createCodec(type::getEnumConstants), type::getEnumConstants);
    }

    public static <S extends Enum<S> & StringIdentifiable> GenericEnumArgumentType<S> genericEnum(Class<S> type) {

        return new GenericEnumArgumentType<>(type);
    }

    public static <S extends Enum<S> & StringIdentifiable> S getGenericEnum(CommandContext<?> context, String id, Class<S> type) {
        return context.getArgument(id, type);
    }
}
