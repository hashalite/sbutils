package net.xolt.sbutils.mixins;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.xolt.sbutils.SbUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ClientboundCommandsPacket.class)
public class ClientboundCommandsPacketMixin {

    // Remove conflicting commands from the server command tree
    @Inject(method = "getRoot", at = @At("RETURN"))
    private void onGetCommandTree(CallbackInfoReturnable<RootCommandNode<SharedSuggestionProvider>> cir) {
        @SuppressWarnings("unchecked")
        Map<String, CommandNode<SharedSuggestionProvider>> nodes = ((CommandNodeAccessor<SharedSuggestionProvider>)cir.getReturnValue()).getChildren();
        for (String command : SbUtils.commands) {
            nodes.remove(command);
        }
    }
}
