package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCrate {

    private static boolean waitingForCrate;
    private static long crateClosedAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> autoCrateNode = dispatcher.register(ClientCommandManager.literal("autocrate")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().autoCrate = !ModConfig.INSTANCE.getConfig().autoCrate;
                    ModConfig.INSTANCE.save();
                    if (!ModConfig.INSTANCE.getConfig().autoCrate)
                        reset();
                    Messenger.printChangedSetting("text.sbutils.config.category.autocrate", ModConfig.INSTANCE.getConfig().autoCrate);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("mode")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.crateMode", ModConfig.INSTANCE.getConfig().crateMode);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("voter")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().crateMode = ModConfig.CrateMode.VOTER;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.crateMode", ModConfig.CrateMode.VOTER);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("common")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().crateMode = ModConfig.CrateMode.COMMON;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.crateMode", ModConfig.CrateMode.COMMON);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("delay")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.crateDelay", ModConfig.INSTANCE.getConfig().crateDelay);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("seconds", DoubleArgumentType.doubleArg(0.0))
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().crateDelay = DoubleArgumentType.getDouble(context, "seconds");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.crateDelay", ModConfig.INSTANCE.getConfig().crateDelay);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("range")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.crateDistance", ModConfig.INSTANCE.getConfig().crateDistance);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("range", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().crateDistance = DoubleArgumentType.getDouble(context, "range");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.crateDistance", ModConfig.INSTANCE.getConfig().crateDistance);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("ac")
                .executes(context ->
                        dispatcher.execute("autocrate", context.getSource())
                )
                .redirect(autoCrateNode));
    }

    public static void tick() {
        if (!ModConfig.INSTANCE.getConfig().autoCrate || waitingForCrate || System.currentTimeMillis() - crateClosedAt < ModConfig.INSTANCE.getConfig().crateDelay * 1000 || MC.player == null) {
            return;
        }

        BlockPos cratePos = findCrate();

        if (cratePos == null || !cratePos.isWithinDistance(MC.player.getPos(), ModConfig.INSTANCE.getConfig().crateDistance)) {
            Messenger.printMessage("message.sbutils.autoCrate.crateTooFar");
            ModConfig.INSTANCE.getConfig().autoCrate = false;
            ModConfig.INSTANCE.save();
            reset();
            return;
        }

        if (MC.player.getInventory().getEmptySlot() == -1) {
            Messenger.printMessage("message.sbutils.autoCrate.inventoryFull");
            ModConfig.INSTANCE.getConfig().autoCrate = false;
            ModConfig.INSTANCE.save();
            reset();
            return;
        }

        if (!isItemKey(MC.player.getInventory().getMainHandStack()) && !moveKeysToHand()) {
            Messenger.printMessage("message.sbutils.autoCrate.finished");
            ModConfig.INSTANCE.getConfig().autoCrate = false;
            ModConfig.INSTANCE.save();
            reset();
            return;
        }

        if (useKey(cratePos)) {
            waitingForCrate = true;
        }
    }

    public static void onServerCloseScreen() {
        if (!ModConfig.INSTANCE.getConfig().autoCrate || !waitingForCrate) {
            return;
        }
        waitingForCrate = false;
        crateClosedAt = System.currentTimeMillis();
    }

    public static void onPlayerCloseScreen() {
        if (!ModConfig.INSTANCE.getConfig().autoCrate || !(MC.currentScreen instanceof GenericContainerScreen)) {
            return;
        }

        Messenger.printMessage("message.sbutils.autoCrate.closeGui");

        ModConfig.INSTANCE.getConfig().autoCrate = false;
        ModConfig.INSTANCE.save();
        reset();
    }

    private static boolean moveKeysToHand() {
        ClientPlayerEntity player = MC.player;
        if (player == null) {
            return false;
        }

        if (isItemKey(player.getInventory().getMainHandStack())) {
            return true;
        }

        int keySlot = findKeys();
        if (keySlot == -1) {
            return false;
        }

        if (!MC.player.getInventory().getMainHandStack().isEmpty()) {
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1) {
                MC.player.getInventory().selectedSlot = emptySlot;
            }
        }

        InvUtils.swapToHotbar(keySlot, MC.player.getInventory().selectedSlot);

        return true;
    }

    private static int findKeys() {
        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack item = MC.player.getInventory().getStack(i);
            if (isItemKey(item)) {
                return i;
            }
        }
        return -1;
    }

    private static BlockPos findCrate() {
        Iterable<Entity> entities = MC.world.getEntities();

        for (Entity entity : entities) {
            if (entity instanceof ArmorStandEntity && getCrateFilter().matcher(entity.getDisplayName().getString()).matches()) {
                return new BlockPos((int)Math.floor(entity.getX()), (int)Math.round(entity.getY()) - 1, (int)Math.floor(entity.getZ()));
            }
        }
        return null;
    }

    private static boolean isItemKey(ItemStack itemStack) {
        if (!itemStack.getItem().equals(Items.TRIPWIRE_HOOK) || !itemStack.hasNbt()) {
            return false;
        }

        NbtCompound itemNbt = itemStack.getNbt();
        NbtCompound displayNbt;
        if (itemNbt.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) {
            displayNbt = itemNbt.getCompound(ItemStack.DISPLAY_KEY);
        } else {
            return false;
        }

        NbtList lore;
        if (displayNbt.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE)) {
            lore = displayNbt.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        } else {
            return false;
        }

        if (lore.size() < 1) {
            return false;
        }

        MutableText loreText = Text.Serializer.fromJson(lore.getString(0));

        return loreText != null && getKeyFilter().matcher(loreText.getString()).matches();
    }

    private static boolean useKey(BlockPos cratePos) {
        if (cratePos == null || MC.interactionManager == null) {
            return false;
        }
        MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, new BlockHitResult(cratePos.toCenterPos(), Direction.UP, cratePos, false));
        return true;
    }

    private static Pattern getKeyFilter() {
        return ModConfig.INSTANCE.getConfig().crateMode == ModConfig.CrateMode.COMMON ? RegexFilters.commonKeyFilter : RegexFilters.voterKeyFilter;
    }

    private static Pattern getCrateFilter() {
        return ModConfig.INSTANCE.getConfig().crateMode == ModConfig.CrateMode.COMMON ? RegexFilters.commonCrateFilter : RegexFilters.voterCrateFilter;
    }

    public static void reset() {
        waitingForCrate = false;
        crateClosedAt = 0;
    }
}
