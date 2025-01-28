package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.item.SignItem;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoPrivate extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "autoPrivate.enabled", Boolean.class, (config) -> config.autoPrivate.enabled, (config, value) -> config.autoPrivate.enabled = value);
    private final ListOptionBinding<ModConfig, String> names = new ListOptionBinding<>("sbutils", "autoPrivate.names", "", String.class, (config) -> config.autoPrivate.names, (config, value) -> config.autoPrivate.names = value, new ListConstraints<>(null, 2, new StringConstraints(false)));

    private boolean sneaked;

    public AutoPrivate() {
        super("sbutils", "autoPrivate", "autoprivate", "ap");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, names);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoPrivateNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                        .then(CommandHelper.stringList("names", "name", names, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, autoPrivateNode);
    }

    public void onInteractBlock() {
        if (MC.player == null || MC.getConnection() == null)
            return;
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !(MC.player.getMainHandItem().getItem() instanceof SignItem))
            return;
        MC.getConnection().send(new ServerboundPlayerCommandPacket(MC.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        sneaked = true;
    }

    public void afterInteractBlock() {
        if (!sneaked || MC.getConnection() == null || MC.player == null)
            return;
        MC.getConnection().send(new ServerboundPlayerCommandPacket(MC.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        sneaked = false;
    }

    public static boolean onSignEditorOpen(ClientboundOpenSignEditorPacket packet) {
        if (!ModConfig.HANDLER.instance().autoPrivate.enabled || !packet.isFrontText())
            return false;
        return updateSign(packet);
    }

    private static boolean updateSign(ClientboundOpenSignEditorPacket packet) {
        if (MC.getConnection() == null || MC.player == null)
            return false;

        List<String> names = ModConfig.HANDLER.instance().autoPrivate.names;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++)
            lines[i] = names.get(i);

        MC.getConnection().send(new ServerboundSignUpdatePacket(packet.getPos(), true, "[private]", MC.player.getName().getString(), lines[0], lines[1]));
        return true;
    }
}
