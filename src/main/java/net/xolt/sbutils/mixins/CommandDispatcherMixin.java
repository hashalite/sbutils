package net.xolt.sbutils.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import net.xolt.sbutils.SbUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin {

    // Remove conflicting server commands from command tree
    @ModifyVariable(method = "<init>(Lcom/mojang/brigadier/tree/RootCommandNode;)V", at = @At("HEAD"), argsOnly = true)
    private static RootCommandNode<CommandSource> onInitCommandDispatcher(final RootCommandNode<CommandSource> root) {
        Collection<CommandNode<CommandSource>> nodes = root.getChildren();

        nodes.removeIf((node) -> SbUtils.commands.contains(node.getName()));

        RootCommandNode<CommandSource> newRootNode = new RootCommandNode<>();

        nodes.forEach(newRootNode::addChild);

        return root;
    }
}
