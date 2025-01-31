package net.xolt.sbutils.mixins;

import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.Mentions;
import net.xolt.sbutils.feature.features.NoGMT;
import net.xolt.sbutils.feature.features.Notifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.sbutils.SbUtils.MC;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Invoker
    abstract void callAddMessage(Component message, @Nullable MessageSignature signature, int ticks, @Nullable GuiMessageTag indicator, boolean refresh);

    @Invoker
    abstract void callLogChatMessage(Component message, @Nullable GuiMessageTag indicator);

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    public void onAddMessage(Component message, MessageSignature signature, GuiMessageTag indicator, CallbackInfo ci) {
        Component modified = message;
        if (ModConfig.HANDLER.getConfig().mentions.enabled && ModConfig.HANDLER.getConfig().mentions.highlight && Mentions.isValidMessage(modified) && Mentions.mentioned(modified)) {
            ci.cancel();
            modified = Mentions.modifyMessage(modified);
        }

        if (NoGMT.shouldModify(modified)) {
            ci.cancel();
            modified = NoGMT.modifyMessage(modified);
        }

        if (Notifier.shouldModify(modified)) {
            ci.cancel();
            modified = Notifier.modifyMessage(message);
        }

        if (ci.isCancelled()) {
            callLogChatMessage(message, indicator);
            callAddMessage(modified, signature, MC.gui.getGuiTicks(), indicator, false);
        }
    }
}
