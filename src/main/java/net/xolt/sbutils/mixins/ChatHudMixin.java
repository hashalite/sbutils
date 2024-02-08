package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.Mentions;
import net.xolt.sbutils.features.NoGMT;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Invoker
    abstract void callAddMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh);

    @Invoker
    abstract void callLogChatMessage(Text message, @Nullable MessageIndicator indicator);

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    public void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        Text modified = message;
        if (ModConfig.HANDLER.instance().mentions.enabled && ModConfig.HANDLER.instance().mentions.highlight && Mentions.isValidMessage(modified) && Mentions.mentioned(modified)) {
            ci.cancel();
            modified = Mentions.modifyMessage(modified);
        }

        if (NoGMT.shouldModify(modified)) {
            ci.cancel();
            modified = NoGMT.modifyMessage(modified);
        }

        if (ci.isCancelled()) {
            callLogChatMessage(message, indicator);
            callAddMessage(modified, signature, MC.inGameHud.getTicks(), indicator, false);
        }
    }
}
