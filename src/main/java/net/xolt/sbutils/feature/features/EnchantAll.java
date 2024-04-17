package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class EnchantAll extends Feature {
    private static final Pattern[] enchantResponses = {RegexFilters.enchantError, RegexFilters.enchantAllSuccess, RegexFilters.enchantSingleSuccess, RegexFilters.unenchantSuccess, RegexFilters.noPermission};

    private final OptionBinding<ModConfig.EnchantMode> mode = new OptionBinding<>("enchantAll.mode", ModConfig.EnchantMode.class, (config) -> config.enchantAll.mode, (config, value) -> config.enchantAll.mode = value);
    protected final OptionBinding<Boolean> tpsSync = new OptionBinding<>("enchantAll.tpsSync", Boolean.class, (config) -> config.enchantAll.tpsSync, (config, value) -> config.enchantAll.tpsSync = value);
    protected final OptionBinding<Double> delay = new OptionBinding<>("enchantAll.delay", Double.class, (config) -> config.enchantAll.delay, (config, value) -> config.enchantAll.delay = value);
    protected final OptionBinding<Integer> cooldownFrequency = new OptionBinding<>("enchantAll.cooldownFrequency", Integer.class, (config) -> config.enchantAll.cooldownFrequency, (config, value) -> config.enchantAll.cooldownFrequency = value);
    protected final OptionBinding<Double> cooldownTime = new OptionBinding<>("enchantAll.cooldownTime", Double.class, (config) -> config.enchantAll.cooldownTime, (config, value) -> config.enchantAll.cooldownTime = value);
    private final OptionBinding<Boolean> excludeFrost = new OptionBinding<>("enchantAll.excludeFrost", Boolean.class, (config) -> config.enchantAll.excludeFrost, (config, value) -> config.enchantAll.excludeFrost = value);

    private boolean enchanting;
    private boolean unenchanting;
    private boolean inventory;
    private boolean awaitingResponse;
    private boolean noPermission;
    private boolean pause;
    private boolean cooldown;
    private int prevSelectedSlot;
    private int itemPrevSlot;
    private int commandCount;
    private long lastActionPerformedAt;

    public EnchantAll() {
        this("enchantAll", "enchall", "eall");
    }

    protected EnchantAll(String path, String command, String commandAlias) {
        super(path, command, commandAlias);
        reset();
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(mode, tpsSync, delay, cooldownFrequency, cooldownTime, excludeFrost);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> enchantAllNode = dispatcher.register(
                CommandHelper.runnable(command, () -> onEnchantAllCommand(false, false))
                    .then(CommandHelper.runnable("inv", () -> onEnchantAllCommand(false, true)))
                    .then(CommandHelper.genericEnum("mode", "mode", mode))
                    .then(CommandHelper.doubl("delay", "seconds", delay))
                    .then(CommandHelper.integer("cooldownFrequency", "frequency", cooldownFrequency))
                    .then(CommandHelper.doubl("cooldownTime", "seconds", cooldownTime))
                    .then(CommandHelper.bool("excludeFrost", excludeFrost))
                    .then(CommandHelper.bool("tpsSync", tpsSync))
        );
        registerAlias(dispatcher, enchantAllNode);
    }

    protected void onEnchantAllCommand(boolean unenchant, boolean inv) {
        if (MC.player == null)
            return;

        if (enchanting || unenchanting) {
            ChatUtils.printMessage("message.sbutils.enchantAll.pleaseWait", ChatFormatting.RED);
            return;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;
        prevSelectedSlot = MC.player.getInventory().selected;
        inventory = inv;

        if (done()) {
            if (inv)
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantableItems", ChatFormatting.RED);
            else
                ChatUtils.printMessage("message.sbutils.enchantAll.itemNotEnchantable", ChatFormatting.RED);
            reset();
            return;
        }

        if (inv && !(MC.player.getMainHandItem().getItem() instanceof AirItem) && getEnchantsForItem(MC.player.getMainHandItem(), unenchant).isEmpty()) {
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot < 8) {
                MC.player.getInventory().selected = prevSelectedSlot = enchantableSlot;
                return;
            }
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1)
                MC.player.getInventory().selected = prevSelectedSlot = emptySlot;
        }
    }

    public void tick() {
        if ((!enchanting && !unenchanting) || awaitingResponse || MC.player == null)
            return;

        if (noPermission) {
            if (ModConfig.HANDLER.instance().enchantAll.mode == ModConfig.EnchantMode.ALL && enchanting) {
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantAllPermission", ChatFormatting.RED);
            } else {
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantPermission", ChatFormatting.RED);
            }
            reset();
            return;
        }

        if (MC.player.getInventory().selected != prevSelectedSlot) {
            ChatUtils.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", ChatFormatting.RED);
            reset();
            return;
        }

        if (done()) {
            if (inventory && itemPrevSlot != -1) {
                AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
                if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                    return;
                InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
            }
            ChatUtils.printMessage("message.sbutils.enchantAll.complete");
            reset();
            return;
        }

        if (commandCount >= ModConfig.HANDLER.instance().enchantAll.cooldownFrequency) {
            cooldown = true;
            ChatUtils.printWithPlaceholders("message.sbutils.enchantAll.cooldown", ModConfig.HANDLER.instance().enchantAll.cooldownTime);
            commandCount = 0;
        }

        if (delayLeft() > 0)
            return;

        if (pause || cooldown) {
            pause = false;
            cooldown = false;
            return;
        }

        if (inventory)
            doEnchantInv(unenchanting);
        else
            doEnchant(unenchanting);
    }

    public void onDisconnect() {
        reset();
    }

    private void onEnchantResponse(Component response) {
        if ((!enchanting && !unenchanting) || !awaitingResponse)
            return;

        awaitingResponse = false;

        if (response == null) {
            reset();
            return;
        }

        String messageString = response.getString();
        if (RegexFilters.noPermission.matcher(messageString).matches()) {
            noPermission = true;
        }
    }

    private void afterSendCommand() {
        commandCount++;
        lastActionPerformedAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    private void doEnchantInv(boolean unenchant) {
        if (MC.player == null)
            return;

        ItemStack hand = MC.player.getMainHandItem();
        List<Enchantment> enchants = getEnchantsForItem(hand, unenchant);

        if (enchants.isEmpty()) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(Enchantments.FROST_WALKER, true);
                afterSendCommand();
                return;
            }
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot != -1) {
                AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
                if (itemPrevSlot != -1) {
                    if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                        return;
                    InvUtils.swapToHotbar(itemPrevSlot, MC.player.getInventory().selected, currentScreenHandler);
                    itemPrevSlot = -1;
                }
                if (!InvUtils.canSwapSlot(enchantableSlot, currentScreenHandler))
                    return;
                InvUtils.swapToHotbar(enchantableSlot, MC.player.getInventory().selected, currentScreenHandler);
                itemPrevSlot = enchantableSlot;
                pause = true;
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
        afterSendCommand();
    }

    private void doEnchant(boolean unenchant) {
        if (MC.player == null)
            return;

        ItemStack hand = MC.player.getMainHandItem();

        if (!hand.getItem().isEnchantable(hand))
            return;

        List<Enchantment> enchants = getEnchantsForItem(MC.player.getMainHandItem(), unenchant);

        if (enchants.isEmpty()) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(Enchantments.FROST_WALKER, true);
                afterSendCommand();
            }
            return;
        }

        sendNextEnchant(enchants, unenchant);
        afterSendCommand();
    }

    private int delayLeft() {
        long delay;
        if (cooldown)
            delay = (long)(ModConfig.HANDLER.instance().enchantAll.cooldownTime * 1000.0);
        else if (pause)
            delay = 250L;
        else
            delay = (long)(ModConfig.HANDLER.instance().enchantAll.delay * 1000.0);

        if (ModConfig.HANDLER.instance().enchantAll.tpsSync)
            delay = (int)((double)delay / (SbUtils.TPS_ESTIMATOR.getCappedTickRate() / 20.0));

        return (int)Math.max(delay - (System.currentTimeMillis() - lastActionPerformedAt), 0L);
    }

    private boolean done() {
        if (MC.player == null)
            return true;

        ItemStack hand = MC.player.getMainHandItem();

        return ((inventory && findEnchantableSlot(unenchanting) == -1) ||
                (!inventory && getEnchantsForItem(hand, unenchanting).isEmpty())) &&
                !shouldRemoveFrost();
    }

    private void reset() {
        enchanting = false;
        unenchanting = false;
        inventory = false;
        awaitingResponse = false;
        noPermission = false;
        pause = false;
        cooldown = false;
        prevSelectedSlot = -1;
        itemPrevSlot = -1;
        commandCount = 0;
    }

    public boolean active() {
        return enchanting || unenchanting;
    }

    private void sendNextEnchant(List<Enchantment> enchants, boolean unenchant) {
        if (!unenchant && ModConfig.HANDLER.instance().enchantAll.mode == ModConfig.EnchantMode.ALL) {
            sendEnchantAllCommand();
            return;
        }

        sendEnchantCommand(enchants.iterator().next(), unenchant);
    }

    private void sendEnchantCommand(Enchantment enchantment, boolean unenchant) {
        ResourceLocation enchant = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (enchant == null || MC.getConnection() == null)
            return;
        String enchantName = enchant.getPath().replaceAll("_", "");
        SbUtils.COMMAND_SENDER.sendCommand("enchant " + enchantName + " " + (unenchant ? 0 : enchantment.getMaxLevel()), false, this::onEnchantResponse, enchantResponses);
    }

    private void sendEnchantAllCommand() {
        if (MC.getConnection() == null)
            return;

        SbUtils.COMMAND_SENDER.sendCommand("enchantall", false, this::onEnchantResponse, enchantResponses);
    }

    private static int findEnchantableSlot(boolean unenchant) {
        if (MC.player == null)
            return -1;

        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            if (i >= 36 && i <= 39) {
                // Skip armor slots
                continue;
            }
            ItemStack itemStack = MC.player.getInventory().getItem(i);
            if (!getEnchantsForItem(itemStack, unenchant).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private static List<Enchantment> getEnchantsForItem(ItemStack itemStack, boolean unenchant) {
        Item item = itemStack.getItem();

        if (!itemStack.getItem().isEnchantable(itemStack))
            return new ArrayList<>();

        Map<Enchantment, Integer> itemsEnchants = EnchantmentHelper.deserializeEnchantments(itemStack.getEnchantmentTags());
        List<Enchantment> enchantments = new ArrayList<>();
        if (!unenchant) {
            for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
                if (enchantment.canEnchant(new ItemStack(item)))
                    enchantments.add(enchantment);
            }

            for (Enchantment enchantment : itemsEnchants.keySet()) {
                if (enchantment.getMaxLevel() == itemsEnchants.get(enchantment))
                    enchantments.remove(enchantment);
            }

        } else {
            enchantments.addAll(itemsEnchants.keySet());
        }

        enchantments.remove(Enchantments.SILK_TOUCH);
        enchantments.remove(Enchantments.BINDING_CURSE);
        enchantments.remove(Enchantments.VANISHING_CURSE);

        if (ModConfig.HANDLER.instance().enchantAll.excludeFrost && !unenchant)
            enchantments.remove(Enchantments.FROST_WALKER);

        return enchantments;
    }

    private static boolean shouldRemoveFrost() {
        if (MC.player == null)
            return false;

        return ModConfig.HANDLER.instance().enchantAll.excludeFrost &&
                EnchantmentHelper.deserializeEnchantments(MC.player.getMainHandItem().getEnchantmentTags()).containsKey(Enchantments.FROST_WALKER);
    }
}
