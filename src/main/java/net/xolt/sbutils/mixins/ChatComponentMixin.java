package net.xolt.sbutils.mixins;

import net.minecraft.client.GuiMessage;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.Mentions;
import net.xolt.sbutils.feature.features.NoGMT;
import net.xolt.sbutils.feature.features.Notifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.sbutils.SbUtils.MC;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;

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
