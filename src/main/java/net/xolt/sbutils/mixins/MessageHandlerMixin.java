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
        onMessage(text);
        if (ChatFilters.shouldFilter(text)) {
            ci.cancel();
        }
    }

    @Inject(method = "onProfilelessMessage", at = @At("HEAD"), cancellable = true)
    private void onProfilelessMessage(Text content, MessageType.Parameters params, CallbackInfo ci) {
        Text text = params.applyChatDecoration(content);
        onMessage(text);
        if (ChatFilters.shouldFilter(text)) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay) {
            return;
        }

        onMessage(message);
        if (ChatFilters.shouldFilter(message)) {
            ci.cancel();
        }
    }

    private static void onMessage(Text message) {
        Mentions.processMessage(message);
        ChatLogger.processMessage(message);
        AutoFix.processMessage(message);
        AutoLottery.processMessage(message);
        AutoReply.processMessage(message);
    }
}
