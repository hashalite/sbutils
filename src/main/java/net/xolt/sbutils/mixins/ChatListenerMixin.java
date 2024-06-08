package net.xolt.sbutils.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(PlayerChatMessage message, GameProfile sender, ChatType.Bound params, CallbackInfo ci) {
        Component text = params.decorate(message.decoratedContent());
        preFilterMessage(text);
        SbUtils.FEATURES.get(ChatFilters.class).onChatMessage(text, ci);
        if (ci.isCancelled())
            return;
        postFilterMessage(text);
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"), cancellable = true)
    private void onProfilelessMessage(Component content, ChatType.Bound params, CallbackInfo ci) {
        Component text = params.decorate(content);
        preFilterMessage(text);
        SbUtils.FEATURES.get(ChatFilters.class).onChatMessage(text, ci);
        if (ci.isCancelled())
            return;
        postFilterMessage(text);
    }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Component message, boolean overlay, CallbackInfo ci) {
        if (overlay) {
            return;
        }

        preFilterMessage(message);
        SbUtils.FEATURES.get(ChatFilters.class).onChatMessage(message, ci);
        if (ci.isCancelled())
            return;
        postFilterMessage(message);
    }

    private static void preFilterMessage(Component message) {
        SbUtils.FEATURES.get(ChatLogger.class).processMessage(message);
        SbUtils.FEATURES.get(AutoRaffle.class).processMessage(message);
        SbUtils.FEATURES.get(AutoReply.class).processMessage(message);
        SbUtils.FEATURES.get(Notifier.class).processMessage(message);
        SbUtils.COMMAND_SENDER.processMessage(message);
    }

    private static void postFilterMessage(Component message) {
        Mentions.processMessage(message);
    }
}
