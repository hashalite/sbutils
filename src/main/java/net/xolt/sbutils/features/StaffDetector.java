package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
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
                .then(CommandUtils.bool("detectJoin", "staffDetector.detectJoin", () -> ModConfig.HANDLER.instance().staffDetector.detectJoin, (value) -> ModConfig.HANDLER.instance().staffDetector.detectJoin = value))
                .then(CommandUtils.bool("detectLeave", "staffDetector.detectLeave", () -> ModConfig.HANDLER.instance().staffDetector.detectLeave, (value) -> ModConfig.HANDLER.instance().staffDetector.detectLeave = value))
                .then(CommandUtils.genericEnum("sound", "sound", "staffDetector.sound", ModConfig.NotifSound.class, () -> ModConfig.HANDLER.instance().staffDetector.sound, (value) -> ModConfig.HANDLER.instance().staffDetector.sound = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(staffDetectorNode));
    }

    public static void onPlayerJoin(PlayerListEntry player) {
        if (!ModConfig.HANDLER.instance().staffDetector.detectJoin || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, true);
        if (ModConfig.HANDLER.instance().staffDetector.playSound) {
            MC.player.playSound(ModConfig.HANDLER.instance().staffDetector.sound.getSound(), 1, 1);
        }
    }

    public static void onPlayerLeave(PlayerListEntry player) {
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

    private static boolean isStaff(PlayerListEntry player) {
        if (player == null) {
            return false;
        }

        // Special Noobcrew case
        if (player.getProfile().getId().equals(UUID.fromString("1ba2d16f-3d11-4a1f-b214-09e83906e6b5"))) {
            return true;
        }

        String displayName = MC.inGameHud.getPlayerListHud().getPlayerName(player).getString();

        return RegexFilters.staffFilter.matcher(displayName).matches();
    }

    public static boolean staffOnline() {
        if (MC.getNetworkHandler() == null) {
            return false;
        }

        for (PlayerListEntry playerListEntry : MC.getNetworkHandler().getListedPlayerListEntries()) {
            if (isStaff(playerListEntry)) {
                return true;
            }
        }

        return false;
    }
}
