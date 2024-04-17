package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.ListOptionBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import net.xolt.sbutils.config.binding.constraints.StringConstraints;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.mixins.ContainerScreenAccessor;
import net.xolt.sbutils.util.InvUtils;
import net.xolt.sbutils.util.ChatUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static net.xolt.sbutils.SbUtils.MC;

public class InvCleaner extends Feature {
    private final OptionBinding<Double> clickDelay = new OptionBinding<>("invCleaner.clickDelay", Double.class, (config) -> config.invCleaner.clickDelay, (config, value) -> config.invCleaner.clickDelay = value);
    private final ListOptionBinding<String> itemsToClean = new ListOptionBinding<>("invCleaner.itemsToClean", "", String.class, (config) -> config.invCleaner.itemsToClean, (config, value) -> config.invCleaner.itemsToClean = value, new ListConstraints<>(null, null, new StringConstraints(false)));

    private boolean cleaning;
    private boolean openedDisposal;
    private Predicate<ItemStack> garbageFilter;
    private Consumer<Boolean> callback;
    private long lastClick;
    private int stacksCleaned;

    public InvCleaner() {
        super("invCleaner", "invcleaner", "ic");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(clickDelay, itemsToClean);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> invCleanerNode = dispatcher.register(
                CommandHelper.runnable(command, () -> clean(ModConfig.HANDLER.instance().invCleaner.itemsToClean, null))
                        .then(CommandHelper.stringList("items", "item", itemsToClean))
                        .then(CommandHelper.doubl("clickDelay", "seconds", clickDelay))
        );
        registerAlias(dispatcher, invCleanerNode);
    }

    public void tick() {
        if (!cleaning || MC.player == null)
            return;

        Screen screen = MC.screen;

        if (!openedDisposal && !isDisposalScreen(screen)) {
            openDisposal();
            openedDisposal = true;
            return;
        }

        if (isDisposalScreen(screen) && openedDisposal) {
            openedDisposal = false;
        }

        if (openedDisposal)
            return;

        if (System.currentTimeMillis() - lastClick < ModConfig.HANDLER.instance().invCleaner.clickDelay * 1000)
            return;

        doClean();
    }

    private void doClean() {
        if (MC.player == null)
            return;
        for (int i = 0; i < 36; i++) {
            if (!garbageFilter.test(MC.player.getInventory().getItem(i)))
                continue;
            InvUtils.quickMove(i, MC.player.containerMenu);
            lastClick = System.currentTimeMillis();
            stacksCleaned++;
            return;
        }
        MC.player.closeContainer();
        // Callback needs to happen before reset() because stacksCleaned is set to 0 by reset()
        if (callback != null)
            callback.accept(stacksCleaned > 0);
        reset();
    }

    public void clean(List<String> toClean, @Nullable Consumer<Boolean> cleanCallback) {
        cleanPredicate((stack) -> itemsFromStrings(toClean).contains(stack.getItem()), cleanCallback);
    }

    public void cleanItems(List<Item> toClean, @Nullable Consumer<Boolean> cleanCallback) {
        cleanPredicate((stack) -> toClean.contains(stack.getItem()), cleanCallback);
    }

    public void cleanPredicate(Predicate<ItemStack> toClean, @Nullable Consumer<Boolean> cleanCallback) {
        if (toClean == null || cleaning || !hasGarbage(toClean)) {
            if (cleanCallback != null)
                cleanCallback.accept(false);
            else
                ChatUtils.printMessage("message.sbutils.invCleaner.cleanFailed");
            return;
        }
        reset();
        cleaning = true;
        garbageFilter = toClean;
        callback = cleanCallback;
        ChatUtils.printMessage("message.sbutils.invCleaner.cleaning");
    }

    private void onDisposalOpened(Component title) {
        if (title == null)
            reset();
    }

    private void reset() {
        cleaning = false;
        openedDisposal = false;
        garbageFilter = null;
        callback = null;
        lastClick = 0;
        stacksCleaned = 0;
    }

    private void openDisposal() {
        if (MC.getConnection() == null)
            return;
        SbUtils.COMMAND_SENDER.sendCommand("disposal", true, this::onDisposalOpened, Pattern.compile("Disposal"));
    }

    private static boolean isDisposalScreen(Screen screen) {
        if (screen == null)
            return false;
        return screen instanceof ContainerScreen
                && ((ContainerScreenAccessor)screen).getContainerRows() == 4
                && screen.getTitle().getString().equals("Disposal");
    }

    private static boolean hasGarbage(Predicate<ItemStack> itemsToClean) {
        if (MC.player == null || itemsToClean == null)
            return false;
        for (int i = 0; i < 36; i++) {
            if (itemsToClean.test(MC.player.getInventory().getItem(i)))
                return true;
        }
        return false;
    }

    private static List<Item> itemsFromStrings(List<String> strings) {
        List<String> validItems = strings.stream().filter(string -> BuiltInRegistries.ITEM.containsKey(new ResourceLocation(string))).toList();
        return validItems.stream().map((item) -> BuiltInRegistries.ITEM.get(new ResourceLocation(item))).toList();
    }
}
