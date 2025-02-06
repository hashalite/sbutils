package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ApiUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.TextUtils;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

public class StaffDetector extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> detectJoin = new OptionBinding<>("sbutils", "staffDetector.detectJoin", Boolean.class, (config) -> config.staffDetector.detectJoin, (config, value) -> config.staffDetector.detectJoin = value);
    private final OptionBinding<ModConfig, Boolean> detectLeave = new OptionBinding<>("sbutils", "staffDetector.detectLeave", Boolean.class, (config) -> config.staffDetector.detectLeave, (config, value) -> config.staffDetector.detectLeave = value);
    private final OptionBinding<ModConfig, Boolean> playSound = new OptionBinding<>("sbutils", "staffDetector.playSound", Boolean.class, (config) -> config.staffDetector.playSound, (config, value) -> config.staffDetector.playSound = value);
    private final OptionBinding<ModConfig, ModConfig.NotifSound> sound = new OptionBinding<>("sbutils", "staffDetector.sound", ModConfig.NotifSound.class, (config) -> config.staffDetector.sound, (config, value) -> config.staffDetector.sound = value);

    private final Map<UUID, String> staffList = new HashMap<>();
    private boolean checkForNoStaff = false;

    public StaffDetector() {
        super("sbutils", "staffDetector", "staffdetect", "sd");
        ApiUtils.getStaffList(staffList::putAll);
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(detectJoin, detectLeave, playSound, sound);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> staffDetectorNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(CommandHelper.bool("detectJoin", detectJoin, ModConfig.HANDLER))
                .then(CommandHelper.bool("detectLeave", detectLeave, ModConfig.HANDLER))
                .then(CommandHelper.bool("playSound", playSound, ModConfig.HANDLER))
                .then(CommandHelper.genericEnum("sound", "sound", sound, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, staffDetectorNode);
    }

    public void onPlayerLeave(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectLeave || !isStaff(player))
            return;

        showStaffNotification(player, false);

        if (!staffOnline())
            ChatUtils.printMessage("message.sbutils.staffDetector.noStaff");

        checkForNoStaff = true;

        if (MC.player != null && ModConfig.HANDLER.instance().staffDetector.playSound)
            MC.player.playNotifySound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), SoundSource.MASTER, 1.0F, 1.0F);
    }

    public void afterPlayerLeave() {
        if (!checkForNoStaff)
            return;

        if (!staffOnline())
            ChatUtils.printMessage("message.sbutils.staffDetector.noStaff");

        checkForNoStaff = false;
    }

    public void onPlayerJoin(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectJoin || !isStaff(player))
            return;

        showStaffNotification(player, true);
        if (MC.player != null && ModConfig.HANDLER.instance().staffDetector.playSound)
            MC.player.playNotifySound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), SoundSource.MASTER, 1.0F, 1.0F);
    }

    private void showStaffNotification(PlayerInfo player, boolean joined) {
        MutableComponent message = Component.translatable("message.sbutils.staffDetector.notification");
        String position = staffList.get(player.getProfile().getId());
        MutableComponent staff = Component.literal(player.getProfile().getName() + (position.isEmpty() ? "" : " (" + position + ")"));
        MutableComponent status = TextUtils.formatOnlineOffline(joined);
        ChatUtils.printWithPlaceholders(message, staff, status);
    }

    private boolean isStaff(PlayerInfo player) {
        return staffList.containsKey(player.getProfile().getId());
    }

    public boolean staffOnline() {
        if (MC.getConnection() == null)
            return false;

        for (PlayerInfo playerListEntry : MC.getConnection().getListedOnlinePlayers())
            if (isStaff(playerListEntry))
                return true;

        return false;
    }
}
