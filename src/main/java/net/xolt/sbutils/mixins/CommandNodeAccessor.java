package net.xolt.sbutils.mixins;

import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CommandNode.class)
public interface CommandNodeAccessor<S> {

    @Accessor(remap = false)
    Map<String, CommandNode<S>> getChildren();
}
