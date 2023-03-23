package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoMine {

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(ClientCommandManager.literal("automine")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoMine = !ModConfig.INSTANCE.getConfig().autoMine;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.automine", ModConfig.INSTANCE.getConfig().autoMine);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("switch")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoSwitch", ModConfig.INSTANCE.getConfig().autoSwitch);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoSwitch = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoSwitch", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoSwitch = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoSwitch", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("durability")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.switchDurability", ModConfig.INSTANCE.getConfig().switchDurability);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("durability", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().switchDurability = IntegerArgumentType.getInteger(context, "durability");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.switchDurability", ModConfig.INSTANCE.getConfig().switchDurability);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("mine")
                .executes(context ->
                        dispatcher.execute("automine", context.getSource()))
                .redirect(autoMineNode));
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoMine || MC.player == null) {
            return;
        }

        ItemStack holding = MC.player.getInventory().getMainHandStack();
        int minDurability = getMinDurability();

        if (ModConfig.INSTANCE.getConfig().autoSwitch && !AutoFix.fixing() && holding.getItem() instanceof PickaxeItem && holding.getMaxDamage() - holding.getDamage() <= minDurability) {
            int newPickaxeSlot = findNewPickaxe();
            if (newPickaxeSlot != -1) {
                InvUtils.swapToHotbar(newPickaxeSlot, MC.player.getInventory().selectedSlot);
            } else if (!ModConfig.INSTANCE.getConfig().autoFix) {
                ModConfig.INSTANCE.getConfig().autoMine = false;
                ModConfig.INSTANCE.save();
                Messenger.printMessage("message.sbutils.autoMine.noPickaxe");
                return;
            }
        }

        if (!shouldMine()) {
            return;
        }

        if (MC.currentScreen == null) {
            MC.options.attackKey.setPressed(true);
        }
    }

    private static int findNewPickaxe() {
        if (MC.player == null) {
            return -1;
        }

        int minDurability = Math.max(ModConfig.INSTANCE.getConfig().switchDurability, ModConfig.INSTANCE.getConfig().toolSaver ? ModConfig.INSTANCE.getConfig().toolSaverDurability : 0);

        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack itemStack = MC.player.getInventory().getStack(i);
            if (!(itemStack.getItem() instanceof PickaxeItem)) {
                continue;
            }

            if (itemStack.getMaxDamage() - itemStack.getDamage() > minDurability) {
                return i;
            }
        }
        return -1;
    }

    private static int getMinDurability() {
        return Math.max(ModConfig.INSTANCE.getConfig().autoSwitch ? ModConfig.INSTANCE.getConfig().switchDurability : -1, ModConfig.INSTANCE.getConfig().toolSaver ? ModConfig.INSTANCE.getConfig().toolSaverDurability : -1);
    }

    public static boolean shouldMine() {
        return !MC.isPaused() && !AutoFix.fixing() && !ToolSaver.shouldCancelAction();
    }
}
