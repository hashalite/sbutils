package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.common.InvCleaner;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.Messenger;
import net.xolt.sbutils.util.RegexFilters;

import java.util.List;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCrate {

    private static final String COMMAND = "autocrate";
    private static final String ALIAS = "ac";

    private static boolean waitingForCrate;
    private static long crateClosedAt;
    private static boolean cleaning;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> autoCrateNode = dispatcher.register(
                CommandHelper.toggle(COMMAND, "autoCrate", () -> ModConfig.HANDLER.instance().autoCrate.enabled, (value) -> {ModConfig.HANDLER.instance().autoCrate.enabled = value; if (!value) reset();})
                    .then(CommandHelper.genericEnum("mode", "mode", "autoCrate.mode", ModConfig.Crate.class, () -> ModConfig.HANDLER.instance().autoCrate.mode, (value) -> ModConfig.HANDLER.instance().autoCrate.mode = value))
                    .then(CommandHelper.doubl("delay", "seconds", "autoCrate.delay", () -> ModConfig.HANDLER.instance().autoCrate.delay, (value) -> ModConfig.HANDLER.instance().autoCrate.delay = value, 0.0))
                    .then(CommandHelper.doubl("range", "range", "autoCrate.distance", () -> ModConfig.HANDLER.instance().autoCrate.distance, (value) -> ModConfig.HANDLER.instance().autoCrate.distance = value))
                    .then(CommandHelper.bool("cleaner", "autoCrate.cleaner", () -> ModConfig.HANDLER.instance().autoCrate.cleaner, (value) -> ModConfig.HANDLER.instance().autoCrate.cleaner = value)
                            .then(CommandHelper.stringList("items", "item", "autoCrate.itemsToClean", () -> ModConfig.HANDLER.instance().autoCrate.itemsToClean)))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(autoCrateNode));
    }

    public static void tick() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || waitingForCrate || System.currentTimeMillis() - crateClosedAt < ModConfig.HANDLER.instance().autoCrate.delay * 1000 || MC.player == null || cleaning) {
            return;
        }

        BlockPos cratePos = findCrate();

        if (cratePos == null || !cratePos.isWithinDistance(MC.player.getPos(), ModConfig.HANDLER.instance().autoCrate.distance)) {
            Messenger.printMessage("message.sbutils.autoCrate.crateTooFar");
            disable();
            return;
        }

        if (MC.player.getInventory().getEmptySlot() == -1) {
            if (ModConfig.HANDLER.instance().autoCrate.cleaner) {
                // cleaning must be set before clean() is called, in case callback is called immediately
                cleaning = true;
                InvCleaner.clean(getItemsToClean(), AutoCrate::onCleaningCallback);
                return;
            }
            Messenger.printMessage("message.sbutils.autoCrate.inventoryFull");
            disable();
            return;
        }

        if (!isItemKey(MC.player.getInventory().getMainHandStack()) && !moveKeysToHand()) {
            Messenger.printMessage("message.sbutils.autoCrate.finished");
            disable();
            return;
        }

        if (useKey(cratePos)) {
            waitingForCrate = true;
        }
    }

    public static void onCleaningCallback(boolean result) {
        cleaning = false;
        if (result)
            return;
        Messenger.printInvCleanFailed("text.sbutils.config.category.autoCrate");
        disable();
    }

    public static void onServerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || !waitingForCrate) {
            return;
        }
        waitingForCrate = false;
        crateClosedAt = System.currentTimeMillis();
    }

    public static void onPlayerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || !(MC.currentScreen instanceof GenericContainerScreen) || cleaning) {
            return;
        }

        Messenger.printMessage("message.sbutils.autoCrate.closeGui");

        ModConfig.HANDLER.instance().autoCrate.enabled = false;
        ModConfig.HANDLER.save();
        reset();
    }

    private static List<Item> getItemsToClean() {
        return ModConfig.HANDLER.instance().autoCrate.itemsToClean.stream().map((item) -> Registries.ITEM.get(new Identifier(item))).toList();
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

        MutableText loreText = Text.Serialization.fromJson(lore.getString(0));

        return loreText != null && getKeyFilter().matcher(loreText.getString()).matches();
    }

    private static boolean useKey(BlockPos cratePos) {
        if (cratePos == null || MC.interactionManager == null || MC.getNetworkHandler() == null) {
            return false;
        }

        MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, new BlockHitResult(cratePos.toCenterPos(), Direction.UP, cratePos, false));
        return true;
    }

    private static Pattern getKeyFilter() {
        switch(ModConfig.HANDLER.instance().autoCrate.mode) {
            case COMMON:
                return RegexFilters.commonKeyFilter;
            case RARE:
                return RegexFilters.rareKeyFilter;
            case EPIC:
                return RegexFilters.epicKeyFilter;
            case LEGENDARY:
                return RegexFilters.legendaryKeyFilter;
            default:
                return RegexFilters.voterKeyFilter;
        }
    }

    private static Pattern getCrateFilter() {
        switch(ModConfig.HANDLER.instance().autoCrate.mode) {
            case COMMON:
                return RegexFilters.commonCrateFilter;
            case RARE:
                return RegexFilters.rareCrateFilter;
            case EPIC:
                return RegexFilters.epicCrateFilter;
            case LEGENDARY:
                return RegexFilters.legendaryCrateFilter;
            default:
                return RegexFilters.voterCrateFilter;
        }
    }

    private static void disable() {
        ModConfig.HANDLER.instance().autoCrate.enabled = false;
        ModConfig.HANDLER.save();
        reset();
    }

    public static void reset() {
        waitingForCrate = false;
        crateClosedAt = 0;
        cleaning = false;
    }
}
