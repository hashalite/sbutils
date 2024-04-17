package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.argument.TimeArgumentType;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.TextUtils;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoMine extends Feature {
    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoMine.enabled", Boolean.class, (config) -> config.autoMine.enabled, (config, value) -> config.autoMine.enabled = value);
    private final OptionBinding<Boolean> autoSwitch = new OptionBinding<>("autoMine.autoSwitch", Boolean.class, (config) -> config.autoMine.autoSwitch, (config, value) -> config.autoMine.autoSwitch = value);
    private final OptionBinding<Integer> switchDurability = new OptionBinding<>("autoMine.switchDurability", Integer.class, (config) -> config.autoMine.switchDurability, (config, value) -> config.autoMine.switchDurability = value);

    private long disableAt;

    public AutoMine() {
        super("autoMine", "automine", "mine");
        enabled.addListener(this::onToggle);
        reset();
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, autoSwitch, switchDurability);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.runnable("timer", this::onTimerCommand)
                            .then(ClientCommandManager.argument("duration", TimeArgumentType.time())
                                    .executes(context -> onTimerSetCommand(DoubleArgumentType.getDouble(context, "duration")))))
                    .then(CommandHelper.bool("switch", autoSwitch))
                    .then(CommandHelper.integer("durability", "durability", switchDurability))
        );
        registerAlias(dispatcher, autoMineNode);
    }

    private void onTimerCommand() {
        if (disableAt == -1) {
            ChatUtils.printMessage("message.sbutils.autoMine.timerNotSet");
            return;
        }

        long timeLeft = disableAt - System.currentTimeMillis();
        ChatUtils.printWithPlaceholders("message.sbutils.autoMine.disabledIn", Component.translatable("text.sbutils.config.category.autoMine"), TextUtils.formatTime(timeLeft));
    }

    private int onTimerSetCommand(double time) {
        disableAt = System.currentTimeMillis() + (long)(time * 1000.0);
        ModConfig.HANDLER.instance().autoMine.enabled = true;
        ChatUtils.printWithPlaceholders("message.sbutils.autoMine.enabledFor", Component.translatable("text.sbutils.config.category.autoMine"), TextUtils.formatTime(time));
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoMine.enabled || MC.player == null)
            return;

        if (disableAt != -1 && System.currentTimeMillis() >= disableAt) {
            ModConfig.HANDLER.instance().autoMine.enabled = false;
            ModConfig.HANDLER.save();
            MC.options.keyAttack.setDown(false);
            ChatUtils.printChangedSetting("text.sbutils.config.category.autoMine", false);
            disableAt = -1;
            return;
        }

        ItemStack holding = MC.player.getInventory().getSelected();
        int minDurability = getMinDurability();

        if (ModConfig.HANDLER.instance().autoMine.autoSwitch && !SbUtils.FEATURES.get(AutoFix.class).fixing() && holding.getItem() instanceof PickaxeItem && holding.getMaxDamage() - holding.getDamageValue() <= minDurability) {
            int newPickaxeSlot = findNewPickaxe();
            if (newPickaxeSlot != -1) {
                InvUtils.swapToHotbar(newPickaxeSlot, MC.player.getInventory().selected, MC.player.containerMenu);
            } else if (!ModConfig.HANDLER.instance().autoFix.enabled) {
                ModConfig.HANDLER.instance().autoMine.enabled = false;
                ModConfig.HANDLER.save();
                ChatUtils.printMessage("message.sbutils.autoMine.noPickaxe");
                return;
            }
        }

        if (!shouldMine()) {
            MC.options.keyAttack.setDown(false);
            return;
        }

        if (MC.screen == null)
            MC.options.keyAttack.setDown(true);
    }

    private void onToggle(Boolean oldValue, Boolean newValue) {
        if (!newValue)
            reset();
    }

    private void reset() {
        disableAt = -1;
    }

    public void onDisconnect() {
        reset();
    }

    private static int findNewPickaxe() {
        if (MC.player == null)
            return -1;

        int minDurability = Math.max(ModConfig.HANDLER.instance().autoMine.switchDurability, ModConfig.HANDLER.instance().toolSaver.enabled ? ModConfig.HANDLER.instance().toolSaver.durability : 0);

        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (!(itemStack.getItem() instanceof PickaxeItem))
                continue;


            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > minDurability)
                return i;
        }
        return -1;
    }

    private static int getMinDurability() {
        return Math.max(ModConfig.HANDLER.instance().autoMine.autoSwitch ? ModConfig.HANDLER.instance().autoMine.switchDurability : -1, ModConfig.HANDLER.instance().toolSaver.enabled ? ModConfig.HANDLER.instance().toolSaver.durability : -1);
    }

    public static boolean shouldMine() {
        return MC.player != null && !MC.isPaused() && !SbUtils.FEATURES.get(AutoFix.class).fixing() && !ToolSaver.shouldCancelAttack();
    }
}
