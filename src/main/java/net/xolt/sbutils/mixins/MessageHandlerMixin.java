package net.xolt.sbutils.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.xolt.sbutils.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        Text text = params.applyChatDecoration(message.getContent());
        preFilterMessage(text);
        if (ChatFilters.shouldFilter(text)) {
            ci.cancel();
            return;
        }
        postFilterMessage(text);
    }

    @Inject(method = "onProfilelessMessage", at = @At("HEAD"), cancellable = true)
    private void onProfilelessMessage(Text content, MessageType.Parameters params, CallbackInfo ci) {
        Text text = params.applyChatDecoration(content);
        preFilterMessage(text);
        if (ChatFilters.shouldFilter(text)) {
            ci.cancel();
            return;
        }
        postFilterMessage(text);
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay) {
            return;
        }

        preFilterMessage(message);
        if (ChatFilters.shouldFilter(message)) {
            ci.cancel();
            return;
        }
        postFilterMessage(message);
    }

    private static void preFilterMessage(Text message) {
        ChatLogger.processMessage(message);
        AutoFix.processMessage(message);
        AutoRaffle.processMessage(message);
        AutoReply.processMessage(message);
    }

    private static void postFilterMessage(Text message) {
        Mentions.processMessage(message);
    }
}
