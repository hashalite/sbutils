package net.xolt.sbutils.feature.features;

import net.minecraft.client.player.LocalPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.systems.CommandSender;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;
//? if >=1.21 {
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
//? } else if >=1.19.4 {
/*import net.minecraft.core.registries.BuiltInRegistries;
 *///? }

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class EnchantAll extends Feature<ModConfig> {
    private static final Pattern[] enchantResponses = {RegexFilters.enchantError, RegexFilters.enchantAllSuccess, RegexFilters.enchantSingleSuccess, RegexFilters.unenchantSuccess, RegexFilters.noPermission};

    private final OptionBinding<ModConfig, ModConfig.EnchantMode> mode = new OptionBinding<>("sbutils", "enchantAll.mode", ModConfig.EnchantMode.class, (config) -> config.enchantAll.mode, (config, value) -> config.enchantAll.mode = value);
    protected final OptionBinding<ModConfig, Boolean> tpsSync = new OptionBinding<>("sbutils", "enchantAll.tpsSync", Boolean.class, (config) -> config.enchantAll.tpsSync, (config, value) -> config.enchantAll.tpsSync = value);
    protected final OptionBinding<ModConfig, Double> delay = new OptionBinding<>("sbutils", "enchantAll.delay", Double.class, (config) -> config.enchantAll.delay, (config, value) -> config.enchantAll.delay = value);
    protected final OptionBinding<ModConfig, Integer> cooldownFrequency = new OptionBinding<>("sbutils", "enchantAll.cooldownFrequency", Integer.class, (config) -> config.enchantAll.cooldownFrequency, (config, value) -> config.enchantAll.cooldownFrequency = value);
    protected final OptionBinding<ModConfig, Double> cooldownTime = new OptionBinding<>("sbutils", "enchantAll.cooldownTime", Double.class, (config) -> config.enchantAll.cooldownTime, (config, value) -> config.enchantAll.cooldownTime = value);
    private final OptionBinding<ModConfig, Boolean> excludeFrost = new OptionBinding<>("sbutils", "enchantAll.excludeFrost", Boolean.class, (config) -> config.enchantAll.excludeFrost, (config, value) -> config.enchantAll.excludeFrost = value);

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
        this("sbutils", "enchantAll", "enchall", "eall");
    }

    protected EnchantAll(String namespace, String path, String command, String commandAlias) {
        super(namespace, path, command, commandAlias);
        reset();
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(mode, tpsSync, delay, cooldownFrequency, cooldownTime, excludeFrost);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> enchantAllNode = dispatcher.register(
                CommandHelper.runnable(command, () -> onEnchantAllCommand(false, false))
                    .then(CommandHelper.runnable("inv", () -> onEnchantAllCommand(false, true)))
                    .then(CommandHelper.genericEnum("mode", "mode", mode, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("delay", "seconds", delay, ModConfig.HANDLER))
                    .then(CommandHelper.integer("cooldownFrequency", "frequency", cooldownFrequency, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("cooldownTime", "seconds", cooldownTime, ModConfig.HANDLER))
                    .then(CommandHelper.bool("excludeFrost", excludeFrost, ModConfig.HANDLER))
                    .then(CommandHelper.bool("tpsSync", tpsSync, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, enchantAllNode);
    }

    protected void onEnchantAllCommand(boolean unenchant, boolean inv) {
        if (MC.player == null)
            return;

        LocalPlayer player = MC.player;

        if (enchanting || unenchanting) {
            ChatUtils.printMessage("message.sbutils.enchantAll.pleaseWait", ChatFormatting.RED);
            return;
        }

        enchanting = !unenchant;
        unenchanting = unenchant;
        prevSelectedSlot = InvUtils.getSelectedSlot(player);
        inventory = inv;

        if (done()) {
            if (inv)
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantableItems", ChatFormatting.RED);
            else
                ChatUtils.printMessage("message.sbutils.enchantAll.itemNotEnchantable", ChatFormatting.RED);
            reset();
            return;
        }

        if (inv && !(MC.player.getMainHandItem().getItem() instanceof AirItem) && getEnchantsForItem(player.getMainHandItem(), unenchant).isEmpty()) {
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot < 8) {
                InvUtils.setSelectedSlot(player, prevSelectedSlot = enchantableSlot);
                return;
            }
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1)
                InvUtils.setSelectedSlot(player, prevSelectedSlot = emptySlot);
        }
    }

    public void tick() {
        if ((!enchanting && !unenchanting) || awaitingResponse || MC.player == null)
            return;

        LocalPlayer player = MC.player;

        if (noPermission) {
            if (ModConfig.instance().enchantAll.mode == ModConfig.EnchantMode.ALL && enchanting) {
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantAllPermission", ChatFormatting.RED);
            } else {
                ChatUtils.printMessage("message.sbutils.enchantAll.noEnchantPermission", ChatFormatting.RED);
            }
            reset();
            return;
        }

        if (InvUtils.getSelectedSlot(player) != prevSelectedSlot) {
            ChatUtils.printMessage("message.sbutils.enchantAll.cancelSlotSwitch", ChatFormatting.RED);
            reset();
            return;
        }

        if (done()) {
            if (inventory && itemPrevSlot != -1) {
                AbstractContainerMenu currentScreenHandler = MC.player.containerMenu;
                if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                    return;
                InvUtils.swapToHotbar(itemPrevSlot, InvUtils.getSelectedSlot(player), currentScreenHandler);
            }
            ChatUtils.printMessage("message.sbutils.enchantAll.complete");
            reset();
            return;
        }

        if (commandCount >= ModConfig.instance().enchantAll.cooldownFrequency) {
            cooldown = true;
            ChatUtils.printWithPlaceholders("message.sbutils.enchantAll.cooldown", ModConfig.instance().enchantAll.cooldownTime);
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

        String messageString = response.getString();
        if (RegexFilters.noPermission.matcher(messageString).matches()) {
            noPermission = true;
        }
    }

    private void onEnchantCommandTimeout() {
        reset();
    }

    private void afterSendCommand() {
        commandCount++;
        lastActionPerformedAt = System.currentTimeMillis();
        awaitingResponse = true;
    }

    private void doEnchantInv(boolean unenchant) {
        if (MC.player == null)
            return;

        LocalPlayer player = MC.player;
        ItemStack hand = player.getMainHandItem();
        List<Enchantment> enchants = getEnchantsForItem(hand, unenchant);

        if (enchants.isEmpty()) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(InvUtils.getEnchantment(Enchantments.FROST_WALKER), true);
                afterSendCommand();
                return;
            }
            int enchantableSlot = findEnchantableSlot(unenchant);
            if (enchantableSlot != -1) {
                AbstractContainerMenu currentScreenHandler = player.containerMenu;
                if (itemPrevSlot != -1) {
                    if (!InvUtils.canSwapSlot(itemPrevSlot, currentScreenHandler))
                        return;
                    InvUtils.swapToHotbar(itemPrevSlot, InvUtils.getSelectedSlot(player), currentScreenHandler);
                    itemPrevSlot = -1;
                }
                if (!InvUtils.canSwapSlot(enchantableSlot, currentScreenHandler))
                    return;
                InvUtils.swapToHotbar(enchantableSlot, InvUtils.getSelectedSlot(player), currentScreenHandler);
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
        List<Enchantment> enchants = getEnchantsForItem(hand, unenchant);

        if (enchants.isEmpty()) {
            if (shouldRemoveFrost()) {
                sendEnchantCommand(InvUtils.getEnchantment(Enchantments.FROST_WALKER), true);
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
            delay = (long)(ModConfig.instance().enchantAll.cooldownTime * 1000.0);
        else if (pause)
            delay = 250L;
        else
            delay = (long)(ModConfig.instance().enchantAll.delay * 1000.0);

        if (ModConfig.instance().enchantAll.tpsSync)
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
        if (!unenchant && ModConfig.instance().enchantAll.mode == ModConfig.EnchantMode.ALL) {
            sendEnchantAllCommand();
            return;
        }

        sendEnchantCommand(enchants.iterator().next(), unenchant);
    }

    private void sendEnchantCommand(Enchantment enchantment, boolean unenchant) {
        Identifier enchant =
            //? if >=1.19.4 {
                //? if >=1.21 {
                MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                //? } else
                //BuiltInRegistries.ENCHANTMENT
                        .getKey(enchantment);
            //? } else
                //Registry.ENCHANTMENT.getKey(enchantment);
        if (enchant == null || MC.getConnection() == null)
            return;
        String enchantName = enchant.getPath().replaceAll("_", "");
        SbUtils.COMMAND_SENDER.sendCommand("enchant " + enchantName + " " + (unenchant ? 0 : enchantment.getMaxLevel()), this::onEnchantCommandTimeout, new CommandSender.CommandResponseMatcher(this::onEnchantResponse, enchantResponses));
    }

    private void sendEnchantAllCommand() {
        if (MC.getConnection() == null)
            return;

        SbUtils.COMMAND_SENDER.sendCommand("enchantall", this::onEnchantCommandTimeout, new CommandSender.CommandResponseMatcher(this::onEnchantResponse, enchantResponses));
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

        if (!itemStack.
                //? if >=1.21 {
                has(DataComponents.ENCHANTABLE)
                //? } else
                //getItem().isEnchantable(itemStack)
        )
            return new ArrayList<>();

        Map<Enchantment, Integer> itemsEnchants = InvUtils.getEnchantments(itemStack);
        List<Enchantment> enchantments = new ArrayList<>();
        assert MC.level != null;
        Registry<Enchantment> enchantmentRegistry =
                //? if >=1.21 {
                MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                 //? } else if >=1.19.4 {
                /*BuiltInRegistries.ENCHANTMENT;
                 *///? } else
                //Registry.ENCHANTMENT;
        if (!unenchant) {
            for (Enchantment enchantment : enchantmentRegistry) {
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

        enchantments.remove(InvUtils.getEnchantment(Enchantments.SILK_TOUCH));
        enchantments.remove(InvUtils.getEnchantment(Enchantments.BINDING_CURSE));
        enchantments.remove(InvUtils.getEnchantment(Enchantments.VANISHING_CURSE));

        if (ModConfig.instance().enchantAll.excludeFrost && !unenchant)
            enchantments.remove(InvUtils.getEnchantment(Enchantments.FROST_WALKER));

        return enchantments;
    }

    private static boolean shouldRemoveFrost() {
        if (MC.player == null)
            return false;

        return ModConfig.instance().enchantAll.excludeFrost &&
                InvUtils.getEnchantments(MC.player.getMainHandItem()).containsKey(InvUtils.getEnchantment(Enchantments.FROST_WALKER));
    }
}
