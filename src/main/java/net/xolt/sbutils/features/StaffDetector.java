package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
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
                .then(ClientCommandManager.literal("detectJoin")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.detectStaffJoin", ModConfig.INSTANCE.getConfig().detectStaffJoin);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffJoin = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffJoin", ModConfig.INSTANCE.getConfig().detectStaffJoin);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("detectLeave")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.detectStaffLeave", ModConfig.INSTANCE.getConfig().detectStaffLeave);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffLeave = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffLeave", ModConfig.INSTANCE.getConfig().detectStaffLeave);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("sound")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.staffDetectSound", ModConfig.INSTANCE.getConfig().staffDetectSound);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("sound", ModConfig.NotifSound.NotifSoundArgumentType.notifSound())
                                .executes(context ->{
                                    ModConfig.INSTANCE.getConfig().staffDetectSound = ModConfig.NotifSound.NotifSoundArgumentType.getNotifSound(context, "sound");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.staffDetectSound", ModConfig.INSTANCE.getConfig().staffDetectSound);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("staffdetector", context.getSource())
                )
                .redirect(staffDetectorNode));
    }

    public static void onPlayerJoin(PlayerListEntry player) {
        if (!ModConfig.INSTANCE.getConfig().detectStaffJoin || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, true);
        if (ModConfig.INSTANCE.getConfig().playStaffSound) {
            MC.player.playSound(ModConfig.INSTANCE.getConfig().staffDetectSound.getSound(), 1, 1);
        }
    }

    public static void onPlayerLeave(PlayerListEntry player) {
        if (!ModConfig.INSTANCE.getConfig().detectStaffLeave || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, false);

        if (!staffOnline()) {
            Messenger.printMessage("message.sbutils.staffDetector.noStaff");
        }

        checkForNoStaff = true;

        if (ModConfig.INSTANCE.getConfig().playStaffSound) {
            MC.player.playSound(ModConfig.INSTANCE.getConfig().staffDetectSound.getSound(), 1, 1);
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
