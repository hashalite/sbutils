package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.PlayerListEntry;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.NotifSoundArgumentType;
import net.xolt.sbutils.util.RegexFilters;

import java.util.UUID;

import static net.xolt.sbutils.SbUtils.MC;

public class StaffDetector {

    private static boolean checkForNoStaff = false;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> staffDetectorNode = dispatcher.register(ClientCommandManager.literal("staffdetect")
                .then(ClientCommandManager.literal("detectJoin")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.detectStaffJoin", ModConfig.INSTANCE.getConfig().detectStaffJoin);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffJoin = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffJoin", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffJoin = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffJoin", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("detectLeave")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.detectStaffLeave", ModConfig.INSTANCE.getConfig().detectStaffLeave);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffLeave = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffLeave", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().detectStaffLeave = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.detectStaffLeave", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("sound")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.staffDetectSound", ModConfig.INSTANCE.getConfig().staffDetectSound);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("sound", NotifSoundArgumentType.notifSound())
                                .executes(context ->{
                                    ModConfig.INSTANCE.getConfig().staffDetectSound = NotifSoundArgumentType.getNotifSound(context, "sound");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.staffDetectSound", ModConfig.INSTANCE.getConfig().staffDetectSound);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("sd")
                .executes(context ->
                        dispatcher.execute("staffdetector", context.getSource())
                )
                .redirect(staffDetectorNode));
    }

    public static void onPlayerJoin(PlayerListEntry player) {
        if (!ModConfig.INSTANCE.getConfig().detectStaffJoin || player == null || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, true);
        if (ModConfig.INSTANCE.getConfig().playStaffSound) {
            MC.player.playSound(ModConfig.INSTANCE.getConfig().staffDetectSound.getSound(), 1, 1);
        }
    }

    public static void onPlayerLeave(UUID uuid) {
        if (!ModConfig.INSTANCE.getConfig().detectStaffLeave || uuid == null || MC.getNetworkHandler() == null) {
            return;
        }

        PlayerListEntry player = MC.getNetworkHandler().getPlayerListEntry(uuid);
        if (player == null || !isStaff(player)) {
            return;
        }

        Messenger.printStaffNotification(player, false);

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
        for (PlayerListEntry playerListEntry : MC.getNetworkHandler().getPlayerList()) {
            if (isStaff(playerListEntry)) {
                return true;
            }
        }
        return false;
    }
}
