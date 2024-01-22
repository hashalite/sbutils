package net.xolt.sbutils.mixins;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.xolt.sbutils.SbUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(CommandTreeS2CPacket.class)
public class CommandTreeS2CPacketMixin {

    // Remove conflicting commands from the server command tree
    @Inject(method = "getCommandTree", at = @At("RETURN"))
    private void onGetCommandTree(CallbackInfoReturnable<RootCommandNode<CommandSource>> cir) {
        @SuppressWarnings("unchecked")
        Map<String, CommandNode<CommandSource>> nodes = ((CommandNodeAccessor<CommandSource>)cir.getReturnValue()).getChildren();
        for (String command : SbUtils.commands) {
            nodes.remove(command);
        }
    }
}
