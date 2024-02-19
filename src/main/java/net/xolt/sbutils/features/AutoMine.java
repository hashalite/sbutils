package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.argument.TimeArgumentType;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoMine {

    private static final String COMMAND = "automine";
    private static final String ALIAS = "mine";
    private static long disableAt = -1;


    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoMine", () -> ModConfig.HANDLER.instance().autoMine.enabled, (value) -> {ModConfig.HANDLER.instance().autoMine.enabled = value; if (!value) reset();})
                    .then(CommandHelper.runnable("timer", () -> Messenger.printAutoMineTime(disableAt))
                            .then(ClientCommandManager.argument("duration", TimeArgumentType.time())
                                    .executes(context -> onTimerCommand(DoubleArgumentType.getDouble(context, "duration")))))
                    .then(CommandHelper.bool("switch", "autoMine.autoSwitch", () -> ModConfig.HANDLER.instance().autoMine.autoSwitch, (value) -> ModConfig.HANDLER.instance().autoMine.autoSwitch = value))
                    .then(CommandHelper.integer("durability", "durability", "autoMine.switchDurability", () -> ModConfig.HANDLER.instance().autoMine.switchDurability, (value) -> ModConfig.HANDLER.instance().autoMine.switchDurability = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource()))
                .redirect(autoMineNode));
    }

    private static int onTimerCommand(double time) {
        disableAt = System.currentTimeMillis() + (long)(time * 1000.0);
        ModConfig.HANDLER.instance().autoMine.enabled = true;
        Messenger.printAutoMineEnabledFor(disableAt);
        return Command.SINGLE_SUCCESS;
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoMine.enabled || MC.player == null) {
            return;
        }

        if (disableAt != -1 && System.currentTimeMillis() >= disableAt) {
            ModConfig.HANDLER.instance().autoMine.enabled = false;
            ModConfig.HANDLER.save();
            MC.options.attackKey.setPressed(false);
            Messenger.printChangedSetting("text.sbutils.config.category.autoMine", false);
            disableAt = -1;
            return;
        }

        ItemStack holding = MC.player.getInventory().getMainHandStack();
        int minDurability = getMinDurability();

        if (ModConfig.HANDLER.instance().autoMine.autoSwitch && !AutoFix.fixing() && holding.getItem() instanceof PickaxeItem && holding.getMaxDamage() - holding.getDamage() <= minDurability) {
            int newPickaxeSlot = findNewPickaxe();
            if (newPickaxeSlot != -1) {
                InvUtils.swapToHotbar(newPickaxeSlot, MC.player.getInventory().selectedSlot, MC.player.currentScreenHandler);
            } else if (!ModConfig.HANDLER.instance().autoFix.enabled) {
                ModConfig.HANDLER.instance().autoMine.enabled = false;
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

        int minDurability = Math.max(ModConfig.HANDLER.instance().autoMine.switchDurability, ModConfig.HANDLER.instance().toolSaver.enabled ? ModConfig.HANDLER.instance().toolSaver.durability : 0);

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
        return Math.max(ModConfig.HANDLER.instance().autoMine.autoSwitch ? ModConfig.HANDLER.instance().autoMine.switchDurability : -1, ModConfig.HANDLER.instance().toolSaver.enabled ? ModConfig.HANDLER.instance().toolSaver.durability : -1);
    }

    private static void reset() {
        disableAt = -1;
    }

    public static void onDisconnect() {
        reset();
    }

    public static boolean shouldMine() {
        return MC.player != null && !MC.isPaused() && !AutoFix.fixing() && !ToolSaver.shouldCancelAttack();
    }
}
