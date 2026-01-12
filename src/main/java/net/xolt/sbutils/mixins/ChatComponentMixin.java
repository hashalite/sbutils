package net.xolt.sbutils.mixins;

import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.Mentions;
import net.xolt.sbutils.feature.features.NoGMT;
import net.xolt.sbutils.feature.features.Notifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    //? if >=1.20.6 {
    @ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"), index = 1)
    private Component onAddMessage(Component component) {
        return modifyChatMessage(component);
    }
    //? } else {
    /*@ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"), index = 0)
    private Component onAddMessage(Component component) {
        return modifyChatMessage(component);
    }
    //? if >=1.19.4 {
    @ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;logChatMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/GuiMessageTag;)V"), index = 0)
    private Component onLogMessage(Component component) {
        return modifyChatMessage(component);
    }
    //? }
    *///? }

    @Unique
    private Component modifyChatMessage(Component component) {
        Component result = component;
        if (ModConfig.instance().mentions.enabled && ModConfig.instance().mentions.highlight && Mentions.isValidMessage(result) && Mentions.mentioned(result)) {
            result = Mentions.modifyMessage(result);
        }

        if (NoGMT.shouldModify(result)) {
            result = NoGMT.modifyMessage(result);
        }

        if (Notifier.shouldModify(result)) {
            result = Notifier.modifyMessage(result);
        }

        return result;
    }
}
