package net.xolt.sbutils.feature.features;

import net.minecraft.client.player.LocalPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
//? if >=1.21.11 {
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
//? } else {
/*import net.minecraft.world.item.PickaxeItem;
 *///? }

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoMine extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "autoMine.enabled", Boolean.class, (config) -> config.autoMine.enabled, (config, value) -> config.autoMine.enabled = value);
    private final OptionBinding<ModConfig, Boolean> autoSwitch = new OptionBinding<>("sbutils", "autoMine.autoSwitch", Boolean.class, (config) -> config.autoMine.autoSwitch, (config, value) -> config.autoMine.autoSwitch = value);
    private final OptionBinding<ModConfig, Integer> switchDurability = new OptionBinding<>("sbutils", "autoMine.switchDurability", Integer.class, (config) -> config.autoMine.switchDurability, (config, value) -> config.autoMine.switchDurability = value);

    private long disableAt;

    public AutoMine() {
        super("sbutils", "autoMine", "automine", "mine");
        enabled.addListener(this::onToggle);
        reset();
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, autoSwitch, switchDurability);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoMineNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.runnable("timer", this::onTimerCommand)
                            .then(ClientCommandManager.argument("duration", TimeArgumentType.time())
                                    .executes(context -> onTimerSetCommand(DoubleArgumentType.getDouble(context, "duration")))))
                    .then(CommandHelper.bool("switch", autoSwitch, ModConfig.HANDLER))
                    .then(CommandHelper.integer("durability", "durability", switchDurability, ModConfig.HANDLER))
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
        ModConfig.instance().autoMine.enabled = true;
        ChatUtils.printWithPlaceholders("message.sbutils.autoMine.enabledFor", Component.translatable("text.sbutils.config.category.autoMine"), TextUtils.formatTime(time));
        return Command.SINGLE_SUCCESS;
    }

    public void tick() {
        if (!ModConfig.instance().autoMine.enabled || MC.player == null)
            return;

        LocalPlayer player = MC.player;

        if (disableAt != -1 && System.currentTimeMillis() >= disableAt) {
            ModConfig.instance().autoMine.enabled = false;
            ModConfig.HANDLER.save();
            MC.options.keyAttack.setDown(false);
            ChatUtils.printChangedSetting("text.sbutils.config.category.autoMine", false);
            disableAt = -1;
            return;
        }

        ItemStack holding = player.getInventory()
                //? if >=1.21.11 {
                .getSelectedItem();
                //? } else
                //.getSelected();
        int minDurability = getMinDurability();

        if (ModConfig.instance().autoMine.autoSwitch && !SbUtils.FEATURES.get(AutoFix.class).fixing() && isPickaxe(holding.getItem()) && holding.getMaxDamage() - holding.getDamageValue() <= minDurability) {
            int newPickaxeSlot = findNewPickaxe();
            if (newPickaxeSlot != -1) {
                InvUtils.swapToHotbar(newPickaxeSlot, InvUtils.getSelectedSlot(player), player.containerMenu);
            } else if (!ModConfig.instance().autoFix.enabled) {
                ModConfig.instance().autoMine.enabled = false;
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

        int minDurability = Math.max(ModConfig.instance().autoMine.switchDurability, ModConfig.instance().toolSaver.enabled ? ModConfig.instance().toolSaver.durability : 0);

        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (!isPickaxe(itemStack.getItem()))
                continue;


            if (itemStack.getMaxDamage() - itemStack.getDamageValue() > minDurability)
                return i;
        }
        return -1;
    }

    private static int getMinDurability() {
        return Math.max(ModConfig.instance().autoMine.autoSwitch ? ModConfig.instance().autoMine.switchDurability : -1, ModConfig.instance().toolSaver.enabled ? ModConfig.instance().toolSaver.durability : -1);
    }

    public static boolean shouldMine() {
        return MC.player != null && !MC.isPaused() && !SbUtils.FEATURES.get(AutoFix.class).fixing() && !ToolSaver.shouldCancelAttack();
    }

    public static boolean isPickaxe(Item item) {
        //? if >=1.21.11 {
        Tool tool = item.components().get(DataComponents.TOOL);
        if (tool == null)
            return false;
        HolderGetter<Block> holderGetter = BuiltInRegistries.BLOCK;
        return tool.rules().stream().anyMatch((rule) -> rule.blocks().equals(holderGetter.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE)));
        //? } else
        //return item instanceof PickaxeItem;
    }
}
