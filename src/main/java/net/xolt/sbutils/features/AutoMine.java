package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.xolt.sbutils.util.CommandUtils;
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
        final LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "automine", () -> ModConfig.HANDLER.instance().autoMine, (value) -> {ModConfig.HANDLER.instance().autoMine = value; if (!value) reset();})
                    .then(CommandUtils.runnable("timer", () -> Messenger.printAutoMineTime(timer))
                            .then(ClientCommandManager.argument("duration", getTimeArgumentType())
                                    .executes(context -> onTimerCommand(IntegerArgumentType.getInteger(context, "duration")))))
                    .then(CommandUtils.bool("switch", "autoSwitch", () -> ModConfig.HANDLER.instance().autoSwitch, (value) -> ModConfig.HANDLER.instance().autoSwitch = value))
                    .then(CommandUtils.integer("durability", "durability", "switchDurability", () -> ModConfig.HANDLER.instance().switchDurability, (value) -> ModConfig.HANDLER.instance().switchDurability = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(autoMineNode));
    }

    private static int onTimerCommand(int time) {
        timer = time;
        ModConfig.HANDLER.instance().autoMine = true;
        Messenger.printAutoMineEnabledFor(timer);
        return Command.SINGLE_SUCCESS;
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
        if (!ModConfig.HANDLER.instance().autoMine || MC.player == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            if (timer == 0) {
                ModConfig.HANDLER.instance().autoMine = false;
                ModConfig.HANDLER.save();
                MC.options.attackKey.setPressed(false);
                Messenger.printChangedSetting("text.sbutils.config.category.automine", false);
                return;
            }
        }

        ItemStack holding = MC.player.getInventory().getMainHandStack();
        int minDurability = getMinDurability();

        if (ModConfig.HANDLER.instance().autoSwitch && !AutoFix.fixing() && holding.getItem() instanceof PickaxeItem && holding.getMaxDamage() - holding.getDamage() <= minDurability) {
            int newPickaxeSlot = findNewPickaxe();
            if (newPickaxeSlot != -1) {
                InvUtils.swapToHotbar(newPickaxeSlot, MC.player.getInventory().selectedSlot);
            } else if (!ModConfig.HANDLER.instance().autoFix) {
                ModConfig.HANDLER.instance().autoMine = false;
                ModConfig.HANDLER.save();
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

        int minDurability = Math.max(ModConfig.HANDLER.instance().switchDurability, ModConfig.HANDLER.instance().toolSaver ? ModConfig.HANDLER.instance().toolSaverDurability : 0);

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
        return Math.max(ModConfig.HANDLER.instance().autoSwitch ? ModConfig.HANDLER.instance().switchDurability : -1, ModConfig.HANDLER.instance().toolSaver ? ModConfig.HANDLER.instance().toolSaverDurability : -1);
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
