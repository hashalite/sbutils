package net.xolt.sbutils.util;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import static net.xolt.sbutils.SbUtils.MC;

public class SoundUtils {

    public static void playNotifSound(SoundEvent sound, SoundSource source) {
        Identifier soundResource =
                //? if >=1.21.4 {
                sound.location();
                //? } else
                //sound.getLocation();

        SoundInstance soundInstance = new SimpleSoundInstance(
                soundResource, source, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
        );
        MC.getSoundManager().play(soundInstance);
    }
}
