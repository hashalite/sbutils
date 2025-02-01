package net.xolt.sbutils.mixins;

import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.Mentions;
import net.xolt.sbutils.feature.features.NoGMT;
import net.xolt.sbutils.feature.features.Notifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"), index = 1)
    private Component modifyChatMessage(Component component) {
        Component result = component;
        if (ModConfig.HANDLER.instance().mentions.enabled && ModConfig.HANDLER.instance().mentions.highlight && Mentions.isValidMessage(result) && Mentions.mentioned(result)) {
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
