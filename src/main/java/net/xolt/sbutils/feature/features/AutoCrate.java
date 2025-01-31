package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import net.xolt.sbutils.util.RegexFilters;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class AutoCrate extends Feature<ModConfig> {

    private final OptionBinding<ModConfig, Boolean> enabled = new OptionBinding<>("sbutils", "autoCrate.enabled", Boolean.class, (config) -> config.autoCrate.enabled, (config, value) -> config.autoCrate.enabled = value);
    private final OptionBinding<ModConfig, ModConfig.Crate> mode = new OptionBinding<>("sbutils", "autoCrate.mode", ModConfig.Crate.class, (config) -> config.autoCrate.mode, (config, value) -> config.autoCrate.mode = value);
    private final OptionBinding<ModConfig, Double> delay = new OptionBinding<>("sbutils", "autoCrate.delay", Double.class, (config) -> config.autoCrate.delay, (config, value) -> config.autoCrate.delay = value);
    private final OptionBinding<ModConfig, Double> distance = new OptionBinding<>("sbutils", "autoCrate.distance", Double.class, (config) -> config.autoCrate.distance, (config, value) -> config.autoCrate.distance = value);
    private final OptionBinding<ModConfig, Boolean> cleaner = new OptionBinding<>("sbutils", "autoCrate.cleaner", Boolean.class, (config) -> config.autoCrate.cleaner, (config, value) -> config.autoCrate.cleaner = value);
    private final ListOptionBinding<ModConfig, String> itemsToClean = new ListOptionBinding<>("sbutils", "autoCrate.itemsToClean", "", String.class, (config) -> config.autoCrate.itemsToClean, (config, value) -> config.autoCrate.itemsToClean = value, new ListConstraints<>(false, null, new StringConstraints(false)));

    private boolean waitingForCrate;
    private long crateClosedAt;
    private boolean cleaning;

    public AutoCrate() {
        super("sbutils", "autoCrate", "autocrate", "ac");
        enabled.addListener(this::onToggle);
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(enabled, mode, delay, distance, cleaner, itemsToClean);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> autoCrateNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled, ModConfig.HANDLER)
                    .then(CommandHelper.genericEnum("mode", "mode", mode, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("delay", "seconds", delay, ModConfig.HANDLER))
                    .then(CommandHelper.doubl("range", "range", distance, ModConfig.HANDLER))
                    .then(CommandHelper.bool("cleaner", cleaner, ModConfig.HANDLER)
                            .then(CommandHelper.stringList("items", "item", itemsToClean, ModConfig.HANDLER)))
        );
        registerAlias(dispatcher, autoCrateNode);
    }

    public void tick() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || waitingForCrate || System.currentTimeMillis() - crateClosedAt < ModConfig.HANDLER.instance().autoCrate.delay * 1000 || MC.player == null || cleaning) {
            return;
        }

        BlockPos cratePos = findCrate();

        if (cratePos == null || !cratePos.closerToCenterThan(MC.player.position(), ModConfig.HANDLER.instance().autoCrate.distance)) {
            ChatUtils.printMessage("message.sbutils.autoCrate.crateTooFar");
            disable();
            return;
        }

        if (MC.player.getInventory().getFreeSlot() == -1) {
            if (ModConfig.HANDLER.instance().autoCrate.cleaner) {
                // cleaning must be set before clean() is called, in case callback is called immediately
                cleaning = true;
                SbUtils.FEATURES.get(InvCleaner.class).clean(ModConfig.HANDLER.instance().autoCrate.itemsToClean, this::onCleaningCallback);
                return;
            }
            ChatUtils.printMessage("message.sbutils.autoCrate.inventoryFull");
            disable();
            return;
        }

        if (!isItemKey(MC.player.getInventory().getSelected()) && !moveKeysToHand()) {
            ChatUtils.printMessage("message.sbutils.autoCrate.finished");
            disable();
            return;
        }

        if (useKey(cratePos)) {
            waitingForCrate = true;
        }
    }

    private void onToggle(Boolean oldValue, Boolean newValue) {
        if (!oldValue)
            reset();
    }

    public void onCleaningCallback(boolean result) {
        cleaning = false;
        if (result)
            return;
        InvCleaner.showCleanFailedCritical("text.sbutils.config.category.autoCrate");
        disable();
    }

    public void onServerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || !waitingForCrate) {
            return;
        }
        waitingForCrate = false;
        crateClosedAt = System.currentTimeMillis();
    }

    public void onPlayerCloseScreen() {
        if (!ModConfig.HANDLER.instance().autoCrate.enabled || !(MC.screen instanceof ContainerScreen) || cleaning) {
            return;
        }

        ChatUtils.printMessage("message.sbutils.autoCrate.closeGui");

        disable();
    }

    private void disable() {
        enabled.set(ModConfig.HANDLER.instance(), false);
        ModConfig.HANDLER.save();
    }

    public void reset() {
        waitingForCrate = false;
        crateClosedAt = 0;
        cleaning = false;
    }

    private static boolean moveKeysToHand() {
        LocalPlayer player = MC.player;
        if (player == null) {
            return false;
        }

        if (isItemKey(player.getInventory().getSelected())) {
            return true;
        }

        int keySlot = findKeys();
        if (keySlot == -1) {
            return false;
        }

        // If there is a key in the hotbar, swap to it
        if (keySlot < 9) {
            player.getInventory().selected = keySlot;
            return true;
        }

        if (!player.getInventory().getSelected().isEmpty()) {
            int emptySlot = InvUtils.findEmptyHotbarSlot();
            if (emptySlot != -1) {
                player.getInventory().selected = emptySlot;
            }
        }

        InvUtils.swapToHotbar(keySlot, player.getInventory().selected, player.containerMenu);

        return true;
    }

    private static int findKeys() {
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack item = MC.player.getInventory().getItem(i);
            if (isItemKey(item)) {
                return i;
            }
        }
        return -1;
    }

    private static BlockPos findCrate() {
        Iterable<Entity> entities = MC.level.entitiesForRendering();

        for (Entity entity : entities) {
            if (entity instanceof ArmorStand && getCrateFilter().matcher(entity.getDisplayName().getString()).matches()) {
                return new BlockPos((int)Math.floor(entity.getX()), (int)Math.round(entity.getY()) - 1, (int)Math.floor(entity.getZ()));
            }
        }
        return null;
    }

    private static boolean isItemKey(ItemStack itemStack) {
        if (!itemStack.getItem().equals(Items.TRIPWIRE_HOOK) || !itemStack.hasTag()) {
            return false;
        }

        CompoundTag itemNbt = itemStack.getTag();
        CompoundTag displayNbt;
        if (itemNbt.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
            displayNbt = itemNbt.getCompound(ItemStack.TAG_DISPLAY);
        } else {
            return false;
        }

        ListTag lore;
        if (displayNbt.contains(ItemStack.TAG_LORE, Tag.TAG_LIST)) {
            lore = displayNbt.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        } else {
            return false;
        }

        if (lore.isEmpty()) {
            return false;
        }

        MutableComponent loreText = Component.Serializer.fromJson(lore.getString(0));

        return loreText != null && getKeyFilter().matcher(loreText.getString()).matches();
    }

    private static boolean useKey(BlockPos cratePos) {
        if (cratePos == null || MC.gameMode == null || MC.getConnection() == null) {
            return false;
        }

        MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(cratePos.getCenter(), Direction.UP, cratePos, false));
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
}
