package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class EventNotifier extends Feature {
    private final OptionBinding<Boolean> showLlamaTitle = new OptionBinding<>("eventNotifier.showLlamaTitle", Boolean.class, (config) -> config.eventNotifier.showLlamaTitle, (config, value) -> config.eventNotifier.showLlamaTitle = value);
    private final OptionBinding<Boolean> playLlamaSound = new OptionBinding<>("eventNotifier.playLlamaSound", Boolean.class, (config) -> config.eventNotifier.playLlamaSound, (config, value) -> config.eventNotifier.playLlamaSound = value);
    private final OptionBinding<ModConfig.NotifSound> llamaSound = new OptionBinding<>("eventNotifier.llamaSound", ModConfig.NotifSound.class, (config) -> config.eventNotifier.llamaSound, (config, value) -> config.eventNotifier.llamaSound = value);
    private final OptionBinding<Boolean> showTraderTitle = new OptionBinding<>("eventNotifier.showTraderTitle", Boolean.class, (config) -> config.eventNotifier.showTraderTitle, (config, value) -> config.eventNotifier.showTraderTitle = value);
    private final OptionBinding<Boolean> playTraderSound = new OptionBinding<>("eventNotifier.playTraderSound", Boolean.class, (config) -> config.eventNotifier.playTraderSound, (config, value) -> config.eventNotifier.playTraderSound = value);
    private final OptionBinding<ModConfig.NotifSound> traderSound = new OptionBinding<>("eventNotifier.traderSound", ModConfig.NotifSound.class, (config) -> config.eventNotifier.traderSound, (config, value) -> config.eventNotifier.traderSound = value);

    public EventNotifier() {
        super("eventNotifier", "eventnotifier", "enotify");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(showLlamaTitle, playLlamaSound, llamaSound, showTraderTitle, playTraderSound, traderSound);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> eventNotifierNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(ClientCommandManager.literal("vpLlama")
                        .then(CommandHelper.bool("title", showLlamaTitle))
                        .then(CommandHelper.bool("playSound", playLlamaSound))
                        .then(CommandHelper.genericEnum("sound", "sound", llamaSound)))
                .then(ClientCommandManager.literal("trader")
                        .then(CommandHelper.bool("title", showTraderTitle))
                        .then(CommandHelper.bool("playSound", playTraderSound))
                        .then(CommandHelper.genericEnum("sound", "sound", traderSound)))
        );
        registerAlias(dispatcher, eventNotifierNode);
    }

    public static void processMessage(Component message) {
        if (!enabled())
            return;

        String stringMessage = message.getString();

        if (RegexFilters.vpLlamaFilter.matcher(stringMessage).matches())
            doLlamaNotification();
        else if (RegexFilters.wanderingTraderFilter.matcher(stringMessage).matches())
            doTraderNotification();
    }

    private static void doLlamaNotification() {
        if (ModConfig.HANDLER.instance().eventNotifier.playLlamaSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().eventNotifier.llamaSound.getSound(), 1, 1);

        if (ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle)
            ChatUtils.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Component.translatable("message.sbutils.eventNotifier.vpLlama"));
    }

    private static void doTraderNotification() {
        if (ModConfig.HANDLER.instance().eventNotifier.playTraderSound && MC.player != null)
            MC.player.playSound(ModConfig.HANDLER.instance().eventNotifier.traderSound.getSound(), 1, 1);

        if (ModConfig.HANDLER.instance().eventNotifier.showTraderTitle)
            ChatUtils.sendPlaceholderTitle("message.sbutils.eventNotifier.sighted", Component.translatable("message.sbutils.eventNotifier.wanderingTrader"));
    }

    private static boolean enabled() {
        return ModConfig.HANDLER.instance().eventNotifier.showLlamaTitle ||
                ModConfig.HANDLER.instance().eventNotifier.playLlamaSound ||
                ModConfig.HANDLER.instance().eventNotifier.showTraderTitle ||
                ModConfig.HANDLER.instance().eventNotifier.playTraderSound;
    }
}
