package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;
import java.util.UUID;

import static net.xolt.sbutils.SbUtils.MC;

public class StaffDetector extends Feature {

    private static final String COMMAND = "staffdetect";
    private static final String ALIAS = "sd";

    private boolean checkForNoStaff = false;

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> staffDetectorNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(CommandHelper.bool("detectJoin", "staffDetector.detectJoin", () -> ModConfig.HANDLER.instance().staffDetector.detectJoin, (value) -> ModConfig.HANDLER.instance().staffDetector.detectJoin = value))
                .then(CommandHelper.bool("detectLeave", "staffDetector.detectLeave", () -> ModConfig.HANDLER.instance().staffDetector.detectLeave, (value) -> ModConfig.HANDLER.instance().staffDetector.detectLeave = value))
                .then(CommandHelper.genericEnum("sound", "sound", "staffDetector.sound", ModConfig.NotifSound.class, () -> ModConfig.HANDLER.instance().staffDetector.sound, (value) -> ModConfig.HANDLER.instance().staffDetector.sound = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(staffDetectorNode));
    }

    public void onPlayerLeave(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectLeave || !isStaff(player)) {
            return;
        }

        ChatUtils.printStaffNotification(player, false);

        if (!staffOnline()) {
            ChatUtils.printMessage("message.sbutils.staffDetector.noStaff");
        }

        checkForNoStaff = true;

        if (MC.player != null && ModConfig.HANDLER.instance().staffDetector.playSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), 1, 1);
        }
    }

    public void afterPlayerLeave() {
        if (!checkForNoStaff) {
            return;
        }

        if (!staffOnline()) {
            ChatUtils.printMessage("message.sbutils.staffDetector.noStaff");
        }

        checkForNoStaff = false;
    }

    public static void onPlayerJoin(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectJoin || !isStaff(player)) {
            return;
        }

        ChatUtils.printStaffNotification(player, true);
        if (MC.player != null && ModConfig.HANDLER.instance().staffDetector.playSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), 1, 1);
        }
    }

    private static boolean isStaff(PlayerInfo player) {
        if (player == null) {
            return false;
        }

        // Special Noobcrew case
        if (player.getProfile().getId().equals(UUID.fromString("1ba2d16f-3d11-4a1f-b214-09e83906e6b5"))) {
            return true;
        }

        String displayName = MC.gui.getTabList().getNameForDisplay(player).getString();

        return RegexFilters.staffFilter.matcher(displayName).matches();
    }

    public static boolean staffOnline() {
        if (MC.getConnection() == null) {
            return false;
        }

        for (PlayerInfo playerListEntry : MC.getConnection().getListedOnlinePlayers()) {
            if (isStaff(playerListEntry)) {
                return true;
            }
        }

        return false;
    }
}
