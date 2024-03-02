package net.xolt.sbutils.feature;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.binding.ConfigBinding;

import java.util.List;

public abstract class Feature {

    private static final String CATEGORY_KEY = "text.sbutils.config.category.";
    private static final String GROUP_KEY = "text.sbutils.config.group.";

    private final String path;
    protected final String command;
    protected final String commandAlias;

    protected Feature(String path, String command, String commandAlias) {
        this.path = path;
        this.command = command;
        this.commandAlias = commandAlias;
    }

    public abstract List<? extends ConfigBinding<?>> getConfigBindings();

    public abstract void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess);

    protected void registerAlias(CommandDispatcher<FabricClientCommandSource> dispatcher, LiteralCommandNode<FabricClientCommandSource> commandNode) {
        dispatcher.register(ClientCommandManager.literal(commandAlias)
                .executes(context ->
                        dispatcher.execute(command, context.getSource())
                )
                .redirect(commandNode));
    }

    public String getPath() {
        return path;
    }

    public MutableComponent getName() {
        return Component.translatable(getNameTranslation());
    }

    public MutableComponent getGroupName() {
        return Component.translatable(getGroupTranslation());
    }

    public String getNameTranslation() {
        return CATEGORY_KEY + path;
    }

    public String getGroupTranslation() {
        return GROUP_KEY + path;
    }
}
