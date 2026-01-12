package net.xolt.sbutils.mixins;

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
//? if >=1.19.4 {
import com.mojang.authlib.GameProfile;
//? }

@Mixin(ChatListener.class)
public class ChatListenerMixin {

    //? if >=1.19.4 {
    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(PlayerChatMessage message, GameProfile sender, ChatType.Bound params, CallbackInfo ci) {
        Component text = params.decorate(message.decoratedContent());
        onMessage(text, ci);
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("HEAD"), cancellable = true)
    private void onProfilelessMessage(Component content, ChatType.Bound params, CallbackInfo ci) {
        Component text = params.decorate(content);
        onMessage(text, ci);
    }

    //? } else {
    /*@Inject(method = "handleChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(PlayerChatMessage chatMessage, ChatType.Bound boundChatType, CallbackInfo ci) {
        Component text = boundChatType.decorate(chatMessage.serverContent());
        onMessage(text, ci);
    }
    *///? }

    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Component message, boolean overlay, CallbackInfo ci) {
        if (overlay) {
            return;
        }
        onMessage(message, ci);
    }

    private static void onMessage(Component message, CallbackInfo ci) {
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
