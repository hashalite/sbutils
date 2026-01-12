package net.xolt.sbutils.feature.features;

import net.minecraft.network.protocol.game.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.item.SignItem;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.11 {
import net.minecraft.world.entity.player.Input;
//? }

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
        if (!ModConfig.instance().autoPrivate.enabled || !(MC.player.getMainHandItem().getItem() instanceof SignItem))
            return;
        sendSneakPacket(true);
        sneaked = true;
    }

    public void afterInteractBlock() {
        if (!sneaked || MC.getConnection() == null || MC.player == null)
            return;
        sendSneakPacket(MC.player.isShiftKeyDown());
        sneaked = false;
    }

    public static boolean onSignEditorOpen(ClientboundOpenSignEditorPacket packet) {
        if (!ModConfig.instance().autoPrivate.enabled
                //? if >=1.20
                || !packet.isFrontText()
        )
            return false;
        return updateSign(packet);
    }

    //? if >=1.21.11 {
    public void onSendPlayerInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (!sneaked || packet.input().shift() || MC.getConnection() == null)
            return;
        ci.cancel();
        sendSneakPacket(packet.input(), true);
    }
    //? } else {
    /*public void onSendPlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        if (!sneaked)
            return;
        if (packet.getAction().equals(ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY) || packet.getAction().equals(ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY))
            ci.cancel();
    }
    *///? }

    //? if >=1.21.11 {
    public static void sendSneakPacket(boolean pressed) {
        if (MC.player == null)
            return;
        sendSneakPacket(MC.player.input.keyPresses, pressed);
    }
    //? }

    public static void sendSneakPacket(
            //? if >=1.21.11 {
            Input pInput,
            //? }
            boolean pressed) {
        if (MC.getConnection() == null || MC.player == null)
            return;

        //? if >=1.21.11 {
        Input input = new Input(
                pInput.forward(),
                pInput.backward(),
                pInput.left(),
                pInput.right(),
                pInput.jump(),
                pressed,
                pInput.sprint()
        );
        MC.getConnection().send(new ServerboundPlayerInputPacket(input));
        //? } else {
        /*ServerboundPlayerCommandPacket.Action action = pressed ? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY : ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
        MC.getConnection().send(new ServerboundPlayerCommandPacket(MC.player, action));
        *///? }
    }

    private static boolean updateSign(ClientboundOpenSignEditorPacket packet) {
        if (MC.getConnection() == null || MC.player == null)
            return false;

        List<String> names = ModConfig.instance().autoPrivate.names;
        String[] lines = {"", ""};

        for (int i = 0; i < Math.min(names.size(), lines.length); i++)
            lines[i] = names.get(i);

        MC.getConnection().send(new ServerboundSignUpdatePacket(packet.getPos(),
                //? if >=1.20
                true,
                "[private]", MC.player.getName().getString(), lines[0], lines[1]));
        return true;
    }
}
