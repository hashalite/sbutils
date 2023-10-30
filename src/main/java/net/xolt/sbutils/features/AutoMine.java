package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.mixins.TimeArgumentTypeAccessor;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoMine {

    private static final String COMMAND = "automine";
    private static final String ALIAS = "mine";

    private static int timer;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoMine = !ModConfig.INSTANCE.getConfig().autoMine;
                    ModConfig.INSTANCE.save();
                    if (!ModConfig.INSTANCE.getConfig().autoMine) {
                        reset();
                    }
                    Messenger.printChangedSetting("text.sbutils.config.category.automine", ModConfig.INSTANCE.getConfig().autoMine);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("timer")
                        .executes(context -> {
                            if (timer <= 0) {
                                Messenger.printMessage("message.sbutils.autoMine.timerNotSet");
                                return Command.SINGLE_SUCCESS;
                            }
                            Messenger.printAutoMineTime(timer);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("duration", getTimeArgumentType())
                                .executes(context -> {
                                    timer = IntegerArgumentType.getInteger(context, "duration");
                                    ModConfig.INSTANCE.getConfig().autoMine = true;
                                    Messenger.printAutoMineEnabledFor(timer);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("switch")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.autoSwitch", ModConfig.INSTANCE.getConfig().autoSwitch);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().autoSwitch = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.autoSwitch", ModConfig.INSTANCE.getConfig().autoSwitch);
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

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(autoMineNode));
    }

    private static TimeArgumentType getTimeArgumentType() {
        TimeArgumentType timeArgumentType = TimeArgumentType.time(1);
        Object2IntMap<String> units = ((TimeArgumentTypeAccessor)timeArgumentType).getUNITS();
        units.remove("t", 1);
        units.remove("", 1);
        units.remove("d", 24000);
        units.put("", 20);
        units.put("m", 1200);
        units.put("h", 72000);
        return timeArgumentType;
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoMine || MC.player == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            if (timer == 0) {
                ModConfig.INSTANCE.getConfig().autoMine = false;
                ModConfig.INSTANCE.save();
                MC.options.attackKey.setPressed(false);
                Messenger.printChangedSetting("text.sbutils.config.category.automine", false);
                return;
            }
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
            MC.options.attackKey.setPressed(false);
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

    private static void reset() {
        timer = 0;
    }

    public static void onDisconnect() {
        reset();
    }

    public static boolean shouldMine() {
        return MC.player != null && !MC.isPaused() && !AutoFix.fixing() && !ToolSaver.shouldCancelAttack();
    }
}
