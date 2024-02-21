package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;
import java.util.UUID;

import static net.xolt.sbutils.SbUtils.MC;

public class StaffDetector {

    private static final String COMMAND = "staffdetect";
    private static final String ALIAS = "sd";

    private static boolean checkForNoStaff = false;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
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

    public static void onPlayerJoin(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectJoin || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, true);
        if (ModConfig.HANDLER.instance().staffDetector.playSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), 1, 1);
        }
    }

    public static void onPlayerLeave(PlayerInfo player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectLeave || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, false);

        if (!staffOnline()) {
            Messenger.printMessage("message.sbutils.staffDetector.noStaff");
        }

        checkForNoStaff = true;

        if (ModConfig.HANDLER.instance().staffDetector.playSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), 1, 1);
        }
    }

    public static void afterPlayerLeave() {
        if (!checkForNoStaff) {
            return;
        }

        if (!staffOnline()) {
            Messenger.printMessage("message.sbutils.staffDetector.noStaff");
        }

        checkForNoStaff = false;
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
