package net.xolt.sbutils.util.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;
import net.xolt.sbutils.config.ModConfig;

public class NotifSoundArgumentType extends EnumArgumentType<ModConfig.NotifSound> {
    private NotifSoundArgumentType() {
        super(StringIdentifiable.createCodec(ModConfig.NotifSound::values), ModConfig.NotifSound::values);
    }

    public static NotifSoundArgumentType notifSound() {
        return new NotifSoundArgumentType();
    }

    public static ModConfig.NotifSound getNotifSound(CommandContext<?> context, String id) {
        return context.getArgument(id, ModConfig.NotifSound.class);
    }
}
