package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;
import java.util.regex.Matcher;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoFix extends Feature {

    private final OptionBinding<Boolean> enabled = new OptionBinding<>("autoFix.enabled", Boolean.class, (config) -> config.autoFix.enabled, (config, value) -> config.autoFix.enabled = value);
    private final OptionBinding<ModConfig.FixMode> mode = new OptionBinding<>("autoFix.mode", ModConfig.FixMode.class, (config) -> config.autoFix.mode, (config, value) -> config.autoFix.mode = value);
    private final OptionBinding<Double> percent = new OptionBinding<>("autoFix.percent", Double.class, (config) -> config.autoFix.percent, (config, value) -> config.autoFix.percent = value);
    private final OptionBinding<Double> delay = new OptionBinding<>("autoFix.delay", Double.class, (config) -> config.autoFix.delay, (config, value) -> config.autoFix.delay = value);
    private final OptionBinding<Double> retryDelay = new OptionBinding<>("autoFix.retryDelay", Double.class, (config) -> config.autoFix.retryDelay, (config, value) -> config.autoFix.retryDelay = value);
    private final OptionBinding<Integer> maxRetries = new OptionBinding<>("autoFix.maxRetries", Integer.class, (config) -> config.autoFix.maxRetries, (config, value) -> config.autoFix.maxRetries = value);

    private boolean fixing;
    private boolean waitingForResponse;
    private boolean findMostDamaged;
    private long lastActionPerformedAt;
    private int itemPrevSlot;
    private int prevSelectedSlot;
    private int selectedSlot;
    private int tries;
    private long joinedAt;

    public AutoFix() {
        super("autoFix", "autofix", "af");
        this.enabled.addListener(this::onToggle);
        this.percent.addListener(this::onChangeMaxFixPercent);
        reset();
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, mode, percent, delay, retryDelay, maxRetries);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoFixNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.runnable("info", () -> ChatUtils.printAutoFixInfo(ModConfig.HANDLER.instance().autoFix.enabled, fixing, findMostDamaged(), delayLeft())))
                    .then(CommandHelper.runnable("reset", () -> {reset(); ChatUtils.printMessage("message.sbutils.autoFix.reset");}))
                    .then(CommandHelper.genericEnum("mode", "mode", mode))
                    .then(CommandHelper.doubl("percent", "percent", percent))
                    .then(CommandHelper.doubl("delay", "seconds", delay))
                    .then(CommandHelper.doubl("retryDelay", "seconds", retryDelay))
                    .then(CommandHelper.integer("maxRetries", "retries", maxRetries))
        );
        registerAlias(dispatcher, autoFixNode);
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoFix.enabled || SbUtils.FEATURES.get(EnchantAll.class).active() || MC.player == null)
            return;

        long currentTime = System.currentTimeMillis();

        // 10s delay needed due to chat messages being held for 10 seconds upon joining
        if (currentTime - joinedAt < 10000)
            return;

        if (findMostDamaged && !fixing) {
            itemPrevSlot = findMostDamaged();
            findMostDamaged = false;
        }

        if (waitingForResponse)
            return;

        if (tries > ModConfig.HANDLER.instance().autoFix.maxRetries) {
            ChatUtils.printWithPlaceholders("message.sbutils.autoFix.maxTriesReached", tries);
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND) {
                returnAndSwapBack();
            }
            ModConfig.HANDLER.instance().autoFix.enabled = false;
            ModConfig.HANDLER.save();
            reset();
        }

        if (delayLeft() > 0)
            return;

        doAutoFix();
    }

    private void onToggle(Boolean oldValue, Boolean newValue) {
        reset();
    }

    public void onJoinGame() {
        joinedAt = System.currentTimeMillis();
    }

    public void onDisconnect() {
        reset();
    }

    public void onUpdateInventory() {
        if (!ModConfig.HANDLER.instance().autoFix.enabled || fixing)
            return;

        findMostDamaged = true;
    }

    public void onChangeMaxFixPercent(Double oldValue, Double newValue) {
        findMostDamaged = true;
    }

    private void doAutoFix() {
        if (MC.player == null || itemPrevSlot == -1)
            return;

        if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.ALL) {
            sendFixCommand();
            return;
        }

        if (!fixing) {
            AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
            if (InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler)) {
                fixing = true;
                prevSelectedSlot = MC.player.getInventory().selected;

                if (itemPrevSlot < 9) {
                    MC.player.getInventory().selected = itemPrevSlot;
                    selectedSlot = itemPrevSlot;
                } else {
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
                    selectedSlot = MC.player.getInventory().selected;
                }

                lastActionPerformedAt = System.currentTimeMillis();
            }
            return;
        }

        if (MC.player.getInventory().selected != findMostDamaged()) {
            reset();
            return;
        }

        if (tries == 0)
            ChatUtils.printMessage("message.sbutils.autoFix.fixingItem");

        sendFixCommand();
    }

    private void onFixResponse(Component message) {
        if (!waitingForResponse)
            return;

        waitingForResponse = false;

        if (message == null) {
            return;
        }

        String messageString = message.getString();

        Matcher fixNoPermsMatcher = RegexFilters.noPermission.matcher(messageString);
        if (fixNoPermsMatcher.matches()) {
            ChatUtils.printMessage(ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND ? "message.sbutils.autoFix.noFixPermission" : "message.sbutils.autoFix.noFixAllPermission");
            ModConfig.HANDLER.instance().autoFix.enabled = false;
            ModConfig.HANDLER.save();
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND)
                returnAndSwapBack();
            reset();
            return;
        }

        Matcher fixFailMatcher = RegexFilters.fixFailFilter.matcher(messageString);
        if (fixFailMatcher.matches()) {
            String minutesText = fixFailMatcher.group(3);
            String secondsText = fixFailMatcher.group(6);
            int minutes = minutesText == null || minutesText.isEmpty() ? 0 : Integer.parseInt(minutesText);
            int seconds = secondsText == null || secondsText.isEmpty() ? 0 : Integer.parseInt(secondsText);
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND)
                returnAndSwapBack();
            reset();
            lastActionPerformedAt = calculateLastCommandSentAt((((long)minutes * 60000) + ((long)seconds * 1000) + 2000));
            return;
        }

        Matcher fixSuccessMatcher = RegexFilters.fixSuccessFilter.matcher(messageString);
        if (fixSuccessMatcher.matches()) {
            if (ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND)
                returnAndSwapBack();
            reset();
            lastActionPerformedAt = System.currentTimeMillis();
        }
    }

    private void sendFixCommand() {
        if (MC.getConnection() == null)
            return;
        String command = ModConfig.HANDLER.instance().autoFix.mode == ModConfig.FixMode.HAND ? "fix" : "fix all";
        SbUtils.COMMAND_SENDER.sendCommand(command, false, this::onFixResponse, ModConfig.HANDLER.instance().autoFix.retryDelay, RegexFilters.noPermission, RegexFilters.fixFailFilter, RegexFilters.fixSuccessFilter);
        tries++;
        lastActionPerformedAt = System.currentTimeMillis();
        waitingForResponse = true;
    }

    private void returnAndSwapBack() {
        if (MC.player == null)
            return;

        AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
        if (itemPrevSlot == -1 || (itemPrevSlot >= 9 && !InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler)))
            return;

        InvUtils.swapToHotbar(itemPrevSlot, selectedSlot, currentScreenHandler);
        MC.player.getInventory().selected = prevSelectedSlot;
    }

    private int delayLeft() {
        long delay = fixing ? 250L : delay();
        return (int)Math.max((delay - (System.currentTimeMillis() - lastActionPerformedAt)), 0L);
    }

    public boolean fixing() {
        return fixing;
    }

    private void reset() {
        fixing = false;
        waitingForResponse = false;
        findMostDamaged = true;
        lastActionPerformedAt = 0;
        itemPrevSlot = -1;
        prevSelectedSlot = 0;
        selectedSlot = 0;
        tries = 0;
    }

    private static int findMostDamaged() {
        if (MC.player == null)
            return -1;

        int result = -1;
        int mostDamage = 0;
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            if (i >= 36 && i <= 39)
                // Skip armor slots
                continue;

            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (!itemStack.isDamageableItem())
                continue;

            double maxDamage = itemStack.getMaxDamage();

            if (ModConfig.HANDLER.instance().autoFix.percent > -1 && (maxDamage - (double)itemStack.getDamageValue()) / maxDamage > ModConfig.HANDLER.instance().autoFix.percent)
                continue;

            if (itemStack.getDamageValue() > mostDamage) {
                result = i;
                mostDamage = itemStack.getDamageValue();
            }
        }
        return result;
    }

    private static int delay() {
        return (int)(ModConfig.HANDLER.instance().autoFix.delay * 1000.0) + 2000;
    }

    private static long calculateLastCommandSentAt(long timeLeft) {
        long timeElapsed = (delay() + 2000) - timeLeft;
        return System.currentTimeMillis() - timeElapsed;
    }
}
